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

import lombok.AllArgsConstructor;
import ucar.grib.grib1.Grib1Record;
import ucar.grid.GridParameter;

/**
 * Grouping of Grib header data
 * @author Klaus Groenbaek
 *         Created 30/03/17.
 */
@AllArgsConstructor
public class ParameterAndRecord {
    final GridParameter parameter;
    final Grib1Record record;
}
