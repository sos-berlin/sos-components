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

import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationLogEvent;


@Plugin(name = NotificationAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = false)
public final class NotificationAppender extends AbstractAppender {
    
    public static final String PLUGIN_NAME = "Notification";
    public static final String APPENDER_NAME = NotificationAppender.class.getSimpleName();

    protected  NotificationAppender() {
        super(APPENDER_NAME, null, null, true, null);
    }
    
    protected  NotificationAppender(Filter filter) {
        super(APPENDER_NAME, filter, null, true, null);
    }
    
    protected  NotificationAppender(String name, Filter filter) {
        super(name, filter, null, true, null);
    }
    
    @PluginFactory
    public static NotificationAppender createAppender(
      @PluginAttribute("name") String name, 
      @PluginElement("Filter") Filter filter) {
        return new NotificationAppender(name, filter);
    }


    @Override
    public void append(final LogEvent event) {
        if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
            NotificationLogEvent e = logEventToNotification(event);
            EventBus.getInstance().post(e);
            //System.out.println(e);
        }
    }
    
    private NotificationLogEvent logEventToNotification(final LogEvent event) {
        String category = event.getContextData().isEmpty() ? "JOC" : "SYSTEM";
        return new NotificationLogEvent(event.getLevel().name(), category, event.getInstant().getEpochMillisecond(), event.getLoggerName(), event
                .getMessage().getFormattedMessage(), event.getThrown());
    }
}