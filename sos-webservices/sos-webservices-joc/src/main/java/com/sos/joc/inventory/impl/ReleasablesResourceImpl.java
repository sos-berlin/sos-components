package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.inventory.resource.IReleasablesResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.deploy.DeployablesFilter;
import com.sos.joc.model.inventory.release.ResponseReleasableTreeItem;
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

            Collection<Integer> releasableTypes = JocInventory.getReleasableTypes(in.getObjectTypes());
            if (in.getRecursive() || (in.getObjectTypes() != null && in.getObjectTypes().contains(ConfigurationType.FOLDER))) {
                releasableTypes.add(ConfigurationType.FOLDER.intValue());
            }
            Set<ResponseReleasableTreeItem> releasables = new TreeSet<>(Comparator.comparing(ResponseReleasableTreeItem::getFolder).thenComparing(
                    ResponseReleasableTreeItem::getObjectName));
            
            DBItemInventoryConfiguration folder = dbLayer.getConfiguration(in.getFolder(), ConfigurationType.FOLDER.intValue());
            if (folder != null && folder.getDeleted()) {
                releasables.addAll(getResponseStreamOfDeletedItem(Arrays.asList(folder), Collections.emptyList(), permittedFolders));
            } else {
                // get deleted folders
                List<String> deletedFolders = dbLayer.getDeletedFolders();
                // get not deleted deployables (only these needs left join with historic table DEP_HISTORY)
                Set<Long> notDeletedIds = dbLayer.getNotDeletedConfigurations(releasableTypes, in.getFolder(), in.getRecursive(), deletedFolders);
                // get deleted deployables outside deleted folders (avoid left join to the historic table DEP_HISTORY)
                if (!in.getWithoutRemovedObjects()) {
                    List<DBItemInventoryConfiguration> folders = dbLayer.getFolderContent(in.getFolder(), in.getRecursive(), Arrays.asList(
                            ConfigurationType.FOLDER.intValue()));
                    releasables.addAll(getResponseStreamOfDeletedItem(dbLayer.getDeletedConfigurations(releasableTypes, in.getFolder(), in
                            .getRecursive(), deletedFolders), folders, permittedFolders));
                }
                releasables.addAll(getResponseStreamOfNotDeletedItem(dbLayer.getConfigurations(notDeletedIds), in.getOnlyValidObjects(),
                        permittedFolders));
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
    
    private Set<ResponseReleasableTreeItem> getResponseStreamOfDeletedItem(List<DBItemInventoryConfiguration> deletedConfs,
            List<DBItemInventoryConfiguration> folders, Set<Folder> permittedFolders) {
        if (deletedConfs != null) {
            Map<String, DBItemInventoryConfiguration> foldersMap = folders.stream().collect(Collectors.toMap(DBItemInventoryConfiguration::getPath,
                    Function.identity()));
            Set<ResponseReleasableTreeItem> items = deletedConfs.stream().filter(item -> folderIsPermitted(item.getFolder(), permittedFolders)).map(
                    item -> ReleasableResourceImpl.getResponseReleasableTreeItem(item)).collect(Collectors.toSet());
            // add parent folders
            Set<ResponseReleasableTreeItem> parentFolders = new HashSet<>();
            for (ResponseReleasableTreeItem item : items) {
                if (JocInventory.ROOT_FOLDER.equals(item.getFolder())) {
                    continue;
                }
                Set<String> keys = foldersMap.keySet().stream().filter(key -> (key + "/").startsWith(item.getFolder()) || key.equals(item
                        .getFolder())).collect(Collectors.toSet());
                keys.forEach(key -> parentFolders.add(ReleasableResourceImpl.getResponseReleasableTreeItem(foldersMap.remove(key))));
            }
            items.addAll(parentFolders);
            return items;
        } else {
            return Collections.emptySet();
        }
    }
    
    private Set<ResponseReleasableTreeItem> getResponseStreamOfNotDeletedItem(List<DBItemInventoryConfiguration> list,
            Boolean onlyValidObjects, Set<Folder> permittedFolders) {
        if (list != null) {
            final Set<String> paths = list.stream().filter(item -> ConfigurationType.FOLDER.intValue() != item.getType()).map(item -> item.getPath())
                    .collect(Collectors.toSet());
            Predicate<DBItemInventoryConfiguration> folderIsNotEmpty = item -> {
                if (ConfigurationType.FOLDER.intValue() != item.getType()) {
                    return true;
                } else {
                    return folderIsNotEmpty(item.getPath(), paths);
                }
            };
            return list.stream()
                    //.filter(item -> ConfigurationType.FOLDER.intValue() != item.getConfiguration().getType())
                    .filter(folderIsNotEmpty)
                    .filter(item -> !item.getReleased())
                    .filter(item -> !onlyValidObjects || item.getDeleted() || item.getValid())
                    .filter(item -> folderIsPermitted(item.getFolder(), permittedFolders))
                    .map(item -> {
                        ResponseReleasableTreeItem treeItem = ReleasableResourceImpl.getResponseReleasableTreeItem(item);
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
