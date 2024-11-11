package com.sos.js7.converter.autosys;

import java.nio.file.Path;

import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class Autosys2JS7ConverterMain extends JS7ConverterMain {

    @Override
    public String getProductAndVersion() {
        return "Autosys 2024-11-11 JS7 2.7.2";
    }

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir) throws Exception {
        Autosys2JS7Converter.convert(input, outputDir, reportDir);
    }

    public static void main(String[] args) {
        new Autosys2JS7ConverterMain().doMain(Autosys2JS7Converter.CONFIG, args);
    }

}
