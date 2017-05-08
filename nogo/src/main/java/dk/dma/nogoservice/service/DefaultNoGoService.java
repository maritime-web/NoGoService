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

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.exception.APIException;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.nogoservice.dto.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.ArrayList;
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

    private final List<GridDataQueryArea> queryAreas;
    private final NoGoResponseMerger noGoResponseMerger;

    @Autowired
    public DefaultNoGoService(List<GridDataQueryArea> queryAreas, NoGoResponseMerger noGoResponseMerger) {
        this.noGoResponseMerger = noGoResponseMerger;
        Preconditions.checkArgument(!queryAreas.isEmpty(), "");
        this.queryAreas = queryAreas;
    }

    @Override
    @SneakyThrows(ParseException.class)
    public NoGoResponse getNoGoAreas(@Valid NoGoRequest request) {

        GeoCoordinate northWest = request.getNorthWest();
        GeoCoordinate southEast = request.getSouthEast();

        if (northWest.getLon() > southEast.getLon()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LOT);
        }
        if (northWest.getLat() < southEast.getLat()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LAT);
        }

        String wkt = request.toWKT();
        WKTReader reader = new WKTReader();
        Geometry area = reader.read(wkt);
        List<CalculatedNoGoArea> areas = new ArrayList<>();
        for (GridDataQueryArea queryArea : queryAreas) {
            AreaMatch match = queryArea.matches(area);
            if (match.matches()) {
                NoGoRequest sectionRequest = new NoGoRequest().setDraught(request.getDraught()).setTime(request.getTime())
                        .setNorthWest(match.getNorthWest()).setSouthEast(match.getSouthEast());
                CalculatedNoGoArea nogoAreas = queryArea.getNogoAreas(sectionRequest);
                nogoAreas.setArea(match.getIntersection());
                areas.add( nogoAreas);
            }
        }

        // todo: we should probably add a warning if there is no data for part of the requested area.

        // find a way to join the nogo area polygons
        if (areas.size() > 0) {
            return noGoResponseMerger.merge(areas).toResponse();
        }


        throw new APIException(ErrorMessage.OUTSIDE_GRID, "Depth service does not support the give area, supported areas are " +
                queryAreas.stream().map(QueryArea::getName).collect(Collectors.joining(",")));
    }

    @Override
    public AreaInfos getInfo() {
        AreaInfos response = new AreaInfos();
        List<AreaInfo> areas = new ArrayList<>();
        for (QueryArea queryArea : queryAreas) {
            areas.add(queryArea.getInfo());
        }
        response.setAreas(areas);
        return response;
    }


}
