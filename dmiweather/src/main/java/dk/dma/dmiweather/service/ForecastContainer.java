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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.dto.JSonWarning;
import dk.dma.common.exception.APIException;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.common.util.MathUtil;
import dk.dma.dmiweather.dto.*;
import lombok.Synchronized;

import javax.annotation.concurrent.GuardedBy;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static dk.dma.dmiweather.service.GribFileWrapper.GRIB_NOT_DEFINED;

/**
 * Forecast data from different GRIB files are grouped together in this class, each GRIB file contains a number of data series for a given area,
 * but all for the same time
 *
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
class ForecastContainer {

    @GuardedBy("$lock")
    private final Map<ForecastConfiguration, GribFileWrapper> files = new HashMap<>();
    private final int coordinateRounding;

    ForecastContainer(int coordinateRounding) {
        this.coordinateRounding = coordinateRounding;
    }

    GridResponse getData(GridRequest request, boolean removeEmpty, boolean gridMetrics) {

        Map<GribFileWrapper, Set<GridParameterType>> sources = findSources(request);

        if (sources.isEmpty()) {
            throw new APIException(ErrorMessage.OUTSIDE_GRID, "There is no grid that contains the entire are, try with a smaller area.");
        }

        double smallestDx = Float.MAX_VALUE;
        GribFileWrapper smallestResolution = null;
        for (GribFileWrapper source : sources.keySet()) {
            if (source.getDx() < smallestDx) {
                smallestResolution = source;
                smallestDx = source.getDx();
            }
        }

        double dx = smallestResolution.getDx();
        double dy = smallestResolution.getDy();


        GeoCoordinate northWest = request.getNorthWest();
        GeoCoordinate southEast = request.getSouthEast();

        double lonDistance = southEast.getLon() - northWest.getLon();
        double latDistance = northWest.getLat() - southEast.getLat();
        int nativeNx = (int) Math.round(lonDistance / dx) +1;
        int nativeNy = (int) Math.round(latDistance / dy) +1;
        int Nx, Ny;
        double latSpacing = 0, lonSpacing = 0;
        if (request.getNx() == null) {
            // use resolution of the smallest data series
            Nx = nativeNx;
            Ny = nativeNy;
            lonSpacing = dx;
            latSpacing = dy;
        } else {
            // if the desired Nx, Ny is higher than the native resolution, we default to the native resolution

            if (nativeNx < request.getNx() || nativeNy < request.getNy()) {
                Nx = nativeNx;
                Ny = nativeNy;
            } else {
                Nx = request.getNx();
                Ny = request.getNy();
            }

            // spacing between points, we always include a point on the boundary, hence the +dx/dy
            if (Ny> 1) {
                latSpacing = (latDistance) / (Ny-1);
            }
            if (Nx > 1) {
                lonSpacing = (lonDistance) / (Nx-1);
            }
        }

        ArrayList<GridDataPoint> points = new ArrayList<>(Nx * Ny);
        for (int y = 0; y < Ny; y++) {
            for (int x = 0; x < Nx; x++) {
                double lon = northWest.getLon() + x * lonSpacing;
                double lat = southEast.getLat() + y * latSpacing;
                if (coordinateRounding != -1) {
                    lon = MathUtil.round(lon, coordinateRounding);
                    lat = MathUtil.round(lat, coordinateRounding);
                }
                points.add(new GridDataPoint().setCoordinate(new GeoCoordinate(lon, lat)));
            }
        }

        ArrayList<ForecastInfo> forecasts = new ArrayList<>();
        Set<GridParameterType> missingParameters = request.getParameters().getParamTypes();
        for (Map.Entry<GribFileWrapper, Set<GridParameterType>> entry : sources.entrySet()) {
            GribFileWrapper file = entry.getKey();
            Collection<GridParameterType> parameterTypes = entry.getValue();
            forecasts.add(new ForecastInfo().setForecastDate(file.getDate()).setCreationDate(file.getCreation()).setParameters(GridParameters.parametersFromTypes(parameterTypes)));
            missingParameters.removeAll(parameterTypes);


            for (GridParameterType type : parameterTypes) {
                float[] data = file.getDataProviders().get(type).getData(northWest, southEast, Nx, Ny, lonSpacing, latSpacing);
                for (int i = 0; i < points.size(); i++) {
                    GridDataPoint point = points.get(i);
                    if (data[i] != GRIB_NOT_DEFINED) {
                        point.setData(type, data[i]);
                    }
                }
            }
        }

        if (removeEmpty) {
            points.removeIf(p -> !p.hasValues());
        }
        GridResponse response = new GridResponse().setPoints(points).setForecasts(forecasts).setQueryTime(Instant.now());

        // set the deprecate time to the first forecast time
        response.setForecastDate(forecasts.get(0).getForecastDate());

        if (!missingParameters.isEmpty()) {
            WarningMessage msg = WarningMessage.MISSING_DATA;
            response.setWarning(new JSonWarning().setId(msg.getId()).setMessage(msg.getMessage()).setDetails("There was no data for the following parameters, " +
                missingParameters.stream().map(Object::toString).collect(Collectors.joining(","))));
        }

        if (gridMetrics) {
            response.setDx(lonSpacing);
            response.setDy(latSpacing);
            response.setNx(Nx);
            response.setNy(Ny);
            if (!removeEmpty) {
                GeoCoordinate first = points.get(0).getCoordinate();
                GeoCoordinate last = points.get(points.size() - 1).getCoordinate();
                response.setNorthWest(new GeoCoordinate(first.getLon(), last.getLat()));
                response.setSouthEast(new GeoCoordinate(last.getLon(), first.getLat()));
            }
        }
        return response;
    }

    @Synchronized
    private Map<GribFileWrapper, Set<GridParameterType>> findSources(GridRequest request) {

        Geometry requestArea = toGeometry(request);
        Collection<GribFileWrapper> values = files.values();
        Set<GridParameterType> set = request.getParameters().getParamTypes();
        Map<GribFileWrapper, Set<GridParameterType>> map = new HashMap<>();
        for (GribFileWrapper gribFile : values) {
            if (gribFile.getArea().contains(requestArea)) {
                Sets.SetView<GridParameterType> intersection = Sets.intersection(set, gribFile.getParameterTypes());
                if (!intersection.isEmpty()) {
                    map.put(gribFile, intersection);
                }

            }
            if (set.isEmpty()) {
                break;
            }
        }
        return map;
    }

    private Geometry toGeometry(GridRequest request) {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[5];
        GeoCoordinate northWest = request.getNorthWest();
        GeoCoordinate southEast = request.getSouthEast();
        coordinates[0] = new Coordinate(northWest.getLon(), southEast.getLat());
        coordinates[1] = new Coordinate(southEast.getLon(), southEast.getLat());
        coordinates[2] = new Coordinate(southEast.getLon(), northWest.getLat());
        coordinates[3] = new Coordinate(northWest.getLon(), northWest.getLat());
        coordinates[4] = new Coordinate(northWest.getLon(), southEast.getLat());

        return new Polygon(new LinearRing(new CoordinateArraySequence(coordinates), factory), new LinearRing[0], factory);
    }

    @Synchronized
    void newFile(ForecastConfiguration configuration, GribFileWrapper wrapper) {
        files.put(configuration, wrapper);
    }

    void markOld(ForecastConfiguration configuration) {
        GribFileWrapper wrapper = files.get(configuration);
        if (wrapper != null) {
            wrapper.markAsOld();
        }
    }

    /**
     * If all configuration are old, then this ForecastData is outdated (in the past) and the owner should throw it away
     * @return true if all configurations have been marked as old
     */
    boolean isOld() {
        for (GribFileWrapper gribFileWrapper : files.values()) {
            if (!gribFileWrapper.isOld()) {
                return false;
            }
        }
        return true;
    }

    @Synchronized
    Map<ForecastConfiguration, GribFileWrapper> getGribFiles() {
        return Maps.newHashMap(this.files);
    }
}
