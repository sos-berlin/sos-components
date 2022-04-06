package com.sos.joc.publish.repository.impl;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.repository.DeleteFromFilter;
import com.sos.joc.publish.repository.resource.IRepositoryDelete;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@javax.ws.rs.Path("inventory/repository")
public class RepositoryDeleteImpl extends JOCResourceImpl implements IRepositoryDelete {

    private static final String API_CALL = "./inventory/repository/delete";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDeleteImpl.class);

    @Override
    public JOCDefaultResponse postDelete(String xAccessToken, byte[] deleteFromFilter) throws Exception {
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

            storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
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
            deleteFolders(filter, repositoriesBase);
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** delete from repository finished ***" + apiCallFinished);
            LOGGER.trace("complete WS time : " + (apiCallFinished.getTime() - started.getTime()) + " ms");
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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
    
    private static void deleteFolders(DeleteFromFilter filter, Path repositoriesBase) {
        Set<Configuration> folders = filter.getConfigurations().stream()
                .filter(cfg -> ConfigurationType.FOLDER.equals(cfg.getConfiguration().getObjectType()))
                .map(config -> config.getConfiguration()).collect(Collectors.toSet());
        folders.forEach(folder -> {
            try {
                Path relFolder = Paths.get("/").relativize(Paths.get(folder.getPath()));
                Path pathToDelete = repositoriesBase.resolve(relFolder);
                LOGGER.info("resolved path: " + pathToDelete.toString());
                deleteFolders(pathToDelete);
                LOGGER.debug(String.format("Folder %1$s has been deleted.", folder.getPath()));
            } catch (IOException e) {
                LOGGER.debug(String.format("Folder - %1$s - could not be deleted!", folder.getPath()), e);
            }
        });
    }
    
    private static void deleteFolders(Path path) throws IOException {
        Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
              });
    }
}