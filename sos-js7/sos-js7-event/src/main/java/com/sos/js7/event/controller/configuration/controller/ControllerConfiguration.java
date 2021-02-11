package com.sos.js7.event.controller.configuration.controller;

import java.util.Properties;

import com.sos.commons.util.SOSString;

public class ControllerConfiguration {

    private Properties config;
    private Controller primary;
    private Controller secondary;
    private Controller current;

    public void load(final Properties conf) throws Exception {
        config = conf;
        Controller primary = new Controller(conf.getProperty("controller_id"), conf.getProperty("primary_controller_uri"), conf.getProperty(
                "primary_controller_cluster_uri"), conf.getProperty("primary_controller_user"), conf.getProperty("primary_controller_user_password"));

        Controller secondary = null;
        if (!SOSString.isEmpty(conf.getProperty("secondary_controller_uri"))) {
            secondary = new Controller(primary.getId(), conf.getProperty("secondary_controller_uri"), conf.getProperty(
                    "secondary_controller_cluster_uri"), conf.getProperty("secondary_controller_user"), conf.getProperty(
                            "secondary_controller_user_password"));
        }
        init(primary, secondary);
    }

    private void init(Controller primaryController, Controller secondaryController) throws Exception {
        if (primaryController == null) {
            throw new Exception("primaryController is null");
        }
        primary = primaryController;
        primary.setPrimary(true);
        current = primary;
        secondary = secondaryController;
        if (secondary != null) {
            secondary.setId(primaryController.getId());
            secondary.setPrimary(false);
        }
    }

    public void setClusterControllers(Controller controller, boolean isPrimary) {
        if (isPrimary && controller.equals(primary)) {
            return;
        }
        if (secondary != null) {
            if (!isPrimary && controller.equals(secondary)) {
                return;
            }
            Controller oldSecondary = secondary;

            secondary = primary;
            secondary.setPrimary(false);

            primary = oldSecondary;
            primary.setPrimary(true);
        }
    }

    public Controller getPrimary() {
        return primary;
    }

    public Controller getSecondary() {
        return secondary;
    }

    public Controller getCurrent() {
        return current;
    }

    public Controller getNotCurrent() {
        if (current != null && secondary != null) {
            return current.equals(primary) ? secondary : primary;
        }
        return null;
    }

    public void switchCurrent() {
        if (current != null && secondary != null) {
            current = current.equals(primary) ? secondary : primary;
        }
    }

    public void setCurrent(Controller val) {
        current = val;
    }

    public ControllerConfiguration copy(String user, String pass) {
        Properties p = config;
        p.put("primary_controller_user", user);
        p.put("primary_controller_user_password", pass);
        if (secondary != null) {
            p.put("secondary_controller_user", user);
            p.put("secondary_controller_user_password", pass);
        }
        ControllerConfiguration m = new ControllerConfiguration();
        try {
            m.load(p);
        } catch (Exception e) {
        }
        return m;
    }
}
