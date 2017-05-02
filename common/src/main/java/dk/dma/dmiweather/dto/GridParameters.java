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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GridParameters {

    /**
     * Does not check all values, since it know which are set together
     * @param parameterTypes collection of parameter types
     * @return a parameters instance withthe relevant boolean flags set
     */
    public static GridParameters parametersFromTypes(Collection<GridParameterType> parameterTypes) {
        GridParameters gridParameters = new GridParameters();

        for (GridParameterType parameterType : parameterTypes) {
            if (parameterType == GridParameterType.SeaLevel) {
                gridParameters.setSeaLevel(true);
            }
            if (parameterType == GridParameterType.CurrentDirection) {
                gridParameters.setCurrent(true);
            }
            if (parameterType == GridParameterType.WindDirection) {
                gridParameters.setWind(true);
            }
            if (parameterType == GridParameterType.Density) {
                gridParameters.setDensity(true);
            }
            if (parameterType == GridParameterType.WavePeriod ) {
              gridParameters.setWave(true);
            }
        }
        return gridParameters;
    }

    private Boolean wind;
    private Boolean wave;
    private Boolean current;
    private Boolean density;
    private Boolean seaLevel;

    @JsonIgnore
    public Set<GridParameterType> getParamTypes() {
        Set<GridParameterType> set = new HashSet<>();
        if (isTrue(wind)) {
            set.add(GridParameterType.WindDirection);
            set.add(GridParameterType.WindSpeed);
        }
        if (isTrue(wave)) {
            set.add(GridParameterType.WaveHeight);
            set.add(GridParameterType.WaveDirection);
            set.add(GridParameterType.WavePeriod);
        }
        if (isTrue(current)) {
            set.add(GridParameterType.CurrentDirection);
            set.add(GridParameterType.CurrentSpeed);
        }
        if (isTrue(density)) {
            set.add(GridParameterType.Density);
        }
        if (isTrue(seaLevel)) {
            set.add(GridParameterType.SeaLevel);
        }
        return set;
    }

    @JsonIgnore
    private boolean isTrue(Boolean wind) {
        return Boolean.TRUE.equals(wind);
    }


}
