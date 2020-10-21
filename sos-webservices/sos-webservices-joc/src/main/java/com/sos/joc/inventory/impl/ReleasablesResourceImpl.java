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
import com.sos.joc.db.inventory.items.InventoryReleasablesTreeFolderItem;
import com.sos.joc.db.inventory.items.InventoryReleaseItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.IReleasablesResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.deploy.DeployablesFilter;
import com.sos.joc.model.inventory.release.ResponseReleasableTreeItem;
import com.sos.joc.model.inventory.release.ResponseReleasableVersion;
import com.sos.joc.model.inventory.release.ResponseReleasables;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReleasablesResourceImpl extends JOCResourceImpl implements IReleasablesResource {

    @Override
    public JOCDefaultResponse releasables(final String accessToken, final byte[] inBytes) {
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
                response = JOCDefaultResponse.responseStatus200(releasables(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private ResponseReleasables releasables(DeployablesFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            Collection<Integer> releasableTypes = JocInventory.getReleasableTypesWithFolder(in.getObjectTypes());
            Set<ResponseReleasableTreeItem> releasables = new TreeSet<>(Comparator.comparing(ResponseReleasableTreeItem::getFolder).thenComparing(
                    ResponseReleasableTreeItem::getObjectName));
            
            // get deleted folders
            List<String> deletedFolders = dbLayer.getDeletedFolders();
            // get not deleted deployables (only these needs left join with historic table DEP_HISTORY)
            Set<Long> notDeletedIds = dbLayer.getNotDeletedConfigurations(releasableTypes, in.getFolder(), in.getRecursive(), deletedFolders);
            // get deleted deployables outside deleted folders (avoid left join to the historic table DEP_HISTORY)
            releasables.addAll(getResponseStreamOfDeletedItem(dbLayer.getDeletedConfigurations(releasableTypes, in.getFolder(), in.getRecursive(), deletedFolders),
                    permittedFolders));
            if (in.getWithVersions()) {
                releasables.addAll(getResponseStreamOfNotDeletedItem(dbLayer.getConfigurationsWithAllReleases(notDeletedIds), in
                        .getOnlyValidObjects(), permittedFolders));
            } else {
                releasables.addAll(getResponseStreamOfNotDeletedItem(dbLayer.getConfigurationsWithMaxRelease(notDeletedIds), in
                        .getOnlyValidObjects(), permittedFolders));
            }
            ResponseReleasables result = new ResponseReleasables();
            result.setDeliveryDate(Date.from(Instant.now()));
            result.setReleasables(releasables);
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
    
    private Set<ResponseReleasableTreeItem> getResponseStreamOfDeletedItem(List<DBItemInventoryConfiguration> deletedConfs, Set<Folder> permittedFolders) {
        if (deletedConfs != null) {
            return deletedConfs.stream()
                    .filter(item -> folderIsPermitted(item.getFolder(), permittedFolders))
                    .map(item -> ReleasableResourceImpl.getResponseReleasableTreeItem(item))
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<ResponseReleasableTreeItem> getResponseStreamOfNotDeletedItem(Map<DBItemInventoryConfiguration, Set<InventoryReleaseItem>> map,
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (map != null) {
            final Set<String> paths = map.keySet().stream().map(item -> item.getPath()).collect(Collectors.toSet());
            Predicate<Map.Entry<DBItemInventoryConfiguration, Set<InventoryReleaseItem>>> folderIsNotEmpty = entry -> {
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
                        Set<InventoryReleaseItem> releases = entry.getValue();
                        ResponseReleasableTreeItem treeItem = ReleasableResourceImpl.getResponseReleasableTreeItem(conf);
                        if (releases != null && !releases.isEmpty()) {
                            Set<ResponseReleasableVersion> versions = new LinkedHashSet<>();
                            if (treeItem.getReleased()) {
                                treeItem.setReleaseId(releases.iterator().next().getId());
                            } else {
                                if (conf.getValid()) {
                                    ResponseReleasableVersion draft = new ResponseReleasableVersion();
                                    draft.setId(conf.getId());
                                    draft.setVersionDate(conf.getModified());
                                    draft.setVersions(null);
                                    versions.add(draft);
                                }
                            }
                            versions.addAll(ReleasableResourceImpl.getVersions(conf.getId(), releases));
                            if (versions.isEmpty()) {
                                versions = null;
                            }
                            treeItem.setReleaseVersions(versions);
                        }
                        return treeItem;
                    })
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<ResponseReleasableTreeItem> getResponseStreamOfNotDeletedItem(List<InventoryReleasablesTreeFolderItem> list,
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (list != null) {
            final Set<String> paths = list.stream().map(item -> item.getConfiguration().getPath()).collect(Collectors.toSet());
            Predicate<InventoryReleasablesTreeFolderItem> folderIsNotEmpty = item -> {
              if (ConfigurationType.FOLDER.intValue() != item.getConfiguration().getType()) {
                  return true;
              } else {
                  return folderIsNotEmpty(item.getConfiguration().getPath(), paths);
              }
            };
            return list.stream()
                    //.filter(item -> ConfigurationType.FOLDER.intValue() != item.getConfiguration().getType())
                    .filter(folderIsNotEmpty)
                    .filter(item -> !onlyValidObjects || item.getRelease() != null || item.getConfiguration().getValid())
                    .filter(item -> folderIsPermitted(item.getConfiguration().getFolder(), permittedFolders))
                    .map(item -> {
                        ResponseReleasableTreeItem treeItem = ReleasableResourceImpl.getResponseReleasableTreeItem(item.getConfiguration());
                        if (item.getRelease() != null) {
                            if (treeItem.getReleased() || !item.getConfiguration().getValid()) {
                                treeItem.setReleaseId(item.getRelease().getId());
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
