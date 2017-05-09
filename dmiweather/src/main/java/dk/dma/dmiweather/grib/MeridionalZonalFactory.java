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
 *
 * factory that provides direction and speed dataseries from a Zonal and Meridional parameter
 * @author Klaus Groenbaek
 *         Created 30/03/17.
 */
public class MeridionalZonalFactory implements DataProviderFactory {


    private final File file;
    private final ParameterAndRecord meridional;
    private final ParameterAndRecord zonal;
    private final GridParameterType directionType;
    private final GridParameterType speedType;
    private final int dataRounding;

    /**
     * constructs the factory
     * @param file the data
     * @param meridional the meridional parameter
     * @param zonal the zonal parameter
     * @param directionType the parameter type for the direction data
     * @param speedType the parameter type for the speed data
     * @param dataRounding number of decimals to round data to
     */
    public MeridionalZonalFactory(File file, ParameterAndRecord meridional, ParameterAndRecord zonal, GridParameterType directionType, GridParameterType speedType, int dataRounding) {
        this.file = file;
        this.meridional = meridional;
        this.zonal = zonal;
        this.directionType = directionType;
        this.speedType = speedType;
        this.dataRounding = dataRounding;
    }

    @Override
    public Map<GridParameterType, AbstractDataProvider> create()  {

        HashMap<GridParameterType, AbstractDataProvider> map = new HashMap<>();
        map.put(directionType, new DirectionDataProvider(this.meridional, zonal, file, dataRounding));
        map.put(speedType, new SpeedDataProvider(this.meridional, zonal, file, dataRounding));
        return map;

    }

}
