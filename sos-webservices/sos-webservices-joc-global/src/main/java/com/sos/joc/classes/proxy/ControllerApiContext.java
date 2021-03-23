package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.ControllerConnectionRefusedException;

import js7.data_for_java.auth.JAdmission;
import js7.data_for_java.auth.JHttpsConfig;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JProxyContext;

public class ControllerApiContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerApiContext.class);
    
    private ControllerApiContext() {
    }

    protected static JControllerApi newControllerApi(JProxyContext proxyContext, ProxyCredentials credentials)
            throws ControllerConnectionRefusedException {
        LOGGER.info(String.format("connect ControllerApi of %s", toString(credentials)));
        checkCredentials(credentials);
        List<JAdmission> admissions = null;
        if (credentials.getBackupUrl() != null) {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()), JAdmission.of(credentials.getBackupUrl(),
                    credentials.getAccount()));
        } else {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()));
        }
        return proxyContext.newControllerApi(admissions, credentials.getHttpsConfig());
    }

    private static String toString(ProxyCredentials credentials) {
        if (credentials.getBackupUrl() != null) {
            return String.format("'%s' cluster (%s, %s)", credentials.getControllerId(), credentials.getUrl(), credentials.getBackupUrl());
        } else {
            return String.format("'%s' (%s)", credentials.getControllerId(), credentials.getUrl());
        }
    }

    private static void checkCredentials(ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        if (credentials.getUrl() == null) {
            throw new ControllerConnectionRefusedException("URL is undefined");
        } else if (credentials.getUrl().startsWith("https://") || (credentials.getBackupUrl() != null && credentials.getBackupUrl().startsWith(
                "https://"))) {
            JHttpsConfig httpsConfig = credentials.getHttpsConfig();
            if (httpsConfig.asScala().trustStoreRefs() == null || httpsConfig.asScala().trustStoreRefs().toIterable().isEmpty()) {
                throw new ControllerConnectionRefusedException("Required truststore not found");
            } else if (credentials.getAccount().toScala().isEmpty() && !httpsConfig.asScala().keyStoreRef().nonEmpty()) {
                throw new ControllerConnectionRefusedException("Neither account is specified nor client certificate was found");
            }
        }
    }
}
