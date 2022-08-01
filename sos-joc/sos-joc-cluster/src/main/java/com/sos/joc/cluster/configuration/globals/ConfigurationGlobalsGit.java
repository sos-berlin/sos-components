package com.sos.joc.cluster.configuration.globals;

import java.util.Arrays;
import java.util.List;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsGit extends AConfigurationSection {

    private static final List<String> VALUES = Arrays.asList("local", "rollout");
    private static final String DEFAULT_VALUE_ROLLOUT = VALUES.get(1);
    private static final String DEFAULT_VALUE_LOCAL = VALUES.get(0);

    // default: remote
    private ConfigurationEntry holdWorkflows = new ConfigurationEntry("git_hold_workflows", DEFAULT_VALUE_ROLLOUT, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdLocks = new ConfigurationEntry("git_hold_resource_locks", DEFAULT_VALUE_ROLLOUT, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdFileOrderSources = new ConfigurationEntry("git_hold_file_order_sources", DEFAULT_VALUE_ROLLOUT, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdNoticeBoards = new ConfigurationEntry("git_hold_notice_boards", DEFAULT_VALUE_ROLLOUT, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdScriptIncludes = new ConfigurationEntry("git_hold_script_includes", DEFAULT_VALUE_ROLLOUT, VALUES,
            GlobalSettingsSectionValueType.LIST);

    // default: local
    private ConfigurationEntry holdJobResources = new ConfigurationEntry("git_hold_job_resources", DEFAULT_VALUE_LOCAL, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdCalendars = new ConfigurationEntry("git_hold_calendars", DEFAULT_VALUE_LOCAL, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdSchedules = new ConfigurationEntry("git_hold_schedules", DEFAULT_VALUE_LOCAL, VALUES,
            GlobalSettingsSectionValueType.LIST);
    private ConfigurationEntry holdJobs = new ConfigurationEntry("git_hold_job_templates", DEFAULT_VALUE_LOCAL, VALUES,
            GlobalSettingsSectionValueType.LIST);

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
        holdJobs.setOrdering(++index);
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
    
    public ConfigurationEntry getHoldJobs() {
        return holdJobs;
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