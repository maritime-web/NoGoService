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

import com.vividsolutions.jts.geom.*;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.dto.JSonWarning;
import dk.dma.nogoservice.dto.NoGoPolygon;
import dk.dma.nogoservice.dto.NoGoResponse;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A grouping of the nogo calculation and the coresponding area
 * @author Klaus Groenbaek
 *         Created 04/05/17.
 */
@Data
@Accessors(chain = true)
class CalculatedNoGoArea {

    private Geometry area;
    private List<Geometry> nogoAreas;
    private JSonWarning warning;

    /**
     * Converts the internal JTS representation to the JSon response DTO
     */
    NoGoResponse toResponse() {
        List<NoGoPolygon> list = new ArrayList<>();
        for (Geometry geometry : nogoAreas) {
            if (geometry instanceof Polygon) {
                if (!geometry.isEmpty())
                addPolygon(list, (Polygon) geometry);
            }
            else if (geometry instanceof MultiPolygon) {
                // it is possible to get multi polygons when overlapping areas are processed, but should not be possible with a single nogo area.
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                for (int i= 0; i < multiPolygon.getNumGeometries(); i++) {
                    Geometry poly = multiPolygon.getGeometryN(i);
                    if (!poly.isEmpty()) {
                        addPolygon(list, (Polygon) poly);
                    }
                }
            }
            else {
                throw new IllegalStateException("Unsupported Geomety type " + geometry.getClass());
            }
        }
        return new NoGoResponse().setPolygons(list).setWarning(warning);
    }

    /**
     * this setter validates the polygons, as invalid polygons throws exception if we need to merge them (in calls to intersection()/difference())
     * @param nogoAreas the list of areas
     */
    CalculatedNoGoArea setNogoAreas(List<Geometry> nogoAreas) {
        for (Geometry nogoArea : nogoAreas) {
            if (!nogoArea.isValid()) {
                throw new IllegalStateException("Only valid geometry objects may be set");
            }
        }
        this.nogoAreas = nogoAreas;
        return this;
    }


    private void addPolygon(List<NoGoPolygon> list, Polygon geometry) {
        List<GeoCoordinate> points = Arrays.stream(geometry.getCoordinates())
                .map(c -> new GeoCoordinate(c.x, c.y)).collect(Collectors.toList());
        list.add(new NoGoPolygon().setPoints(points));

    }
}
