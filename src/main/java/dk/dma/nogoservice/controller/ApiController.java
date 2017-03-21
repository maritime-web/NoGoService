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

import dk.dma.nogoservice.dto.NoGoRequest;
import dk.dma.nogoservice.dto.NoGoResponse;
import dk.dma.nogoservice.service.NoGoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    @PostMapping(value = "/depthinfo")
    public NoGoResponse getDepthInformation(@Valid @RequestBody NoGoRequest request) {
        return noGoService.getDepthInformation(request);
    }




}
