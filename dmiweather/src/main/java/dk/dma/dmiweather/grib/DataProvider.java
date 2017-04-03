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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ucar.grib.grib1.Grib1GDSVariables;

/**
 * Class for modeling coordinate base extraction agains a single dimensional float array.
 * @author Klaus Groenbaek
 *         Created 30/03/17.
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")   // we don't want to copy the array
public class DataProvider {

    private final ParameterAndRecord parameterAndRecord;
    private final float[] dataGrid;
    private final float dx, dy;

    public DataProvider(ParameterAndRecord parameterAndRecord, float[] dataGrid) {
        this.parameterAndRecord = parameterAndRecord;
        this.dataGrid = dataGrid;

        // we have to recalculate dx, dy because the GRIB format is not precise enough (it only has two bytes, and then divides with 1000)
        Grib1GDSVariables vars = parameterAndRecord.record.getGDS().getGdsVars();
        dy = (vars.getLa2() - vars.getLa1()) / (vars.getNy() -1);
        dx = (vars.getLo2() - vars.getLo1()) / (vars.getNx() -1);
    }


    public float[] getData(GeoCoordinate northWest, GeoCoordinate southEast, float dx, float dy) {
        Grib1GDSVariables vars = parameterAndRecord.record.getGDS().getGdsVars();
        validate(northWest, southEast, dx, dy, vars);

        int startY = (int) Math.round((southEast.getLat() - vars.getLa1()) / this.dy);
        int startX = (int) Math.round((northWest.getLon() - vars.getLo1()) / this.dx);

        int deltaX = (int) Math.round((southEast.getLon() - northWest.getLon()) / this.dx) +1;
        int deltaY = (int) Math.round((northWest.getLat() - southEast.getLat()) / this.dy) +1;

        float[] grid = new float[(deltaY) *(deltaX)];

        for (int row = 0; row < deltaY; row++) {
            System.arraycopy(dataGrid, (startY + row) * vars.getNx() + startX, grid, row*deltaX, deltaX);
        }

        return grid;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    private void validate(GeoCoordinate northWest, GeoCoordinate southEast, double dx, double dy, Grib1GDSVariables vars) {

        if (northWest.getLon() < vars.getLo1() || northWest.getLat() > vars.getLa2() || southEast.getLon() > vars.getLo2() || southEast.getLat() < vars.getLa1()) {
            throw new IllegalArgumentException(String.format("Query is outside data grid, grid corners northWest:%s, southEast:%s",
                    new GeoCoordinate(vars.getLo1(), vars.getLa1()), new GeoCoordinate(vars.getLo2(), vars.getLa2())));
        }

        int resolutionX = DoubleMath.fuzzyCompare(this.dx, dx, 0.000001);
        int resolutionY = DoubleMath.fuzzyCompare(this.dy, dy, 0.000001);

        if (resolutionX != 0 || resolutionY != 0) {
            throw new IllegalArgumentException(String.format("Currently only request with the same resolution as the data is supported dx:%f dy:%f", this.dx, this.dy));
        }
    }
}
