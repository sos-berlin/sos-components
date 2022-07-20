package com.sos.js7.converter.js1;

import java.nio.file.Path;

import com.sos.js7.converter.commons.JS7ConverterMain;
import com.sos.js7.converter.js1.output.js7.JS7Converter;

public class JS12JS7ConverterMain extends JS7ConverterMain {

    @Override
    public String getProductAndVersion() {
        return "JS1 0.02 2.4.1 2022-07-19";
    }

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir) throws Exception {
        JS7Converter.convert(input, outputDir, reportDir);
    }

    public static void main(String[] args) {
        new JS12JS7ConverterMain().doMain(JS7Converter.CONFIG, args);
    }

}
