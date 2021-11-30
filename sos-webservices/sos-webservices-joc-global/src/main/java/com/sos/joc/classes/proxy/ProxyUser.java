package com.sos.joc.classes.proxy;

import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.configuration.globals.GlobalSettings;

import js7.data_for_java.auth.JCredentials;
import scala.compat.java8.OptionConverters;

public enum ProxyUser {
    
    JOC("JOC", "JS7-JOC"),
    HISTORY("History", "JS7-History");
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUser.class);
    private final String user;
    private final String pwd;

    private ProxyUser(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return this.user;
    }
    
    public String getUser() {
        return user;
    }
    
    protected JCredentials value() {
        return JCredentials.of(getUser(), getPwd());
    }
    
    public static Optional<String> getBasicAuthorization(JCredentials credential) {
        if (credential == null) {
           return Optional.empty(); 
        }
        return OptionConverters.toJava(credential.toScala()).map(c -> new String(Base64.getEncoder().encode((c.userId().string() + ":" + c.password()
                .string()).getBytes())));
    }
    
    public static String getBasicAuthorization(String user, String pwd) {
        return new String(Base64.getEncoder().encode((user + ":" + pwd).getBytes()));
    }
    
    public String getBasicAuthorization() {
        return new String(Base64.getEncoder().encode((getUser() + ":" + getPwd()).getBytes()));
    }
    
    public String getPwd() {
        ConfigurationGlobalsJoc settings = Globals.getConfigurationGlobalsJoc();
        switch (this) {
        case JOC:
            if (settings.getJOCPwd() != null && settings.getJOCPwd().getValue() != null) {
                return settings.getJOCPwd().getValue();
            }
            break;
        case HISTORY:
            if (settings.getHistoryPwd() != null && settings.getJOCPwd().getValue() != null) {
                return settings.getHistoryPwd().getValue();
            }
            break;
        }
        
        String _pwd = getPwdFromDB();
        if (_pwd != null) {
            return _pwd; 
        }
        LOGGER.info("JOC settings doesn't contain password for user '" + this.user + "' -> default is used");
        return this.pwd;
    }

    private String getPwdFromDB() {
        SOSHibernateSession sosHibernateSession = null;
        ConfigurationGlobals configurations = new ConfigurationGlobals();
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ProxyUser");
            DBLayerJocCluster dbLayer = new DBLayerJocCluster(sosHibernateSession);
            DBItemJocConfiguration item = dbLayer.getGlobalsSettings();
            if (item != null && !SOSString.isEmpty(item.getConfigurationItem()) && !item.getConfigurationItem().equals(
                    ConfigurationGlobals.DEFAULT_CONFIGURATION_ITEM)) {
                GlobalSettings settings = Globals.objectMapper.readValue(item.getConfigurationItem(), GlobalSettings.class);
                configurations.setConfigurationValues(settings);
                switch (this) {
                case JOC:
                    return ((ConfigurationGlobalsJoc) configurations.getConfigurationSection(DefaultSections.joc)).getJOCPwd().getValue();
                case HISTORY:
                    return ((ConfigurationGlobalsJoc) configurations.getConfigurationSection(DefaultSections.joc)).getHistoryPwd().getValue();
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return null;
    }
}
