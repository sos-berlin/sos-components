package com.sos.joc.publish.repository.git.commands.impl;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.git.results.GitCheckoutCommandResult;
import com.sos.commons.git.results.GitCloneCommandResult;
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
import com.sos.joc.model.publish.git.commands.CheckoutFilter;
import com.sos.joc.model.publish.git.commands.GitCommandResponse;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.git.commands.resource.IGitCommandCheckout;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository/git")
public class GitCommandCheckoutImpl extends JOCResourceImpl implements IGitCommandCheckout {

    private static final String API_CALL = "./inventory/repository/git/checkout";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommandCheckoutImpl.class);

    @Override
    public JOCDefaultResponse postCommandCheckout(String xAccessToken, byte[] checkoutFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** checkout started ***" + started);
            initLogging(API_CALL, checkoutFilter, xAccessToken);
            JsonValidator.validate(checkoutFilter, CheckoutFilter.class);
            CheckoutFilter filter = Globals.objectMapper.readValue(checkoutFilter, CheckoutFilter.class);
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
            GitCheckoutCommandResult result = GitCommandUtils.checkout(
                    filter, account, dbLayer, Globals.getConfigurationGlobalsJoc().getEncodingCharset());

            GitCommandResponse response = new GitCommandResponse();
            response.setCommand(result.getOriginalCommand());
            response.setExitCode(result.getExitCode());
            response.setStdOut(result.getStdOut());
            response.setStdErr(result.getStdErr());

            Date finished = Date.from(Instant.now());
            LOGGER.trace("*** checkout finished ***" + finished);
            LOGGER.trace(String.format("ws took %1$d ms.", finished.getTime() - started.getTime()));
            return JOCDefaultResponse.responseStatus200(response);
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
