package com.sos.joc.log4j2;

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

import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;

@Plugin(name = NotificationAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = false)
public final class NotificationAppender extends AbstractAppender {

    public static final String PLUGIN_NAME = "Notification";
    public static final String APPENDER_NAME = NotificationAppender.class.getSimpleName();

    public static boolean doNotify = false;

    private static final String NOT_NOTIFY_LOGGER = WebserviceConstants.NOT_NOTIFY_LOGGER.getName();

    protected NotificationAppender(String name, Filter filter) {
        super(name, filter, null, true, null);
    }

    @PluginFactory
    public static NotificationAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter) {
        return new NotificationAppender(name, filter);
    }

    @Override
    public void append(final LogEvent event) {
        if (doNotify) {
            if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
                if (event.getMarker() == null) {
                    post(event);
                } else if (!NOT_NOTIFY_LOGGER.equals(event.getMarker().getName())) {
                    post(event);
                }
            }
        }
    }

    private void post(final LogEvent event) {
        NotificationLogEvent e = logEventToNotification(event);
        EventBus.getInstance().post(e);
        // System.out.println(e);
    }

    private NotificationLogEvent logEventToNotification(final LogEvent event) {
        String markerName = event.getMarker() != null ? event.getMarker().getName() : null;
        return new NotificationLogEvent(event.getLevel().name(), event.getInstant().getEpochMillisecond(), event.getLoggerName(), markerName, event
                .getMessage().getFormattedMessage(), event.getThrown());
    }
}