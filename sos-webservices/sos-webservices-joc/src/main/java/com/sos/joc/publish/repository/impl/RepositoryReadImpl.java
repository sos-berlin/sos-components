package com.sos.joc.publish.repository.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.publish.repository.ReadFromFilter;
import com.sos.joc.model.publish.repository.ResponseFolder;
import com.sos.joc.model.publish.repository.ResponseFolderItem;
import com.sos.joc.publish.repository.resource.IRepositoryRead;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("inventory/repository")
public class RepositoryReadImpl extends JOCResourceImpl implements IRepositoryRead{

    private static final String API_CALL = "./inventory/repository/read";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryReadImpl.class);

    @Override
    public JOCDefaultResponse postRead(String xAccessToken, byte[] readFromFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            Date started = Date.from(Instant.now());
            LOGGER.trace("*** read from repository started ***" + started);
            initLogging(API_CALL, readFromFilter, xAccessToken);
            JsonValidator.validate(readFromFilter, ReadFromFilter.class);
            ReadFromFilter filter = Globals.objectMapper.readValue(readFromFilter, ReadFromFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Path repositoriesBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            Path repo = null;
            if(filter.getFolder().startsWith("/")) {
                repo = repositoriesBase.resolve(filter.getFolder().substring(1));
            } else {
                repo = repositoriesBase.resolve(filter.getFolder());
            }
            final Path repository = repo;
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            boolean isPermittedForFolder = SOSAuthFolderPermissions.isPermittedForFolder(Globals.normalizePath(repository.toString()), permittedFolders);
            if(!isPermittedForFolder) {
                throw new JocFolderPermissionsException();
            }
            ResponseFolder result = getResponseFolder(repository, repositoriesBase, filter.getRecursive());
            Date apiCallFinished = Date.from(Instant.now());
            LOGGER.trace("*** read from repository finished ***" + apiCallFinished);
            LOGGER.trace("complete WS time : " + (apiCallFinished.getTime() - started.getTime()) + " ms");
            return JOCDefaultResponse.responseStatus200(result);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private ResponseFolder getResponseFolder (Path repository, Path repositoryBase, boolean recursive) throws Exception {
        TreeSet<Path> repoTree = RepositoryUtil.readRepositoryAsTreeSet(repository);
        Set<ResponseFolderItem> responseFolderItems = repoTree.stream().filter(path -> Files.isRegularFile(path))
                .map(path -> RepositoryUtil.getResponseFolderItem(repositoryBase, path)).collect(Collectors.toSet());
        final Map<String, Set<ResponseFolderItem>> groupedFolderItems = responseFolderItems.stream().collect(Collectors.groupingBy(
                ResponseFolderItem::getFolder, Collectors.toSet()));
        SortedSet<ResponseFolder> responseFolder = RepositoryUtil.initTreeByFolder(repository, recursive).stream().map(t -> {
                    ResponseFolder r = new ResponseFolder();
                    String path = Globals.normalizePath(RepositoryUtil.subPath(repositoryBase, Paths.get(t.getPath())).toString());
                    r.setLastModified(RepositoryUtil.getLastModified(Paths.get(t.getPath())));
                    r.setPath(path);
                    if (groupedFolderItems.containsKey(path)) {
                        r.getItems().addAll(groupedFolderItems.get(path));
                    }
                    return r;
                }).filter(folder -> !folder.getPath().contains(".git")).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ResponseFolder::getPath).reversed())));
        return RepositoryUtil.getTree(responseFolder, RepositoryUtil.subPath(repositoryBase, repository), 
                RepositoryUtil.subPath(repositoryBase, repositoryBase), recursive);
    }
    
    private static String getSubrepositoryFromFilter (ReadFromFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }
}
