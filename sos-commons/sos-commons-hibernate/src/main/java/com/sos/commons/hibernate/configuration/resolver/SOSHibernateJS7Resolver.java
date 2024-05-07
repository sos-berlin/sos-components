package com.sos.commons.hibernate.configuration.resolver;

import org.hibernate.cfg.Configuration;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;

/** JOC-1510<br/>
 * TODO: move to JS7 packages<br/>
 */
public class SOSHibernateJS7Resolver implements ISOSHibernateConfigurationResolver {

    // JS7 Environment variables for substitution
    private static final String JS7_DBMS_URL_PARAMETER = "JS7_DBMS_URL_PARAMETER";
    private static final String JS7_DBMS_USER = "JS7_DBMS_USER";
    private static final String JS7_DBMS_PASSWORD = "JS7_DBMS_PASSWORD";

    @Override
    public Configuration resolve(Configuration configuration) throws SOSHibernateConfigurationException {
        if (configuration == null) {
            return configuration;
        }

        String url = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
        if (url != null && url.contains("${" + JS7_DBMS_URL_PARAMETER + "}")) {
            configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, resolve(url, JS7_DBMS_URL_PARAMETER));
        }
        String username = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME);
        if (username != null && username.contains("${" + JS7_DBMS_USER + "}")) {
            configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, resolve(username, JS7_DBMS_USER));
        }
        String password = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD);
        if (password != null && password.contains("${" + JS7_DBMS_PASSWORD + "}")) {
            configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, resolve(password, JS7_DBMS_PASSWORD));
        }

        return configuration;
    }

    private String resolve(String confValue, String key) {
        String envVar = getEnvironmentVariable(key);
        if (JS7_DBMS_URL_PARAMETER.equals(key)) {
            envVar = normalize(envVar, confValue);
        }
        return confValue.replaceFirst("\\$\\{" + key + "\\}", envVar);
    }

    private String getEnvironmentVariable(String key) {
        String envVar = System.getProperty(getSystemPropKey(key));
        if (envVar == null) {
            envVar = System.getenv(key);
        }
        if (envVar == null) {
            envVar = "";
        }
        return envVar;
    }

    private String getSystemPropKey(String key) {
        // e.g. env key to system prop key: JS7_DBMS_PASSWORD -> js7.dbms.password
        return key.toLowerCase().replace('_', '.');
    }

    private String normalize(String envVar, String url) {
        if (!envVar.isEmpty()) {
            int index = url.indexOf("${" + JS7_DBMS_URL_PARAMETER + "}");
            if (index > -1) {
                char firstCharOfEnvVar = envVar.charAt(0);
                if (firstCharOfEnvVar == '&' || firstCharOfEnvVar == '?') {
                    envVar = envVar.substring(1);
                }
                char charBeforeEnvVar = url.charAt(index - 1);
                if (charBeforeEnvVar != '&' && charBeforeEnvVar != '?') {
                    if (url.contains("?")) {
                        envVar = "&" + envVar;
                    } else {
                        envVar = "?" + envVar;
                    }
                }
            }
        }
        return envVar;
    }

}
