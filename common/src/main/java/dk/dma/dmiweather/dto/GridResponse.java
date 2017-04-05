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
package dk.dma.dmiweather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.dto.JSonWarning;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GridResponse {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.000Z", timezone = "UTC")
    private Instant forecastDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.000Z", timezone = "UTC")
    private Instant queryTime;
    private List<GridDataPoint> points;
    private JSonWarning warning;
    // values below are optional and only included when gridMetrics=true is on the URL
    private Float dx;
    private Float dy;
    private Integer Nx;
    private Integer Ny;
    private GeoCoordinate northWest;
    private GeoCoordinate southEast;

}
