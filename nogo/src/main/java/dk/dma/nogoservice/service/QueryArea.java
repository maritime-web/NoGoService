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
package dk.dma.nogoservice.service;

import com.vividsolutions.jts.geom.Geometry;
import dk.dma.nogoservice.dto.AreaInfo;
import dk.dma.nogoservice.dto.NoGoRequest;

/**
 * @author Klaus Groenbaek
 *         Created 13/03/17.
 */
public interface QueryArea {

    String getName();
    AreaMatch matches(Geometry area);
    CalculatedNoGoArea getNogoAreas(NoGoRequest request);
    AreaInfo getInfo();
}
