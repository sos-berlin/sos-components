package com.sos.js7.converter.autosys.config;

import java.nio.file.Path;
import java.util.Properties;

import com.sos.js7.converter.autosys.config.items.AutosysInputConfig;
import com.sos.js7.converter.autosys.config.items.AutosysOutputConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig;

public class AutosysConverterConfig extends JS7ConverterConfig {

    public static final int DIRECTORY_PARSER_MAX_DEPTH = 1;// Integer.MAX_VALUE

    private final Autosys autosys;

    public AutosysConverterConfig() {
        super();
        autosys = new Autosys();
    }

    public Properties parse(Path propertiesFile) throws Exception {
        Properties p = super.parse(propertiesFile);
        autosys.getOutputConfig().parse(p);
        autosys.getInputConfig().parse(p);
        return p;
    }

    public Autosys getAutosys() {
        return autosys;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (!autosys.getOutputConfig().isEmpty()) {
            sb.append(",").append(autosys.getOutputConfig().toString());
        }
        if (!autosys.getInputConfig().isEmpty()) {
            sb.append(",").append(autosys.getInputConfig().toString());
        }
        return sb.toString();
    }

    public class Autosys {

        private final AutosysOutputConfig outputConfig;
        private final AutosysInputConfig inputConfig;

        private Autosys() {
            outputConfig = new AutosysOutputConfig();
            inputConfig = new AutosysInputConfig();
        }

        public AutosysOutputConfig getOutputConfig() {
            return outputConfig;
        }

        public AutosysInputConfig getInputConfig() {
            return inputConfig;
        }

    }

}
