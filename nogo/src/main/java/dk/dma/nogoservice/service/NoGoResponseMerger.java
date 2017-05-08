/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.nogoservice.service;

import com.google.common.collect.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import dk.dma.common.dto.JSonWarning;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class that can merge NoGo responses from different areas into a single response.
 * The problem is that the grid provided by each country only provide data within their exclusive economic zone eez, but since data is delivered as
 * rectangular grids and borders are never a single horizontal or vertical lines there will be overlap. One NoGo calculation will report NoGo for all foreign space,
 * so when there are multiple ares involved the overlapping areas must be joined.
 * DMA has more info about this here https://dma-enav.atlassian.net/wiki/display/OP/NoGo+Service
 *
 * @author Klaus Groenbaek
 *         Created 04/05/17.
 */
@Component
public class NoGoResponseMerger {


    /**
     * Merge algorithm for multiple NoGoAreas.
     * <p>
     * <ol>
     * <li>Find the exclusive zone for each area (the space that is not overlapped with other areas) </li>
     * <li>Find all the combinations of overlaps. </li>
     * <li>In the exclusive zone the nogo polygons (after the overlaps have been extracted) can be used directly</li>
     * <li>In the overlap zones the intersections of NoGo areas, will define the actual NoGo areas, because Go wins over NoGO (as foreign territory is nogo)</li>
     * </ol>
     *
     * @param areas the calculated NoGo areas
     * @return the response
     */
    CalculatedNoGoArea merge(List<CalculatedNoGoArea> areas) {

        if (areas.size() == 1) {
            return areas.get(0);
        }


        SetMultimap<CalculatedNoGoArea, Geometry> unprocessed = HashMultimap.create();
        areas.forEach(a -> unprocessed.putAll(a, a.getNogoAreas()));

        List<Geometry> processedNogoAreas = new ArrayList<>();
        for (CalculatedNoGoArea area : areas) {
            List<CalculatedNoGoArea> copy = new ArrayList<>(areas);
            copy.remove(area);
            List<Geometry> otherAreas = copy.stream().map(CalculatedNoGoArea::getArea).collect(Collectors.toList());

            // we now have the exclusive zone for this area. Now find the polygons inside the exclusive zone, if a polygon intersects with the boundary of the
            // exclusive zone, then split it in two, the part inside the exclusive zone is done, the other part will be processed later inside the overlapped zone
            Set<Geometry> nogoAreas = new HashSet<>(unprocessed.get(area));

            // take all the nogo areas and calculate the intersection with the exclusive zone
            for (Geometry nogoArea : nogoAreas) {
                Geometry result = nogoArea;
                for (Geometry otherArea : otherAreas) {
                    if (otherArea.intersects(nogoArea)) {
                        Geometry intersection = otherArea.intersection(nogoArea);
                        if (!intersection.isEmpty()) {
                            result = result.difference(otherArea);
                            // add the intersection pat of the NoGo area to the unprocessed set so we can process it during overlap handling
                            if (!result.isEmpty()) {
                                // if there was no difference, this nogo area is completely inside the other area
                                unprocessed.put(area, intersection);
                            }
                        }
                    }
                }

                if (!result.isEmpty()) {
                    processedNogoAreas.add(result);
                    unprocessed.remove(area, nogoArea); // area has now been processed
                }
            }
        }

        // now process all combinations of overlaps
        Set<Overlap> overlapCombinations = calculateOverlapCombinations(areas);

        for (Overlap overlapCombination : overlapCombinations) {
            Geometry exclusiveOverlapArea = overlapCombination.getExclusiveArea();

            List<Geometry> nogoAreas = new ArrayList<>();
            overlapCombination.included.forEach(g->nogoAreas.addAll(unprocessed.get(g)));
            List<Geometry> noGoLayers = overlapCombination.included.stream()
                    .map(unprocessed::get)
                    .filter(g->!g.isEmpty())
                    .map(g -> iterativeOperation(g, Geometry::union))
                    .collect(Collectors.toList());
            List<Geometry> goAreas = noGoLayers.stream()
                    .map(exclusiveOverlapArea::difference)
                    .collect(Collectors.toList());
            if (!goAreas.isEmpty()) {
                // We need to join all the NoGo areas, in a way so Go wins over NoGo.
                Geometry totalGoArea = iterativeOperation(goAreas, Geometry::union);
                Geometry totalNoGoArea = exclusiveOverlapArea.difference(totalGoArea);
                if (!totalGoArea.isEmpty()) {
                    processedNogoAreas.add(totalNoGoArea);
                }
            }
        }

        List<Geometry> collect = areas.stream().map(CalculatedNoGoArea::getArea).collect(Collectors.toList());
        Iterator<Geometry> iterator = collect.iterator();
        Geometry totalArea = iterator.next();
        while (iterator.hasNext()) {
            totalArea = totalArea.union(iterator.next());
        }

        TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(totalArea);
        totalArea = simplifier.getResultGeometry();

        // construct the result
        CalculatedNoGoArea result = new CalculatedNoGoArea();
        result.setArea(totalArea);
        result.setNogoAreas(processedNogoAreas);
        Optional<JSonWarning> optionalWarning = areas.stream().map(CalculatedNoGoArea::getWarning).filter(Objects::nonNull).findFirst();
        optionalWarning.ifPresent(result::setWarning);

        return result;

    }

