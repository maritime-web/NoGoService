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
import dk.dma.nogoservice.controller.ApiController;
import dk.dma.nogoservice.dto.NoGoPolygon;
import dk.dma.nogoservice.dto.NoGoRequest;
import dk.dma.nogoservice.dto.NoGoResponse;
import dk.dma.nogoservice.dto.GeoCoordinate;
import dk.dma.nogoservice.service.TestNoGoService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(ApiProfiles.TEST)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@Component
public class NoGoServiceTest {

    @Autowired
    private ApiController controller;

    @Autowired
    private TestNoGoService depthService;

    @LocalServerPort
    private int port;

    @Test
    public void staticRequest() throws Exception {


        NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296))
                .setSouthEast(new GeoCoordinate(12.704356943619615, 55.61035686376758));
        request.setDrought(3d);
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

        NoGoResponse noGoResponse = makeDepthRequest(request, response);

        assertEquals("depthResponse", response, noGoResponse);
    }

    private NoGoResponse makeDepthRequest(NoGoRequest request, NoGoResponse response) {
        depthService.addRequestResponseMapping(request, response);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

        RequestEntity<NoGoRequest> requestEntity = new RequestEntity<>(request, headers, HttpMethod.POST, getURI("/depthinfo"));
        ResponseEntity<NoGoResponse> responseEntity = template.exchange(requestEntity, NoGoResponse.class);
        return responseEntity.getBody();
    }

    @Test
    public void missingCoordinate() {

        try {
            NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296));
            makeDepthRequest(request, null);
        } catch (HttpClientErrorException e) {
            Asserts.assertContains(e.getResponseBodyAsString(), "southEast");
        }
    }


    @Test
    public void missingLatitude() {

        try {
            NoGoRequest request = new NoGoRequest().setNorthWest(new GeoCoordinate(12.645173535741588, 55.64053813461296))
                    .setSouthEast(new GeoCoordinate().setLon(12.704356943619615));
            makeDepthRequest(request, null);
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
