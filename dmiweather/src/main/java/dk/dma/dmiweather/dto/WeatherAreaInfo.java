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

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
@Data
@Accessors(chain = true)
public class WeatherAreaInfo {
    private String name;
    private String wkt;
    private double dx;
    private double dy;
    private int nx;
    private int ny;
    private Instant start;
    private Instant end;
}
