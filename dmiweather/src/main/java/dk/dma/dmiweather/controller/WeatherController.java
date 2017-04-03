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
package dk.dma.dmiweather.controller;

import dk.dma.common.dto.JSonError;
import dk.dma.dmiweather.dto.*;
import dk.dma.dmiweather.service.WeatherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Klaus Groenbaek
 *         Created 03/04/17.
 */
@RestController
public class WeatherController {


    private final WeatherServiceImpl service;

    @Autowired
    public WeatherController(WeatherServiceImpl service) {
        this.service = service;
    }

    /**
     *
     * @param request the request with coordinate and parameter information
     * @param removeEmpty remove points that have no data (over land)
     * @return the coordinates with weather data
     */
    @PostMapping("/grid")
    public GridResponse getGrid(@RequestBody @Valid GridRequest request, @RequestParam(name = "removeEmpty", required = false) boolean removeEmpty) {
        return service.request(request, removeEmpty);
    }

    @ExceptionHandler(WeatherException.class)
    public ResponseEntity<JSonError> handleException(WeatherException e) {
        return new ResponseEntity<>(e.toJsonError(), HttpStatus.valueOf(e.getError().getHttpCode()));
    }
}
