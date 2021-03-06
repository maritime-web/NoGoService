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

import dk.dma.common.dto.GeoCoordinate;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Request for weather for a grid.
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Data
@Accessors(chain = true)
public class GridRequest {
    @Valid
    private GridParameters parameters;
    @Valid
    private GeoCoordinate northWest;
    @Valid
    private GeoCoordinate southEast;
    @NotNull
    private Instant time;

    private Integer nx = null;
    private Integer ny = null;

    /**
     * Since you can't do composite field validation, we have to create a dummyProperty so we can validate if the
     * fields are set correctly
     * @return true if both or non of the nx/ny values are set
     */
    @AssertTrue(message =  "You must either set both nx and ny or none of them.")
    public boolean isInValid() {
        return (nx == null && ny == null) || (nx != null && ny != null);
    }

}
