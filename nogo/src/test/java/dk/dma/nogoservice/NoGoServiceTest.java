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
package dk.dma.nogoservice;

import com.google.common.collect.Lists;
import dk.dma.Asserts;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.nogoservice.dto.*;
import dk.dma.nogoservice.service.TestNoGoService;
import dk.dma.nogoservice.service.TestTokenProvider;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Tests validation and response format. Test on real data has to be made in integration tests.
 *
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({ApiProfiles.TEST, ApiProfiles.SECURITY})
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NoGoServiceTest {


    @Autowired
    private TestNoGoService testService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestTokenProvider tokenProvider;

    @Test
    public void staticRequest() throws Exception {


        NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296))
                .setSouthEast(new GeoCoordinate(12.704356943619615, 55.61035686376758));
        request.setDraught(3d);
        NoGoResponse response = new NoGoResponse();
        ArrayList<NoGoPolygon> polygons = new ArrayList<>();
        ArrayList<GeoCoordinate> points = new ArrayList<>();

        points.add(new GeoCoordinate(9.419409, 54.36294));
        points.add(new GeoCoordinate(13.149009, 54.36294));
        points.add(new GeoCoordinate(13.149009,56.36316));
        points.add(new GeoCoordinate(9.419409, 56.36316));
        points.add(new GeoCoordinate(9.419409, 54.36294));

        polygons.add(new NoGoPolygon().setPoints(points));
        response.setPolygons(polygons);

        NoGoResponse noGoResponse = makeDepthRequest(request, response, "/area", NoGoResponse.class);

        assertEquals("response", response, noGoResponse);
    }

    @Test
    public void staticRequest2() throws Exception {


        NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296))
                .setSouthEast(new GeoCoordinate(12.704356943619615, 55.61035686376758));
        request.setDraught(3d);
        NoGoResponse response = new NoGoResponse();
        ArrayList<NoGoPolygon> polygons = new ArrayList<>();
        ArrayList<GeoCoordinate> points = new ArrayList<>();

        points.add(new GeoCoordinate( 9.419409,54.36294));
        points.add(new GeoCoordinate(13.149009,54.36294));
        points.add(new GeoCoordinate(13.149009,56.36316));
        points.add(new GeoCoordinate( 9.419409,56.36316));
        points.add(new GeoCoordinate( 9.419409,54.36294));

        polygons.add(new NoGoPolygon().setPoints(points));
        response.setPolygons(polygons);

        MultiPolygon noGoResponse = makeDepthRequest(request, response, "/area/wkt", MultiPolygon.class);

        assertEquals("wkt result",
                "MULTIPOLYGON (((9.419409 54.36294, 13.149009 54.36294, 13.149009 56.36316, 9.419409 56.36316, 9.419409 54.36294)))", noGoResponse.getWkt());
    }


    private <T> T makeDepthRequest(NoGoRequest request, NoGoResponse response, String path, Class<T> responseClass) {
        testService.addRequestResponseMapping(request, response);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + tokenProvider.getToken());

        RequestEntity<NoGoRequest> requestEntity = new RequestEntity<>(request, headers, HttpMethod.POST, getURI(path));
        ResponseEntity<T> responseEntity = template.exchange(requestEntity, responseClass);
        return responseEntity.getBody();
    }

    @Test
    public void missingCoordinate() {

        try {
            NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296));
            makeDepthRequest(request, null, "/area", NoGoResponse.class);
        } catch (HttpClientErrorException e) {
            Asserts.assertContains(e.getResponseBodyAsString(), "southEast");
        }
    }


    @Test
    public void missingLatitude() {

        try {
            NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296))
                    .setSouthEast(new GeoCoordinate().setLon(12.70435694f));
            makeDepthRequest(request, null, "/area", NoGoResponse.class);
        } catch (HttpClientErrorException e) {
            Asserts.assertContains(e.getResponseBodyAsString(), "southEast.lat");
            Asserts.assertContains(e.getResponseBodyAsString(), "may not be null");

        }
    }


    @SneakyThrows(URISyntaxException.class)
    private URI getURI(String path) {
        return new URI("http://localhost:" +port + "/nogo" + path);
    }


}
