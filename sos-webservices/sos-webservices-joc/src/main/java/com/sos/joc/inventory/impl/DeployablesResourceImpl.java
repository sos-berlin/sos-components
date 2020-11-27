package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.items.InventoryDeployablesTreeFolderItem;
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
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class DeployablesResourceImpl extends JOCResourceImpl implements IDeployablesResource {

    @Override
    public JOCDefaultResponse deployables(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, DeployablesFilter.class);
            DeployablesFilter in = Globals.objectMapper.readValue(inBytes, DeployablesFilter.class);

            JOCDefaultResponse response = initPermissions(null, getPermissonsJocCockpit("", accessToken).getInventory().getConfigurations().isEdit());

            if (response == null) {
                if (!folderPermissions.isPermittedForFolder(in.getFolder())) {
                    throw new JocFolderPermissionsException("Access denied for folder: " + in.getFolder());
                }
                response = JOCDefaultResponse.responseStatus200(deployables(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseDeployables deployables(DeployablesFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Collection<Integer> deployableTypes = JocInventory.getDeployableTypesWithFolder(in.getObjectTypes());
            Set<ResponseDeployableTreeItem> deployables = new TreeSet<>(Comparator.comparing(ResponseDeployableTreeItem::getFolder).thenComparing(
                    ResponseDeployableTreeItem::getObjectName));
            
            // get deleted folders
            List<String> deletedFolders = dbLayer.getDeletedFolders();
            // get not deleted deployables (only these needs left join with historic table DEP_HISTORY)
            Set<Long> notDeletedIds = dbLayer.getNotDeletedConfigurations(deployableTypes, in.getFolder(), in.getRecursive(), deletedFolders);
            // get deleted deployables outside deleted folders (avoid left join to the historic table DEP_HISTORY)
            if (!in.getWithoutRemovedObjects()) {
                List<DBItemInventoryConfiguration> folders = dbLayer.getFolderContent(in.getFolder(), in.getRecursive(), Arrays.asList(
                        ConfigurationType.FOLDER.intValue()));
                deployables.addAll(getResponseStreamOfDeletedItem(dbLayer.getDeletedConfigurations(deployableTypes, in.getFolder(), in.getRecursive(),
                        deletedFolders), folders, permittedFolders));
            }
            if (in.getWithVersions()) {
                deployables.addAll(getResponseStreamOfNotDeletedItem(dbLayer.getConfigurationsWithAllDeployments(notDeletedIds), in
                        .getOnlyValidObjects(), permittedFolders));
            } else {
                deployables.addAll(getResponseStreamOfNotDeletedItem(dbLayer.getConfigurationsWithMaxDeployment(notDeletedIds), in
                        .getOnlyValidObjects(), permittedFolders));
            }
            ResponseDeployables result = new ResponseDeployables();
            result.setDeliveryDate(Date.from(Instant.now()));
            result.setDeployables(deployables);
            return result;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            return true;
        }
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
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
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (map != null) {
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
                    //.filter(entry -> ConfigurationType.FOLDER.intValue() != entry.getKey().getType())
                    .filter(folderIsNotEmpty)
                    .filter(entry -> !onlyValidObjects || (entry.getValue() != null && entry.getValue().iterator().next() != null) || entry.getKey().getValid())
                    .filter(entry -> folderIsPermitted(entry.getKey().getFolder(), permittedFolders))
                    .map(entry -> {
                        DBItemInventoryConfiguration conf = entry.getKey();
                        Set<InventoryDeploymentItem> deployments = entry.getValue();
                        ResponseDeployableTreeItem treeItem = DeployableResourceImpl.getResponseDeployableTreeItem(conf);
                        if (deployments != null && !deployments.isEmpty()) {
                            Set<ResponseDeployableVersion> versions = new LinkedHashSet<>();
                            if (ConfigurationType.FOLDER.intValue() != conf.getType()) {
                                if (treeItem.getDeployed()) {
                                    treeItem.setDeploymentId(deployments.iterator().next().getId());
                                } else {
                                    if (conf.getValid()) {
                                        ResponseDeployableVersion draft = new ResponseDeployableVersion();
                                        draft.setId(conf.getId());
                                        draft.setVersionDate(conf.getModified());
                                        draft.setVersions(null);
                                        versions.add(draft);
                                    }
                                }
                                versions.addAll(DeployableResourceImpl.getVersions(conf.getId(), deployments));
                                if (versions.isEmpty()) {
                                    versions = null;
                                }
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
    
    private Set<ResponseDeployableTreeItem> getResponseStreamOfNotDeletedItem(List<InventoryDeployablesTreeFolderItem> list,
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (list != null) {
            final Set<String> paths = list.stream().filter(item -> ConfigurationType.FOLDER.intValue() != item.getType()).map(item -> item
                    .getConfiguration().getPath()).collect(Collectors.toSet());
            Predicate<InventoryDeployablesTreeFolderItem> folderIsNotEmpty = item -> {
                if (ConfigurationType.FOLDER.intValue() != item.getConfiguration().getType()) {
                    return true;
                } else {
                    return folderIsNotEmpty(item.getConfiguration().getPath(), paths);
                }
            };
            return list.stream()
                    //.filter(item -> ConfigurationType.FOLDER.intValue() != item.getConfiguration().getType())
                    .filter(folderIsNotEmpty)
                    .filter(item -> !onlyValidObjects || item.getDeployment() != null || item.getConfiguration().getValid())
                    .filter(item -> folderIsPermitted(item.getConfiguration().getFolder(), permittedFolders))
                    .map(item -> {
                        ResponseDeployableTreeItem treeItem = DeployableResourceImpl.getResponseDeployableTreeItem(item.getConfiguration());
                        if (item.getDeployment() != null) {
                            if (treeItem.getDeployed() || !item.getConfiguration().getValid()) {
                                treeItem.setDeploymentId(item.getDeployment().getId());
                            }
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

}
