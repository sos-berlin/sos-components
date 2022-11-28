package com.sos.joc.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;


public class SOSLog4j2Configuration extends XmlConfiguration {

    public SOSLog4j2Configuration(LoggerContext loggerContext, ConfigurationSource configSource) {
        super(loggerContext, configSource);
    }
    
    @Override
    protected void doConfigure() {
        super.doConfigure();
        
        if (!getAppenders().containsKey(NotificationAppender.APPENDER_NAME)) {
            final Filter filter = LevelRangeFilter.createFilter(Level.WARN, Level.FATAL, Filter.Result.ACCEPT, Filter.Result.DENY);
            final Appender appender = new NotificationAppender(NotificationAppender.APPENDER_NAME, filter);
            appender.start();
            addAppender(appender);
            if (!getRootLogger().getAppenders().containsKey(NotificationAppender.APPENDER_NAME)) {
                getRootLogger().addAppender(appender, Level.WARN, null);
            }
        }
    }

}
