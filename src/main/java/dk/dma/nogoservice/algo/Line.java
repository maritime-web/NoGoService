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
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lines are always horizontal or vertical ( can go in both directions), but never diagonal
 * @author Klaus Groenbaek
 *         Created 15/03/17.
 */
@EqualsAndHashCode(callSuper = true)
public class Line extends Figure {

    Line(List<Point> points) {
        super(points);
    }

    Line(Point start, Point end) {
        this(Lists.newArrayList(start, end));
    }

    @Override
    public String toString() {
        return "Line " + getPoints().stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
