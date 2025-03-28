package com.sos.joc.cluster.configuration.globals;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsKiosk extends AConfigurationSection {

    private static enum Views {
        
        dashboard("dashboard", 20), 
        monitorOrderNotification("monitor_order_notification", 15), 
        monitorSystemNotification("monitor_system_notification", 15), 
        historyTasks("history_tasks", 30), 
        historyOrders("history_orders", 0);
        
        private final ConfigurationEntry entry;
        private static final String VIEW_KEY_PREFIX = "view_";
        private static final String VIEW_KEY_SUFFIX = "_duration";
        
        private Views(String key, Integer durationDefault) {
            entry = new ConfigurationEntry(setKey(key), durationDefault + "", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
        }
        
        private ConfigurationEntry value() {
            return this.entry;
        }
        
        private static String setKey(String key) {
            return VIEW_KEY_PREFIX + key + VIEW_KEY_SUFFIX;
        }
    }
    
    //name of the role for kiosk mode
    private ConfigurationEntry kioskRole = new ConfigurationEntry("kiosk_role", "kiosk", GlobalSettingsSectionValueType.STRING);
    @SuppressWarnings("unused")
    private ConfigurationEntry view1 = Views.dashboard.value();
    @SuppressWarnings("unused")
    private ConfigurationEntry view2 = Views.monitorOrderNotification.value();
    @SuppressWarnings("unused")
    private ConfigurationEntry view3 = Views.monitorSystemNotification.value();
    @SuppressWarnings("unused")
    private ConfigurationEntry view4 = Views.historyTasks.value();
    @SuppressWarnings("unused")
    private ConfigurationEntry view5 = Views.historyOrders.value();


    public ConfigurationGlobalsKiosk() {
        AtomicInteger index = new AtomicInteger(0);
        kioskRole.setOrdering(index.getAndIncrement());
        EnumSet.allOf(Views.class).forEach(e -> e.value().setOrdering(index.getAndIncrement()));
    }
    
    public ConfigurationEntry getKioskRole() {
        return kioskRole;
    }
    
    public Map<String, ConfigurationEntry> getKioskViews() {
        return EnumSet.allOf(Views.class).stream().collect(Collectors.toMap(Views::name, Views::value));
    }
    
}
