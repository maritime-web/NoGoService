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

import com.google.common.math.DoubleMath;
import dk.dma.common.dto.GeoCoordinate;
import dk.dma.common.util.MathUtil;
import dk.dma.common.exception.ErrorMessage;
import dk.dma.common.exception.APIException;
import dk.dma.dmiweather.service.GribFileWrapper;
import lombok.Synchronized;
import ucar.grib.grib1.Grib1GDSVariables;

import java.lang.ref.WeakReference;

/**
 * Abstract class for providing data. Handles the request validation, and selecting the data points that match the request
 * subclasses provides the data.
 * This class does a simple WeakReference caching to void loading data from disk on every request
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public abstract class AbstractDataProvider implements DataProvider {
    final ParameterAndRecord parameterAndRecord;
    private final int dataRounding;
    private final float dx;
    private final float dy;
    private WeakReference<float[]> cachedData;  // todo consider using a GUAVA cache, and share it between all dataProviders to get LRU behaviour

    AbstractDataProvider(ParameterAndRecord parameterAndRecord, int dataRounding) {
        this.parameterAndRecord = parameterAndRecord;
        this.dataRounding = dataRounding;
        // we have to recalculate dx, dy because the GRIB format is not precise enough (it only has two bytes, and then divides with 1000)
        Grib1GDSVariables vars = parameterAndRecord.record.getGDS().getGdsVars();
        dy = (vars.getLa2() - vars.getLa1()) / (vars.getNy() -1);
        dx = (vars.getLo2() - vars.getLo1()) / (vars.getNx() -1);
    }

    @Override
    public float[] getData(GeoCoordinate northWest, GeoCoordinate southEast, float dx, float dy) {
        Grib1GDSVariables vars = parameterAndRecord.record.getGDS().getGdsVars();
        validate(northWest, southEast, dx, dy, vars);

        int startY = (int) Math.round((southEast.getLat() - vars.getLa1()) / this.dy);
        int startX = (int) Math.round((northWest.getLon() - vars.getLo1()) / this.dx);

        int deltaX = (int) Math.round((southEast.getLon() - northWest.getLon()) / this.dx) +1;
        int deltaY = (int) Math.round((northWest.getLat() - southEast.getLat()) / this.dy) +1;

        float[] grid = new float[(deltaY) *(deltaX)];

        float[] data = roundAndCache();
        for (int row = 0; row < deltaY; row++) {
            System.arraycopy(data, (startY + row) * vars.getNx() + startX, grid, row*deltaX, deltaX);
        }

        return grid;
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
                if (data[i] != GribFileWrapper.GRIB_NOT_DEFINED) {
                    data[i] = MathUtil.round(data[i], dataRounding);
                }
            }
        }
        return data;
    }

    public abstract float[] getData();

    @Override
    public float getDx() {
        return dx;
    }

    @Override
    public float getDy() {
        return dy;
    }

    private void validate(GeoCoordinate northWest, GeoCoordinate southEast, double dx, double dy, Grib1GDSVariables vars) {

        if (northWest.getLon() < vars.getLo1() || northWest.getLat() > vars.getLa2() || southEast.getLon() > vars.getLo2() || southEast.getLat() < vars.getLa1()) {
            throw new APIException(ErrorMessage.OUTSIDE_GRID, String.format("Query is outside data grid, grid corners northWest:(%s), southEast:(%s)",
                    new GeoCoordinate(vars.getLo1(), vars.getLa1()), new GeoCoordinate(vars.getLo2(), vars.getLa2())));
        }

        if (northWest.getLon() > southEast.getLon()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LOT);
        }
        if (northWest.getLat() < southEast.getLat()) {
            throw new APIException(ErrorMessage.INVALID_GRID_LAT);
        }

        int resolutionX = DoubleMath.fuzzyCompare(this.dx, dx, 0.000001);
        int resolutionY = DoubleMath.fuzzyCompare(this.dy, dy, 0.000001);

        if (resolutionX != 0 || resolutionY != 0) {
            throw new IllegalArgumentException(String.format("Currently only request with the same resolution as the data is supported dx:%f dy:%f", this.dx, this.dy));
        }
    }
}
