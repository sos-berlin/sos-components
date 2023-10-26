package com.sos.js7.converter.js1;

import java.nio.file.Path;

import com.sos.js7.converter.commons.JS7ConverterMain;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;

public class JS12JS7ConverterMain extends JS7ConverterMain {

    @Override
    public String getProductAndVersion() {
        return "JS1 2023-10-26 JS7 2.5.6";
    }

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir) throws Exception {
        JS12JS7Converter.convert(input, outputDir, reportDir);
    }

    public static void main(String[] args) {
        new JS12JS7ConverterMain().doMain(JS12JS7Converter.CONFIG, args);
    }

}
