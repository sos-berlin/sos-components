package com.sos.joc.publish.repository.impl;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.publish.GitSemaphore;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.resource.IRepositoryStore;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.joc.publish.repository.util.StoreItemsCategory;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository")
public class RepositoryStoreImpl extends JOCResourceImpl implements IRepositoryStore {

    private static final String API_CALL = "./inventory/repository/store";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryStoreImpl.class);

    @Override
    public JOCDefaultResponse postStore(String xAccessToken, byte[] copyToFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        boolean permitted = false;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** store to repository started ***" + started);
            copyToFilter = initLogging(API_CALL, copyToFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(copyToFilter, CopyToFilter.class);
            CopyToFilter filter = Globals.objectMapper.readValue(copyToFilter, CopyToFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            permitted = GitSemaphore.tryAcquire();
            if (!permitted) {
                throw new JocConcurrentAccessException(GitCommandUtils.CONCURRENT_ACCESS_MESSAGE);
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbAudit = storeAuditLog(filter.getAuditLog());
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            Path repositoriesBase = Globals.sosCockpitProperties.resolvePath("repositories");
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            Set<ConfigurationObject> deployables = RepositoryUtil.getDeployableRolloutConfigurationsFromDB(filter, dbLayer, null);
            deployables.addAll(RepositoryUtil.getDeployableLocalConfigurationsFromDB(filter, dbLayer, null));
            deployables = deployables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<ConfigurationObject> releasables = RepositoryUtil.getReleasableRolloutConfigurationsFromDB(filter, dbLayer);
            releasables.addAll(RepositoryUtil.getReleasableLocalConfigurationsFromDB(filter, dbLayer));
            releasables = releasables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
            if (filter.getRollout() != null && filter.getLocal() != null) {
//              both
                RepositoryUtil.writeToRepository(deployables, releasables, repositoriesBase, StoreItemsCategory.BOTH);
          } else if (filter.getRollout() != null) {
//              only rollout
              RepositoryUtil.writeToRepository(deployables, releasables, repositoriesBase, StoreItemsCategory.ROLLOUT);
          } else if (filter.getLocal() != null) {
//              only local
              RepositoryUtil.writeToRepository(deployables, releasables, repositoriesBase, StoreItemsCategory.LOCAL);
          }
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** store to repository finished ***" + apiCallFinished);
            LOGGER.trace("complete WS time : " + (apiCallFinished.getTime() - started.getTime()) + " ms");
            return responseStatusJSOk(Date.from(Instant.now()));
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
