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

import com.fasterxml.jackson.annotation.JsonInclude;
import dk.dma.common.dto.GeoCoordinate;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GridDataPoint {
    GeoCoordinate coordinate;
    Float seaLevel; // 82
    Float windDirection;     // derived from 33 and 34
    Float windSpeed;         // derived from 33 and 34
    Float wave;              // from the wave GRIB files, they have different grid resolution
    Float currentDirection;  // derived from 49 and 50
    Float currentSpeed;      // derived from 49 and 50
    Float density;           // can be calculated from Salinity (88) and temperature (80)

    public void setData(GridParameterType type, Float dataValue) {
        if (dataValue != null) {
            switch (type) {
                case CurrentDirection:
                    setCurrentDirection(dataValue);
                    break;
                case CurrentSpeed:
                    setCurrentSpeed(dataValue);
                    break;
                case Density:
                    setDensity(dataValue);
                    break;
                case SeaLevel:
                    setSeaLevel(dataValue);
                    break;
                case Wave:
                    setWave(dataValue);
                    break;
                case WindDirection:
                    setWindDirection(dataValue);
                    break;
                case WindSpeed:
                    setWindSpeed(dataValue);
                    break;
            }
        }
    }

    public boolean hasValues() {
       return seaLevel != null || windDirection != null || windSpeed != null || wave != null || currentDirection != null ||
                currentSpeed != null ||  density != null;
    }
}
