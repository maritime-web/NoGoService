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

import dk.dma.nogoservice.ApiProfiles;
import dk.dma.nogoservice.dto.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@Service
@Profile(ApiProfiles.TEST)
public class TestNoGoService implements NoGoService {

    private Map<NoGoRequest, NoGoResponse> mapping = new HashMap<>();

    @Override
    public NoGoResponse getNoGoAreas(@Valid NoGoRequest request) {
        NoGoResponse response = mapping.get(request);
        if (response == null) {
            throw new IllegalStateException("No mapping for request " + request);
        }
        return response;
    }

    @Override
    public AreaInfos getInfo() {
        return new AreaInfos();
    }

    public void addRequestResponseMapping(NoGoRequest request, NoGoResponse response) {
        mapping.put(request, response);
    }

}
