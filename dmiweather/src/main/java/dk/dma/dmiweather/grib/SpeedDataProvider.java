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

import ucar.grib.grib1.Grib1Data;

/**
 * Provides a data series of speed (like wind speed) data based on a zonal and meridional data series
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public class SpeedDataProvider extends MeridionalZonalDataProvider {

    SpeedDataProvider(ParameterAndRecord meridional, ParameterAndRecord zonal, Grib1Data data, int dataRounding) {
        super(meridional, zonal, data, dataRounding);
    }

    @Override
    protected float calculate(float u, float v) {
        return (float) Math.sqrt(v * v + u * u);
    }
}
