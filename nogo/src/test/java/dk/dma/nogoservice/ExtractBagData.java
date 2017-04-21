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
package dk.dma.nogoservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.nogoservice.dto.GridData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * Utility main that can transform extracted BAG data to GridData used for nogo areas
 *
 * First you will download the HDF 5 utility pack, to get the h5dump application which can export the data to ascii format
 * https://support.hdfgroup.org/ftp/HDF5/releases/hdf5-1.8.7/obtain5187.html
 * The data is extracted the data use the following command  h5dump -o export.txt --noindex -d BAG_root/elevation EfficienSea2/10m_min_GRON.bag
 * the -d argument specifies the name of the data series you wish to export, the exported data is basically a grid, but when the --noindex switch is given
 * you basically get a one dimensional array of values separated by ','. As with GRIB files the data is structured as rows of columns
 *
 *
 * @author Klaus Groenbaek
 *         Created 21/04/17.
 */
@Slf4j
@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
public class ExtractBagData {

    private static final int H5DUMP_TIMEOUT = 1;
    private static final int H5_FAILED = 2;
    private static final int NO_XML_FILE = 3;
    private static final float NO_DATA = 1e+6f;

    public static void main(String[] args) throws Exception {

        try {
            String bagFile = "/Users/kg/work/NoGoService/EfficienSea2/10m_min_GRON.bag";
            String dataSet = "BAG_root/elevation";
            String uncertaintyDataSet = "BAG_root/uncertainty";
            String hdf5Dump = "/Users/kg/work/NoGoService/hdf5-1.8.7-mac-intel-x86_64-static/bin/h5dump";


            GridData gridData = new ExtractBagData(hdf5Dump).createGridData(bagFile, dataSet, uncertaintyDataSet);
            gridData.setDescription("Bathymetric data for Flintrannan Sweden, -9999.0 means no value (land), and depth is given as negative altitude.");
            gridData.setName("Flintrannan");
            File outFile = new File("/Users/kg/work/NoGoService/Flintrannan_depth.json");

            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(outFile, gridData);

        } catch (ExitCodeException e) {
            System.exit(e.exitCode);
        }
    }


    private final String hdf5Dump;

    private ExtractBagData(String hdf5Dump) {
        this.hdf5Dump = hdf5Dump;
    }

    private GridData createGridData(String bagFile, String dataSet, String uncertaintyDataSet) {
        File dataFile = dumpData(bagFile, dataSet);
        File uncertaintyFile = dumpData(bagFile, uncertaintyDataSet);
        GridData data = readMetaData(bagFile);
        return addData(data, dataFile, uncertaintyFile);
    }
    @SneakyThrows(IOException.class)
    private GridData addData(GridData data, File asciiFile, File uncertaintyFile) {

        try (BufferedReader dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(asciiFile), StandardCharsets.US_ASCII));
             BufferedReader uncertaintyReader = new BufferedReader(new InputStreamReader(new FileInputStream(uncertaintyFile), StandardCharsets.US_ASCII))) {

            List<String> values = dataReader.lines().map(line -> line.split(",")).flatMap(Arrays::stream).collect(Collectors.toList());
            List<String> corrections = uncertaintyReader.lines().map(line -> line.split(",")).flatMap(Arrays::stream).collect(Collectors.toList());
            float[] array = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                String string = values.get(i);
                String correction = corrections.get(i);
                float value = Float.parseFloat(string);
                float uncertainty = Float.parseFloat(correction);
                if (value == NO_DATA) {
                    array[i] = GridData.NO_DATA;
                } else {
                    if (uncertainty == NO_DATA) {
                        System.out.println("Data point has no uncertainty, is the data correct?");
                    }
                    array[i] = value + uncertainty;
                }
            }
            data.setData(array);
        }

        return data;
    }

    @SneakyThrows(DocumentException.class)
    private GridData readMetaData(String bagFile) {
        File xmlFile = new File(bagFile + ".xml");
        if (!xmlFile.exists()) {
            throw new ExitCodeException(NO_XML_FILE);
        }

        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlFile);

        // we use fully qualified XPath, as we would like to fail if the format change
        Node coordinateBox = document.selectSingleNode("/gmi:MI_Metadata/gmd:identificationInfo/bag:BAG_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");
        Node westLon = coordinateBox.selectSingleNode("gmd:westBoundLongitude/gco:Decimal");
        Node eastLon = coordinateBox.selectSingleNode("gmd:eastBoundLongitude/gco:Decimal");
        Node southLat = coordinateBox.selectSingleNode("gmd:southBoundLatitude/gco:Decimal");
        Node northLat = coordinateBox.selectSingleNode("gmd:northBoundLatitude/gco:Decimal");

        Node columnNode = document.selectSingleNode("/gmi:MI_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:dimensionName/gmd:MD_DimensionNameTypeCode[@codeListValue=\"column\"]/../../gmd:dimensionSize/gco:Integer");
        Node rowNode = document.selectSingleNode("/gmi:MI_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:dimensionName/gmd:MD_DimensionNameTypeCode[@codeListValue=\"row\"]/../../gmd:dimensionSize/gco:Integer");

        GridData gridData = new GridData();
        gridData.setLo1(Float.parseFloat(westLon.getText()));
        gridData.setLo2(Float.parseFloat(eastLon.getText()));
        gridData.setLa1(Float.parseFloat(southLat.getText()));
        gridData.setLa2(Float.parseFloat(northLat.getText()));
        gridData.setNx(Integer.parseInt(columnNode.getText()));
        gridData.setNy(Integer.parseInt(rowNode.getText()));


        return gridData;
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
    private File dumpData(String bagFile, String dataSet) {

        File tempFile = File.createTempFile("bagfile", "");
        Runtime rt = Runtime.getRuntime();
        String command = hdf5Dump + " -o " + tempFile + " --noindex -d " + dataSet + " " + bagFile;
        Process process = rt.exec(command);
        Thread outReader = new Thread(new StreamConsumer(process.getInputStream(), System.out));
        Thread errorReader = new Thread(new StreamConsumer(process.getErrorStream(), System.err));
        outReader.start();
        errorReader.start();

        // if you ever change this timeout be aware if the size of the generated data file means that any of the service code needs to change
        boolean exited = process.waitFor(60, TimeUnit.SECONDS);
        if (!exited) {
            System.err.println("h5dump filed to complete in 60 seconds");
            throw new ExitCodeException(H5DUMP_TIMEOUT);
        } else {
            outReader.join();
            errorReader.join();
            if (process.exitValue() == 0) {
                return tempFile;
            } else {
                System.err.println("h5dump exited with errorCode " + process.exitValue());
                throw new ExitCodeException(H5_FAILED);
            }

        }
    }

    /**
     * For reading the out and error streams associated with a process
     */
    private static class StreamConsumer implements Runnable {
        private final InputStream inputStream;
        private final PrintStream out;

        private StreamConsumer(InputStream inputStream, PrintStream out) {
            this.inputStream = inputStream;
            this.out = out;
        }

        public void run() {

            BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.US_ASCII));
            String line;
            try {
                while ((line = input.readLine()) != null) {
                    out.println(line);
                }
            } catch (IOException e) {
                log.error("Problem reading process output", e);
            }
        }
    }

    private static class ExitCodeException extends RuntimeException {
        private final int exitCode;

        private ExitCodeException(int exitCode) {
            this.exitCode = exitCode;
        }
    }
}
