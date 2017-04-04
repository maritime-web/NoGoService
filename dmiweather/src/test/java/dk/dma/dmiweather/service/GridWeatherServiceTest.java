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
package dk.dma.dmiweather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.dmiweather.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;


/**
 * Tests with known data. To verify the data you can open the file in openCPN, https://opencpn.org/OpenCPN/info/downloads.html
 * @author Klaus Groenbaek
 *         Created 31/03/17.
 */
@Slf4j
public class GridWeatherServiceTest {

    @Test
    public void topLeftHorizontalLine() throws Exception {
        check(r-> r.setNorthWest(new GeoCoordinate(10.52, 57.5))
                .setSouthEast(new GeoCoordinate(10.57, 57.5)), "topLeftHorizontalLine.json");
    }


    @Test
    public void topLeftVerticalLine() throws Exception {
        check(r-> r.setNorthWest(new GeoCoordinate(9.0, 57.5))
                .setSouthEast(new GeoCoordinate(9.0, 57.45)), "topLeftVerticalLine.json");
    }

    @Test
    public void topLeftVerticalLine2() throws Exception {
        GridResponse response = makeRequest(r -> r.setNorthWest(new GeoCoordinate(9.0, 57.5))
                .setSouthEast(new GeoCoordinate(9.0, 57.45)), true);

        assertEquals("Point should all be empty", 0, response.getPoints().size());

    }

    @Test
    public void allData() throws Exception {

        GridResponse response = makeRequest(r-> r.setNorthWest(new GeoCoordinate(9.0, 57.5))
                .setSouthEast(new GeoCoordinate(15.0, 53.0)).getParameters().setSealevel(true), false);

        assertEquals("Number of dataPoints", 433 *541, response.getPoints().size());
        // find the first datapoint with values
        GridDataPoint found = null;
        for (GridDataPoint dataPoint : response.getPoints()) {
            if (dataPoint.getSeaLevel() != null && dataPoint.getWindDirection() != null && dataPoint.getWindSpeed() != null) {
                found = dataPoint;
                break;
            }
        }
        assertTrue("Did not find a data point with all requested values", found != null);
    }

    @Test
    public void tooFarWest() throws Exception {
        outsideGrid(r -> r.setNorthWest(new GeoCoordinate(8.0, 57.5))
                .setSouthEast(new GeoCoordinate(15.0, 53.0)));
    }

    @Test
    public void tooFarEast() throws Exception {
        outsideGrid(r -> r.setNorthWest(new GeoCoordinate(9.0, 57.5))
                .setSouthEast(new GeoCoordinate(16.0, 53.0)));
    }

    @Test
    public void tooFarNorth() throws Exception {
        outsideGrid(r -> r.setNorthWest(new GeoCoordinate(9.0, 59.5))
                .setSouthEast(new GeoCoordinate(15.0, 53.0)));
    }

    @Test
    public void tooFarSouth() throws Exception {
        outsideGrid(r -> r.setNorthWest(new GeoCoordinate(9.0, 57.5))
                .setSouthEast(new GeoCoordinate(15.0, 51.0)));
    }

    @Test
    public void wrongData() throws Exception {
        try {
            makeRequest(r->r.setTime(r.getTime().minus(3, ChronoUnit.DAYS)), true);
            fail("Should throw exception.");
        } catch (WeatherException e) {
            assertEquals(ErrorMessage.OUT_OF_RANGE, e.getError());
        }
    }

    @Test
    public void notOnTheHourRequest() throws Exception {
        ZonedDateTime departure = ZonedDateTime.now(ZoneOffset.UTC).withMonth(3).withDayOfMonth(30).withHour(12).withMinute(29).withSecond(0).withNano(0);

        GridResponse response = makeRequest(r -> r.setTime(departure.toInstant()).setNorthWest(new GeoCoordinate(10.52, 57.5))
                .setSouthEast(new GeoCoordinate(10.57, 57.5)), false);

        String expected = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000Z").format(departure.withMinute(0));
        assertEquals(expected, response.getForecastDate()) ;
    }

    @Test
    public void notOnTheHourRequest2() throws Exception {
        ZonedDateTime departure = ZonedDateTime.now(ZoneOffset.UTC).withMonth(3).withDayOfMonth(30).withHour(12).withMinute(31).withSecond(0).withNano(0);

        GridResponse response = makeRequest(r -> r.setTime(departure.toInstant()).setNorthWest(new GeoCoordinate(10.52, 57.5))
                .setSouthEast(new GeoCoordinate(10.57, 57.5)), false);

        String expected = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000Z").format(departure.withMinute(0).plusHours(1));
        assertEquals(expected, response.getForecastDate()) ;
    }

    @Test
    public void westLargerThanEast() throws Exception {
        try {
            makeRequest(r -> r.setNorthWest(new GeoCoordinate(12.0, 57.5))
                    .setSouthEast(new GeoCoordinate(11.0, 53.0)), true);
            fail("Request not valid");
        } catch (WeatherException e) {
            assertEquals(ErrorMessage.INVALID_GRID_LOT, e.getError());

        }
    }

    @Test
    public void southLargerThanNorth() throws Exception {
        try {
            makeRequest(r -> r.setNorthWest(new GeoCoordinate(11.0, 54.5))
                    .setSouthEast(new GeoCoordinate(12.0, 55.0)), true);
            fail("Request not valid");
        } catch (WeatherException e) {
            assertEquals(ErrorMessage.INVALID_GRID_LAT, e.getError());
        }
    }


    private void outsideGrid(CoordinateConfigurer configurer) throws IOException {
        try {
            makeRequest(configurer, false);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Query is outside data grid, grid corners northWest:lon: 9.00000, lat:53.00000, southEast:lon: 15.00000, lat:57.50000", e.getMessage());
        }
    }


    private void check(CoordinateConfigurer configurer, String resultFile) throws IOException {
        GridResponse response = makeRequest(configurer, false);

        //log.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));

        ClassPathResource json = new ClassPathResource(resultFile, getClass());
        try (InputStream inputStream = json.getInputStream()) {
            GridResponse expected = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(inputStream, GridResponse.class);
            expected.setQueryTime(response.getQueryTime()); // copy the query time since we don't control that
            assertEquals(expected, response);
        }
    }

    private GridResponse makeRequest(CoordinateConfigurer configurer, boolean removeEmpty) throws IOException {
        ClassPathResource resource1 = new ClassPathResource("DMI_metocean_DK.2017033012.grb", getClass());
        ClassPathResource resource2 = new ClassPathResource("DMI_metocean_DK.2017033013.grb", getClass());
        WeatherService service = new WeatherService(3, 5);
        service.newFiles(Lists.newArrayList(resource1.getFile(), resource2.getFile()));

        ZonedDateTime departure = ZonedDateTime.now(ZoneOffset.UTC).withMonth(3).withDayOfMonth(30).withHour(12).withMinute(0).withSecond(0).withNano(0);

        // Request for a rather small piece close to Skagen where the first non-null data points exist
        GridRequest request = new GridRequest().setParameters(new GridParameters().setWind(true)).setTime(departure.toInstant());
        configurer.setCoordinates(request);

        return service.request(request, removeEmpty);
    }


    @FunctionalInterface
    private interface  CoordinateConfigurer {
        void setCoordinates(GridRequest request);
    }

}
