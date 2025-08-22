package com.sos.joc.publish.repository.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.publish.GitSemaphore;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocGitException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.git.LinkFolderFilter;
import com.sos.joc.publish.repository.git.commands.GitCommandUtils;
import com.sos.joc.publish.repository.resource.ILinkFolder;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository")
public class LinkFolderImpl extends JOCResourceImpl implements ILinkFolder {

    private static final String API_CALL = "./inventory/repository/link";

    @Override
    public JOCDefaultResponse postLink(String xAccessToken, byte[] linkFolderFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        boolean permitted = false;
        try {
            linkFolderFilter = initLogging(API_CALL, linkFolderFilter, xAccessToken, CategoryType.INVENTORY);
            JsonValidator.validate(linkFolderFilter, LinkFolderFilter.class);
            LinkFolderFilter filter = Globals.objectMapper.readValue(linkFolderFilter, LinkFolderFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            permitted = GitSemaphore.tryAcquire();
            if (!permitted) {
                throw new JocConcurrentAccessException(GitCommandUtils.CONCURRENT_ACCESS_MESSAGE);
            }
            
            storeAuditLog(filter.getAuditLog());
            if (folderIsPermitted(filter.getFolder(), folderPermissions.getListOfFolders())) {
                Path repositoriesBaseLocal = Globals.sosCockpitProperties.resolvePath("repositories").resolve("local");
                Path repositoriesBaseRollout = Globals.sosCockpitProperties.resolvePath("repositories").resolve("rollout");
                
                hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                InventoryDBLayer dbLayer = new InventoryDBLayer(hibernateSession);
                
                Set<String> folders = new HashSet<String>();
                folders.add(filter.getFolder());
                boolean folderExists = false;
                if(checkFolderExists(repositoriesBaseLocal, filter.getFolder())) {
                    folderExists = true;
                    RepositoryUtil.updateRepoControlledFlag(folders, repositoriesBaseLocal, dbLayer, true);
                }
                if(checkFolderExists(repositoriesBaseRollout, filter.getFolder())) {
                    folderExists = true;
                    RepositoryUtil.updateRepoControlledFlag(folders, repositoriesBaseRollout, dbLayer, true);
                }
                if(!folderExists) {
                    JocError error = new JocError(String.format(
                        "No cloned git repository found at %1$s. Cannot link folder to cloned repository.",
                        repositoriesBaseLocal.getParent().toString().replace('\\', '/') + "/[LOCAL|ROLLOUT]/" + 
                        (filter.getFolder().startsWith("/") ? filter.getFolder().substring(1): filter.getFolder())
                    ));
                    throw new JocGitException(error);
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
    
    private boolean checkFolderExists(Path repositoriesBase, String folder) {
        Path folderPath = Paths.get(folder);
        if (folderPath.getParent() != null && folderPath.getParent().equals(Paths.get("/"))) {
            // top level folder
            Path pathToCheck = getAbsoluteRepositoryPath(folderPath, repositoriesBase);
            if(Files.exists(pathToCheck)) {
                return true;
            }
        }
        return false;
    }
    
    private Path getAbsoluteRepositoryPath(Path folder, Path repositoryBase) {
        Path relFolder = Paths.get("/").relativize(folder);
        return repositoryBase.resolve(relFolder);
    }
}