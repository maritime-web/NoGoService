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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Data
@Accessors(chain = true)
public class GridParameters {
    private Boolean wind;
    private Boolean wave;
    private Boolean current;
    private Boolean density;
    private Boolean sealevel;

    public Boolean getWind() {
        return wind != null && wind;
    }

    public Boolean getWave() {
        return wave != null && wave;
    }

    public Boolean getCurrent() {
        return current != null && current;
    }

    public Boolean getDensity() {
        return density != null && density;
    }

    public Boolean getSealevel() {
        return sealevel != null && sealevel;
    }

    public List<GridParameterType> getParamTypes() {
        List<GridParameterType> list = new ArrayList<>();
        if (getWind()) {
            list.add(GridParameterType.WindDirection);
            list.add(GridParameterType.WindSpeed);
        }
        if (getWave()) {
            //list.add(GridParameterType.Wave);
        }
        if (getCurrent()) {
            list.add(GridParameterType.CurrentDirection);
            list.add(GridParameterType.CurrentSpeed);
        }
        if (getDensity()) {
            //list.add(GridParameterType.Density);
        }
        if (getSealevel()) {
            list.add(GridParameterType.SeaLevel);
        }
        return list;
    }
}
