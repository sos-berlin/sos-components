package com.sos.joc.cluster.configuration.globals;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsGit extends AConfigurationSection {
    // default: remote 
    private ConfigurationEntry holdWorkflows = new ConfigurationEntry("git_hold_workflows", "remote",
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry holdLocks = new ConfigurationEntry("git_hold_resource_locks", "remote", 
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry holdFileOrderSources = new ConfigurationEntry("git_hold_file_order_sources", "remote", 
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry holdNoticeBoards = new ConfigurationEntry("git_hold_notice_boards", "remote", 
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry holdScriptIncludes = new ConfigurationEntry("git_hold_script_includes", "remote", 
            GlobalSettingsSectionValueType.STRING);
    // default: local
    private ConfigurationEntry holdJobResources = new ConfigurationEntry("git_hold_job_resources", "local", 
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry holdCalendars = new ConfigurationEntry("git_hold_calendars", "local", 
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry holdSchedules = new ConfigurationEntry("git_hold_schedules", "local", 
            GlobalSettingsSectionValueType.STRING);

    public ConfigurationGlobalsGit() {
        int index = -1;
        holdWorkflows.setOrdering(++index);
        holdLocks.setOrdering(++index);
        holdFileOrderSources.setOrdering(++index);
        holdNoticeBoards.setOrdering(++index);
        holdScriptIncludes.setOrdering(++index);
        holdJobResources.setOrdering(++index);
        holdCalendars.setOrdering(++index);
        holdSchedules.setOrdering(++index);
    }
    
    public ConfigurationEntry getHoldWorkflows() {
        return holdWorkflows;
    }
    
    public ConfigurationEntry getHoldLocks() {
        return holdLocks;
    }
    
    public ConfigurationEntry getHoldFileOrderSources() {
        return holdFileOrderSources;
    }
    
    public ConfigurationEntry getHoldNoticeBoards() {
        return holdNoticeBoards;
    }
    
    public ConfigurationEntry getHoldScriptIncludes() {
        return holdScriptIncludes;
    }
    
    public ConfigurationEntry getHoldJobResources() {
        return holdJobResources;
    }
    
    public ConfigurationEntry getHoldCalendars() {
        return holdCalendars;
    }

    public ConfigurationEntry getHoldSchedules() {
        return holdSchedules;
    }

}