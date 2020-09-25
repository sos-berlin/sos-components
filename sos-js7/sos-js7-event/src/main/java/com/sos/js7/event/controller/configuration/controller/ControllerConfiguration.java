package com.sos.js7.event.controller.configuration.controller;

import java.util.Properties;

import com.sos.commons.util.SOSString;

public class ControllerConfiguration {

    private Properties config;
    private Controller primary;
    private Controller backup;
    private Controller current;

    // TODO
    public void load(final Properties conf) throws Exception {
        config = conf;
        Controller primary = new Controller(conf.getProperty("jobscheduler_id"), conf.getProperty("primary_master_uri"), conf.getProperty(
                "primary_cluster_uri"), conf.getProperty("primary_master_user"), conf.getProperty("primary_master_user_password"));

        Controller backup = null;
        if (!SOSString.isEmpty(conf.getProperty("backup_master_uri"))) {
            backup = new Controller(primary.getId(), conf.getProperty("backup_master_uri"), conf.getProperty("backup_cluster_uri"), conf.getProperty(
                    "backup_master_user"), conf.getProperty("backup_master_user_password"));
        }
        init(primary, backup);
    }

    private void init(Controller primaryController, Controller backupController) throws Exception {
        if (primaryController == null) {
            throw new Exception("primaryController is null");
        }
        primary = primaryController;
        primary.setPrimary(true);
        current = primary;
        backup = backupController;
        if (backup != null) {
            backup.setId(primaryController.getId());
            backup.setPrimary(false);
        }
    }

    public void setClusterControllers(Controller controller, boolean isPrimary) {
        if (isPrimary && controller.equals(primary)) {
            return;
        }
        if (backup != null) {
            if (!isPrimary && controller.equals(backup)) {
                return;
            }
            Controller oldBackUp = backup;

            backup = primary;
            backup.setPrimary(false);

            primary = oldBackUp;
            primary.setPrimary(true);
        }
    }

    public Controller getPrimary() {
        return primary;
    }

    public Controller getBackup() {
        return backup;
    }

    public Controller getCurrent() {
        return current;
    }

    public Controller getNotCurrent() {
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

    public void setCurrent(Controller val) {
        current = val;
    }

    public ControllerConfiguration copy(String user, String pass) {
        Properties p = config;
        p.put("primary_master_user", user);
        p.put("primary_master_user_password", pass);
        if (backup != null) {
            p.put("backup_master_user", user);
            p.put("backup_master_user_password", pass);
        }
        ControllerConfiguration m = new ControllerConfiguration();
        try {
            m.load(p);
        } catch (Exception e) {
        }
        return m;
    }
}
