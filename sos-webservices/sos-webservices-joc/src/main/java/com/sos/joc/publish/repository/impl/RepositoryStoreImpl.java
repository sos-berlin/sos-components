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
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.repository.resource.IRepositoryStore;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository")
public class RepositoryStoreImpl extends JOCResourceImpl implements IRepositoryStore {

    private static final String API_CALL = "./inventory/repository/store";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryStoreImpl.class);

    @Override
    public JOCDefaultResponse postStore(String xAccessToken, byte[] copyToFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** store to repository started ***" + started);
            initLogging(API_CALL, copyToFilter, xAccessToken);
            JsonValidator.validate(copyToFilter, CopyToFilter.class);
            CopyToFilter filter = Globals.objectMapper.readValue(copyToFilter, CopyToFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbAudit = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            Path repositoriesBase = Globals.sosCockpitProperties.resolvePath("repositories");
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            Set<ConfigurationObject> deployables = RepositoryUtil.getDeployableRolloutConfigurationsFromDB(filter, dbLayer, null);
            deployables.addAll(RepositoryUtil.getDeployableLocalConfigurationsFromDB(filter, dbLayer, null));
            deployables = deployables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<ConfigurationObject> releasables = RepositoryUtil.getReleasableConfigurationsFromDB(filter, dbLayer);
            releasables = releasables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
            RepositoryUtil.writeToRepository(deployables, releasables, repositoriesBase);
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** store to repository finished ***" + apiCallFinished);
            LOGGER.trace("complete WS time : " + (apiCallFinished.getTime() - started.getTime()) + " ms");
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
