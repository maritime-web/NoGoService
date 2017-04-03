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

import dk.dma.common.util.MathUtil;
import dk.dma.dmiweather.dto.GridParameterType;
import dk.dma.dmiweather.service.GribFileWrapper;
import ucar.grib.grib1.Grib1Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * GRIB files have wind and current as meridional and zonal (x and y direction) and we need to convert it to a direction and a speed
 * Since we don't want to read the data twice we use a factory which will produce two providers with the converted data
 * Speed is calculated by adding two vectors (or the length of the hypotenuse in a triangle). The angle is calculated using atan2, since due south is 0 and
 * increasing clockwise
 * See dmi_weather.tab for column definitions
 * 33:Uatm:Meridional wind at 10m (positive values west -> east) [m/s]
 * 34:Vatm:Zonal wind at 10m (positive values south -> north) [m/s]
 *
 * @author Klaus Groenbaek
 *         Created 30/03/17.
 */
public class MeridionalZonalFactory implements DataProviderFactory {

    private static final double R2D = 45.0 / Math.atan(1.0);
    private final Grib1Data data;
    private final ParameterAndRecord meridional;
    private final ParameterAndRecord zonal;
    private final GridParameterType directionType;
    private final GridParameterType speedType;
    private final int dataRounding;

    /**
     * constructs the factory
     * @param data the data
     * @param meridional the meridional parameter
     * @param zonal the zonal parameter
     * @param directionType the parameter type for the direction data
     * @param speedType the parameter type for the speed data
     * @param dataRounding number of decimals to round data to
     */
    public MeridionalZonalFactory(Grib1Data data, ParameterAndRecord meridional, ParameterAndRecord zonal, GridParameterType directionType, GridParameterType speedType, int dataRounding) {
        this.data = data;
        this.meridional = meridional;
        this.zonal = zonal;
        this.directionType = directionType;
        this.speedType = speedType;
        this.dataRounding = dataRounding;
    }

    @Override
    public Map<GridParameterType, DataProvider> create() throws IOException {
        float[] meridional = DataProviderFactory.getData(this.meridional, data);
        float[] zonal = DataProviderFactory.getData(this.zonal, data);

        float[] directions = new float[zonal.length];
        float[] speeds = new float[zonal.length];
        for (int i = 0; i < zonal.length; i++) {
            float u = zonal[i];
            float v = meridional[i];

            if (u == GribFileWrapper.GRIB_NOT_DEFINED  || v == GribFileWrapper.GRIB_NOT_DEFINED) {
                directions[i] = GribFileWrapper.GRIB_NOT_DEFINED;
                speeds[i]= GribFileWrapper.GRIB_NOT_DEFINED;
            }else {
                // direction is different than a normal coordinate system, the rotation is clockwise starting due south
                // direction is calculated using a formula found here http://www.ncl.ucar.edu/Document/Functions/Built-in/atan2.shtml
                double direction = Math.atan2(v,u) * R2D + 180;
                float speed = (float) Math.sqrt(v * v + u * u);
                if (dataRounding != -1) {
                    direction = MathUtil.round(direction, dataRounding);
                    speed = MathUtil.round(speed, dataRounding);
                }
                directions[i] = (float) direction;
                speeds[i]= speed;

            }
        }

       // DataDebugger.showAsImage(meridional, vars.getNx(), GribFileWrapper.GRIB_NOT_DEFINED);

        HashMap<GridParameterType, DataProvider> map = new HashMap<>();
        map.put(directionType, new DataProvider(this.meridional, directions));
        map.put(speedType, new DataProvider(this.meridional, speeds));
        return map;

    }

}
