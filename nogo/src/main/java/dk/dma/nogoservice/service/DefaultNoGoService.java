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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import dk.dma.nogoservice.dto.NoGoRequest;
import dk.dma.nogoservice.dto.NoGoResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static dk.dma.nogoservice.ApiProfiles.PRODUCTION;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@Service
@Profile(PRODUCTION)
public class DefaultNoGoService implements NoGoService {

    private final List<QueryArea> queryAreas;

    @Autowired
    public DefaultNoGoService(List<QueryArea> queryAreas) {
        this.queryAreas = queryAreas;
    }

    @Override
    @SneakyThrows(ParseException.class)
    public NoGoResponse getNoGoAreas(@Valid NoGoRequest request) {

        String wkt = request.toWKT();
        WKTReader reader = new WKTReader();
        Geometry area = reader.read(wkt);
        for (QueryArea queryArea : queryAreas) {
            if (queryArea.matches(area)) {
                return queryArea.getNogoAreas(request);
            }
        }

        throw new IllegalArgumentException("Depth service does not support the give area, supported areas are " +
                queryAreas.stream().map(QueryArea::getName).collect(Collectors.joining(",")));
    }



}
