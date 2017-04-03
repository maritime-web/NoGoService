a tablelookup.lst is included at the same location inside the Grib library.
The grib library has an extension mechanism to add new formats, but it only seems to work with a file reference, which is not ideal inside a webapp,
so instead we shadow the original with our own version. This
Since we only need to read the DMI format defined in denmark_1.tab, this is the only format loaded