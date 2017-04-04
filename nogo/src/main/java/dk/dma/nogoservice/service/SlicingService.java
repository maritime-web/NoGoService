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

import dk.dma.nogoservice.dto.NoGoSliceRequest;
import dk.dma.nogoservice.dto.NoGoSliceResponse;

/**
 *
 * Service that returns a number of slice resources, which will contain information as it is calculated
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public interface SlicingService {

    /**
     * Queues a new async slice request, and returns the resource URLs for the work in progress
     * @param request the request
     * @return the resource information
     */
    NoGoSliceResponse request(NoGoSliceRequest request);

    /**
     * returns the current state of the async processing
     * @param id the resource id
     * @return the current state of the async process
     */
    ResourceProcessingResult getStateAndData(String id);
}
