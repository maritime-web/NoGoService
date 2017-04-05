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
package dk.dma.dmiweather.dto;

import dk.dma.common.dto.JSonError;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Klaus Groenbaek
 *         Created 03/04/17.
 */
@AllArgsConstructor
@Getter
public enum ErrorMessage {

    FTP_PROBLEM(404, 1000, "Unable to load upstream data from weather provider."),
    DATA_NOT_LOADED(404, 1001, "Data is not available yet, please try again later."),
    OUT_OF_DATE_RANGE(404, 1002, "No data in the requested range."),
    INVALID_GRID_LAT(400, 1003, "The south coordinate is larger than the north coordinate."),
    INVALID_GRID_LOT(400, 1004, "The west coordinate is larger than the east coordinate."),
    NO_PARAMETERS(400, 1005, "You must specify at least one parameter (wind, wave, seaLevel, current, density) on the URL"),
    OUTSIDE_GRID(404, 1006, "The requested coordinates are outside the supported grid."),
    INVALID_RTZ(400, 1007, "The provided RTZ is not valid."),
    UNCAUGHT_EXCEPTION(500, 2000, "Internal server error.")
    ;
    private final int httpCode;
    private final int id;
    private final String message;

    public JSonError toJsonError() {
        return new JSonError().setId(id).setMessage(message);
    }

}
