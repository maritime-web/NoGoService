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
package dk.dma.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Some times we have to issue warnings, we want to group the warning message with a messageID
 * Errors are sent in a similar fashion, but warnings are only sent with 200 OK responses.
 * @author Klaus Groenbaek
 *         Created 03/04/17.
 */
@Data
@Accessors(chain = true)
public class JSonWarning {
    private int id;
    private String message;
    private String details;
}
