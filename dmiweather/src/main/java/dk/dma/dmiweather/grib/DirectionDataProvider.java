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

import java.io.File;

/**
 * Data provider that calculates the speed based on Zonal and Meridional data series
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public class DirectionDataProvider extends MeridionalZonalDataProvider {
    private static final double R2D = 45.0 / Math.atan(1.0);

    DirectionDataProvider(ParameterAndRecord meridional, ParameterAndRecord zonal, File data, int dataRounding) {
        super(meridional, zonal, data, dataRounding);
    }

    @Override
    protected float calculate(float u, float v) {
        return (float) (Math.atan2(v,u) * R2D + 180);
    }
}
