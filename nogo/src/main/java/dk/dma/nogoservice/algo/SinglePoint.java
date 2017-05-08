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

/**
 * @author Klaus Groenbaek
 *         Created 15/03/17.
 */
@EqualsAndHashCode(callSuper = true)
public class SinglePoint extends Figure {

    SinglePoint(Point points) {
        super(points);
    }

    @Override
    public String toString() {
        return "Point " + getPoints().get(0);
    }

    @Override
    public Geometry toGeomerty() {
        Point point = getPoints().get(0);
        return new GeometryFactory().createPoint(new Coordinate(point.x, point.y));
    }
}
