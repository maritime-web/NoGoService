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
import lombok.Getter;

/**
 * @author Klaus Groenbaek
 *         Created 04/05/17.
 */
@Getter
class AreaMatch {

    private final Geometry intersection;
    private final GeoCoordinate northWest;
    private final GeoCoordinate southEast;

    AreaMatch(Geometry intersection) {
        this.intersection = intersection;
        if (intersection.isEmpty()) {
            northWest = null;
            southEast = null;
        } else {
            if (intersection instanceof Polygon) {
                Polygon polygon = (Polygon) intersection;
                // does not support looparound from -180 to 180
                double north = -90, south = 90, east = -180, west = 180;
                for (Coordinate coordinate : polygon.getCoordinates()) {
                    west = Math.min(coordinate.x, west);
                    south = Math.min(coordinate.y, south);
                    east = Math.max(coordinate.x, east);
                    north = Math.max(coordinate.y, north);
                }
                northWest = new GeoCoordinate(west, north);
                southEast = new GeoCoordinate(east, south);
            } else {
                throw new IllegalArgumentException("Intersection between request and area should always be a polygon.");
            }
        }

    }


    boolean matches() {
        return !intersection.isEmpty();
    }
}
