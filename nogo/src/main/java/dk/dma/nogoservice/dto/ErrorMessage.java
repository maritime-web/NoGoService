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
package dk.dma.nogoservice.dto;

import dk.dma.common.dto.JSonError;
import lombok.AllArgsConstructor;

/**
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@AllArgsConstructor
public enum  ErrorMessage {
    UNCAUGHT_EXCEPTION(500, 20000, "Internal server error.")
    ;
    private final int httpCode;
    private final int id;
    private final String message;

    public JSonError toJsonError() {
        return new JSonError().setId(id).setMessage(message);
    }
}
