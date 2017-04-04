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
import lombok.Getter;

/**
 * @author Klaus Groenbaek
 *         Created 03/04/17.
 */

@Getter
public class WeatherException extends RuntimeException {

    private static String createMessage(ErrorMessage error, String details) {
        if (details == null) {
            details = "";
        }
        return error.getMessage() + " " + details;
    }

    private final ErrorMessage error;
    private final String details;

    public WeatherException(ErrorMessage error) {
        this(error, null);
    }

    public WeatherException(ErrorMessage error, String details) {
        super(createMessage(error, details));
        this.error = error;
        this.details = details;

    }

    public JSonError toJsonError() {
        return error.toJsonError().setDetails(details);
    }
}
