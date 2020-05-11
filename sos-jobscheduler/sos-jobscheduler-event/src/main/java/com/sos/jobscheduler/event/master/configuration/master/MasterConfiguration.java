package com.sos.jobscheduler.event.master.configuration.master;

import java.util.Properties;

import com.sos.commons.util.SOSString;

public class MasterConfiguration {

    private Master primary;
    private Master backup;
    private Master current;

    // TODO
    public void load(final Properties conf) throws Exception {
        Master primaryMaster = new Master(conf.getProperty("jobscheduler_id"), conf.getProperty("primary_master_uri"), conf.getProperty(
                "primary_cluster_uri"), conf.getProperty("primary_master_user"), conf.getProperty("primary_master_user_password"));

        Master backupMaster = null;
        if (!SOSString.isEmpty(conf.getProperty("backup_master_uri"))) {
            backupMaster = new Master(primaryMaster.getJobSchedulerId(), conf.getProperty("backup_master_uri"), conf.getProperty(
                    "backup_cluster_uri"), conf.getProperty("backup_master_user"), conf.getProperty("backup_master_user_password"));
        }
        init(primaryMaster, backupMaster);
    }

    private void init(Master primaryMaster, Master backupMaster) throws Exception {
        if (primaryMaster == null) {
            throw new Exception("primaryMaster is null");
        }
        primary = primaryMaster;
        primary.setPrimary(true);
        current = primary;
        backup = backupMaster;
        if (backup != null) {
            backup.setJobSchedulerId(primaryMaster.getJobSchedulerId());
            backup.setPrimary(false);
        }
    }

    public void setClusterMasters(Master master, boolean isPrimary) {
        if (isPrimary && master.equals(primary)) {
            return;
        }
        if (backup != null) {
            if (!isPrimary && master.equals(backup)) {
                return;
            }
            Master oldBackUp = backup;

            backup = primary;
            backup.setPrimary(false);

            primary = oldBackUp;
            primary.setPrimary(true);
        }
    }

    public Master getPrimary() {
        return primary;
    }

    public Master getBackup() {
        return backup;
    }

    public Master getCurrent() {
        return current;
    }

    public Master getNotCurrent() {
        if (current != null && backup != null) {
            return current.equals(primary) ? backup : primary;
        }
        return null;
    }

    public void switchCurrent() {
        if (current != null && backup != null) {
            current = current.equals(primary) ? backup : primary;
        }
    }

    public void setCurrent(Master val) {
        current = val;
    }
}
