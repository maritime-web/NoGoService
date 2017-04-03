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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * A figure is a list of points.
 * @author Klaus Groenbaek
 *         Created 15/03/17.
 */
@AllArgsConstructor
@EqualsAndHashCode
public abstract class Figure {
    private final List<Point> points;

    Figure(Point... points) {
        this(Lists.newArrayList(points));
    }

    @Override
    public String toString() {
        throw new IllegalStateException("Sub classes must override to string, for debugging purposes.");
    }

    public List<Point> getPoints() {
        return points;
    }

}
