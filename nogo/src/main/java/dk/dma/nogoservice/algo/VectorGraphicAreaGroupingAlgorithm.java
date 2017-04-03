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
package dk.dma.nogoservice.algo;

import jankovicsandras.imagetracer.ImageTracer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * An area grouping algorithm based on the translation of a bitmap to SVG images. It uses the internal algorithms from an open source library
 * <p>
 * The figures produced by this algorithm is not exact, because it uses interpolation, which may result in loss of information
 *
 * @author Klaus Groenbaek
 *         Created 28/03/17.
 */
@Slf4j
public class VectorGraphicAreaGroupingAlgorithm<Value> implements AreaGroupingAlgorithm<Value> {

    private final List<List<Value>> grid;
    private final NoGoMatcher<Value> matcher;

    public VectorGraphicAreaGroupingAlgorithm(List<List<Value>> grid, NoGoMatcher<Value> matcher) {
        this.grid = grid;
        this.matcher = matcher;
    }

    @Override
    public List<Figure> getFigures() {

        int width = grid.get(0).size();
        int height = grid.size();

        int[][] arr = new int[height + 2][width + 2];
        // do the -1 padding around the grid
        for (int j = 0; j < (height + 2); j++) {
            arr[j][0] = -1;
            arr[j][width + 1] = -1;
        }
        for (int i = 0; i < (width + 2); i++) {
            arr[0][i] = -1;
            arr[height + 1][i] = -1;
        }

        // fill the array. NoGo values use the first color in the pallet
        for (int y = 0; y < grid.size(); y++) {
            List<Value> southKattegats = grid.get(y);
            for (int x = 0; x < southKattegats.size(); x++) {
                Value southKattegat = southKattegats.get(x);
                if (matcher.matches(southKattegat)) {
                    arr[y + 1][x + 1] = 0; // first color in palette
                } else {
                    arr[y + 1][x + 1] = 1; // second color in palette
                }
            }
        }

        HashMap<String, Float> options = getOptions();

        // create the structure the Image api works on
        ImageTracer.IndexedImage myimage = new ImageTracer.IndexedImage(arr, getPalette());

        int[][][] rawlayers = ImageTracer.layering(myimage);
        // 3. Batch pathscan
        ArrayList<ArrayList<ArrayList<Integer[]>>> bps = ImageTracer.batchpathscan(rawlayers, 0);
        // 4. Batch interpollation
        ArrayList<ArrayList<ArrayList<Double[]>>> binternodes = new ArrayList<>();
        for (int k = 0; k < bps.size(); k++) {
            binternodes.add(ImageTracer.internodes(bps.get(k)));
        }

        // 5. Batch tracing
        myimage.layers = ImageTracer.batchtracelayers(binternodes, options.get("ltres"), options.get("qtres"));

        if (log.isDebugEnabled()) {
            // remember the image looks flipped over the x-axis, because the grid has y=0 as the first row and y=1 as the next (down the array)
            log.debug("svg image " + ImageTracer.getsvgstring(myimage, options));
        }

        ArrayList<ArrayList<Double[]>> redLayer = myimage.layers.get(0);

        // coordinates are doubles because of interpolation, here we need to convert them back to a grid
        // this process may cause a small inaccuracy since we round all numbers down
        List<Figure> list = new ArrayList<>();
        for (ArrayList<Double[]> figure : redLayer) {
            List<Point> points = new ArrayList<>();
            for (Double[] doubles : figure) {
                int x =  Math.min(doubles[1].intValue(), width -1);
                int y = Math.min(doubles[2].intValue(), height -1);
                points.add(new Point(x, y));
            }
            list.add(new Polygon(points));
        }
        return list;
    }

    private HashMap<String, Float> getOptions() {
        HashMap<String,Float> options = new HashMap<>();
        options.put( "ltres", 0.5f ); // Linear error treshold
        options.put( "qtres", 0.5f ); // Quadratic spline error treshold
        options.put("roundcoords",1f);
        options.put("colorsampling",0f);
        return options;
    }

    /**
     * A color pallet where the first color is read and the second is black.
     * @return
     */
    private byte[][] getPalette() {
        byte[][] palette = new byte[2][4];
        // black color
        palette[0][0] = -128;    // R
        palette[0][1] = -128;   // G
        palette[0][2] = -128;   // B
        palette[0][3] = 127;    // A

        // blue color
        palette[1][0] = -128;    // R
        palette[1][1] = -128;   // G
        palette[1][2] = 127;   // B
        palette[1][3] = 127;    // A
        return palette;
    }


}
