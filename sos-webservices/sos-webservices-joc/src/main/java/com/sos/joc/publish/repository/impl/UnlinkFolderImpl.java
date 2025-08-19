package com.sos.joc.publish.repository.impl;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.sos.joc.model.publish.git.UnlinkFolderFilter;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.resource.IUnlinkFolder;
import com.sos.joc.publish.repository.util.RepositoryDeleteUtil;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository")
public class UnlinkFolderImpl extends JOCResourceImpl implements IUnlinkFolder {

    private static final String API_CALL = "./inventory/repository/unlink";

    @Override
    public JOCDefaultResponse postUnlink(String xAccessToken, byte[] unlinkFolderFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        boolean permitted = false;
        try {
            unlinkFolderFilter = initLogging(API_CALL, unlinkFolderFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(unlinkFolderFilter, UnlinkFolderFilter.class);
            UnlinkFolderFilter filter = Globals.objectMapper.readValue(unlinkFolderFilter, UnlinkFolderFilter.class);
            
            permitted = GitSemaphore.tryAcquire();
            if (!permitted) {
                throw new JocConcurrentAccessException(GitCommandUtils.CONCURRENT_ACCESS_MESSAGE);
            }
            
            storeAuditLog(filter.getAuditLog());
            final Set<String> permittedFolders = folderPermissions.getListOfFolders().stream().map(Folder::getFolder)
                    .collect(Collectors.toSet());
            if (permittedFolders.contains(filter.getFolder())) {
                Path repositoriesBaseLocal = Globals.sosCockpitProperties.resolvePath("repositories").resolve("local");
                Path repositoriesBaseRollout = Globals.sosCockpitProperties.resolvePath("repositories").resolve("rollout");
                
                hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                InventoryDBLayer dbLayer = new InventoryDBLayer(hibernateSession);
                
                Set<String> folders = new HashSet<String>();
                folders.add(filter.getFolder());
                RepositoryUtil.updateRepoControlledFlag(folders, repositoriesBaseLocal, dbLayer);
                RepositoryUtil.updateRepoControlledFlag(folders, repositoriesBaseRollout, dbLayer);
                if(filter.getDeleteRepository()) {
                    RepositoryDeleteUtil.deleteFolders(folders, repositoriesBaseLocal);
                    RepositoryDeleteUtil.deleteFolders(folders, repositoriesBaseRollout);
                }
            }
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