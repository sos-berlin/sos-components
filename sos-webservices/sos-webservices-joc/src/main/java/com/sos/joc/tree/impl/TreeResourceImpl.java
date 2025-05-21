package com.sos.joc.tree.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.tree.TreePermanent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.tree.TreeFilter;
import com.sos.joc.model.tree.TreeType;
import com.sos.joc.model.tree.TreeView;
import com.sos.joc.tree.resource.ITreeResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("tree")
public class TreeResourceImpl extends JOCResourceImpl implements ITreeResource {

    private static final String API_CALL = "./tree";

    @Override
    public JOCDefaultResponse postTree(String accessToken, byte[] treeBodyBytes) {
        try {
            treeBodyBytes = initLogging(API_CALL, treeBodyBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(treeBodyBytes, TreeFilter.class);
            TreeFilter treeBody = Globals.objectMapper.readValue(treeBodyBytes, TreeFilter.class);

            boolean treeForInventoryTrash = treeBody.getForInventoryTrash() == Boolean.TRUE;
            boolean treeForInventory = !treeForInventoryTrash && ((treeBody.getForInventory() != null && treeBody.getForInventory()) || (treeBody
                    .getTypes() != null && treeBody.getTypes().contains(TreeType.INVENTORY)));
            boolean treeForDescriptorsTrash = treeBody.getForDescriptorsTrash() == Boolean.TRUE;
            boolean treeForDescriptors = treeBody.getForDescriptors() == Boolean.TRUE;
            
            String controllerId = (treeForInventory || treeForInventoryTrash) ? "" : treeBody.getControllerId();
            List<TreeType> types = TreePermanent.getAllowedTypes(treeBody.getTypes(), getBasicJocPermissions(accessToken), getBasicControllerPermissions(
                    controllerId, accessToken), treeForInventory, treeForInventoryTrash, treeForDescriptors, treeForDescriptorsTrash);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, types.size() > 0);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            treeBody.setTypes(types);
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                checkFoldersFilterParam(treeBody.getFolders());
            }
            SortedSet<Tree> folders = Collections.emptySortedSet();
            if (treeForInventory) {
                folders = TreePermanent.initFoldersByFoldersForInventory(treeBody);
            } else if (treeForInventoryTrash) {
                folders = TreePermanent.initFoldersByFoldersForInventoryTrash(treeBody);
            } else if (treeForDescriptors) {
                folders = TreePermanent.initFoldersByFoldersForDescriptors(treeBody);
            } else if (treeForDescriptorsTrash) {
                folders = TreePermanent.initFoldersByFoldersForDescriptorsTrash(treeBody);
            } else {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
            }

            Tree root = TreePermanent.getTree(folders, controllerId, folderPermissions);
            TreeView entity = new TreeView();
            if (root != null) {
                entity.getFolders().add(root);
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
//    @Override
//    public JOCDefaultResponse postTagTree(String accessToken, byte[] treeBodyBytes) {
//        try {
//            initLogging(API_CALL, treeBodyBytes, accessToken);
//            JsonValidator.validateFailFast(treeBodyBytes, TagTreeFilter.class);
//            TagTreeFilter treeBody = Globals.objectMapper.readValue(treeBodyBytes, TagTreeFilter.class);
//
//            boolean treeForInventoryTrash = treeBody.getForInventoryTrash() == Boolean.TRUE;
//            boolean treeForInventory = !treeForInventoryTrash && ((treeBody.getForInventory() != null && treeBody.getForInventory()) || (treeBody
//                    .getTypes() != null && treeBody.getTypes().contains(TreeType.INVENTORY)));
//            boolean treeForDescriptorsTrash = treeBody.getForDescriptorsTrash() == Boolean.TRUE;
//            boolean treeForDescriptors = treeBody.getForDescriptors() == Boolean.TRUE;
//            
//            String controllerId = (treeForInventory || treeForInventoryTrash) ? "" : treeBody.getControllerId();
//            List<TreeType> types = TreePermanent.getAllowedTypes(treeBody.getTypes(), getJocPermissions(accessToken), getControllerPermissions(
//                    controllerId, accessToken), treeForInventory, treeForInventoryTrash, treeForDescriptors, treeForDescriptorsTrash);
//            
//            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, types.size() > 0);
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//            
//            treeBody.setTypes(types);
//            if (treeBody.getTags() != null && !treeBody.getTags().isEmpty()) {
//                checkTagsFilterParam(treeBody.getTags());
//            }
//            Tree root = null;
//            if (treeForInventory) {
//                root = TreePermanent.initFoldersByTagsForInventory(treeBody, folderPermissions);
//            } else if (treeForInventoryTrash) {
//                root = TreePermanent.initFoldersByTagsForInventoryTrash(treeBody, folderPermissions);
//            } else if (treeForDescriptors) {
//                root = TreePermanent.initFoldersByTagsForDescriptors(treeBody, folderPermissions);
//            } else if (treeForDescriptorsTrash) {
//                root = TreePermanent.initFoldersByTagsForDescriptorTrash(treeBody, folderPermissions);
//            } else {
//                root = TreePermanent.initFoldersByTagsForViews(treeBody, folderPermissions);
//            }
//
//            TreeView entity = new TreeView();
//            if (root != null) {
//                entity.getFolders().add(root);
//            }
//            entity.setDeliveryDate(Date.from(Instant.now()));
//            return JOCDefaultResponse.responseStatus200(entity);
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        }
//    }

    private void checkFoldersFilterParam(List<Folder> folders) throws Exception {
        if (folders != null && !folders.isEmpty() && folders.stream().parallel().anyMatch(folder -> folder.getFolder() == null || folder.getFolder()
                .isEmpty())) {
            throw new JocMissingRequiredParameterException("undefined 'folder'");
        }

    }
    
//    private void checkTagsFilterParam(List<Tag> tags) throws Exception {
//        if (tags != null && !tags.isEmpty() && tags.stream().parallel().anyMatch(tag -> tag.getTag() == null || tag.getTag()
//                .isEmpty())) {
//            throw new JocMissingRequiredParameterException("undefined 'tag'");
//        }
//
//    }
}