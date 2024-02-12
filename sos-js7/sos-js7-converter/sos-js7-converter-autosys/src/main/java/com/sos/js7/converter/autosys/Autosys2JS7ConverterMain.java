package com.sos.js7.converter.autosys;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.autosys.input.AFileParser;
import com.sos.js7.converter.autosys.input.JILJobParser;
import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class Autosys2JS7ConverterMain extends JS7ConverterMain {

    @Override
    public String getProductAndVersion() {
        return "Autosys 2024-02-12 JS7 2.5.9";
    }

    @Override
    public void doConvert(Path input, Path outputDir, Path reportDir) throws Exception {
        boolean xmlParser = false;
        if (Files.isDirectory(input)) {
            List<Path> l = SOSPath.getFileList(input, ".*\\.xml$", java.util.regex.Pattern.CASE_INSENSITIVE);
            xmlParser = l != null && l.size() > 0;
        } else {
            xmlParser = input.getFileName().toString().toLowerCase().endsWith("xml");
        }
        AFileParser parser = xmlParser ? new XMLJobParser(Autosys2JS7Converter.CONFIG, reportDir) : new JILJobParser(Autosys2JS7Converter.CONFIG,
                reportDir);
        Autosys2JS7Converter.convert(parser, input, outputDir, reportDir);
    }

    public static void main(String[] args) {
        new Autosys2JS7ConverterMain().doMain(Autosys2JS7Converter.CONFIG, args);
    }

}
