package com.sos.js7.converter.commons.config.items;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.report.ConfigReport;

public abstract class AConfigItem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AConfigItem.class);

    protected static final String LIST_VALUE_DELIMITER = ";";

    private final List<String> EXCLUDED_PROPERTIES = Arrays.asList("EXCLUDED_PROPERTIES", "configKey", "propertiesFile", "forcedWindowsNewLine",
            "forcedUnixNewLine");

    private final String configKey;
    private Path configFile;

    protected AConfigItem(String configKey) {
        this.configKey = configKey;
    }

    protected abstract void parse(String key, String val) throws Exception;

    public abstract boolean isEmpty();

    public AConfigItem parse(Properties properties) {
        return parse(properties, null);
    }

    // TODO optimize
    public AConfigItem parse(Properties properties, Path configFile) {
        this.configFile = configFile;
        Set<Object> toRemove = new HashSet<>();
        properties.entrySet().stream().filter(e -> e.getKey().toString().startsWith(configKey + ".")).forEach(e -> {
            String key = e.getKey().toString().trim();
            String val = e.getValue().toString().trim();
            try {
                parse(key.substring(configKey.length() + 1), val);
            } catch (Throwable t) {
                String msg = getErrorMessage(key, val);
                LOGGER.error(msg + "[error]" + t.getMessage());
                ConfigReport.INSTANCE.addErrorRecord(configFile, msg, t);
            } finally {
                toRemove.add(e.getKey());
            }
        });
        properties.keySet().removeAll(toRemove);
        return this;
    }

    private String getErrorMessage(String name, String value) {
        return String.format("[cannot parse config][%s=%s]", name, value);
    }

    public Path getConfigFile() {
        return configFile;
    }

    public Path getValueFile(String val) throws Exception {
        Path f = Paths.get(val);
        if (!f.isAbsolute()) {
            if (configFile != null && configFile.getParent() != null) {
                f = configFile.getParent().resolve(val);
            }
        }
        if (!Files.exists(f)) {
            throw new Exception("[" + configKey + "][" + f.toAbsolutePath() + "]file not found");
        }
        return f;
    }

    @Override
    public String toString() {
        return SOSString.toString(this, EXCLUDED_PROPERTIES, true);
    }

}
