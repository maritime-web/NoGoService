a tablelookup.lst is included at the same location inside the Grib library.
The grib library has an extension mechanism to add new formats, but it only seems to work with a file reference, which is not ideal inside a webapp,
so instead we shadow the original with our own version. This
Since we only need to read the DMI format defined in denmark_1.tab, this is the only format loaded

The formats are defined using 3 properties, center, subsenter and table version, these are based on certain bytes read
inside the GRIB file. the first entry in tablelookup.lst has -1 for all values, so any GRIB file that does not have an explicit
 match will be treated as that format.