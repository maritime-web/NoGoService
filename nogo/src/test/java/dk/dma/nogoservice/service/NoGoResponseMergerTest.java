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
package dk.dma.nogoservice.service;

import com.vividsolutions.jts.geom.*;
import dk.dma.Asserts;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 *
 * Tests the algorithm which merges to NoGo areas
 *
 * @author Klaus Groenbaek
 *         Created 04/05/17.
 */
public class NoGoResponseMergerTest {

    @Test
    public void noOverlap() {

        NoGoResponseMerger merger = new NoGoResponseMerger();
        ArrayList<CalculatedNoGoArea> areas = new ArrayList<>();
        CalculatedNoGoArea first = new CalculatedNoGoArea().setArea(createArea(10, 10, 0, 0))
                .setNogoAreas(Lists.newArrayList(createArea(8, 8, 1, 1)));

        CalculatedNoGoArea second = new CalculatedNoGoArea().setArea(createArea(10, 10, 10, 0))
                .setNogoAreas(Lists.newArrayList(createArea(8, 8, 11, 1)));

        areas.add(first);
        areas.add(second);

        CalculatedNoGoArea area = merger.merge(areas);
        assertEquals(area.getArea(), createArea(20, 10, 0, 0 ));
        Asserts.geometryEquals("first area", area.getNogoAreas().get(0), createArea(8,8,1,1));
        Asserts.geometryEquals("second area", area.getNogoAreas().get(1), createArea(8,8,11,1));
    }

    /**
     * Two 10 by 10 areas with an offset of 5. Each has an 8x8 nogo are inside
     */
    @Test
    public void oneOverlap() {
        NoGoResponseMerger merger = new NoGoResponseMerger();
        ArrayList<CalculatedNoGoArea> areas = new ArrayList<>();

        CalculatedNoGoArea first = new CalculatedNoGoArea().setArea(createArea(10, 10, 0, 0))
                .setNogoAreas(Lists.newArrayList(createArea(8, 8, 1, 1)));

        CalculatedNoGoArea second = new CalculatedNoGoArea().setArea(createArea(10, 10, 5, 0))
                .setNogoAreas(Lists.newArrayList(createArea(8, 8, 6, 1)));

        areas.add(first);
        areas.add(second);

        CalculatedNoGoArea area = merger.merge(areas);
        assertEquals(area.getArea(), createArea(15, 10, 0, 0 ));
        Asserts.geometryEquals("first area", area.getNogoAreas().get(0), createArea(4,8,1,1));
        Asserts.geometryEquals("second area", area.getNogoAreas().get(1), createArea(4,8,10,1));
        Asserts.geometryEquals("third area", area.getNogoAreas().get(2), createArea(3, 8, 6, 1));

    }

    @Test
    public void hole() {

        NoGoResponseMerger merger = new NoGoResponseMerger();
        ArrayList<CalculatedNoGoArea> areas = new ArrayList<>();
        CalculatedNoGoArea first = new CalculatedNoGoArea().setArea(createArea(10, 10, 0, 0))
                .setNogoAreas(Lists.newArrayList(createArea(7, 8, 3 , 1)));

        CalculatedNoGoArea second = new CalculatedNoGoArea().setArea(createArea(8, 10, 2, 0))
                .setNogoAreas(Lists.newArrayList(createArea(5, 6, 5, 2)));

        areas.add(first);
        areas.add(second);

        CalculatedNoGoArea area = merger.merge(areas);
        String wkt = area.toResponse().toMultiPolygon().getWkt();
        System.out.println(wkt);
        assertEquals(area.getArea(), createArea(10, 10, 0, 0 ));
        Asserts.geometryEquals("first area", createArea(5,6,5,2), area.getNogoAreas().get(0));



    }

    private Geometry createArea(int lenghtX, int lengthY, int offsetX, int offsetY) {

        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(offsetX,                offsetY);
        coordinates[1] = new Coordinate(offsetX,             lengthY +offsetY);
        coordinates[2] = new Coordinate(offsetX + lenghtX,lengthY + offsetY);
        coordinates[3] = new Coordinate(offsetX + lenghtX,   offsetY);
        coordinates[4] = new Coordinate(offsetX,                offsetY);

        return factory.createPolygon(coordinates);
    }

}
