package com.sos.joc.publish.repository.impl;

import java.io.IOException;
import java.nio.file.Files;
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
import com.sos.joc.model.publish.repository.DeleteFromFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.repository.resource.IRepositoryDelete;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository")
public class RepositoryDeleteImpl extends JOCResourceImpl implements IRepositoryDelete {

    private static final String API_CALL = "./inventory/repository/delete";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDeleteImpl.class);

    @Override
    public JOCDefaultResponse postDelete(String xAccessToken, byte[] deleteFromFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** delete from repository started ***" + started);
            initLogging(API_CALL, deleteFromFilter, xAccessToken);
            JsonValidator.validate(deleteFromFilter, DeleteFromFilter.class);
            DeleteFromFilter filter = Globals.objectMapper.readValue(deleteFromFilter, DeleteFromFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);

            DBItemJocAuditLog dbAuditlog = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            
            Set<Path> toDelete = RepositoryUtil.getRelativePathsToDeleteFromDB(filter, dbLayer);
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Path repositories = Globals.sosCockpitProperties.resolvePath("repositories");

            toDelete = toDelete.stream().filter(item -> canAdd(Globals.normalizePath(item.toString()), permittedFolders))
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            toDelete.forEach(path -> {
                try {
                    Files.deleteIfExists(repositories.resolve(path));
                } catch (IOException e) {
                    LOGGER.debug(String.format("file - %1$s - could not be deleted!", Globals.normalizePath(path.toString())), e);
                }
            });
            
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** delete from repository finished ***" + apiCallFinished);
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