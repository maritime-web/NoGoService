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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory that will extract data from a single data line
 * @author Klaus Groenbaek
 *         Created 31/03/17.
 */
public class SimpleDataProviderFactory implements DataProviderFactory {


    private final File file;
    private final ParameterAndRecord parameterAndRecord;
    private GridParameterType parameterType;
    private final int dataRounding;

    /**
     * Factory for processing a simple value where no calculations are needed (except rounding)
     * @param grib1Data the GRIB data
     * @param parameterAndRecord the GRIB parameter info
     * @param parameterType the type of GridRequest parameter
     * @param dataRounding number of decimals used for data rounding, -1 for no rounding
     */
    public SimpleDataProviderFactory(File grib1Data, ParameterAndRecord parameterAndRecord, GridParameterType parameterType, int dataRounding) {
        this.file = grib1Data;
        this.parameterAndRecord = parameterAndRecord;
        this.parameterType = parameterType;
        this.dataRounding = dataRounding;
    }

    @Override
    public Map<GridParameterType, AbstractDataProvider> create() {
        HashMap<GridParameterType, AbstractDataProvider> map = new HashMap<>();
        map.put(parameterType, new SimpleDataProvider(parameterAndRecord, file, dataRounding));
        return map;
    }
}
