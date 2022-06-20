package com.sos.js7.converter.js1;

import java.nio.file.Path;

import com.sos.js7.converter.commons.JS7ConverterMain;
import com.sos.js7.converter.js1.output.js7.JS7Converter;

public class JS12JS7ConverterMain extends JS7ConverterMain {

    private static final String VERSION = "JS12JS7Converter 0.01";

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir) throws Exception {
        JS7Converter.convert(input, outputDir, reportDir);
    }

    public static void main(String[] args) {
        new JS12JS7ConverterMain().doMain(JS7Converter.CONFIG, args, VERSION);
    }

}
