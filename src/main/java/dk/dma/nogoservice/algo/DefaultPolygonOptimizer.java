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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 22/03/17.
 */
public final class DefaultPolygonOptimizer implements PolygonOptimizer {
    /**
     * Looks at 3 consecutive points if they have the same x value they form a vertical line, if the have the same x/y ration the they also form a line
     * in both cases the middle point can be removed
     * NOTE: this method modifies the incoming polygon
     * @param polygon the polygon to be optimized
     * @return the polygon (same as input, only returned to allow chaining calls)
     */
    @Override
    public Polygon optimize(Polygon polygon) {
        List<Point> points = polygon.getPoints();
        List<Point> remove = new ArrayList<>();
        for (int i = 1; i < points.size()-1; i++) {
            Point first = points.get(i-1);
            Point middle = points.get(i);
            Point last = points.get(i+1);
            if (first.x == middle.x && middle.x == last.x) {
                // vertical line
                remove.add(middle);
            }
            else if (middle.x - first.x == last.x - middle.x && middle.y - first.y == last.y - middle.y) {
                // since polygons are built from lines, we know that non-vertical lines will have consecutive y values, and can just look at delta x and y
                remove.add(middle);

            }
            // we don't need to check for horizontal lines, because Polygons are built by lines
        }
        points.removeAll(remove);
        points.removeAll(remove);
        return polygon;
    }
}
