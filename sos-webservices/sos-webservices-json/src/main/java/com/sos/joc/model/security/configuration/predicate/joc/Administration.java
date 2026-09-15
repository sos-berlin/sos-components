
package com.sos.joc.model.security.configuration.predicate.joc;

import com.sos.joc.model.security.configuration.predicate.joc.admin.Accounts;
import com.sos.joc.model.security.configuration.predicate.joc.admin.Certificates;
import com.sos.joc.model.security.configuration.predicate.joc.admin.Controllers;
import com.sos.joc.model.security.configuration.predicate.joc.admin.Customization;
import com.sos.joc.model.security.configuration.predicate.joc.admin.Settings;

public class Administration {
    
    private final String prefix;

    private Accounts accounts;
    private Settings settings;
    private Controllers controllers;
    private Certificates certificates;
    private Customization customization;

    public Administration(String parentPrefix) {
        prefix = parentPrefix + ":" + "administration";
        accounts = new Accounts(prefix);
        settings = new Settings(prefix);
        controllers = new Controllers(prefix);
        certificates = new Certificates(prefix);
        customization = new Customization(prefix);
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public Settings getSettings() {
        return settings;
    }
    
    public Controllers getControllers() {
        return controllers;
    }

    public Certificates getCertificates() {
        return certificates;
    }

    public Customization getCustomization() {
        return customization;
    }

}
