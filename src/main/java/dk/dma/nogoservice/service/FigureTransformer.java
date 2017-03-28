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

import com.google.common.collect.Lists;
import dk.dma.nogoservice.algo.*;
import dk.dma.nogoservice.dto.GeoCoordinate;
import dk.dma.nogoservice.dto.NoGoPolygon;
import dk.dma.nogoservice.entity.GeoCoordinateProvider;
import dk.dma.nogoservice.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transforms figures with x,y grid coordinates into geoLocations by extracting lon, lat from the values in the grid
 * @author Klaus Groenbaek
 *         Created 21/03/17.
 */
@Component
@Slf4j
public class FigureTransformer {
    /**
     * The data is made up by distinct points, with a distance between. We therefore take half the distance between a Go and noGo point and add it as padding
     * The spacing is 0.00111 between points on both the lon and lat axis, which corresponds to a longitude delta of approximately 70m, and latitude delta of 123m
     */
    public static final double halfLatSpacing = 0.00055504;
    public static final double halfLongSpacing = 0.00055504;

    public <Value  extends GeoCoordinateProvider> List<NoGoPolygon> convertToGeoLocations(List<List<Value>> grid, List<Figure> figures) {

        ArrayList<NoGoPolygon> noGoPolygons = new ArrayList<>();

        for (Figure figure : figures) {
            List<GeoCoordinate> points = figure.getPoints().stream().map(new Function<Point, GeoCoordinate>() {
                @Override
                public GeoCoordinate apply(Point point) {
                    Value element = grid.get(point.y).get(point.x);
                    return new GeoCoordinate(element.getLon(), element.getLat());
                }
            }).collect(Collectors.toList());
            if (figure instanceof Polygon) {
                noGoPolygons.add(new NoGoPolygon().setPoints(points));
            }
            if (figure instanceof Line) {
                if (figure.getPoints().get(0).x == figure.getPoints().get(1).x) {
                    // use the figures integer indexes to see if the line is vertical (avoid doubleMath)
                    GeoCoordinate first = points.get(0);
                    GeoCoordinate second = points.get(1);
                    if (first.getLat() < second.getLat()) {
                        GeoCoordinate downLeft = first.adjusted(-halfLongSpacing, -halfLatSpacing);
                        GeoCoordinate upLeft = second.adjusted(-halfLongSpacing, halfLatSpacing);
                        GeoCoordinate upRight = second.adjusted(halfLongSpacing, halfLatSpacing);
                        GeoCoordinate downRight = first.adjusted(halfLongSpacing, -halfLatSpacing);
                        noGoPolygons.add(new NoGoPolygon().setPoints(Lists.newArrayList(downLeft, upLeft, upRight, downRight, downLeft)));
                    } else {
                        GeoCoordinate temp = first;
                        first = second;
                        second = temp;

                        GeoCoordinate downLeft = first.adjusted(-halfLongSpacing, -halfLatSpacing);
                        GeoCoordinate upLeft = second.adjusted(-halfLongSpacing, halfLatSpacing);
                        GeoCoordinate upRight = second.adjusted(halfLongSpacing, halfLatSpacing);
                        GeoCoordinate downRight = first.adjusted(halfLongSpacing, -halfLatSpacing);
                        noGoPolygons.add(new NoGoPolygon().setPoints(Lists.newArrayList(downLeft, upLeft, upRight, downRight, downLeft)));
                    }
                }
            }

            if (figure instanceof SinglePoint) {
                GeoCoordinate center = points.get(0);
                GeoCoordinate downLeft = center.adjusted(-halfLongSpacing, -halfLatSpacing);
                GeoCoordinate upLeft = center.adjusted(-halfLongSpacing, halfLatSpacing);
                GeoCoordinate upRight = center.adjusted(halfLongSpacing, halfLatSpacing);
                GeoCoordinate downRight = center.adjusted(halfLongSpacing, -halfLatSpacing);
                noGoPolygons.add(new NoGoPolygon().setPoints(Lists.newArrayList(downLeft, upLeft, upRight, downRight, downLeft)));
            }
        }

        noGoPolygons.stream().map(p->p.getPoints()).flatMap(l->l.stream()).forEach(new Consumer<GeoCoordinate>() {
            @Override
            public void accept(GeoCoordinate coordinate) {
                coordinate.setLon(MathUtil.round(coordinate.getLon(), 6));
                coordinate.setLat(MathUtil.round(coordinate.getLat(), 6));
            }
        });
        return noGoPolygons;
    }
}
