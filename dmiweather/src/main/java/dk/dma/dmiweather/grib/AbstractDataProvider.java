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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.exception.APIException;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.common.util.MathUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import ucar.grib.grib1.Grib1GDSVariables;

import java.lang.ref.WeakReference;

import static dk.dma.dmiweather.service.GribFileWrapper.GRIB_NOT_DEFINED;

/**
 * Abstract class for providing data. Handles the request validation, and selecting the data points that match the request
 * subclasses provides the data.
 * This class does a simple WeakReference caching to void loading data from disk on every request
 *
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@Slf4j
public abstract class AbstractDataProvider implements DataProvider {
    private final int dataRounding;
    private final double dx;
    private final double dy;
    private final int Ny;
    private final int Nx;
    private final double lo1;
    private final double la2;
    private final double la1;
    private final double lo2;
    private WeakReference<float[]> cachedData;  // todo consider using a GUAVA cache, and share it between all dataProviders to get LRU behaviour


    AbstractDataProvider(ParameterAndRecord parameterAndRecord, int dataRounding) {
        this.dataRounding = dataRounding;
        // we have to recalculate dx, dy because the GRIB format is not precise enough (it only has two bytes, and then divides with 1000)
        Grib1GDSVariables vars = parameterAndRecord.record.getGDS().getGdsVars();
        Ny = vars.getNy();
        Nx = vars.getNx();
        dy = (vars.getLa2() - vars.getLa1()) / (Ny - 1);
        dx = (vars.getLo2() - vars.getLo1()) / (Nx - 1);
        lo1 = vars.getLo1();
        la2 = vars.getLa2();
        la1 = vars.getLa1();
        lo2 = vars.getLo2();
    }

    /**
     * Test constructor allowing you to set the grid metrics directly whitout needing a grib file
     */
    @VisibleForTesting
    protected AbstractDataProvider(int dataRounding, float dx, float dy, int ny, int nx, float lo1, float la2, float la1, float lo2) {
        this.dataRounding = dataRounding;
        this.dx = dx;
        this.dy = dy;
        Ny = ny;
        Nx = nx;
        this.lo1 = lo1;
        this.la2 = la2;
        this.la1 = la1;
        this.lo2 = lo2;
    }

    @Override
    public float[] getData(GeoCoordinate northWest, GeoCoordinate southEast, int Nx, int Ny, double lonSpacing, double lonOffset, double latSpacing, double latOffset) {

        validate(northWest, southEast);

        int startY = (int) Math.round((southEast.getLat() - this.la1) / this.dy);
        int startX = (int) Math.round((northWest.getLon() - this.lo1) / this.dx);

        float[] grid = new float[Ny * Nx];

        double tolerance = 0.00001;
        if (DoubleMath.fuzzyEquals(this.dx, lonSpacing, tolerance) && DoubleMath.fuzzyEquals(this.dy, latSpacing, tolerance)) {
            // native resolution
            float[] data = roundAndCache();
            for (int row = 0; row < Ny; row++) {
                System.arraycopy(data, (startY + row) * this.Nx + startX, grid, row * Nx, Nx);
            }
        } else {
            if (Nx > this.Nx && Ny < this.Ny || Ny > this.Ny && Nx < this.Nx) {
                throw new APIException(ErrorMessage.INVALID_SCALING, String.format("Native Nx:%s, Ny:%s, desired Nx:%s, Ny:%s", this.Nx, this.Ny, Nx, Ny));
            }

            if (Nx > this.Nx) {
                // scaling up
                lonOffset = -0.00001f;
                latOffset = -0.00001f;
                float[] data = roundAndCache();
                for (int row = 0; row < Ny; row++) {
                    int y = startY + (int) Math.round(Math.floor(row * latSpacing - latOffset));
                    for (int col = 0; col < Nx; col++) {
                        int x = startX + (int) Math.round(Math.floor(col * lonSpacing - lonOffset));

                        float datum = data[y * this.Nx + x];
                        grid[row * Nx + col] = datum;
                    }
                }

            } else {

                float[] data = roundAndCache();

                for (int row = 0; row < Ny; row++) {
                    int y = startY + (int ) Math.round(row * latSpacing/this.dy + latOffset);
                    for (int col = 0; col < Nx; col++) {

                        int x = startX + (int) Math.round(col * lonSpacing/this.dx + lonOffset);
                        float datum = data[y * this.Nx + x];

                        if (datum == GRIB_NOT_DEFINED) {
                            // when we down-sample we may end up selecting only NOT_DEFINED, so we look around to see if there is a defined value close by
                            int xSearchDistance = (int) Math.round(lonSpacing / 2);
                            int ySearchDistance = (int) Math.round(latSpacing / 2); // we search the space between points

                            float[] candidate = new float[]{this.Nx + this.Ny, GRIB_NOT_DEFINED};   // to hold the closest distance and the value

                            searchRow(data, y, x, xSearchDistance, candidate);      // search this row

                            for (int i = 1; i <= ySearchDistance && i < candidate[0]; i++) {
                                if (y - i > 0) {
                                    // there is a row before
                                    searchRow(data, y - i, x, xSearchDistance, candidate);
                                } else if (y + i < this.Ny) {
                                    //there is a row after
                                    searchRow(data, y + i, x, xSearchDistance, candidate);
                                } else {
                                    break;
                                }
                            }
                            datum = candidate[1];
                        }

                        grid[row * Nx + col] = datum;
                    }
                }
            }
        }

        return grid;
    }

    private void searchRow(float[] data, int y, int x, int xSearchDistance, float[] candidate) {
        float current = data[y * this.Nx + x];
        if (current != GRIB_NOT_DEFINED) {
            candidate[0] = 1;   // may not actually be distance 1, but it is the closest, because this is the first point in a new row
            candidate[1] = current;
            return;
        }
        // first search the current row left then right, taking care not to run outside the grid
        for (int i = 1; i <= xSearchDistance && i < candidate[0]; i++) {

            if (x - i > 0) {
                // we can go left
                current = data[y * this.Nx + x - i];
            } else if (x + i < this.Nx) {
                // we can go right
                current = data[y * this.Nx + x + i];
            } else {
                break;
            }
            if (current != GRIB_NOT_DEFINED) {
                candidate[0] = i;           // distance
                candidate[1] = current;     // value
            }
        }
    }


    @Synchronized
    private float[] roundAndCache() {
        if (cachedData == null) {
            float[] data = loadAndRound();
            cachedData = new WeakReference<>(data);

            return data;
        } else {
            float[] data = cachedData.get();
            if (data == null) {
                data = loadAndRound();
                cachedData = new WeakReference<>(data);
            }
            return data;
        }
    }

    private float[] loadAndRound() {
        float[] data = getData();
        if (dataRounding != -1) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != GRIB_NOT_DEFINED) {
                    data[i] = MathUtil.round(data[i], dataRounding);
                }
            }
        }
        return data;
    }

    public abstract float[] getData();

    @Override
    public int getNx() {
        return Nx;
    }

    @Override
    public int getNy() {
        return Ny;
    }


    @Override
    public double getDx() {
        return dx;
    }

    @Override
    public double getDy() {
        return dy;
    }

    @Override
    public void validate(GeoCoordinate northWest, GeoCoordinate southEast) {
        if (northWest.getLon() < lo1 || northWest.getLat() > la2 || southEast.getLon() > lo2 || southEast.getLat() < la1) {
            throw new APIException(ErrorMessage.OUTSIDE_GRID, String.format("Query is outside data grid, grid corners northWest:(%s), southEast:(%s)",
                    new GeoCoordinate(lo1, la1), new GeoCoordinate(lo2, la2)));
        }

        if (northWest.getLon() > southEast.getLon()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LOT);
        }
        if (northWest.getLat() < southEast.getLat()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LAT);
        }
    }
}
