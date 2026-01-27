package com.sos.js7.converter.autosys;

import java.nio.file.Path;

import com.sos.commons.util.SOSVersionInfo;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class Autosys2JS7ConverterMain extends JS7ConverterMain {

    @Override
    public String getProductAndVersion() {
        return "Autosys " + SOSVersionInfo.VERSION_BUILD_DATE;
    }

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir, Path references) throws Exception {
        Autosys2JS7Converter.convert(input, outputDir, reportDir, references);
    }

    public static void main(String[] args) {
        Autosys2JS7Converter.CONFIG.getGenerateConfig().withCyclicOrders(false);
        new Autosys2JS7ConverterMain().doMain(Autosys2JS7Converter.CONFIG, args);
    }

}
