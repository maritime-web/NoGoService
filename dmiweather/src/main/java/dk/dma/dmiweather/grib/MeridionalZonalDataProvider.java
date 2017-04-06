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

import dk.dma.dmiweather.service.GribFileWrapper;
import lombok.SneakyThrows;
import ucar.grib.grib1.*;

import java.io.IOException;

/**
 * Abstract class for matching zonal and meridional data serices to provide a new calculated data value besed on the extending class's implementation of the
 * calculate method
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public abstract class MeridionalZonalDataProvider extends AbstractDataProvider {

    private final ParameterAndRecord meridional;
    private final ParameterAndRecord zonal;
    private final Grib1Data data;

    MeridionalZonalDataProvider(ParameterAndRecord meridional, ParameterAndRecord zonal, Grib1Data data, int dataRounding) {
        super(meridional, dataRounding);
        this.meridional = meridional;
        this.zonal = zonal;
        this.data = data;
    }

    @Override
    @SneakyThrows(IOException.class)
    public float[] getData() {
        Grib1Record record = meridional.record;
        Grib1ProductDefinitionSection pds = record.getPDS();

        //noinspection deprecation
        float[] meridional = data.getData(this.meridional.record.getDataOffset(), pds.getDecimalScale(), pds.bmsExists());
        //noinspection deprecation
        float[] zonal = data.getData(this.zonal.record.getDataOffset(), pds.getDecimalScale(), pds.bmsExists());

        float[] array = new float[zonal.length];

        for (int i = 0; i < zonal.length; i++) {
            float u = zonal[i];
            float v = meridional[i];

            if (u == GribFileWrapper.GRIB_NOT_DEFINED  || v == GribFileWrapper.GRIB_NOT_DEFINED) {
                array[i] = GribFileWrapper.GRIB_NOT_DEFINED;
            }else {
                // direction is different than a normal coordinate system, the rotation is clockwise starting due south
                // direction is calculated using a formula found here http://www.ncl.ucar.edu/Document/Functions/Built-in/atan2.shtml
                float value = calculate(u, v);
                array[i]= value;
            }
        }

        return array;
    }

    protected abstract float calculate(float u, float v);
}
