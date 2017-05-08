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
package dk.dma.nogoservice.controller;

import com.google.common.base.Stopwatch;
import dk.dma.nogoservice.dto.*;
import dk.dma.nogoservice.service.NoGoService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@RestController
@Slf4j
public class ApiController {

    private final NoGoService noGoService;

    @Autowired
    public ApiController(NoGoService noGoService) {
        this.noGoService = noGoService;
    }

    @PostMapping(value = "/area")
    @ApiOperation(value = "Get NoGo area", notes = "Returns structured data for all the NoGo polygons. If time is included the tidal information will be included in the NoGo calculation.")
    public NoGoResponse getNoGoAreas(@Valid @RequestBody NoGoRequest request) {
        Stopwatch timer = Stopwatch.createStarted();
        NoGoResponse noGoAreas = noGoService.getNoGoAreas(request);
        log.info("NoGo request processed in {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return noGoAreas;

    }

    @PostMapping(value = "/area/wkt")
    @ApiOperation(value = "Get NoGo area as WKT", notes = "Returns a single MultiPolygon with all the nogo areas. If time is included the tidal information will be included in the NoGo calculation.")
    public MultiPolygon getNoGoAreasAsWKT(@Valid @RequestBody NoGoRequest request) {
        Stopwatch timer = Stopwatch.createStarted();
        NoGoResponse nogo = noGoService.getNoGoAreas(request);
        log.info("NoGo (wkt) request processed in {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return nogo.toMultiPolygon();

    }

    @GetMapping("/info")
    @ApiOperation(value = "Provides a list of the areas for which NoGo information is provided.")
    public AreaInfos info() {
        return noGoService.getInfo();
    }

}
