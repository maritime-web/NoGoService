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
import com.google.common.base.Stopwatch;
import dk.dma.dmiweather.dto.GridRequest;
import dk.dma.dmiweather.dto.GridResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 *
 * Simple Main that will generate some load.
 * @author Klaus Groenbaek
 *         Created 02/05/17.
 */
public class Main_LoadTest {

    public static void main(String[] args) throws Exception {


        RestTemplate template = new RestTemplate();

        String str = "{\n" +
                "  \"parameters\": {\n" +
                "    \"wave\" : true\n" +
                "  },\n" +
                "  \"northWest\": {\n" +
                "    \"lon\": 11.5,\n" +
                "    \"lat\": 55.9\n" +
                "  },\n" +
                "  \"southEast\": {\n" +
                "    \"lon\": 12.5,\n" +
                "    \"lat\": 54.9\n" +
                "  },\n" +
                "  \"time\": \"2017-05-02T14:10:00Z\"\n" +
                "}";

        GridRequest request = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(str, GridRequest.class);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Instant now = Instant.now();
        for (int i =0; i < 100; i++) {
            request.setTime(now.plus(i, ChronoUnit.HOURS));
            ResponseEntity<GridResponse> entity = template.postForEntity("http://localhost:9090/weather/grid", request, GridResponse.class);
            System.out.println(i);
        }
        System.out.println("Done " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + " ms");

    }

}
