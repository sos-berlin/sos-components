package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;

import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.data.auth.JAdmission;
import js7.proxy.javaapi.data.auth.JHttpsConfig;

public class ControllerApiContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerApiContext.class);
    private ProxyCredentials credentials;
    private JControllerApi controllerApi;

    protected ControllerApiContext(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        this.credentials = credentials;
        connect(proxyContext, credentials);
    }

    protected JControllerApi get() {
        return controllerApi;
    }
    
    protected void connect(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        LOGGER.info(String.format("connect ControllerApi of %s", toString()));
        checkCredentials(credentials);
        List<JAdmission> admissions = null;
        if (credentials.getBackupUrl() != null) {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()), JAdmission.of(credentials.getBackupUrl(),
                    credentials.getAccount()));
        } else {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()));
        }
        this.controllerApi = proxyContext.newControllerApi(admissions, credentials.getHttpsConfig());
    }
    
    @Override
    public String toString() {
        if (credentials.getBackupUrl() != null) {
            return String.format("'%s' cluster (%s, %s)", credentials.getJobSchedulerId(), credentials.getUrl(), credentials.getBackupUrl());
        } else {
            return String.format("'%s' (%s)", credentials.getJobSchedulerId(), credentials.getUrl());
        }
    }

    private void checkCredentials(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        if (credentials.getUrl() == null) {
            throw new JobSchedulerConnectionRefusedException("URL is undefined");
        } else if (credentials.getUrl().startsWith("https://") || (credentials.getBackupUrl() != null && credentials.getBackupUrl().startsWith(
                "https://"))) {
            JHttpsConfig httpsConfig = credentials.getHttpsConfig();
            if (httpsConfig.asScala().trustStoreRefs() == null || httpsConfig.asScala().trustStoreRefs().toIterable().isEmpty()) {
                throw new JobSchedulerConnectionRefusedException("Required truststore not found");
            } else if (credentials.getAccount().toScala().isEmpty() && !httpsConfig.asScala().keyStoreRef().nonEmpty()) {
                throw new JobSchedulerConnectionRefusedException("Neither account is specified nor client certificate was found");
            }
        }
    }
}
