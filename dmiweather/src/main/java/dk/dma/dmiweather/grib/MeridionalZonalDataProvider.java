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
import lombok.extern.slf4j.Slf4j;
import ucar.grib.grib1.*;
import ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class for matching zonal and meridional data serices to provide a new calculated data value besed on the extending class's implementation of the
 * calculate method
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@Slf4j
public abstract class MeridionalZonalDataProvider extends AbstractDataProvider {

    private final ParameterAndRecord meridional;
    private final ParameterAndRecord zonal;
    private final File file;

    MeridionalZonalDataProvider(ParameterAndRecord meridional, ParameterAndRecord zonal, File file, int dataRounding) {
        super(meridional, dataRounding);
        this.meridional = meridional;
        this.zonal = zonal;
        this.file = file;
    }

    @Override
    @SneakyThrows(IOException.class)
    public float[] getData() {
        Grib1Record record = meridional.record;
        Grib1ProductDefinitionSection pds = record.getPDS();

        RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "r");
        try {
            raf.order(RandomAccessFile.BIG_ENDIAN);
            Grib1Data gd = new Grib1Data(raf);

            //noinspection deprecation
            float[] meridional = getData(pds, gd, this.meridional);
            //noinspection deprecation
            float[] zonal = getData(pds, gd, this.zonal);

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
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                log.info("Unable to close GRIB file", e);
            }
        }
    }

    protected abstract float calculate(float u, float v);
}
