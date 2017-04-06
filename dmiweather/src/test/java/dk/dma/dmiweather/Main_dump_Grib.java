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
package dk.dma.dmiweather;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ucar.grib.grib1.*;
import ucar.grid.GridParameter;
import ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Simple Main example for dumping grib data.
 * @author Klaus Groenbaek
 *         Created 28/03/17.
 */
@SuppressFBWarnings()
public class Main_dump_Grib {

    public static void main(String[] args) throws Exception {


        File tempFile = new File("c:/code/NoGoService/dmiweather/DMI_waves_Atl.2017040606.grb");
        //File tempFile = new File("/Users/kg/work/NoGoService/DMI_waves_DK.2017033000.grb");
        RandomAccessFile raf = new RandomAccessFile(tempFile.getAbsolutePath(), "r");
        raf.order(RandomAccessFile.BIG_ENDIAN);

        System.out.println("======= Dump ==========");
        Grib1Dump.main(new String[] {tempFile.getAbsolutePath()});

        System.out.println("======= Grib1Input ==========");
        Grib1Input input = new Grib1Input(raf);
        input.scan(false, false);

        ArrayList<Grib1Record> records = input.getRecords();

        for (int i = 0; i < input.getRecords().size(); i++) {
            Grib1Record record = records.get(i);
            Grib1IndicatorSection is = record.getIs();
            Grib1ProductDefinitionSection pds = record.getPDS();
            Grib1GridDefinitionSection gds = record.getGDS();

            Grib1Pds pdsv = pds.getPdsVars();
            int center = pdsv.getCenter();
            int subCenter = pdsv.getSubCenter();
            int pn = pdsv.getParameterNumber();
            GribPDSParamTable parameter_table = GribPDSParamTable.getParameterTable(
                    center, subCenter, pdsv.getParameterTableVersion());
            GridParameter parameter = parameter_table.getParameter(pn);
            if (parameter.getNumber() == 34) {
                Grib1Data gd = new Grib1Data(raf);

                float[] data = gd.getData(record.getDataOffset(),pds.getDecimalScale(), pds.bmsExists());
                Arrays.sort(data);
                float datum = data[100000];
                float datum2 = data[200000];
                System.out.println(datum + " " + datum2);

            }
            System.out.println(parameter);
        }




    }

}
