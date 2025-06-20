package com.sos.joc.publish.repository.git.credentials.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocGitException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.publish.git.AddCredentialsFilter;
import com.sos.joc.model.publish.git.GitCredentials;
import com.sos.joc.model.publish.git.GitCredentialsList;
import com.sos.joc.publish.repository.git.credentials.resource.IGitCredentialsAdd;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository/git")
public class GitCredentialsAddImpl extends JOCResourceImpl implements IGitCredentialsAdd {

    private static final String API_CALL = "./inventory/repository/git/credentials/add";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCredentialsAddImpl.class);

    @Override
    public JOCDefaultResponse postAddCredentials(String xAccessToken, byte[] addCredentialsFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** add credentials started ***" + started);
            // obscure security relevant data
            AddCredentialsFilter filterForLogging = Globals.objectMapper.readValue(addCredentialsFilter, AddCredentialsFilter.class);
            Set<String> wrongServernames = new HashSet<String>();
            for(GitCredentials cred : filterForLogging.getCredentials()) {
                if(cred.getGitServer().contains("://")) {
                    wrongServernames.add(cred.getGitServer());
                }
            }
            if(!wrongServernames.isEmpty()) {
                StringBuilder message = new StringBuilder("Credentials could not be stored! GitServer entries malformed! Allowed Format HOST(:PORT).\n");
                message.append("Malformed GitServers:\n");
                wrongServernames.stream().forEach(servername -> message.append(servername).append(", "));
                throw new JocGitException(message.toString());
            }
            for(GitCredentials cred : filterForLogging.getCredentials()) {
               if(cred.getPassword() != null && !cred.getPassword().isEmpty()) {
                   cred.setPassword("********");
               }
               if(cred.getPersonalAccessToken() != null && !cred.getPersonalAccessToken().isEmpty()) {
                   cred.setPersonalAccessToken("********");
               }
            }
            byte[] filterForLog = Globals.objectMapper.writeValueAsBytes(filterForLogging);
            initLogging(API_CALL, filterForLog, addCredentialsFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(addCredentialsFilter, AddCredentialsFilter.class);
            AddCredentialsFilter filter = Globals.objectMapper.readValue(addCredentialsFilter, AddCredentialsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            storeAuditLog(filter.getAuditLog());
            String account = null;
            
            if(JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            } else {
                account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            }
            JocConfigurationDbLayer dbLayer = new JocConfigurationDbLayer(hibernateSession);
            JocConfigurationFilter dbFilter = new JocConfigurationFilter();
            dbFilter.setAccount(account);
            dbFilter.setConfigurationType(ConfigurationType.GIT.value());
            List<DBItemJocConfiguration> existing = dbLayer.getJocConfigurations(dbFilter, 0);
            GitCredentialsList credList = null;
            DBItemJocConfiguration dbItem = null;
            if(existing != null && !existing.isEmpty()) {
                dbItem = existing.get(0);
                credList =  Globals.objectMapper.readValue(dbItem.getConfigurationItem(), GitCredentialsList.class);
            } else {
                dbItem = new DBItemJocConfiguration();
                dbItem.setAccount(account);
                dbItem.setConfigurationType(ConfigurationType.GIT.value());
                dbItem.setShared(false);
                dbItem.setInstanceId(0L);
                dbItem.setControllerId(DBLayer.DEFAULT_KEY);
                dbItem.setModified(Date.from(Instant.now()));
                credList = new GitCredentialsList();
            }
            if(filter.getCredentials() != null && !filter.getCredentials().isEmpty()) {
                for(GitCredentials credentials : filter.getCredentials()) {
                    boolean exists = checkExists(credentials, credList);
                    if(!exists) {
                        credList.getCredentials().add(credentials);
                    } else {
                        throw new JocGitException(String.format("credentials for server %1$s already exist.", credentials.getGitServer()));
                    }
                }
            }
            dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(credList));
            dbLayer.saveOrUpdateConfiguration(dbItem);
            Date finished = Date.from(Instant.now());
            LOGGER.trace("*** add credentials finished ***" + finished);
            LOGGER.trace(String.format("ws took %1$d ms.", finished.getTime() - started.getTime()));
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private boolean checkExists(GitCredentials credentials, GitCredentialsList existing) {
        if (!existing.getCredentials().isEmpty()) {
            for (GitCredentials cred : existing.getCredentials()) {
                if (cred.getGitServer().equals(credentials.getGitServer())) {
                    return true;
                }
            } 
        }
        return false;
    }

}
