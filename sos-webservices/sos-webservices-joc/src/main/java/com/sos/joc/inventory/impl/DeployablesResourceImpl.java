package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
            deployables.addAll(getResponseStreamOfDeletedItem(dbLayer.getDeletedConfigurations(deployableTypes, in.getFolder(), in.getRecursive(), deletedFolders),
                    permittedFolders));
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
    
    private Set<ResponseDeployableTreeItem> getResponseStreamOfDeletedItem(List<DBItemInventoryConfiguration> deletedConfs, Set<Folder> permittedFolders) {
        if (deletedConfs != null) {
            return deletedConfs.stream()
                    .filter(item -> folderIsPermitted(item.getFolder(), permittedFolders))
                    .map(item -> DeployableResourceImpl.getResponseDeployableTreeItem(item))
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<ResponseDeployableTreeItem> getResponseStreamOfNotDeletedItem(Map<DBItemInventoryConfiguration, Set<InventoryDeploymentItem>> map,
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (map != null) {
            final Set<String> paths = map.keySet().stream().map(item -> item.getPath()).collect(Collectors.toSet());
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
                            versions.addAll(DeployableResourceImpl.getVersions(deployments));
                            if (versions.isEmpty()) {
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
    
    private Set<ResponseDeployableTreeItem> getResponseStreamOfNotDeletedItem(List<InventoryDeployablesTreeFolderItem> list,
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (list != null) {
            final Set<String> paths = list.stream().map(item -> item.getConfiguration().getPath()).collect(Collectors.toSet());
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

//    public ResponseDeployables getDeployables(InventoryDBLayer dbLayer, List<InventoryDeployablesTreeFolderItem> list, String folder,
//            boolean addVersions) throws Exception {
//        ResponseDeployables result = new ResponseDeployables();
//        if (list == null || list.size() == 0) {
//            result.setDeliveryDate(Date.from(Instant.now()));
//            result.setDeployables(Collections.emptySet());
//            return result;
//        }
//
//        Long configId = 0L;
//        ResponseDeployableTreeItem treeItem = null;
//        for (InventoryDeployablesTreeFolderItem item : list) {
//            DBItemInventoryConfiguration conf = item.getConfiguration();
//            if (configId.equals(conf.getId())) {
//                continue;
//            }
//
//            ConfigurationType type = JocInventory.getType(conf.getType());
//            if (ConfigurationType.FOLDER.equals(type)) {
//                
//                    if (folder.equals(conf.getFolder())) {  // Hä?
//                        continue;
//                    }
//                    // TODO reduce check isFolderEmpty
//                    if (!conf.getDeleted() && isFolderEmpty(dbLayer, conf.getFolder())) {
//                        continue;
//                    }
//            } else if (item.getDeployment() == null) {
//                if (!conf.getValid()) {
//                    continue;
//                }
//            }
//
//            if (folderPermissions != null && !folderPermissions.isPermittedForFolder(getParent(conf.getPath()))) {
//                LOGGER.info(String.format("[skip][%s]due to folder permissions", conf.getPath()));
//                continue;
//            }
//
//            treeItem = new ResponseDeployableTreeItem();
//            treeItem.setId(conf.getId());
//            treeItem.setFolder(conf.getFolder());
//            treeItem.setObjectName(conf.getName());
//            treeItem.setObjectType(JocInventory.getType(conf.getType()));
//            treeItem.setDeleted(conf.getDeleted());
//            treeItem.setDeployed(conf.getDeployed());
//
//            if (conf.getDeleted()) {
//                addVersions = false;
//                item.setDeployment(null);
//            }
//
//            if (item.getDeployment() != null) {
//                if (addVersions) {
//                    
//                    List<InventoryDeployablesTreeFolderItem> deployments = getDeployments(list, treeItem.getId());
//
//                    Collections.sort(deployments, new Comparator<InventoryDeployablesTreeFolderItem>() {
//
//                        public int compare(InventoryDeployablesTreeFolderItem d1, InventoryDeployablesTreeFolderItem d2) {// deploymentDate descending
//                            return d2.getDeployment().getDeploymentDate().compareTo(d1.getDeployment().getDeploymentDate());
//                        }
//                    });
//
//                    if (treeItem.getDeployed()) {
//                        treeItem.setDeploymentId(deployments.get(0).getDeployment().getId());
//                    } else {
//                        if (conf.getValid()) {
//                            ResponseDeployableVersion draft = new ResponseDeployableVersion();
//                            draft.setId(conf.getId());
//                            draft.setVersionDate(conf.getModified());
//                            draft.setVersions(null);
//                            treeItem.getDeployablesVersions().add(draft);
//                        }
//                    }
//
//                    Date date = null;
//                    ResponseDeployableVersion dv = null;
//                    for (InventoryDeployablesTreeFolderItem deployment : deployments) {
//                        if (date == null || !date.equals(deployment.getDeployment().getDeploymentDate())) {
//                            dv = new ResponseDeployableVersion();
//                            dv.setId(deployment.getId());
//                            dv.setVersions(new LinkedHashSet<ResponseItemDeployment>());
//                            dv.setVersionDate(deployment.getDeployment().getDeploymentDate());
//                            dv.setDeploymentId(deployment.getDeployment().getId());
//                            dv.setDeploymentOperation(OperationType.fromValue(deployment.getDeployment().getOperation()).name().toLowerCase());
//                            if (!conf.getPath().equals(deployment.getDeployment().getPath())) {
//                                dv.setDeploymentPath(deployment.getDeployment().getPath());
//                            }
//                            treeItem.getDeployablesVersions().add(dv);
//                        }
//                        ResponseItemDeployment id = new ResponseItemDeployment();
//                        id.setVersion(deployment.getDeployment().getVersion());
//                        id.setControllerId(deployment.getDeployment().getControllerId());
//                        dv.getVersions().add(id);
//
//                        date = deployment.getDeployment().getDeploymentDate();
//                    }
//                } else {
//                    if (treeItem.getDeployed() || !conf.getValid()) {
//                        treeItem.setDeploymentId(item.getDeployment().getId());
//                    }
//                }
//            }
//            if (!addVersions || item.getDeployment() == null) {
//                treeItem.setDeployablesVersions(null);
//            }
//            result.getDeployables().add(treeItem);
//            configId = conf.getId();
//        }
//        result.setDeployables(sort(result.getDeployables()));
//        result.setDeliveryDate(new Date());
//        return result;
//    }

//    private boolean isFolderEmpty(InventoryDBLayer dbLayer, String folder) {
//        Long result = null;
//        try {
//            dbLayer.getSession().beginTransaction();
//            result = dbLayer.getCountConfigurationsByFolder(folder, true);
//            dbLayer.getSession().commit();
//        } catch (SOSHibernateException e) {
//            try {
//                dbLayer.getSession().rollback();
//            } catch (SOSHibernateException e1) {
//            }
//        }
//        return result == null || result.equals(0L);
//    }

//    private Set<ResponseDeployableTreeItem> sort(Set<ResponseDeployableTreeItem> set) {
//        if (set == null || set.size() == 0) {
//            return set;
//        }
//        return set.stream().sorted(Comparator.comparing(ResponseDeployableTreeItem::getObjectName)).collect(Collectors.toCollection(
//                LinkedHashSet::new));
//    }

//    private List<InventoryDeployablesTreeFolderItem> getDeployments(List<InventoryDeployablesTreeFolderItem> items, final Long configId) {
//        return items.stream().filter(config -> config.getConfiguration().getId().equals(configId) && config.getDeployment() != null).collect(Collectors.toList());
//    }

}
