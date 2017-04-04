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
     * @param dx the distance between longitude sample points
     * @param dy the distance between latitude sample points
     * @return a one dimensional array for the data vules in the grid
     */
    float[] getData(GeoCoordinate northWest, GeoCoordinate southEast, float dx, float dy);

    /**
     * The native longitude resolution of the data provided
     * @return x resolution
     */
    float getDx();

    /**
     * The native latitude resolution of data provided
     * @return y resolution
     */
    float getDy();
}
