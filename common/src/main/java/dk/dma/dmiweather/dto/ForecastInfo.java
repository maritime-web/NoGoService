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

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * Since a data can come from forecasts generated at different times (different GRIB files) we need to specify interval
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
@Data
@Accessors(chain = true)
public class ForecastInfo {




    private GridParameters parameters;
    private Instant forecastDate;
    private Instant creationDate;


}
