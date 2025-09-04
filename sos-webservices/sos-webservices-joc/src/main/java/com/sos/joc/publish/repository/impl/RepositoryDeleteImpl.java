package com.sos.joc.publish.repository.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.repository.DeleteFromFilter;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.resource.IRepositoryDelete;
import com.sos.joc.publish.repository.util.RepositoryDeleteUtil;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository")
public class RepositoryDeleteImpl extends JOCResourceImpl implements IRepositoryDelete {

    private static final String API_CALL = "./inventory/repository/delete";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDeleteImpl.class);

    @Override
    public JOCDefaultResponse postDelete(String xAccessToken, byte[] deleteFromFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        boolean permitted = false;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** delete from repository started ***" + started);
            deleteFromFilter = initLogging(API_CALL, deleteFromFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(deleteFromFilter, DeleteFromFilter.class);
            DeleteFromFilter filter = Globals.objectMapper.readValue(deleteFromFilter, DeleteFromFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getDeploy()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            permitted = GitSemaphore.tryAcquire();
            if (!permitted) {
                throw new JocConcurrentAccessException(GitCommandUtils.CONCURRENT_ACCESS_MESSAGE);
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(hibernateSession);

            storeAuditLog(filter.getAuditLog());
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Path repositoriesBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            
            Set<Path> toDelete = RepositoryUtil.getPathsToDeleteFromFS(filter, repositoriesBase);
            toDelete = toDelete.stream().filter(item -> canAdd(
                    Globals.normalizePath(
                                    Paths.get("/").resolve(repositoriesBase.relativize(RepositoryUtil.stripFileExtensionFromPath(item))).toString()),
                    permittedFolders))
                .filter(Objects::nonNull).collect(Collectors.toSet());
            toDelete.forEach(path -> {
                try {
                    if (!Files.isDirectory(repositoriesBase.resolve(path))) {
                        LOGGER.debug("resolved path: " + repositoriesBase.resolve(path).toString());
                        boolean deleted = Files.deleteIfExists(repositoriesBase.resolve(path));
                        if (deleted) {
                            LOGGER.debug(String.format("File %1$s has been deleted.", repositoriesBase.resolve(path).toString()));
                        } else {
                            LOGGER.debug(String.format("File %1$s does not exist in filesystem.", repositoriesBase.resolve(path).toString()));
                        }
                    }
                } catch (IOException e) {
                    LOGGER.debug(String.format("file - %1$s - could not be deleted!", Globals.normalizePath(path.toString())), e);
                }
            });
            Set<Configuration> deletedFolders = RepositoryDeleteUtil.deleteFolders(filter, repositoriesBase);
            // check top level folders if containing any data and set back repoControlled flag in dbItem
            RepositoryUtil.updateRepoControlledFlagForConfigurations(deletedFolders, repositoriesBase, dbLayer);
            
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** delete from repository finished ***" + apiCallFinished);
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

    private static String getSubrepositoryFromFilter (DeleteFromFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }
    
}