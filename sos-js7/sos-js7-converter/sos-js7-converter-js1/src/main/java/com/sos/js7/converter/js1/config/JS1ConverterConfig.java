package com.sos.js7.converter.js1.config;

import java.nio.file.Path;
import java.util.Properties;

import com.sos.js7.converter.commons.config.JS7ConverterConfig;
import com.sos.js7.converter.js1.config.items.JS1JobStreamConfig;

public class JS1ConverterConfig extends JS7ConverterConfig {

    private final JS1 js1;

    public JS1ConverterConfig() {
        super();
        js1 = new JS1();
    }

    public Properties parse(Path propertiesFile) throws Exception {
        Properties p = super.parse(propertiesFile);
        js1.getJobStreamConfig().parse(p);
        return p;
    }

    public JS1 getJS1() {
        return js1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (!js1.getJobStreamConfig().isEmpty()) {
            sb.append(",").append(js1.getJobStreamConfig().toString());
        }
        return sb.toString();
    }

    public class JS1 {

        private final JS1JobStreamConfig jobStreamConfig;

        private JS1() {
            jobStreamConfig = new JS1JobStreamConfig();
        }

        public JS1JobStreamConfig getJobStreamConfig() {
            return jobStreamConfig;
        }
    }

}
