#!/usr/bin/env bash
# requires at least 1GB of memory, or it will not be able to load the 121 GRIB file into memory
java -jar -Xms1400M dmiweather.war