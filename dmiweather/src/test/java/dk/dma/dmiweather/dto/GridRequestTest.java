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
package dk.dma.dmiweather.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.dma.common.dto.GeoCoordinate;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
public class GridRequestTest {

    @Test
    public void fromJson() throws Exception {
        String json = "{\n" +
                "  \"parameters\": {\n" +
                "    \"wind\": true,\n" +
                "    \"wave\": true,\n" +
                "    \"current\": true,\n" +
                "    \"density\": true\n" +
                "  },\n" +
                "  \"northWest\": {\n" +
                "    \"lon\": 12.5,\n" +
                "    \"lat\": 56.1\n" +
                "  },\n" +
                "  \"southEast\": {\n" +
                "    \"lon\": 12.9,\n" +
                "    \"lat\": 55.51\n" +
                "  },\n" +
                "  \"time\": \"2016-07-21T14:00:00Z\"\n" +
                "}";


        GridRequest request = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(json, GridRequest.class);
        assertTrue("current", request.getParameters().getCurrent());
        assertFalse("selevel", request.getParameters().getSeaLevel());
        assertEquals("NorthWest", new GeoCoordinate(12.5, 56.1), request.getNorthWest());
        assertEquals("southEast", new GeoCoordinate(12.9, 55.51), request.getSouthEast());

        ZonedDateTime expected = ZonedDateTime.now(ZoneOffset.UTC).withYear(2016).withMonth(7).withDayOfMonth(21).withHour(14).withMinute(0).withSecond(0).withNano(0);
        assertEquals("date", expected.toInstant(), request.getTime());
    }
}
