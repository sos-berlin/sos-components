package com.sos.joc.inventory.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeploymentItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.IDeployablesResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.deploy.DeployablesFilter;
import com.sos.joc.model.inventory.deploy.ResponseDeployableTreeItem;
import com.sos.joc.model.inventory.deploy.ResponseDeployableVersion;
import com.sos.joc.model.inventory.deploy.ResponseDeployables;
import com.sos.joc.model.tree.Tree;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;

@javax.ws.rs.Path(JocInventory.APPLICATION_PATH)
public class DeployablesResourceImpl extends JOCResourceImpl implements IDeployablesResource {

    @Override
    public JOCDefaultResponse deployables(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_OLD, inBytes, accessToken);
            JsonValidator.validate(inBytes, DeployablesFilter.class);
            DeployablesFilter in = Globals.objectMapper.readValue(inBytes, DeployablesFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());

            if (response == null) {
                if (in.getFolder().isEmpty()) {
                    in.setFolder("/");
                }
                response = JOCDefaultResponse.responseStatus200(deployables(in, false));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse deployablesTree(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, DeployablesFilter.class);
            DeployablesFilter in = Globals.objectMapper.readValue(inBytes, DeployablesFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());

            if (response == null) {
                if (in.getFolder().isEmpty()) {
                    in.setFolder("/");
                }
                response = JOCDefaultResponse.responseStatus200(deployables(in, true));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private ResponseDeployables deployables(DeployablesFilter in, boolean withTree) throws Exception {
        SOSHibernateSession session = null;
        try {
            if (in.getWithoutDeployed() == Boolean.TRUE && in.getWithoutDrafts() == Boolean.TRUE) {
                ResponseDeployables result = new ResponseDeployables();
                result.setDeliveryDate(Date.from(Instant.now()));
                result.setFolders(Collections.emptyList());
                result.setDeployables(Collections.emptySet());
                Path folderPath = Paths.get(in.getFolder());
                result.setName(folderPath.getFileName() == null ? "" : folderPath.getFileName().toString());
                result.setPath(in.getFolder());
                return result;
            }
            
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Set<Integer> deployableTypes = JocInventory.getDeployableTypes(in.getObjectTypes());
            if (in.getRecursive() || (in.getObjectTypes() != null && in.getObjectTypes().contains(ConfigurationType.FOLDER))) {
                deployableTypes.add(ConfigurationType.FOLDER.intValue());
            }
            
            Comparator<ResponseDeployableTreeItem> comp = Comparator.comparing(ResponseDeployableTreeItem::getFolder).thenComparing(
                    ResponseDeployableTreeItem::getObjectName);
            SortedSet<ResponseDeployableTreeItem> deployables = new TreeSet<>(comp);
            
            DBItemInventoryConfiguration folder = dbLayer.getConfiguration(in.getFolder(), ConfigurationType.FOLDER.intValue());
            if (folder != null && folder.getDeleted()) {
                deployables.addAll(getResponseStreamOfDeletedItem(Arrays.asList(folder), Collections.emptyList(), permittedFolders));
            } else {
                // get deleted folders
                List<String> deletedFolders = dbLayer.getDeletedFolders();
                // get deleted deployables outside deleted folders
                if (in.getWithRemovedObjects()) {
                    List<DBItemInventoryConfiguration> folders = dbLayer.getFolderContent(in.getFolder(), in.getRecursive(), Collections.singleton(
                            ConfigurationType.FOLDER.intValue()));
                    deployables.addAll(getResponseStreamOfDeletedItem(dbLayer.getDeletedConfigurations(deployableTypes, in.getFolder(), in
                            .getRecursive(), deletedFolders), folders, permittedFolders));
                }
                deployables.addAll(getResponseStreamOfNotDeletedItem(dbLayer.getConfigurationsWithAllDeployments(deployableTypes, in.getFolder(), in
                        .getRecursive(), deletedFolders), in.getOnlyValidObjects(), permittedFolders, in.getWithoutDrafts(), in.getWithoutDeployed(),
                        in.getLatest()));
            }
            
            final boolean withSync = in.getControllerId() != null && !in.getControllerId().isEmpty();
            final JControllerState currentstate = SyncStateHelper.getControllerState(in.getControllerId(), null, getJocError());
            
            Map<Integer, Map<Long, String>> deployedPaths = getDeployedInventoryPaths(in.getControllerId(), in.getFolder(), in.getRecursive(),
                    deployableTypes, session);

            if (withTree) {
                final Set<String> notPermittedParentFolders = folderPermissions.getNotPermittedParentFolders().getOrDefault("", Collections
                        .emptySet());
                Stream<ResponseDeployableTreeItem> deployablesStream = deployables.stream().filter(item -> !JocInventory.isFolder(item
                        .getObjectType()));
                if (withSync) {
                    deployablesStream = deployablesStream.peek(item -> item.setSyncState(SyncStateHelper.getState(currentstate, item.getId(),
                            item.getObjectType(), deployedPaths.get(item.getObjectType().intValue()))));
                }
                final Map<String, TreeSet<ResponseDeployableTreeItem>> groupedDeployables = deployablesStream.collect(Collectors.groupingBy(
                        ResponseDeployableTreeItem::getFolder, Collectors.toCollection(() -> new TreeSet<>(comp))));

                Path folderPath = Paths.get(in.getFolder());
                SortedSet<ResponseDeployables> responseDeployablesFolder = initTreeByFolder(folderPath, in.getRecursive(), in.getOnlyValidObjects(),
                        deployableTypes, dbLayer).stream().filter(fld -> {
                            boolean isPermittedForFolder = SOSAuthFolderPermissions.isPermittedForFolder(fld.getPath(), permittedFolders);
                            boolean isNotPermittedParentFolder = notPermittedParentFolders.contains(fld.getPath());

                            return isPermittedForFolder || isNotPermittedParentFolder;
                        }).map(t -> {
                            ResponseDeployables r = new ResponseDeployables();
                            r.setPath(t.getPath());
                            r.setDeployables(groupedDeployables.get(t.getPath()));
                            return r;
                        }).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ResponseDeployables::getPath).reversed())));

                ResponseDeployables result = getTree(responseDeployablesFolder, folderPath, in.getRecursive());
                result.setDeliveryDate(Date.from(Instant.now()));
                result.setName(folderPath.getFileName() == null ? "" : folderPath.getFileName().toString());
                return result;
            } else {
                ResponseDeployables result = new ResponseDeployables();
                result.setDeliveryDate(Date.from(Instant.now()));
                if (withSync) {
                    result.setDeployables(deployables.stream().peek(item -> item.setSyncState(SyncStateHelper.getState(currentstate, item.getId(),
                            item.getObjectType(), deployedPaths.get(item.getObjectType().intValue())))).collect(Collectors
                                    .toCollection(() -> new TreeSet<>(comp))));
                } else {
                    result.setDeployables(deployables);
                }
                result.setFolders(null);
                return result;
            }
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Set<ResponseDeployableTreeItem> getResponseStreamOfDeletedItem(List<DBItemInventoryConfiguration> deletedConfs,
            List<DBItemInventoryConfiguration> folders, Set<Folder> permittedFolders) {
        if (deletedConfs != null) {
            Map<String, DBItemInventoryConfiguration> foldersMap = folders.stream().collect(Collectors.toMap(DBItemInventoryConfiguration::getPath,
                    Function.identity()));
            Set<ResponseDeployableTreeItem> items = deletedConfs.stream().filter(item -> folderIsPermitted(item.getFolder(), permittedFolders)).map(
                    item -> DeployableResourceImpl.getResponseDeployableTreeItem(item)).collect(Collectors.toSet());
            // add parent folders
            Set<ResponseDeployableTreeItem> parentFolders = new HashSet<>();
            for (ResponseDeployableTreeItem item : items) {
                if (JocInventory.ROOT_FOLDER.equals(item.getFolder())) {
                    continue;
                }
                Set<String> keys = foldersMap.keySet().stream().filter(key -> (key + "/").startsWith(item.getFolder()) || key.equals(item
                        .getFolder())).collect(Collectors.toSet());
                keys.forEach(key -> parentFolders.add(DeployableResourceImpl.getResponseDeployableTreeItem(foldersMap.remove(key))));
            }
            items.addAll(parentFolders);
            return items;
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<ResponseDeployableTreeItem> getResponseStreamOfNotDeletedItem(Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> map,
            Boolean onlyValidObjects, Set<Folder> permittedFolders, Boolean withoutDrafts, Boolean withoutDeployed, Boolean onlyLatest) {
        if (map != null) {
            Set<DBItemInventoryConfiguration> toRemoves = new HashSet<>();
            
            for (Map.Entry<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> entry : map.entrySet()) {
                if (ConfigurationType.FOLDER.intValue().equals(entry.getKey().getType())) {
                    continue;
                }
                if (onlyValidObjects && !entry.getKey().getValid()) {
                    toRemoves.add(entry.getKey());
                }
                if (withoutDrafts) { // valid drafts which have never been deployed
                    if (entry.getValue() == null || entry.getValue().isEmpty()) {
                        toRemoves.add(entry.getKey());
                    } else if (entry.getValue().stream().filter(Objects::nonNull).count() == 0L) {
                        toRemoves.add(entry.getKey());
                    }
                }
                if (withoutDeployed && (entry.getKey().getDeployed())) {
                    toRemoves.add(entry.getKey());
                }
            }
            
            for (DBItemInventoryConfiguration toRemove : toRemoves) {
                map.remove(toRemove);
            }
            
            final Set<String> paths = map.keySet().stream().filter(item -> ConfigurationType.FOLDER.intValue() != item.getType()).map(item -> item
                    .getPath()).collect(Collectors.toSet());
            Predicate<Map.Entry<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>>> folderIsNotEmpty = entry -> {
                if (ConfigurationType.FOLDER.intValue() != entry.getKey().getType()) {
                    return true;
                } else {
                    return folderIsNotEmpty(entry.getKey().getPath(), paths);
                }
            };
            return map.entrySet().stream()
                    .filter(folderIsNotEmpty)
                    .filter(entry -> folderIsPermitted(entry.getKey().getFolder(), permittedFolders))
                    .map(entry -> {
                        DBItemInventoryConfiguration conf = entry.getKey();
                        Set<InventoryDeploymentItem> deployments = entry.getValue();
                        ResponseDeployableTreeItem treeItem = DeployableResourceImpl.getResponseDeployableTreeItem(conf);
                        if (deployments != null && !deployments.isEmpty()) {
                            Set<ResponseDeployableVersion> versions = new LinkedHashSet<>();
                            if (ConfigurationType.FOLDER.intValue() != conf.getType()) {
                                if (!treeItem.getDeployed() && conf.getValid() && !withoutDrafts) {
                                    ResponseDeployableVersion draft = new ResponseDeployableVersion();
                                    draft.setId(conf.getId());
                                    draft.setVersionDate(conf.getModified());
                                    draft.setVersions(null);
                                    versions.add(draft);
                                }
                                versions.addAll(DeployableResourceImpl.getVersions(conf.getId(), deployments, withoutDeployed, onlyLatest));
//                                if (versions.isEmpty()) {
//                                    versions = null;
//                                }
                            } else {
                                versions = null;
                            }
                            treeItem.setDeployablesVersions(versions);
                        }
                        return treeItem;
                    })
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
    
    private static boolean folderIsNotEmpty(String folder, Set<String> paths) {
        Predicate<String> filter = f -> f.startsWith((folder + "/").replaceAll("//+", "/"));
        return paths.stream().parallel().anyMatch(filter);
    }
    
    public static SortedSet<Tree> initTreeByFolder(Path path, Boolean recursive, Boolean onlyValidObjects, Set<Integer> deployableTypes,
            InventoryDBLayer dbLayer) throws JocException {
        Comparator<Tree> comparator = Comparator.comparing(Tree::getPath).reversed();
        SortedSet<Tree> folders = new TreeSet<>(comparator);
        Set<Tree> results = dbLayer.getFoldersByFolderAndTypeForInventory(path.toString().replace('\\', '/'), deployableTypes, onlyValidObjects);
        final int parentDepth = path.getNameCount();
        if (results != null && !results.isEmpty()) {
            if (recursive != null && recursive) {
                folders.addAll(results);
            } else {
                folders.addAll(results.stream().filter(item -> Paths.get(item.getPath()).getNameCount() <= parentDepth + 1).collect(Collectors
                        .toSet()));
            }
        }
        return folders;
    }
    
    private static ResponseDeployables getTree(SortedSet<ResponseDeployables> folders, Path startFolder, Boolean recursive) {
        Map<Path, ResponseDeployables> treeMap = new HashMap<>();
        for (ResponseDeployables folder : folders) {

            Path pFolder = Paths.get(folder.getPath());
            ResponseDeployables tree = null;
            if (treeMap.containsKey(pFolder)) {
                tree = treeMap.get(pFolder);
                tree.setDeployables(folder.getDeployables());
                if (recursive != null && recursive) {
                    tree.getFolders().removeIf(child -> (child.getFolders() == null || child.getFolders().isEmpty()) && (child
                            .getDeployables() == null || child.getDeployables().isEmpty()));
                }
            } else {
                tree = folder;
                tree.setFolders(Collections.emptyList());
                tree.setName(pFolder.getFileName() == null ? "" : pFolder.getFileName().toString());
                treeMap.put(pFolder, tree);
            }
            fillTreeMap(treeMap, pFolder, tree);
        }
        if (treeMap.isEmpty()) {
            return new ResponseDeployables();
        }
        return treeMap.get(startFolder);
    }
    
    private static void fillTreeMap(Map<Path, ResponseDeployables> treeMap, Path folder, ResponseDeployables tree) {
        Path parent = folder.getParent();
        if (parent != null) {
            ResponseDeployables parentTree = null;
            if (treeMap.containsKey(parent)) {
                parentTree = treeMap.get(parent);
                List<ResponseDeployables> treeList = parentTree.getFolders();
                if (treeList == null) {
                    treeList = new ArrayList<>();
                    treeList.add(tree);
                    parentTree.setFolders(treeList);
                } else {
                    if (treeList.contains(tree)) {
                        treeList.remove(tree);
                    }
                    treeList.add(0, tree);
                }
            } else {
                parentTree = new ResponseDeployables();
                parentTree.setPath(parent.toString().replace('\\', '/'));
                List<ResponseDeployables> treeList = new ArrayList<>();
                treeList.add(tree);
                parentTree.setFolders(treeList);
                treeMap.put(parent, parentTree);
            }
            fillTreeMap(treeMap, parent, parentTree);
        }
    }
    
    private Map<Integer, Map<Long, String>> getDeployedInventoryPaths(String controllerId, String folder, Boolean recursive, Set<Integer> types, SOSHibernateSession session) {
        if (controllerId != null && !controllerId.isEmpty()) {
            DeployedConfigurationDBLayer deployedDbLayer = new DeployedConfigurationDBLayer(session);
            DeployedConfigurationFilter deployedFilter = new DeployedConfigurationFilter();
            deployedFilter.setControllerId(controllerId);
            Folder fld = new Folder();
            fld.setFolder(folder);
            fld.setRecursive(recursive);
            deployedFilter.setFolders(Collections.singleton(fld));
            deployedFilter.setObjectTypes(types);
            return deployedDbLayer.getDeployedNames(deployedFilter);
        }
        return Collections.emptyMap();
    }

}
