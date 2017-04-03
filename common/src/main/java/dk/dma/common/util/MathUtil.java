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
package dk.dma.common.util;

import java.math.BigDecimal;

/**
 * @author Klaus Groenbaek
 *         Created 23/03/17.
 */
public class MathUtil {

    public static double round(double value, int decimals) {
        return round(value, decimals, BigDecimal.ROUND_HALF_UP);
    }

    public static double round(double value, int decimals, int roundingMode) {
        return new BigDecimal(value).setScale(decimals, roundingMode).doubleValue();
    }

    public static float round(float value, int decimals) {
        return round(value, decimals, BigDecimal.ROUND_HALF_UP);
    }

    public static float round(float value, int decimals, int roundingMode) {
        return new BigDecimal(value).setScale(decimals, roundingMode).floatValue();
    }


}
