package com.sos.joc.log4j2;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;

/**
 * 
 * delete comment at @Plugin annotation to activate the Notification appender
 *
 */
@Plugin(name = SOSLog4j2ConfigurationFactory.PLUGIN_NAME, category = ConfigurationFactory.CATEGORY)
@Order(10)
public class SOSLog4j2ConfigurationFactory extends ConfigurationFactory {
    
    public static final String PLUGIN_NAME = "SOSLog4j2ConfigurationFactory";

    /**
     * Valid file extensions for XML files.
     */
    public static final String[] SUFFIXES = new String[] {".xml"};
    
    /**
     * Returns the file suffixes for XML files.
     * @return An array of File extensions.
     */
    @Override
    public String[] getSupportedTypes() {
        return SUFFIXES;
    }

    /**
     * Return the Configuration.
     * @param loggerContext
     * @param source The InputSource.
     * @return The Configuration.
     */
    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        return new SOSLog4j2Configuration(loggerContext, source);
    }

}
