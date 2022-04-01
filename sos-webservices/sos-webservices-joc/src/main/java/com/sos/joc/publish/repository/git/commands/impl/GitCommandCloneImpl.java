package com.sos.joc.publish.repository.git.commands.impl;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.git.commands.CloneFilter;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.git.commands.resource.IGitCommandClone;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository/git")
public class GitCommandCloneImpl extends JOCResourceImpl implements IGitCommandClone {

    private static final String API_CALL = "./inventory/repository/git/clone";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommandCloneImpl.class);

    @Override
    public JOCDefaultResponse postCommandClone(String xAccessToken, byte[] cloneFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.info("*** clone started ***" + started);
            initLogging(API_CALL, cloneFilter, xAccessToken);
            JsonValidator.validate(cloneFilter, CloneFilter.class);
            CloneFilter filter = Globals.objectMapper.readValue(cloneFilter, CloneFilter.class);
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
            GitCommandUtils.cloneGitRepository(filter, account, dbLayer);
            Date finished = Date.from(Instant.now());
            LOGGER.info("*** clone finished ***" + finished);
            LOGGER.info(String.format("ws took %1$d ms.", finished.getTime() - started.getTime()));
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

}
