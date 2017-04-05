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
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Data
@Getter
@Accessors(chain = true)
public class GridParameters {
    private boolean wind;
    private boolean wave;
    private boolean current;
    private boolean density;
    private boolean seaLevel;

    public List<GridParameterType> getParamTypes() {
        List<GridParameterType> list = new ArrayList<>();
        if (isWind()) {
            list.add(GridParameterType.WindDirection);
            list.add(GridParameterType.WindSpeed);
        }
        if (isWave()) {
            //list.add(GridParameterType.Wave);
        }
        if (isCurrent()) {
            list.add(GridParameterType.CurrentDirection);
            list.add(GridParameterType.CurrentSpeed);
        }
        if (isDensity()) {
            list.add(GridParameterType.Density);
        }
        if (isSeaLevel()) {
            list.add(GridParameterType.SeaLevel);
        }
        return list;
    }


}
