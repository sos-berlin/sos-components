package com.sos.commons.util;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSMissingDataException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class SOSPrivateConf {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPrivateConf.class);
    private String filename = "./config/private/private.conf";
    private Config config = null;

    public SOSPrivateConf(String filename) {
        super();
        this.filename = filename;
    }

    private void init() throws SOSMissingDataException {
        if (config == null) {
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                config = ConfigFactory.parseFile(path.toFile());
            } else {
                String s = String.format("File %s not found", path);
                LOGGER.warn(s);
                throw new SOSMissingDataException(s);
            }
        }
    }

    public String getValue(String key) throws SOSMissingDataException {
        LOGGER.debug("reading key: " + key);
        String value = "";
        try {
            init();
        } catch (SOSMissingDataException e) {
            return "";
        }

        value = config.getString(key);
        return value;
    }

    public String getValueDefaultEmpty(String key) {
        LOGGER.debug("reading key: " + key);
        try {
            init();
            String value = null;
            value = config.getString(key);
            return value;
        } catch (ConfigException | SOSMissingDataException e) {
            return "";
        }
    }

    public String getValue(String objectId, String key) {
        LOGGER.debug("reading key: " + key);
        Config configClass = null;
        String value = "";

        try {
            init();
            configClass = config.getConfig(objectId);
        } catch (ConfigException.Missing e) {
            LOGGER.warn("The configuration item " + objectId + " is missing in private.conf!");
            LOGGER.warn("see https://kb.sos-berlin.com/x/NwgCAQ for further details on how to setup a secure connection");
        } catch (SOSMissingDataException e) {

        }
        if (configClass != null) {
            value = configClass.getString(key);
        }
        return value;

    }

    public String getValueDefaultEmpty(String objectId, String key) {
        LOGGER.debug("reading key: " + key);

        Config configClass = null;
        String value = "";
        try {
            init();
            configClass = config.getConfig(objectId);
        } catch (ConfigException.Missing e) {
            LOGGER.warn("The configuration item " + objectId + " is missing in private.conf!");
            LOGGER.warn("see https://kb.sos-berlin.com/x/NwgCAQ for further details on how to setup a secure connection");
        } catch (SOSMissingDataException e) {

        }
        if (configClass != null) {
            try {
                value = configClass.getString(key);
            } catch (ConfigException.Missing e) {
                value = "";
            }
        }
        return value;
    }

    public String getDecodedValue(String objectId, String key) throws UnsupportedEncodingException {
        String s = getValueDefaultEmpty(objectId, key);
        if (!s.isEmpty()) {
            return new String(Base64.getDecoder().decode(s.getBytes("UTF-8")), "UTF-8");
        } else {
            return null;
        }
    }

    public String getDecodedValue(String key) throws UnsupportedEncodingException {
        String s = getValueDefaultEmpty(key);
        if (!s.isEmpty()) {
            return new String(Base64.getDecoder().decode(s.getBytes("UTF-8")), "UTF-8");
        } else {
            return null;
        }
    }

}