    /**
     * Calculate the number of ways areas can overlap.
     *
     * @param areas the calculated nogo areas
     * @return the combination of all possible overlaps, identified by the indexes
     */
    private Set<Overlap> calculateOverlapCombinations(List<CalculatedNoGoArea> areas) {

        Set<Integer> indexes = IntStream.range(0, areas.size()).boxed().collect(Collectors.toSet());
        // PowerSet calculates combinations of all sizes, overlaps are combinations of size 2 or more
        Set<Set<Integer>> indexSets = Sets.powerSet(indexes).stream().filter(s -> s.size() > 1).collect(Collectors.toSet());
        Set<Overlap> overlaps = new HashSet<>();
        for (Set<Integer> indexSet : indexSets) {
            Set<CalculatedNoGoArea> included = new HashSet<>();
            List<CalculatedNoGoArea> copy = new ArrayList<>(areas);
            List<CalculatedNoGoArea> toBeRemoved = new ArrayList<>();
            for (Integer index : indexSet) {
                CalculatedNoGoArea area = copy.get(index);
                toBeRemoved.add(area);
                included.add(area);
            }
            copy.removeAll(toBeRemoved);
            overlaps.add(new Overlap(included, copy));
        }
        return overlaps;
    }

    /**
     * Applies an operation to a sequence os element. No operation is applied to the start element, and the result of each iteration is used as input to the next
     *
     * @param start      the initial area.
     * @param collection the collection
     * @param operation  the operation
     * @return the resulting geometry
     */
    private Geometry iterativeOperation(Geometry start, Collection<Geometry> collection, GeometryOperation operation) {
        if (collection.isEmpty()) {
            return start;
        }

        List<Geometry> list = Lists.newArrayList(start);
        list.addAll(collection);
        return iterativeOperation(list, operation);
    }

    /**
     * Applies an operation to a sequence os element. No operation is applied to the first element, and the result of each iteration is used as input to the next
     *
     * @param collection the collection
     * @param operation  the operation
     * @return the resulting geometry
     */
    private Geometry iterativeOperation(Collection<Geometry> collection, GeometryOperation operation) {

        Iterator<Geometry> iterator = collection.iterator();
        Geometry result = iterator.next();
        while (iterator.hasNext()) {
            Geometry second = iterator.next();
            result = operation.apply(result, second);
        }
        return result;
    }

    /**
     * Defines an overlap which includes 2 or more areas, and excludes 0 or more areas
     */
    @AllArgsConstructor
    @Getter
    private class Overlap {
        private final Set<CalculatedNoGoArea> included;
        private final List<CalculatedNoGoArea> excluded;

        /**
         * Calculate the exclusive overlap area, that is the are which the included areas share exclusive (not overlapped with the excluded)
         *
         * @return the exclusive overlap area for this overlap combination
         */
        Geometry getExclusiveArea() {
            List<Geometry> includedAreas = included.stream().map(CalculatedNoGoArea::getArea).collect(Collectors.toList());
            List<Geometry> excludedAreas = excluded.stream().map(CalculatedNoGoArea::getArea).collect(Collectors.toList());

            Geometry included = iterativeOperation(includedAreas, Geometry::intersection);
            Geometry exclusiveArea = iterativeOperation(included, excludedAreas, Geometry::difference);
            return exclusiveArea;

        }
    }

    @FunctionalInterface
    private interface GeometryOperation {
        Geometry apply(Geometry first, Geometry second);
    }


}
