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

import dk.dma.nogoservice.dto.NoGoResponse;
import lombok.Getter;

/**
 * models the state of the async NoGo processing used when slicing
 */
@Getter
public class ResourceProcessingResult {

    static ResourceProcessingResult done(NoGoResponse response) {
        return new ResourceProcessingResult(ResourceState.Done, response, null);
    }

    static ResourceProcessingResult failed(Exception exception) {
        return new ResourceProcessingResult(ResourceState.Failed, null, exception);
    }

    private final ResourceState state;
    private final Exception exception;
    private final NoGoResponse response;

    ResourceProcessingResult() {
        this(ResourceState.Working, null, null);
    }

    private ResourceProcessingResult(ResourceState state, NoGoResponse response, Exception exception) {
        this.state = state;
        this.exception = exception;
        this.response = response;
    }
}
