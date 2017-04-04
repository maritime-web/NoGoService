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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import dk.dma.common.dto.JSonError;
import dk.dma.common.dto.JsonErrorException;
import dk.dma.dmiweather.dto.GridRequest;
import dk.dma.dmiweather.dto.GridResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@Component
public class RemoteWeatherService implements WeatherService {

    private final RestTemplate template;
    private ObjectMapper mapper = new ObjectMapper();

    @Value("${weatherservice.url}")
    private String weatherServiceURL;

    @Autowired
    public RemoteWeatherService(PoolingHttpClientConnectionManager connectionManager) {
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        template = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        template.setErrorHandler(new RemoteErrorHandler());
    }

    @Override
    public GridResponse getWeather(GridRequest request) {
        ResponseEntity<GridResponse> postForEntity = template.postForEntity(weatherServiceURL + "grid?gridMetrics=true", request, GridResponse.class);
        return postForEntity.getBody();
    }

    private class RemoteErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            JSonError jSonError = mapper.readValue(CharStreams.toString(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8)), JSonError.class);
            throw new JsonErrorException(jSonError);
        }
    }

}
