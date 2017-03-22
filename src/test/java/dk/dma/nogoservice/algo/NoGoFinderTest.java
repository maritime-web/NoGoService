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
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Klaus Groenbaek
 *         Created 20/03/17.
 */
@SuppressWarnings("Duplicates")
@Slf4j
public class NoGoFinderTest {

    @Test
    public void singlePoint() {
        String [][] twoDArray = {
                {"1"},
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("1", "Point (0,0)", collect.get(0).toString());
    }

    @Test
    public void singleLineUp() {
        String [][] twoDArray = {
                {"1", "1"},
                {"1", " "},
                {"1", " "},
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("1", "Polygon (0,0),(0,2),(1,2),(0,0)", collect.get(0).toString());
    }


    @Test
    public void testLine() {
        String [][] twoDArray = {
                {"1", " "},
                {"1", " "},
                {"1", " "},
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Line (0,0),(0,2)", collect.get(0).toString());
    }
    @Test
    public void testLineUp() {
        String [][] twoDArray = {
                {"1", "1", "1"},
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Line (0,0),(2,0)", collect.get(0).toString());
    }


    @Test
    public void gab() {
        String [][] twoDArray = {
                {"1", "1", "1", " ", "1", "1", "1"},
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 2, collect.size());

        assertEquals("1", "Line (0,0),(2,0)", collect.get(0).toString());
        assertEquals("2", "Line (4,0),(6,0)", collect.get(1).toString());
    }


    @Test
    public void testTwoLines() {
        String [][] twoDArray = {
                {"1", "1"},
                {"1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Polygon (0,0),(0,1),(1,1),(1,0),(0,0)", collect.get(0).toString());
    }

    @Test
    public void testTwoJaggedLines() {
        String [][] twoDArray = {
                {"1", "1", " "},
                {"1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Polygon (0,0),(0,1),(1,1),(2,0),(0,0)", collect.get(0).toString());
    }


    @Test
    public void testTwoJaggedLines2() {
        String [][] twoDArray = {
                {" ", "1", "1"},
                {"1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Polygon (0,0),(1,1),(2,1),(2,0),(0,0)", collect.get(0).toString());
    }

    @Test
    public void testTwoJaggedLines3() {
        String [][] twoDArray = {
                {"1", "1", "1"},
                {"1", "1", " "},
                {"1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Polygon (0,0),(0,2),(2,2),(1,1),(2,0),(0,0)", collect.get(0).toString());
    }

    @Test
    public void verticalLeftToCorner() {
        String [][] twoDArray = {
                {" ", "1", "1"},
                {"1", "1", "1"},
                {"1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());
        assertEquals("1", "Polygon (0,0),(0,1),(1,2),(2,2),(2,0),(0,0)", collect.get(0).toString());
    }


    @Test
    public void verticalRightToCorner() {
        String [][] twoDArray = {
                {"1", "1", " "},
                {"1", "1", "1"},
                {"1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());
        assertEquals("1", "Polygon (0,0),(0,2),(1,2),(2,1),(2,0),(0,0)", collect.get(0).toString());
    }



    @Test
    public void baseLineWithTwoLines() {
        String [][] twoDArray = {
                {" ", "1", "1", " ", "1", "1"},
                {"1", "1", "1", "1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 2, collect.size());

        assertEquals("1", "Polygon (0,0),(1,1),(2,1),(5,0),(0,0)", collect.get(0).toString());
        assertEquals("2", "Line (4,1),(5,1)", collect.get(1).toString());

    }


    @Test
    public void twoPoly() {
        String [][] twoDArray = {
                {" ", "1", "1", " ", "1", "1"},
                {"1", "1", "1", " ", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 2, collect.size());

        assertEquals("1", "Polygon (0,0),(1,1),(2,1),(2,0),(0,0)", collect.get(0).toString());
        assertEquals("2", "Polygon (4,0),(4,1),(5,1),(5,0),(4,0)", collect.get(1).toString());

    }


    @Test
    public void twoTriangles() {
        String [][] twoDArray = {
                {"1", "1", "1", " "},
                {"1", "1", " ", " "},
                {"1", " ", " ", "1"},
                {" ", " ", "1", "1"},
                {" ", "1", "1", "1"},
                {"1", "1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 2, collect.size());

        assertEquals("1", "Polygon (0,0),(3,3),(3,0),(0,0)", collect.get(0).toString());
        assertEquals("2", "Polygon (0,3),(0,5),(2,5),(0,3)", collect.get(1).toString());
    }


    @Test
    public void rhombus() {
        String [][] twoDArray = {
                {" ", " ", " ", " ", " ", " ", " ", "1"},
                {" ", " ", " ", " ", " ", " ", "1", "1"},
                {" ", " ", " ", " ", " ", "1", "1", "1"},
                {" ", " ", " ", " ", "1", "1", "1", "1"},
                {" ", " ", " ", "1", "1", "1", "1", " "},
                {" ", " ", "1", "1", "1", "1", " ", " "},
                {" ", "1", "1", "1", "1", " ", " ", " "},
                {"1", "1", "1", "1", " ", " ", " ", " "},
                {"1", "1", "1", " ", " ", " ", " ", " "}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());
        assertEquals("1", "Polygon (0,0),(0,1),(7,8),(7,5),(2,0),(0,0)", collect.get(0).toString());

    }

    @Test
    public void zigzag() {
        String [][] twoDArray = {
                {"1", "1", "1"},
                {" ", "1", "1"},
                {"1", "1", "1"},
                {" ", "1", " "},
                {"1", "1", "1"},
                {" ", "1", "1"},
                {"1", "1", " "},
                {" ", "1", "1"},
                {"1", "1", "1"}
        };
        List<Figure> collect = getConnectedAreas(twoDArray);
        assertEquals("number of areas", 1, collect.size());

        assertEquals("1", "Polygon (0,0),(1,1),(0,2),(1,3),(0,4),(1,5),(0,6),(1,7),(0,8),(2,8),(2,6),(1,5),(2,4),(2,3),(1,2),(2,1),(2,0),(0,0)", collect.get(0).toString());
    }

    private List<Figure> getConnectedAreas(String[][] twoDArray) {
        List<List<String>> grid = Arrays.stream(twoDArray).map(Lists::newArrayList).collect(Collectors.toList());
        Collections.reverse(grid);
        NoGoAlgorithm<String> algo = new NoGoAlgorithm<>(grid, s -> s.equals("1"), new DefaultPolygonOptimizer());
        long start = System.nanoTime();
        List<Figure> collect = algo.getFigures();
        long delta = System.nanoTime() - start;
        log.info("collected polygons {} in {} ms", collect.size(), TimeUnit.NANOSECONDS.toMillis(delta));
        return collect;
    }
}
