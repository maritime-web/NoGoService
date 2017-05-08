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
import dk.dma.nogoservice.dto.GridData;
import dk.dma.nogoservice.entity.GeoCoordinateProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms figures with x,y grid coordinates into geoLocations by extracting lon, lat from the values in the grid
 *
 * @author Klaus Groenbaek
 *         Created 21/03/17.
 */
@Component
@Slf4j
public class FigureTransformer {


    <Value extends GeoCoordinateProvider> List<Geometry> convertToGeoLocations(List<List<Value>> grid, List<Geometry> figures, GridData gridData) {

        double halfLatSpacing = gridData.getDy() / 2;
        double halfLongSpacing = gridData.getDx() / 2;
        double buffer = (halfLatSpacing + halfLongSpacing) / 2;
        GeometryFactory factory = new GeometryFactory();
        // convert from x,y grid to long/lat, and add buffering
        List<Geometry> collect = figures.stream().map(geometry -> {
            // Although coordinate is not immutable, modifying it directly give strangeResults
            Geometry result;
            if (geometry instanceof Polygon) {
                Polygon polygon = (Polygon) geometry;
                // since this is used with nogo areas we know there are no holes
                Coordinate[] exteriorRing = convertCoordinates(grid, polygon.getExteriorRing().getCoordinates());
                result = factory.createPolygon(exteriorRing);
            } else if (geometry instanceof LineString) {
                LineString lineString = (LineString) geometry;
                result = factory.createLineString(convertCoordinates(grid, lineString.getCoordinates()));
            } else if (geometry instanceof Point) {
                Point point = (Point) geometry;
                result = factory.createLineString(convertCoordinates(grid, point.getCoordinates()));
            } else {
                throw new IllegalArgumentException("Unsupported Geometry " + geometry.getClass());
            }

            return result.buffer(buffer, 2);
        }).collect(Collectors.toList());
        return collect;
    }

    private <Value extends GeoCoordinateProvider> Coordinate[] convertCoordinates(List<List<Value>> grid, Coordinate[] coordinates) {
        return Arrays.stream(coordinates)
                            .map(coordinate -> grid.get((int) coordinate.y).get((int) coordinate.x))
                            .map(v->new Coordinate(v.getLon(), v.getLat())).toArray(Coordinate[]::new);
    }

}
