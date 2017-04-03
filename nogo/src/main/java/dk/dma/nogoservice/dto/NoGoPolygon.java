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

import dk.dma.common.dto.GeoCoordinate;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Klaus Groenbaek
 *         Created 12/03/17.
 */
@Data
@Accessors(chain = true)
public class NoGoPolygon {
    private List<GeoCoordinate> points;

    public String toWKT() {
        return "POLYGON ((" + getPoints().stream().map(GeoCoordinate::toWKT).collect(Collectors.joining(", ")) + "))";
    }
}
