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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import ucar.grib.grib1.*;

import java.io.IOException;

/**
 * data provider that looks at a single GRIB data series
 * @author Klaus Groenbaek
 *         Created 30/03/17.
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class SimpleDataProvider extends AbstractDataProvider {

    private final ParameterAndRecord parameterAndRecord;
    private final Grib1Data grib1Data;

    SimpleDataProvider(ParameterAndRecord parameterAndRecord, Grib1Data grib1Data, int dataRounding) {
        super(parameterAndRecord, dataRounding);
        this.parameterAndRecord = parameterAndRecord;
        this.grib1Data = grib1Data;
    }


    @Override
    @SneakyThrows(IOException.class)
    public float[] getData() {
        Grib1Record record = parameterAndRecord.record;
        Grib1ProductDefinitionSection pds = record.getPDS();
        //noinspection deprecation
        return grib1Data.getData(record.getDataOffset(), pds.getDecimalScale(), pds.bmsExists());
    }

}
