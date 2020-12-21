package com.sos.joc.deploy.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ProxyUser;

import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.data.auth.JAdmission;
import js7.proxy.javaapi.data.auth.JCredentials;
import js7.proxy.javaapi.data.auth.JHttpsConfig;

public class JProxyTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(JProxyTestClass.class);

    private JProxyContext context;

    public JProxyTestClass() {
        context = new JProxyContext();
    }

    public JControllerApi getControllerApi(ProxyUser user, String uriPrimary) {
        return getControllerApi(user, uriPrimary, null);
    }

    public JControllerApi getControllerApi(ProxyUser user, String uriPrimary, String uriBackup) {
        LOGGER.info(String.format("[%s]getControllerApi ...", uriPrimary));
        return context.newControllerApi(getAdmissions(user, uriPrimary, uriBackup), JHttpsConfig.empty());
    }

    private List<JAdmission> getAdmissions(ProxyUser user, String uriPrimary, String uriBackup) {
        JCredentials credentials = JCredentials.of(user.getUser(), user.getPwd());
        List<JAdmission> l = new ArrayList<JAdmission>();
        l.add(JAdmission.of(uriPrimary, credentials));
        if (!SOSString.isEmpty(uriBackup)) {
            l.add(JAdmission.of(uriBackup, credentials));
        }
        return l;
    }

    public void close() {
        if (context != null) {
            context.close();
            context = null;
            LOGGER.info("context closed");
        } else {
            LOGGER.info("context already closed");
        }
    }

}
