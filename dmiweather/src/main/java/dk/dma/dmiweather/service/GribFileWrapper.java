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
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.dto.JSonWarning;
import dk.dma.common.util.MathUtil;
import dk.dma.dmiweather.dto.*;
import dk.dma.dmiweather.grib.*;
import ucar.grib.NoValidGribException;
import ucar.grib.grib1.*;
import ucar.grid.GridParameter;
import ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
public class GribFileWrapper {

    public static final float GRIB_NOT_DEFINED = -9999;     // Grib1BinaryDataSection.UNDEFINED
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final int MERIDIONAL_WIND = 33;
    private static final int ZONAL_WIND = 34;
    private static final int MERIDIONAL_CURRENT = 49;
    private static final int ZONAL_CURRENT = 50;
    private static final int SEA_LEVEL = 82;
    private static final int DENSITY = 89;

    private final Instant date;
    private final ImmutableMap<GridParameterType, DataProvider> dataProviders;
    private final int dataRounding;
    private final int coordinateRouding;

    GribFileWrapper(Instant date, File file, int dataRounding, int coordinateRouding) {
        this.date = date;
        this.dataRounding = dataRounding;
        this.coordinateRouding = coordinateRouding;

        dataProviders = ImmutableMap.copyOf(initProviders(file));
    }

    /**
     * creates a dataProvider for each parameter type from the information in the GRIB file
     * @return an Immutable map of providers
     * @param file the GRIB file
     */
    private ImmutableMap<GridParameterType, DataProvider> initProviders(File file) {
        HashMap<GridParameterType, DataProvider> map = new HashMap<>();

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

        return ImmutableMap.copyOf(map);
    }


    GridResponse getData(GridRequest request, boolean removeEmpty, boolean gridMetrics)  {


        // find the parameter with the smallest resolution, and ask for data in that format
        float dx = 360;
        float dy = 360;
        List<GridParameterType> parameterTypes = request.getParameters().getParamTypes();
        for (GridParameterType type : parameterTypes) {
            dx = Math.min(dataProviders.get(type).getDx(), dx);
            dy = Math.min(dataProviders.get(type).getDy(), dy);
        }

        GeoCoordinate northWest = request.getNorthWest();
        GeoCoordinate southEast = request.getSouthEast();

        int deltaX = (int) Math.round((southEast.getLon() - northWest.getLon()) / dx) +1;
        int deltaY = (int) Math.round((northWest.getLat() - southEast.getLat()) / dy) +1;


        ArrayList<GridDataPoint> points = new ArrayList<>(deltaX + deltaY);
        for (int y= 0; y < deltaY; y++) {
            for (int x= 0; x < deltaX; x++) {
                double lon = northWest.getLon() + x * dx;
                double lat = southEast.getLat() + y * dy;
                if (coordinateRouding != -1) {
                    lon = MathUtil.round(lon, coordinateRouding);
                    lat = MathUtil.round(lat, coordinateRouding);
                }
                points.add(new GridDataPoint().setCoordinate(new GeoCoordinate(lon, lat)));
            }
        }

        for (GridParameterType type : parameterTypes) {
            float[] data = dataProviders.get(type).getData(northWest, southEast, dx, dy);
            for (int i = 0; i < points.size(); i++) {
                GridDataPoint point = points.get(i);
                if (data[i] != GRIB_NOT_DEFINED) {
                    point.setData(type, data[i]);
                }
            }
        }
        if (removeEmpty) {
            points.removeIf(p -> !p.hasValues());
        }

        GridResponse response = new GridResponse().setPoints(points).setForecastDate(date).setQueryTime(Instant.now());

        if(request.getParameters().isWave()) {
            WarningMessage msg = WarningMessage.MISSING_DATA;
            response.setWarning(new JSonWarning().setId(msg.getId()).setMessage(msg.getMessage()).setDetails("Wave information is currently not provided."));
        }
        if (gridMetrics) {
            response.setDx(dx);
            response.setDy(dy);
            response.setNx(deltaX);
            response.setNy(deltaY);
            if (!removeEmpty) {
                GeoCoordinate first = points.get(0).getCoordinate();
                GeoCoordinate last = points.get(points.size() - 1).getCoordinate();
                response.setNorthWest(new GeoCoordinate(first.getLon(), last.getLat()));
                response.setSouthEast(new GeoCoordinate(last.getLon(), first.getLat()));
            }
        }
        return response;
    }

    private List<DataProviderFactory> configureFactories(Grib1Data gribData, HashMap<Integer, ParameterAndRecord> lookup) {

        List<DataProviderFactory> factories = new ArrayList<>();

        factories.add(new MeridionalZonalFactory(gribData, lookup.get(MERIDIONAL_WIND), lookup.get(ZONAL_WIND),
                GridParameterType.WindDirection, GridParameterType.WindSpeed, dataRounding));

        factories.add(new MeridionalZonalFactory(gribData, lookup.get(MERIDIONAL_CURRENT), lookup.get(ZONAL_CURRENT),
                GridParameterType.CurrentDirection, GridParameterType.CurrentSpeed, dataRounding));

        factories.add(new SimpleDataProviderFactory(gribData, lookup.get(SEA_LEVEL), GridParameterType.SeaLevel, dataRounding));
        ParameterAndRecord densityParam = lookup.get(DENSITY);
        if (densityParam != null) {
            // old test files need to be updated since they don't have density
            factories.add(new SimpleDataProviderFactory(gribData, densityParam, GridParameterType.Density, dataRounding));
        }

        return factories;
    }



}
