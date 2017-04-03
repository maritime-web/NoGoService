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
import ucar.grib.grib1.*;

import java.io.IOException;
import java.util.Map;

/**
 * @author Klaus Groenbaek
 *         Created 31/03/17.
 */
public interface DataProviderFactory {

    static float[] getData(ParameterAndRecord parameterAndRecord, Grib1Data gribData) throws IOException {
        Grib1Record record = parameterAndRecord.record;
        Grib1ProductDefinitionSection pds = record.getPDS();

        float[] dataData = gribData.getData(record.getDataOffset(), pds.getDecimalScale(), pds.bmsExists());
        for (int i = 0; i < dataData.length; i++) {
            float dataDatum = dataData[i];
            if (dataDatum != -9999f) {
                dataData[i] = dataData[i];
            }
        }
        return dataData;
    }

    Map<GridParameterType, DataProvider> create() throws IOException;
}
