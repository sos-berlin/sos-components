package com.sos.jobscheduler.event.master.configuration.master;

import java.util.Properties;

import com.sos.commons.util.SOSString;

public class MasterConfiguration implements IMasterConfiguration {

    private Master primary;
    private Master backup;
    private Master current;
    
    // TODO
    public void load(final Properties conf) throws Exception {
        Master primaryMaster = new Master(conf.getProperty("master_id"), conf.getProperty("primary_master_uri"), conf.getProperty(
                "primary_master_user"), conf.getProperty("primary_master_user_password"));

        Master backupMaster = null;
        if (!SOSString.isEmpty(conf.getProperty("backup_master_uri"))) {
            backupMaster = new Master(primaryMaster.getId(), conf.getProperty("backup_master_uri"), conf.getProperty("backup_master_user"), conf
                    .getProperty("backup_master_user_password"));
        }
        initMasterSettings(primaryMaster, backupMaster);
    }

    private void initMasterSettings(Master primaryMaster, Master backupMaster) throws Exception {
        if (primaryMaster == null) {
            throw new Exception("primaryMaster is null");
        }

        primary = primaryMaster;
        primary.setPrimary(true);
        current = primary;
        backup = backupMaster;
        if (backup != null) {
            backup.setId(primaryMaster.getId());
            backup.setPrimary(false);
        }
    }

    @Override
    public Master getPrimary() {
        return primary;
    }

    @Override
    public Master getBackup() {
        return backup;
    }

    @Override
    public Master getCurrent() {
        return current;
    }

    @Override
    public void setCurrent(Master val) {
        current = val;
    }
}
