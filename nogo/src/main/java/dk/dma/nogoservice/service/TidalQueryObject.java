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

import dk.dma.dmiweather.dto.GridDataPoint;
import dk.dma.dmiweather.dto.GridResponse;
import dk.dma.nogoservice.entity.GeoCoordinateProvider;

/**
 * wraps a weather response so we can easily query the weather at a give coordinate
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
class TidalQueryObject {

    private final GridResponse response;

    TidalQueryObject(GridResponse response) {
        this.response = response;

    }

    float getTidalHeight(GeoCoordinateProvider coordinateProvider) {

        double lon = coordinateProvider.getLon();
        double lat = coordinateProvider.getLat();

        int startY = (int) Math.round((lat - response.getSouthEast().getLat()) / response.getDy());
        int startX = (int) Math.round((lon - response.getNorthWest().getLon()) / response.getDx());
        int index = startY * response.getNx() + startX;
        GridDataPoint dataPoint = response.getPoints().get(index);
        Float seaLevel = dataPoint.getSeaLevel();
        if (seaLevel == null) {
            return 0f;
        }
        return seaLevel;
    }
}
