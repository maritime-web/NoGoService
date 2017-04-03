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

import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * @author Klaus Groenbaek
 *         Created 30/03/17.
 */
public class DataDebugger {

    /**
     * Can be used to debug a grid of floats, by drawing using different colors for null and not null
     * @param data one dimensional array
     * @param columnsPerRow a number of columns to break the data grid into
     */
    @SneakyThrows
    public static void showAsImage(float[] data, int columnsPerRow, float nullValue) {
        float grid[][] = new float[data.length/columnsPerRow][columnsPerRow];

        for (int i = 0; i < data.length; i++) {
            float v = data[i];
            int y = i / columnsPerRow;
            int x = i % columnsPerRow;
            grid[y][x] = v;
        }


        BufferedImage img = new BufferedImage(grid[0].length, grid.length, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < grid.length; y++) {
            float[] floats = grid[y];
            for (int x = 0; x < floats.length; x++) {
                if (x < 10 && y < 10) {
                    img.getRaster().setPixel(x,y, new int[] {0, 0, 0});
                    continue;
                }

                Float aFloat = floats[x];
                if (aFloat != nullValue) {
                    img.getRaster().setPixel(x,y, new int[] {19, 61, 198}); // blue - water
                } else {
                    img.getRaster().setPixel(x,y, new int[] {42, 168, 73}); // green -land
                }
            }
        }

        JLabel jLabel = new JLabel(new ImageIcon(img));
        JPanel jPanel = new JPanel();
        jPanel.add(jLabel);
        JFrame r = new JFrame();
        r.add(jPanel);
        r.pack();
        r.show();
    }
}
