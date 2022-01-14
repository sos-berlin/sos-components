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
import com.sos.joc.publish.impl.RevokeImpl;
import com.sos.joc.publish.repository.resource.IRepositoryStore;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository")
public class RepositoryStoreImpl extends JOCResourceImpl implements IRepositoryStore {

    private static final String API_CALL = "./inventory/repository/store";
    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeImpl.class);

    @Override
    public JOCDefaultResponse postStore(String xAccessToken, byte[] copyToFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.info("*** store to repository started ***" + started);
            initLogging(API_CALL, copyToFilter, xAccessToken);
            JsonValidator.validate(copyToFilter, CopyToFilter.class);
            CopyToFilter filter = Globals.objectMapper.readValue(copyToFilter, CopyToFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAudit = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            Path repositories = Globals.sosCockpitProperties.resolvePath("repositories");
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            Set<ConfigurationObject> deployables = RepositoryUtil.getDeployableEnvIndependentConfigurationsFromDB(filter, dbLayer, null);
            deployables.addAll(RepositoryUtil.getDeployableEnvRelatedConfigurationsFromDB(filter, dbLayer, null));
            deployables = deployables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<ConfigurationObject> releasables = RepositoryUtil.getReleasableConfigurationsFromDB(filter, dbLayer);
            releasables = releasables.stream().filter(item -> canAdd(item.getPath(), permittedFolders)).filter(Objects::nonNull).collect(Collectors.toSet());
            // TODO: clarify whether to create auditLog Detail entries
//            final Stream<ConfigurationObject> stream = Stream.concat(deployables.stream(), releasables.stream());
//            CompletableFuture.runAsync(() -> JocAuditLog.storeAuditLogDetails(stream.map(i -> new AuditLogDetail(i.getPath(), i.getObjectType().intValue())),
//                    dbAudit.getId(), dbAudit.getCreated()));
            RepositoryUtil.writeToRepository(deployables, releasables, repositories);
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.info("*** store to repository finished ***" + apiCallFinished);
            LOGGER.info("complete WS time : " + (apiCallFinished.getTime() - started.getTime()) + " ms");
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
