package com.sos.js7.converter.autosys;

import java.nio.file.Path;

import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.autosys.output.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class Autosys2JS7ConverterMain extends JS7ConverterMain {

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir) throws Exception {
        Autosys2JS7Converter.convert(new XMLJobParser(), input, outputDir, reportDir);
    }

    public static void main(String[] args) {
        new Autosys2JS7ConverterMain().doMain(Autosys2JS7Converter.CONFIG, args);
    }

}
