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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * A DTO that identifies a REST resource where slice information will be available
 * @author Klaus Groenbaek
 *         Created 04/04/17.
 */
@Data
@Accessors(chain = true)
public class SliceResource {
    private String resourceURL;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.000Z", timezone = "UTC")
    private Instant time;



}
