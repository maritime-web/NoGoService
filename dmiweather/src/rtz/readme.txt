Instead of generating the JAXB classes from the RTZ schema during compilation, we have check in the classes.
This gives a smoother development process for new developers, since the schema does not change often.

If you need to generate the classes again you can xjc which is found in the bin folder of you JDK installation

current directory should be this folder, then copy the generated classes into the main source hierarchy
xjc -d generated -p dk.dma.dmiweather.generated RTZ_1_0.xsd
