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

import dk.dma.common.dto.GeoCoordinate;

/**
 * interface to access GRIB data, or data derived from GRIB
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
public interface DataProvider {
    /**
     * get the data grid from the northWest to the southEast
     * @param northWest the upper left (first data point)
     * @param southEast the lower right (last data point)
     * @param nx the number of columns
     * @param ny the the number of rows
     * @param lonSpacing the spacing between points on the x-axis
     * @param lonOffset the offset before the first point on the x-axis
     * @param latSpacing the spacing between points on the y-axis
     * @param latOffset the offset before the first point on the y-axis
     * @return a one dimensional array for the data vules in the grid
     */
    float[] getData(GeoCoordinate northWest, GeoCoordinate southEast, int nx, int ny, double lonSpacing, double lonOffset, double latSpacing, double latOffset);

    /**
     * The number of columns modeled by this grid
     * @return the Nx defined in the GRIB file
     */
    int getNx();

    /**
     * The number of rows modeled by the grid
     * @return the Ny defined in the GRIB file
     */
    int getNy();

    double getDx();

    double getDy();

    /**
     * Validate that the request is supported by this provider
     */
    void validate(GeoCoordinate northWest, GeoCoordinate southEast);
}
