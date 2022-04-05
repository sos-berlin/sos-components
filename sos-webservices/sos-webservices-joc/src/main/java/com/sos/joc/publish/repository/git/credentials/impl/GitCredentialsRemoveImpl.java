package com.sos.joc.publish.repository.git.credentials.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.publish.git.GitCredentialsList;
import com.sos.joc.model.publish.git.RemoveCredentialsFilter;
import com.sos.joc.publish.repository.git.credentials.resource.IGitCredentialsRemove;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository/git")
public class GitCredentialsRemoveImpl extends JOCResourceImpl implements IGitCredentialsRemove {

    private static final String API_CALL = "./inventory/repository/git/credentials/remove";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCredentialsRemoveImpl.class);

    @Override
    public JOCDefaultResponse postRemoveCredentials(String xAccessToken, byte[] removeCredentialsFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** remove credentials started ***" + started);
            initLogging(API_CALL, removeCredentialsFilter, xAccessToken);
            JsonValidator.validate(removeCredentialsFilter, RemoveCredentialsFilter.class);
            RemoveCredentialsFilter filter = Globals.objectMapper.readValue(removeCredentialsFilter, RemoveCredentialsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = null;
            
            if(JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            } else if (JocSecurityLevel.MEDIUM.equals(Globals.getJocSecurityLevel())) {
                account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            } else {
                throw new JocNotImplementedException("The web service is not available for Security Level HIGH.");
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
            }
            if(filter.getGitServers() != null && !filter.getGitServers().isEmpty()) {
                for(String gitServer : filter.getGitServers()) {
                    deleteIfExists(gitServer, credList);
                }
            }
            dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(credList));
            dbLayer.saveOrUpdateConfiguration(dbItem);
            Date finished = Date.from(Instant.now());
            LOGGER.trace("*** remove credentials finished ***" + finished);
            LOGGER.trace(String.format("ws took %1$d ms.", finished.getTime() - started.getTime()));
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private void deleteIfExists(String gitServer, GitCredentialsList existing) {
        existing.setCredentials(existing.getCredentials().stream()
                .filter(item -> !item.getGitServer().equals(gitServer))
                .collect(Collectors.toList()));
    }
    
}
