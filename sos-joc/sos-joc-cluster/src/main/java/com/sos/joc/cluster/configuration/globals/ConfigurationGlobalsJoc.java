package com.sos.joc.cluster.configuration.globals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsJoc extends AConfigurationSection {

    public static enum ShowViewName {
        dashboard, dailyplan, workflows, resources, history, auditlog, configuration, filetransfer, jobstreams;
    }

    private static final List<String> AUDIT_LOG_COMMENTS = Arrays.asList("System maintenance", "Repeat execution", "Business requirement",
            "Restart failed execution", "Re-instantiate stopped object", "Temporary stop", "Change of Controller object",
            "Rerun with parameter changes", "Change of external dependency", "Application deployment and upgrade");

    private ConfigurationEntry forceCommentsForAuditLog = new ConfigurationEntry("force_comments_for_audit_log", "false",
            GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry commentsForAuditLog = new ConfigurationEntry("comments_for_audit_log", String.join(";", AUDIT_LOG_COMMENTS),
            GlobalSettingsSectionValueType.ARRAY);

    private ConfigurationEntry defaultProfileAccount = new ConfigurationEntry("default_profile_account", "root",
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry enableRememberMe = new ConfigurationEntry("enable_remember_me", "true", GlobalSettingsSectionValueType.BOOLEAN);

    private ConfigurationEntry copyPasteSuffix = new ConfigurationEntry("copy_paste_suffix", "copy", GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry copyPastePrefix = new ConfigurationEntry("copy_paste_prefix", "copy", GlobalSettingsSectionValueType.STRING);

    private ConfigurationEntry restoreSuffix = new ConfigurationEntry("restore_suffix", "restored", GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry restorePrefix = new ConfigurationEntry("restore_prefix", "restored", GlobalSettingsSectionValueType.STRING);

    private ConfigurationEntry importSuffix = new ConfigurationEntry("import_suffix", "imported", GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry importPrefix = new ConfigurationEntry("import_prefix", "imported", GlobalSettingsSectionValueType.STRING);
    
    // controller pwds
    private ConfigurationEntry jocPwd = new ConfigurationEntry("controller_connection_joc_password", "JS7-JOC", GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry historyPwd = new ConfigurationEntry("controller_connection_history_password", "JS7-History", GlobalSettingsSectionValueType.STRING);

    // "jobstreams"
    private ConfigurationEntry showViewDashboard = new ConfigurationEntry("show_view_dashboard", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewDailyplan = new ConfigurationEntry("show_view_dailyplan", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewWorkflows = new ConfigurationEntry("show_view_workflows", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewResources = new ConfigurationEntry("show_view_resources", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewHistory = new ConfigurationEntry("show_view_history", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewAuditlog = new ConfigurationEntry("show_view_auditlog", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewConfiguration = new ConfigurationEntry("show_view_configuration", null,
            GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewfiletransfer = new ConfigurationEntry("show_view_filetransfer", null, GlobalSettingsSectionValueType.BOOLEAN);
    // private ConfigurationEntry showViewJobstreams = new ConfigurationEntry("show_view_jobstreams", null, GlobalSettingsSectionValueType.BOOLEAN);

    // private Map<ShowViewName, ConfigurationEntry> showViews = EnumSet.allOf(ShowViewName.class).stream().collect(Collectors.toMap(s -> s,
    // s -> new ConfigurationEntry("show_view_" + s.name(), null, GlobalSettingsSectionValueType.BOOLEAN)));

    public ConfigurationGlobalsJoc() {
        int index = -1;
        forceCommentsForAuditLog.setOrdering(++index);
        commentsForAuditLog.setOrdering(++index);

        defaultProfileAccount.setOrdering(++index);
        enableRememberMe.setOrdering(++index);

        copyPasteSuffix.setOrdering(++index);
        copyPastePrefix.setOrdering(++index);

        restoreSuffix.setOrdering(++index);
        restorePrefix.setOrdering(++index);

        importSuffix.setOrdering(++index);
        importPrefix.setOrdering(++index);

        showViewDashboard.setOrdering(++index);
        showViewDailyplan.setOrdering(++index);
        showViewWorkflows.setOrdering(++index);
        showViewResources.setOrdering(++index);
        showViewHistory.setOrdering(++index);
        showViewAuditlog.setOrdering(++index);
        showViewConfiguration.setOrdering(++index);
        showViewfiletransfer.setOrdering(++index);
        // showViewJobstreams.setOrdering(++index);
        
        jocPwd.setOrdering(++index);
        historyPwd.setOrdering(++index);
    }

    public static List<String> getAuditLogComments() {
        return AUDIT_LOG_COMMENTS;
    }

    public ConfigurationEntry getForceCommentsForAuditLog() {
        return forceCommentsForAuditLog;
    }

    public ConfigurationEntry getCommentsForAuditLog() {
        return commentsForAuditLog;
    }

    public ConfigurationEntry getDefaultProfileAccount() {
        return defaultProfileAccount;
    }

    public ConfigurationEntry getEnableRememberMe() {
        return enableRememberMe;
    }

    public ConfigurationEntry getCopyPasteSuffix() {
        return copyPasteSuffix;
    }

    public ConfigurationEntry getCopyPastePrefix() {
        return copyPastePrefix;
    }

    public ConfigurationEntry getRestoreSuffix() {
        return restoreSuffix;
    }

    public ConfigurationEntry getRestorePrefix() {
        return restorePrefix;
    }

    public ConfigurationEntry getImportSuffix() {
        return importSuffix;
    }

    public ConfigurationEntry getImportPrefix() {
        return importPrefix;
    }

    public Map<ShowViewName, Boolean> getShowViews() {
        Map<ShowViewName, Boolean> showViews = new HashMap<>();
        showViews.put(ShowViewName.auditlog, getBoolean(showViewAuditlog));
        showViews.put(ShowViewName.configuration, getBoolean(showViewConfiguration));
        showViews.put(ShowViewName.dailyplan, getBoolean(showViewDailyplan));
        showViews.put(ShowViewName.dashboard, getBoolean(showViewDashboard));
        showViews.put(ShowViewName.filetransfer, getBoolean(showViewfiletransfer));
        showViews.put(ShowViewName.history, getBoolean(showViewHistory));
        // showViews.put(ShowViewName.jobstreams, getBoolean(showViewJobstreams));
        showViews.put(ShowViewName.resources, getBoolean(showViewResources));
        showViews.put(ShowViewName.workflows, getBoolean(showViewWorkflows));
        return showViews;
    }
    
    public ConfigurationEntry getJOCPwd() {
        return jocPwd;
    }
    
    public ConfigurationEntry getHistoryPwd() {
        return historyPwd;
    }

    private static Boolean getBoolean(ConfigurationEntry c) {
        String s = c.getValue();
        if (s == null || s.isEmpty()) {
            return null;
        } else if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
