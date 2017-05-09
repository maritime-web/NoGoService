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

import java.util.Iterator;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 05/05/17.
 */
final class DuplicatePointRemover {


    static void removeSequentialDuplicates(List<Point> points) {

        // there are some cases where we get the same point twice in a row, these have to be removed
        Iterator<Point> iterator = points.listIterator();
        if (iterator.hasNext()) {
            Point point = iterator.next();
            while( iterator.hasNext()) {
                Point next = iterator.next();
                if (point.equals(next)) {
                    iterator.remove();
                }
                point = next;
            }
        }
    }
}
