package com.sos.js7.converter.autosys.config;

import java.nio.file.Path;
import java.util.Properties;

import com.sos.js7.converter.autosys.config.items.AutosysGenerateConfig;
import com.sos.js7.converter.autosys.config.items.AutosysInputConfig;
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
        autosys.getGenerateConfig().parse(p);
        autosys.getInputConfig().parse(p);
        return p;
    }

    public Autosys getAutosys() {
        return autosys;
    }

    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (!autosys.getGenerateConfig().isEmpty()) {
            sb.append(",").append(autosys.getGenerateConfig().toString());
        }
        if (!autosys.getInputConfig().isEmpty()) {
            sb.append(",").append(autosys.getInputConfig().toString());
        }
        return sb.toString();
    }

    public class Autosys {

        private final AutosysGenerateConfig generateConfig;
        private final AutosysInputConfig inputConfig;

        private Autosys() {
            generateConfig = new AutosysGenerateConfig();
            inputConfig = new AutosysInputConfig();
        }

        public AutosysGenerateConfig getGenerateConfig() {
            return generateConfig;
        }

        public AutosysInputConfig getInputConfig() {
            return inputConfig;
        }

    }

}
