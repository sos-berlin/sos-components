package com.sos.joc.publish.repository.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.repository.DeleteFromFilter;

public class RepositoryDeleteUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDeleteUtil.class);

    // TODO: check if should return stream instead of collection
    public static Set<Configuration> deleteFolders(DeleteFromFilter filter, Path repositoriesBase) {
        Set<String> folders = filter.getConfigurations().stream()
                .filter(cfg -> ConfigurationType.FOLDER.equals(cfg.getConfiguration().getObjectType()))
                .map(config -> config.getConfiguration()).map(Configuration::getPath).collect(Collectors.toSet());
        Set<String> deletedFolders = deleteFolders(folders, repositoriesBase);
        return filter.getConfigurations().stream().map(Config::getConfiguration).filter(item -> deletedFolders.contains(item.getPath()))
                .collect(Collectors.toSet());
    }
    
    public static Set<String> deleteFolders(Set<String> folders, Path repositoriesBase) {
        folders.forEach(folder -> {
            try {
                Path relFolder = Paths.get("/").relativize(Paths.get(folder));
                Path pathToDelete = repositoriesBase.resolve(relFolder);
                LOGGER.info("resolved path: " + pathToDelete.toString());
                if(Files.exists(pathToDelete)) {
                    deleteFolders(pathToDelete);
                }
                LOGGER.debug(String.format("Folder %1$s has been deleted.", folder));
            } catch (IOException e) {
                LOGGER.debug(String.format("Folder - %1$s - could not be deleted!", folder), e);
            }
        });
        return folders;
    }
    
    private static void deleteFolders(Path path) throws IOException {
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile)
        .forEach(file -> {
            try {
                file.setWritable(true);
                file.delete();
            } catch (Exception e) {
                LOGGER.debug("could not delete item with path: " + file.toString(), e);
            }
        });
    }
    
}
