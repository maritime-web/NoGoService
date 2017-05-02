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
package dk.dma.dmiweather.grib;

import dk.dma.dmiweather.dto.GridParameterType;

import java.util.Map;

/**
 * Interface for factories that create DataProviders. Some providers need multiple data series, like zonal and meridional wind to create wind direction and wind speed
 * @author Klaus Groenbaek
 *         Created 31/03/17.
 */
public interface DataProviderFactory {
    /**
     * Create one or more dataProviders
     * @return a map of providers
     */
    Map<GridParameterType, AbstractDataProvider> create();
}
