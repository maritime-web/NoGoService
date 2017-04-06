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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dk.dma.common.exception.APIException;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.nogoservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A slicer implementation that will create a number requests and process them asynchronously
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@Component
public class SlicingServiceImpl implements SlicingService {

    private final NoGoService noGoService;
    private final ExecutorService executorService;
    private final Cache<String, ResourceProcessingResult> resultCache;

    @Autowired
    public SlicingServiceImpl(NoGoService noGoService, ExecutorService executorService) {
        this.noGoService = noGoService;
        this.executorService = executorService;
        resultCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    }

    @Override
    public NoGoSliceResponse request(NoGoSliceRequest request) {

        ArrayList<SliceResource> slices = new ArrayList<>();
        for (int i= 0 ; i < request.getSlices(); i++) {
            NoGoRequest noGoRequest = new NoGoRequest().setDraught(request.getDraught()).setNorthWest(request.getNorthWest()).setSouthEast(request.getSouthEast());
            Instant start = request.getStart().plus(i * request.getInterval(), ChronoUnit.HOURS);
            noGoRequest.setTime(start);
            String resourceId = UUID.randomUUID().toString();
            executorService.submit(new NoGoWorker(resourceId, noGoRequest));
            resultCache.put(resourceId, new ResourceProcessingResult());
            slices.add(new SliceResource().setResourceURL(resourceId).setTime(start)); // in the controller layer we will create the correct URL
        }

        return new NoGoSliceResponse().setSlices(slices);
    }


    @Override
    public ResourceProcessingResult getStateAndData(String id) {
        return resultCache.getIfPresent(id);
    }

    /**
     * Runnable that will call the NoGo service, and put the result into the cache
     */
    private class NoGoWorker implements Runnable {
        private final String resourceId;
        private final NoGoRequest request;

        NoGoWorker(String resourceId, NoGoRequest request) {
            this.resourceId = resourceId;
            this.request = request;
        }

        @Override
        public void run() {
            try {
                NoGoResponse noGoAreas = noGoService.getNoGoAreas(request);
                resultCache.put(resourceId, ResourceProcessingResult.done(noGoAreas));
            } catch (APIException e) {
                resultCache.put(resourceId, ResourceProcessingResult.failed(e));
            } catch (Exception e) {
                resultCache.put(resourceId, ResourceProcessingResult.failed(new APIException(ErrorMessage.UNCAUGHT_EXCEPTION,e.getMessage())));
            }
        }
    }
}
