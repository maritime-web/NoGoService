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

import dk.dma.common.dto.JSonError;
import dk.dma.nogoservice.RequestUtils;
import dk.dma.nogoservice.dto.*;
import dk.dma.nogoservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Controller for the slicing
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@RestController
@RequestMapping("/slices/")
public class SlicingController {

    private final SlicingService service;

    @Autowired
    public SlicingController(SlicingService service) {
        this.service = service;
    }

    @PostMapping(value = "area/wkt")
    public NoGoSliceResponse slicesWkt(@Valid @RequestBody NoGoSliceRequest request) {
        NoGoSliceResponse response = service.request(request);
        configureURL(response, true);
        return response;
    }

    @PostMapping(value = "area/")
    public NoGoSliceResponse slices(@Valid @RequestBody NoGoSliceRequest request) {
        NoGoSliceResponse response = service.request(request);
        configureURL(response, false);
        return response;
    }

    @GetMapping(value = "resource/wkt/{id}")
    public ResponseEntity<?> responseWkt(@PathVariable("id") String id) {
        return getResponse(id, NoGoResponse::toMultiPolygon);
    }

    @GetMapping(value = "resource/{id}")
    public ResponseEntity<?> response(@PathVariable("id") String id) {
        return getResponse(id, response -> response);
    }

    /**
     * The service does not know the URL, it just returns the ID as the URL, so we have to modify it and set the correct callback URL
     */
    private void configureURL(NoGoSliceResponse response, boolean wkt) {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
        String baseURL = RequestUtils.getContextURL(req) + "/slices/resource/" + (wkt ? "wkt/" : "");
        for (SliceResource resource : response.getSlices()) {
            resource.setResourceURL( baseURL + resource.getResourceURL());
        }
    }

    private <T> ResponseEntity<?> getResponse(String id, Converter<T> converter) {
        ResourceProcessingResult resourceProcessingResult = service.getStateAndData(id);
        if (resourceProcessingResult == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ResourceState state = resourceProcessingResult.getState();
        if (state == ResourceState.Working) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        if (state == ResourceState.Failed) {
            JSonError jSonError = ErrorMessage.UNCAUGHT_EXCEPTION.toJsonError().setDetails(resourceProcessingResult.getException().getMessage());
            return new ResponseEntity<>(jSonError,HttpStatus.INTERNAL_SERVER_ERROR);
        }

        NoGoResponse response = resourceProcessingResult.getResponse();
        T value = converter.convert(response);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @FunctionalInterface
    private interface Converter<T> {
        T convert(NoGoResponse response);
    }

}
