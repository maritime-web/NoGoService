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
package dk.dma.common.dto;

import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GeoCoordinate {
    @NotNull
    private Float lon;
    @NotNull
    private Float lat;

    public GeoCoordinate(float lon, float lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public GeoCoordinate(double lon, double lat) {
        this.lon = (float) lon;
        this.lat = (float) lat;
    }

    public GeoCoordinate setLon(double lon) {
        this.lon = (float) lon;
        return this;
    }

    public GeoCoordinate setLat(double lat) {
        this.lat = (float) lat;
        return this;
    }

    public String toWKT() {
        return lon + " " + lat;
    }

    public GeoCoordinate adjusted(float deltaLon, float deltaLat) {
        return new GeoCoordinate(lon+deltaLon, lat+deltaLat);
    }


    @Override
    public String toString() {
        return String.format("lon: %.5f, lat:%.5f", lon, lat);
    }
}
