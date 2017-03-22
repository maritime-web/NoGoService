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

import dk.dma.nogoservice.dto.*;
import dk.dma.nogoservice.service.NoGoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@RestController
public class ApiController {

    private final NoGoService noGoService;

    @Autowired
    public ApiController(NoGoService noGoService) {
        this.noGoService = noGoService;
    }

    @PostMapping(value = "/area")
    public NoGoResponse getNoGoAreas(@Valid @RequestBody NoGoRequest request) {
        return noGoService.getNoGoAreas(request);
    }

    @PostMapping(value = "/area/wkt")
    public MultiPolygon getNoGoAreasAsWKT(@Valid @RequestBody NoGoRequest request) {
        NoGoResponse nogo = noGoService.getNoGoAreas(request);
        String wkt = "MULTIPOLYGON (" + nogo.getPolygons().stream().map(p->p.toWKT().replace("POLYGON ", "")).collect(Collectors.joining(",")) + ")";
        return new MultiPolygon().setWkt(wkt);
    }




}
