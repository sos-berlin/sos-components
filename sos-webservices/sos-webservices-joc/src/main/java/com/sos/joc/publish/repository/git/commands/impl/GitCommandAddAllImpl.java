package com.sos.joc.publish.repository.git.commands.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.git.results.GitAddCommandResult;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.publish.GitSemaphore;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocGitException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.git.GitCredentials;
import com.sos.joc.model.publish.git.commands.CommonFilter;
import com.sos.joc.model.publish.git.commands.GitCommandResponse;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.git.commands.resource.IGitCommandAddAll;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository/git")
public class GitCommandAddAllImpl extends JOCResourceImpl implements IGitCommandAddAll {

    private static final String API_CALL = "./inventory/repository/git/add";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommandAddAllImpl.class);

    @Override
    public JOCDefaultResponse postCommandAdd(String xAccessToken, byte[] commonFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        boolean permitted = false;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** add all started ***" + started);
            commonFilter = initLogging(API_CALL, commonFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(commonFilter, CommonFilter.class);
            CommonFilter filter = Globals.objectMapper.readValue(commonFilter, CommonFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            permitted = GitSemaphore.tryAcquire();
            if (!permitted) {
                throw new JocConcurrentAccessException(GitCommandUtils.CONCURRENT_ACCESS_MESSAGE);
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
            Path backupPath = GitCommandUtils.backupGitGlobalConfigFile();

            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(
                    GitCommandUtils.getSubrepositoryFromFilter(filter));
            Path localRepo = repositoryBase.resolve(filter.getFolder().startsWith("/") ? 
                    filter.getFolder().substring(1) : filter.getFolder());
            
            GitCredentials credentials = GitCommandUtils.getCredentials(account, workingDir, localRepo, dbLayer);
            if (credentials != null) {
                GitCommandUtils.prepareConfigFile(StandardCharsets.UTF_8, credentials, localRepo);
            } else {
                throw new JocGitException(String.format("Could not read git credentials for account %1$s. Add Git credentials to your profile.", account));
            }

            GitAddCommandResult result = GitCommandUtils.addAllChanges(account, localRepo, workingDir, 
                    Globals.getConfigurationGlobalsJoc().getEncodingCharset());
            GitCommandResponse response = new GitCommandResponse();
            response.setCommand(result.getOriginalCommand());
            response.setExitCode(result.getExitCode());
            response.setStdOut(result.getStdOut());
            response.setStdErr(result.getStdErr());

            //cleanup
            GitCommandUtils.restoreOriginalGitGlobalConfigFile(backupPath);
            
            Date finished = Date.from(Instant.now());
            LOGGER.trace("*** add all finished ***" + finished);
            LOGGER.trace(String.format("ws took %1$d ms.", finished.getTime() - started.getTime()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
            if (permitted) {
                GitSemaphore.release();
            }
        }
    }

}
