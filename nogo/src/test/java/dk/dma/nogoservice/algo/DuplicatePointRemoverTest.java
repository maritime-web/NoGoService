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

import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Klaus Groenbaek
 *         Created 05/05/17.
 */
public class DuplicatePointRemoverTest {

    @Test
    public void test() {
        ArrayList<Point> points = Lists.newArrayList(new Point(1, 1), new Point(1,1 ), new Point(2,2));
        DuplicatePointRemover.removeSequentialDuplicates(points);
        assertEquals(2, points.size());
        assertEquals(new Point(1,1), points.get(0));
        assertEquals(new Point(2,2), points.get(1));
    }

}
