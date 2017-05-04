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

import dk.dma.dmiweather.dto.*;
import dk.dma.dmiweather.service.WeatherService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.DiskSpaceHealthIndicator;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author Klaus Groenbaek
 *         Created 03/04/17.
 */
@RestController
@Slf4j
public class WeatherController {


    private final WeatherService service;

    @Autowired
    private DiskSpaceHealthIndicator indicator;

    @Autowired
    public WeatherController(WeatherService service) {
        this.service = service;
    }

    /**
     *
     * @param request the request with coordinate and parameter information
     * @param removeEmpty remove points that have no data (over land)
     * @return the coordinates with weather data
     */
    @PostMapping("/grid")
    public GridResponse getGrid(@RequestBody @Valid GridRequest request, @RequestParam(name = "removeEmpty", required = false) boolean removeEmpty,
                                @RequestParam(name = "gridMetrics", required = false) boolean gridMetrics) {
        if (removeEmpty && gridMetrics) {
            log.warn("Client requesting with removeEmpty and gridMetrics, this is not a good idea as indexes into data will not work as expected.");
        }
        return service.request(request, removeEmpty, gridMetrics);
    }

    @GetMapping("/info")
    @ApiOperation(value = "Provides a list of the areas for which Weather service has information.")
    public WeatherAreaInfos info() {
        return service.info();
    }

    @GetMapping(value = "/debughealth")
    public Map debug() {
        return indicator.health().getDetails();
    }
}
