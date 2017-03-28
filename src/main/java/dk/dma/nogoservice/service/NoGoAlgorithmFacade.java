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

import dk.dma.nogoservice.algo.*;
import dk.dma.nogoservice.dto.NoGoPolygon;
import dk.dma.nogoservice.entity.GeoCoordinateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This class uses one of the defined AreaGrouping algorithms to calculate nogo areas.
 *
 * @author Klaus Groenbaek
 *         Created 28/03/17.
 */
@Component
public class NoGoAlgorithmFacade {

    private final FigureTransformer figureTransformer;

    @Autowired
    public NoGoAlgorithmFacade(FigureTransformer figureTransformer) {
        this.figureTransformer = figureTransformer;
    }

    public <Value extends GeoCoordinateProvider> List<NoGoPolygon> getNoGo(List<List<Value>> grid, NoGoMatcher<Value> matcher) {

        return vectorGrouping(grid, matcher);
        //return lineGrouping(grid, matcher);
    }

    private <Value extends GeoCoordinateProvider> List<NoGoPolygon> vectorGrouping(List<List<Value>> grid, NoGoMatcher<Value> matcher) {
        AreaGroupingAlgorithm<Value> algo = new VectorGraphicAreaGroupingAlgorithm<>(grid, matcher);
        List<Figure> figures = algo.getFigures();
        return figureTransformer.convertToGeoLocations(grid, figures);
    }

    private <Value extends GeoCoordinateProvider> List<NoGoPolygon> lineGrouping(List<List<Value>> grid, NoGoMatcher<Value> matcher) {
        AreaGroupingAlgorithm<Value> algo = new LineBasedAreaGroupingAlgorithm<>(grid, matcher, new DefaultPolygonOptimizer());
        List<Figure> figures = algo.getFigures();
        return figureTransformer.convertToGeoLocations(grid, figures);
    }
}
