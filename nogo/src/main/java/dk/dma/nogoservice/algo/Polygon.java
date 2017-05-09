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
package dk.dma.nogoservice.algo;

import com.vividsolutions.jts.geom.*;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Klaus Groenbaek
 *         Created 15/03/17.
 */
@EqualsAndHashCode(callSuper = true)
public class Polygon extends Figure {


    Polygon(List<Point> points) {
        super(points);
    }


    @Override
    public String toString() {
        return "Polygon " + getPoints().stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public Geometry toGeomerty() {
        List<Point> points = getPoints();
        boolean connected = points.get(0).equals(points.get(points.size() - 1));
        Coordinate[] array;

        if (connected) {
            array = new Coordinate[points.size()];
        } else {
            array = new Coordinate[points.size()+1];
        }

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            array[i] = new Coordinate(point.x, point.y);
        }
        if (!connected) {
            array[points.size()] = array[0];
        }
        com.vividsolutions.jts.geom.Polygon polygon = new GeometryFactory().createPolygon(array);
        return polygon;
    }
}
