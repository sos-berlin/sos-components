package com.sos.auth.classes;

import java.nio.file.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.model.security.configuration.SecurityConfiguration;

public class SOSSecurityConfiguration {

    public SOSSecurityConfiguration(String string) {
    }

    public SecurityConfiguration readConfigurationFromFilesystem(SOSHibernateSession sosHibernateSession, Path iniFilename) {
        return null;
    }

    public static boolean haveShiro() {
        return false;
    }

}
