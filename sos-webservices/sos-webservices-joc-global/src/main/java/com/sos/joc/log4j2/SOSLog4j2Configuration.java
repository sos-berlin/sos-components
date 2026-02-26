package com.sos.joc.log4j2;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.filter.MarkerFilter;
import org.apache.logging.log4j.core.filter.NoMarkerFilter;
import org.w3c.dom.Element;

import com.sos.commons.xml.SOSXML;
import com.sos.joc.classes.WebserviceConstants;

public class SOSLog4j2Configuration extends XmlConfiguration {

    public SOSLog4j2Configuration(LoggerContext loggerContext, ConfigurationSource configSource) {
        super(loggerContext, configSource);
    }

    @Override
    protected void doConfigure() {
        super.doConfigure();
        
        if (!getAppenders().containsKey(NotificationAppender.APPENDER_NAME)) {
            final Filter filter = LevelRangeFilter.createFilter(Level.FATAL, Level.WARN, Filter.Result.ACCEPT, Filter.Result.DENY);
           final Appender appender = NotificationAppender.createAppender(NotificationAppender.APPENDER_NAME, filter);
            appender.start();
            addAppender(appender);
            LoggerConfig rootLogger = getRootLogger();
            if (!rootLogger.getAppenders().containsKey(NotificationAppender.APPENDER_NAME)) {
                rootLogger.addAppender(appender, Level.WARN, filter);
                //getLoggerContext().updateLoggers();
            }
        }
        
        if (!getAppenders().containsKey(RunningLogAppender.APPENDER_NAME)) {
            LoggerConfig rootLogger = getRootLogger();
            final Map<String, String> props = Optional.ofNullable(this.getConfigurationSource().getFile()).map(File::toPath).map(t -> {
                try {
                    return SOSXML.parse(t);
                } catch (Exception e) {
                    return null;
                }
            }).map(doc -> SOSXML.getChildNode(doc.getDocumentElement(), "Properties")).map(SOSXML::getChildElemens).map(List::stream).map(s -> s
                    .collect(Collectors.toMap(e -> e.getAttribute("name"), Element::getTextContent, (e1, e2) -> e2))).orElse(Collections.emptyMap());

            final Filter noMarkerfilter = NoMarkerFilter.newBuilder().setOnMatch(Filter.Result.ACCEPT).setOnMismatch(Filter.Result.DENY).build();
            final Filter markerfilter = MarkerFilter.createFilter(WebserviceConstants.NOT_NOTIFY_LOGGER.getName(), Filter.Result.DENY,
                    Filter.Result.ACCEPT);

            final Filter compositeFilter = CompositeFilter.createFilters(noMarkerfilter, markerfilter);
            final Appender appender = RunningLogAppender.createAppender(RunningLogAppender.APPENDER_NAME, compositeFilter, rootLogger.getLevel(),
                    props);
            appender.start();
            addAppender(appender);
            if (!rootLogger.getAppenders().containsKey(RunningLogAppender.APPENDER_NAME)) {
                rootLogger.addAppender(appender, Level.TRACE, compositeFilter);
            }
        }
    }

}
