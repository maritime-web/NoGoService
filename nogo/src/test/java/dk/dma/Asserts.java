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
package dk.dma;

import com.vividsolutions.jts.geom.Geometry;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
public class Asserts {

    public static void assertContains(String message, String str, String contains) {
        if (!str.contains(contains)) {
            if (message != null) {
                str = message + ", " + str;
            }
            fail(str + " Does not contain: " + contains + ". the sting is: '" + str + "'");
        }
    }

    public static void assertContains(String str, String contains) {
        assertContains(null, str, contains);
    }

    public static void assertListSize(String listName, int size, List<?> list) {
        if (list.size() != size) {
            fail("expected '" + listName + "' of size " + size + ", but was " + list.size() + ", elements " +
                    list.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
    }

    public static <T> void  listContains(String message, List<T> list, T element) {
        if (message == null) {
            message = "";
        }
        if (!list.contains(element)) {
            fail(message + " " + element + " not contained in " + list.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
    }

    public static void geometryEquals(String message, Geometry expected, Geometry actual) {
        if (!expected.equalsNorm(actual)) {
            fail(message + String.format("%nExpected %s%nActual %s", expected, actual));
        }
    }

}
