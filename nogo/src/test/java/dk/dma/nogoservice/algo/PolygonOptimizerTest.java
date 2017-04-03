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

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Klaus Groenbaek
 *         Created 22/03/17.
 */
public class PolygonOptimizerTest {

    @Test
    public void verticalLine() {
        Point removed = new Point(0, 1);
        Polygon polygon = new Polygon(Lists.newArrayList(new Point(0, 0), removed, new Point(0, 3), new Point(1, 3), new Point(0, 0)));
        new DefaultPolygonOptimizer().optimize(polygon);
        assertFalse(removed + " should have been removed", polygon.getPoints().contains(removed));
    }


    /**
     * @see NoGoFinderTest#rhombus()
     */
    @Test
    public void rhombus() {
        // all the points of a rhombus
        // Polygon 
        Polygon polygon = new Polygon(Lists.newArrayList(
                new Point(0, 0), new Point(0, 1), new Point(1, 2), new Point(2, 3), new Point(3, 4),
                new Point(4, 5), new Point(5, 6), new Point(6, 7), new Point(7, 8), new Point(7, 7),
                new Point(7, 5), new Point(6, 4), new Point(5, 3), new Point(4, 2), new Point(3, 1),
                new Point(2, 0), new Point(0, 0)));

        new DefaultPolygonOptimizer().optimize(polygon);

        Polygon expected = new Polygon(Lists.newArrayList(
                new Point(0, 0), new Point(0, 1), new Point(7, 8), new Point(7, 5), new Point(2, 0),  new Point(0, 0)));

        assertEquals("Not correctly optimized", expected, polygon);

    }


}
