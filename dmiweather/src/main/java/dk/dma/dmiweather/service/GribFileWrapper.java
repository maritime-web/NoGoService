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
package dk.dma.dmiweather.service;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.*;
import dk.dma.dmiweather.dto.GridParameterType;
import dk.dma.dmiweather.grib.*;
import lombok.Getter;
import ucar.grib.NoValidGribException;
import ucar.grib.grib1.*;
import ucar.grid.GridParameter;
import ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Getter
public class GribFileWrapper {

    public static final float GRIB_NOT_DEFINED = -9999;     // Grib1BinaryDataSection.UNDEFINED
    public static final Comparator<GribFileWrapper> SMALLEST_RESOLUTION = new Comparator<GribFileWrapper>() {
        @Override
        public int compare(GribFileWrapper o1, GribFileWrapper o2) {
            return Double.compare(o1.getDx(), o2.getDx());
        }
    };

    private static final int MERIDIONAL_WIND = 33;
    private static final int ZONAL_WIND = 34;
    private static final int MERIDIONAL_CURRENT = 49;
    private static final int ZONAL_CURRENT = 50;
    private static final int SEA_LEVEL = 82;
    private static final int DENSITY = 89;
    private static final int WAVE_HEIGHT = 229; //SWH:Significant wave height  [m]
    private static final int WAVE_DIRECTION = 230; //MWD:Mean wave direction  [degrees]
    private static final int WAVE_PERIOD = 232; //MWP:Mean wave period  [s]


    private final ForecastConfiguration configuration;
    private final ImmutableMap<GridParameterType, AbstractDataProvider> dataProviders;
    private final Instant creation;
    private final int dataRounding;
    private final int coordinateRouding;
    private final double dx;
    private final double dy;
    private final Polygon polygon;
    private final int nx;
    private final int ny;
    private volatile boolean old;

    GribFileWrapper(ForecastConfiguration configuration, Instant creation, File file, int dataRounding, int coordinateRouding) {
        this.configuration = configuration;
        this.creation = creation;
        this.dataRounding = dataRounding;
        this.coordinateRouding = coordinateRouding;
        dataProviders = ImmutableMap.copyOf(initProviders(file));

        double smallestDx = Float.MAX_VALUE;
        AbstractDataProvider found = null;
        for (AbstractDataProvider dataProvider : dataProviders.values()) {
            if (dataProvider.getDx()  < smallestDx ) {
                smallestDx = dataProvider.getDx();
                found = dataProvider;
            }
        }

        dx = found.getDx();
        dy = found.getDy();
        nx = found.getNx();
        ny = found.getNy();

        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(found.getLo1(), found.getLa1());
        coordinates[1] = new Coordinate(found.getLo2(), found.getLa1());
        coordinates[2] = new Coordinate(found.getLo2(), found.getLa2());
        coordinates[3] = new Coordinate(found.getLo1(), found.getLa2());
        coordinates[4] = new Coordinate(found.getLo1(), found.getLa1());

        polygon = factory.createPolygon(coordinates);
    }

    Set<GridParameterType> getParameterTypes() {
        return dataProviders.keySet();
    }

    /**
     * creates a dataProvider for each parameter type from the information in the GRIB file
     * @return an Immutable map of providers
     * @param file the GRIB file
     */
    private Map<GridParameterType, AbstractDataProvider> initProviders(File file) {
        HashMap<GridParameterType, AbstractDataProvider> map = new HashMap<>();

        try {
            RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "r");
            raf.order(RandomAccessFile.BIG_ENDIAN);

            Grib1Input input = new Grib1Input(raf);
            input.scan(false, false);
            ArrayList<Grib1Record> records = input.getRecords();

            Grib1Data gd = new Grib1Data(raf);

            // loop through the records and create a map from parameter number to ParameterAndRecord, since some data series is build from two parameters
            HashMap<Integer, ParameterAndRecord> lookup = new HashMap<>();
            for (int i = 0; i < input.getRecords().size(); i++) {
                Grib1Record record = records.get(i);
                Grib1ProductDefinitionSection pds = record.getPDS();
                Grib1Pds pdsv = pds.getPdsVars();
                int center = pdsv.getCenter();
                int subCenter = pdsv.getSubCenter();
                int pn = pdsv.getParameterNumber();
                GribPDSParamTable parameter_table = GribPDSParamTable.getParameterTable(
                        center, subCenter, pdsv.getParameterTableVersion());
                GridParameter parameter = parameter_table.getParameter(pn);

                lookup.put(parameter.getNumber(), new ParameterAndRecord(parameter, record));
            }
            // configure the factories which will create data providers
            List<DataProviderFactory> factories = configureFactories(gd, lookup);
            for (DataProviderFactory factory : factories) {
                map.putAll(factory.create());
            }

        } catch (IOException | NoValidGribException e) {
            throw new RuntimeException("Unable to load GRIB file", e);
        }
        return map;
    }

    private List<DataProviderFactory> configureFactories(Grib1Data gribData, HashMap<Integer, ParameterAndRecord> lookup) {

        List<DataProviderFactory> factories = new ArrayList<>();

        ParameterAndRecord meridionalWind = lookup.get(MERIDIONAL_WIND);
        ParameterAndRecord zonalWind = lookup.get(ZONAL_WIND);
        if (meridionalWind != null && zonalWind != null) {
            factories.add(new MeridionalZonalFactory(gribData, meridionalWind, zonalWind,
                    GridParameterType.WindDirection, GridParameterType.WindSpeed, dataRounding));
        }

        ParameterAndRecord meridionalCurrent = lookup.get(MERIDIONAL_CURRENT);
        ParameterAndRecord zonalCurrent = lookup.get(ZONAL_CURRENT);
        if (meridionalCurrent != null && zonalCurrent != null) {
            factories.add(new MeridionalZonalFactory(gribData, meridionalCurrent, zonalCurrent,
                    GridParameterType.CurrentDirection, GridParameterType.CurrentSpeed, dataRounding));
        }

        ParameterAndRecord sealevel = lookup.get(SEA_LEVEL);
        if (sealevel != null) {
            factories.add(new SimpleDataProviderFactory(gribData, sealevel, GridParameterType.SeaLevel, dataRounding));
        }
        ParameterAndRecord densityParam = lookup.get(DENSITY);
        if (densityParam != null) {
            // old test files need to be updated since they don't have density
            factories.add(new SimpleDataProviderFactory(gribData, densityParam, GridParameterType.Density, dataRounding));
        }

        ParameterAndRecord waveHeight = lookup.get(WAVE_HEIGHT);
        if (waveHeight != null) {
            factories.add(new SimpleDataProviderFactory(gribData, waveHeight, GridParameterType.WaveHeight, dataRounding));
        }

        ParameterAndRecord waveDirection = lookup.get(WAVE_DIRECTION);
        if (waveDirection != null) {
            factories.add(new SimpleDataProviderFactory(gribData, waveDirection, GridParameterType.WaveDirection, dataRounding));
        }

        ParameterAndRecord wavePeriod = lookup.get(WAVE_PERIOD);
        if (wavePeriod != null) {
            factories.add(new SimpleDataProviderFactory(gribData, wavePeriod, GridParameterType.WavePeriod, dataRounding));
        }


        return factories;
    }

    Geometry getArea() {
        return polygon;
    }

    void markAsOld() {
        old = true;
    }
}
