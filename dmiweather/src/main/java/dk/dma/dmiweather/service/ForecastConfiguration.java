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
package dk.dma.dmiweather.service;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Configuration for a logic group of GRIB files that contains forecasts for the same area
 * @author Klaus Groenbaek
 *         Created 01/05/17.
 */
@Getter
public enum ForecastConfiguration {

    DANISH_METOCEAN_SHELF("/mhri/EfficientSea/metocean_shelf","DMI_metocean_DK\\.(\\d{10})\\.grb"),
    BALTIC_METOCEAN_SHELF("/mhri/EfficientSea/metocean_shelf","DMI_metocean_Baltic\\.(\\d{10})\\.grb"),
    NORTH_SEA_METOCEAN_SHELF("/mhri/EfficientSea/metocean_shelf","DMI_metocean_NorthSea\\.(\\d{10})\\.grb"),
    DANISH_WAVE("/mhri/EfficientSea/waves","DMI_waves_DK\\.(\\d{10})\\.grb"),
    ATL_WAVE("/mhri/EfficientSea/waves","DMI_waves_Atl\\.(\\d{10})\\.grb"),
    NWATL_WAVE("/mhri/EfficientSea/waves","DMI_waves_NWAtl\\.(\\d{10})\\.grb");

    private final String folder;
    private final Pattern filePattern;  // also identifies the dateTime of the forcast

    ForecastConfiguration( String folder, String filePattern) {
        this.folder = folder;
        this.filePattern = Pattern.compile(filePattern);
    }
}
