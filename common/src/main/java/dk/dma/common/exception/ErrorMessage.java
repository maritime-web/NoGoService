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
package dk.dma.common.exception;

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
    NO_PARAMETERS(400, 1003, "You must specify at least one parameter (wind, wave, seaLevel, current, density) on the URL"),
    INVALID_RTZ(400, 1004, "The provided RTZ is not valid."),
    INVALID_SCALING(400, 1005, "Can't scale one axis up and another axis down"),

    // general user input errors
    REQUEST_NOT_PARSED(400, 2005, "Could not parse request."),

    // general Grid errors
    INVALID_GRID_LAT(400, 5001, "The south coordinate is larger than the north coordinate."),
    INVALID_GRID_LOT(400, 5002, "The west coordinate is larger than the east coordinate."),
    OUTSIDE_GRID(404, 5003, "The requested coordinates are outside the supported grid."),


    UNCAUGHT_EXCEPTION(500, 10000, "Internal server error.")
    ;
    private final int httpCode;
    private final int id;
    private final String message;

    public JSonError toJsonError() {
        return new JSonError().setId(id).setMessage(message);
    }

}
