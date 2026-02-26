package com.sos.joc.log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.logs.RunningJocLog;

@Plugin(name = RunningLogAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = false)
public final class RunningLogAppender extends AbstractAppender {

    public static final String PLUGIN_NAME = "RunningLog";
    public static final String APPENDER_NAME = RunningLogAppender.class.getSimpleName();
    
    private static final String JOC_CONTEXT_NAME = RunningJocLog.NO_CONTEXT_SOURCE_NAME;
    private Level rootLevel = Level.INFO;
    private Map<String, Level> propLevels = new HashMap<>();

    protected RunningLogAppender(String name, Filter filter, Level rootLevel, Map<String, String> props) {
        super(name, filter, null, true, null);
        this.rootLevel = rootLevel;
        mapProps(props);
    }

    @PluginFactory
    public static RunningLogAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter, Level rootLevel,
            Map<String, String> props) {
        return new RunningLogAppender(name, filter, rootLevel, props);
    }

    @Override
    public void append(final LogEvent event) {
        if (skipAppend(event)) {
            return;
        }
        
        // skip if event level is not corresponding to property such as "JocLogLevel"
        // Nothing to do if rootLevel.isMoreSpecificThan(Level.INFO), i.e. INFO, WARN, ERROR
        if (rootLevel.isLessSpecificThan(Level.DEBUG)) {
            Level evtLevel = event.getLevel();
            if (evtLevel.isLessSpecificThan(Level.DEBUG)) {
                String context = getContext(event.getContextData());
                Level propLevel = propLevels.getOrDefault(context, evtLevel);
                if (!propLevel.isLessSpecificThan(evtLevel)) {
                    return;
                }
            }
        }
        
        RunningJocLog.getInstance().collectEvent(event);
    }
    
    private boolean skipAppend(final LogEvent event) {
        return event.getMarker() != null && WebserviceConstants.NOT_NOTIFY_LOGGER.equals(event.getMarker());
    }
    
    private static String getContext(ReadOnlyStringMap contextData) {
        String source = JOC_CONTEXT_NAME;
        if (contextData.containsKey("clusterService")) {
            source = contextData.getValue("clusterService");
        } else if (contextData.containsKey("context")) {
            source = contextData.getValue("context");
        }
        // possible values: main, cluster, history, dailyplan, cleanup, monitor, lognotification, reports, authentication 
        return source.replaceFirst("^service-", "");
    }
    
    private Level getDebugLevel(String prop) {
        return Optional.ofNullable(prop).map(String::trim).map(String::toUpperCase).map(p -> Level.toLevel(p, Level.INFO)).orElse(Level.INFO);
    }
    
    private void mapProps(Map<String, String> props) {
        propLevels.put(JOC_CONTEXT_NAME, getDebugLevel(props.get("JocLogLevel")));
        propLevels.put("cluster", getDebugLevel(props.get("ServiceClusterLogLevel")));
        propLevels.put("history", getDebugLevel(props.get("ServiceHistoryLogLevel")));
        propLevels.put("dailyplan", getDebugLevel(props.get("ServiceDailyPlanLogLevel")));
        propLevels.put("cleanup", getDebugLevel(props.get("ServiceCleanupLogLevel")));
        propLevels.put("monitor", getDebugLevel(props.get("ServiceMonitorLogLevel")));
        propLevels.put("lognotification", getDebugLevel(props.get("ServiceLogNotificationLogLevel")));
        propLevels.put("reports", getDebugLevel(props.get("ServiceReportsLogLevel")));
        propLevels.put("authentication", getDebugLevel(props.get("AuthLogLevel")));
    }

}