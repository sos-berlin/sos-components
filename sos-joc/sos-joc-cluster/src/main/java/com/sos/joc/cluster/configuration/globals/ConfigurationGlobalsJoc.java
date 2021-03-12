package com.sos.joc.cluster.configuration.globals;

import java.util.Arrays;
import java.util.List;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsJoc extends AConfigurationSection {

    private static final List<String> AUDIT_LOG_COMMENTS = Arrays.asList("System maintenance", "Repeat execution", "Business requirement",
            "Restart failed execution", "Re-instantiate stopped object", "Temporary stop", "Change of JobScheduler object",
            "Rerun with parameter changes", "Change of external dependency", "Application deployment and upgrade");

    private ConfigurationEntry forceCommentsForAuditLog = new ConfigurationEntry("force_comments_for_audit_log", "false",
            GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry commentsForAuditLog = new ConfigurationEntry("comments_for_audit_log", String.join(";", AUDIT_LOG_COMMENTS),
            GlobalSettingsSectionValueType.ARRAY);

    private ConfigurationEntry defaultProfileAccount = new ConfigurationEntry("default_profile_account", "root",
            GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry enableRememberMe = new ConfigurationEntry("enable_remember_me", "true", GlobalSettingsSectionValueType.BOOLEAN);

    private ConfigurationEntry copyPasteSuffix = new ConfigurationEntry("copy_paste_suffix", null, GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry copyPastePrefix = new ConfigurationEntry("copy_paste_prefix", null, GlobalSettingsSectionValueType.STRING);

    private ConfigurationEntry restoreSuffix = new ConfigurationEntry("restore_suffix", null, GlobalSettingsSectionValueType.STRING);
    private ConfigurationEntry restorePrefix = new ConfigurationEntry("restore_prefix", null, GlobalSettingsSectionValueType.STRING);

    // "jobstreams", "filetransfer"
    private ConfigurationEntry showViewDashboard = new ConfigurationEntry("show_view_dashboard", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewDailyplan = new ConfigurationEntry("show_view_dailyplan", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewWorkflows = new ConfigurationEntry("show_view_workflows", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewResources = new ConfigurationEntry("show_view_resources", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewHistory = new ConfigurationEntry("show_view_history", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewAuditlog = new ConfigurationEntry("show_view_auditlog", null, GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry showViewConfiguration = new ConfigurationEntry("show_view_configuration", null,
            GlobalSettingsSectionValueType.BOOLEAN);

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

        showViewDashboard.setOrdering(++index);
        showViewDailyplan.setOrdering(++index);
        showViewWorkflows.setOrdering(++index);
        showViewResources.setOrdering(++index);
        showViewHistory.setOrdering(++index);
        showViewAuditlog.setOrdering(++index);
        showViewConfiguration.setOrdering(++index);
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

    public ConfigurationEntry getShowViewDashboard() {
        return showViewDashboard;
    }

    public ConfigurationEntry getShowViewDailyplan() {
        return showViewDailyplan;
    }

    public ConfigurationEntry getShowViewWorkflows() {
        return showViewWorkflows;
    }

    public ConfigurationEntry getShowViewResources() {
        return showViewResources;
    }

    public ConfigurationEntry getShowViewHistory() {
        return showViewHistory;
    }

    public ConfigurationEntry getShowViewAuditlog() {
        return showViewAuditlog;
    }

    public ConfigurationEntry getShowViewConfiguration() {
        return showViewConfiguration;
    }

}
