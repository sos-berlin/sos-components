package com.sos.joc.tree.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;

import com.sos.auth.predicate.ControllerPermissionsPredicate;
import com.sos.auth.predicate.JocPermissionsPredicate;
import com.sos.auth.records.AuthFolders;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.tree.TreePermanent;
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
            boolean treeForInventory = !treeForInventoryTrash && (treeBody.getForInventory() == Boolean.TRUE || (treeBody
                    .getTypes() != null && treeBody.getTypes().contains(TreeType.INVENTORY)));
            boolean treeForDescriptorsTrash = treeBody.getForDescriptorsTrash() == Boolean.TRUE;
            boolean treeForDescriptors = treeBody.getForDescriptors() == Boolean.TRUE;
            
            String controllerId = (treeForInventory || treeForInventoryTrash) ? "" : treeBody.getControllerId();
            Set<TreeType> types = TreePermanent.getAllowedTypes(treeBody.getTypes(), getBasicJocPermissions(), getBasicControllerPermissions(
                    controllerId), treeForInventory, treeForInventoryTrash, treeForDescriptors, treeForDescriptorsTrash);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(types.size() > 0);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            AuthFolders jocPermittedFolders = new AuthFolders(Optional.empty(), Optional.empty());
            AuthFolders controllerPermittedFolders = new AuthFolders(Optional.empty(), Optional.empty());
            if (treeForInventory || treeForInventoryTrash) {
                // special: tree in Inventory (trash)
                //jocPermittedFolders = getPermittedFoldersByJocPermissions(getJocPermissionsPredicate().getInventory().getView());
            } else if (treeForDescriptors || treeForDescriptorsTrash) {
                // special: tree for Descriptor (trash)
                //jocPermittedFolders = getPermittedFoldersByJocPermissions(getJocPermissionsPredicate().getInventory().getView());
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.WORKFLOW)) {
                // special: tree in Workflows view
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.NOTICEBOARD)) {
                // special: tree in Boards view
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.LOCK)) {
                // special: tree in Locks view
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.REPORT)) {
                // special: tree in Reports view
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.DOCUMENTATION)) {
                // special: tree in Docus view
            } else if (types.stream().allMatch(Set.of(TreeType.WORKINGDAYSCALENDAR, TreeType.NONWORKINGDAYSCALENDAR)::contains)) {
                // special: tree in Calendars view
            } else {
                Predicate<String> jocPermPredicate = null;
                Predicate<String> controllerPermPredicate = null;
                for (TreeType type : types) {
                    if (type.equals(TreeType.WORKFLOW)) {
                        if (controllerPermPredicate == null) {
                            controllerPermPredicate = new ControllerPermissionsPredicate().getWorkflows().getView();
                        } else {
                            controllerPermPredicate = controllerPermPredicate.or(new ControllerPermissionsPredicate().getWorkflows().getView());
                        }
                    } else if (type.equals(TreeType.LOCK)) {
                        if (controllerPermPredicate == null) {
                            controllerPermPredicate = new ControllerPermissionsPredicate().getLocks().getView();
                        } else {
                            controllerPermPredicate = controllerPermPredicate.or(new ControllerPermissionsPredicate().getLocks().getView());
                        }
                    } else if (type.equals(TreeType.NONWORKINGDAYSCALENDAR) || type.equals(TreeType.WORKINGDAYSCALENDAR)) {
                        if (jocPermPredicate == null) {
                            jocPermPredicate = new JocPermissionsPredicate().getCalendars().getView();
                        } else {
                            jocPermPredicate = jocPermPredicate.or(new JocPermissionsPredicate().getCalendars().getView());
                        }
                    } else if (type.equals(TreeType.NOTICEBOARD)) {
                        if (controllerPermPredicate == null) {
                            controllerPermPredicate = new ControllerPermissionsPredicate().getNoticeBoards().getView();
                        } else {
                            controllerPermPredicate = controllerPermPredicate.or(new ControllerPermissionsPredicate().getNoticeBoards().getView());
                        }
                    } else if (type.equals(TreeType.REPORT)) {
                        if (jocPermPredicate == null) {
                            jocPermPredicate = new JocPermissionsPredicate().getReports().getView();
                        } else {
                            jocPermPredicate = jocPermPredicate.or(new JocPermissionsPredicate().getReports().getView());
                        }
                    }
                }
                if (jocPermPredicate != null) {
                    jocPermittedFolders = getPermittedFoldersByJocPermissions(jocPermPredicate);
                }
                if (controllerPermPredicate != null) {
                    controllerPermittedFolders = getPermittedFoldersByControllerPermissions(controllerId, controllerPermPredicate);
                }
            }
            
            
            treeBody.setTypes(types);
            if (treeBody.getFolders() != null && !treeBody.getFolders().isEmpty()) {
                checkFoldersFilterParam(treeBody.getFolders());
            }
            SortedSet<Tree> folders = Collections.emptySortedSet();
            Tree root = null;
            if (treeForInventory) {
                folders = TreePermanent.initFoldersByFoldersForInventory(treeBody);
                root = TreePermanent.getInventoryTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions(), true);
            } else if (treeForInventoryTrash) {
                folders = TreePermanent.initFoldersByFoldersForInventoryTrash(treeBody);
                root = TreePermanent.getInventoryTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (treeForDescriptors) {
                folders = TreePermanent.initFoldersByFoldersForDescriptors(treeBody);
                root = TreePermanent.getInventoryTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (treeForDescriptorsTrash) {
                folders = TreePermanent.initFoldersByFoldersForDescriptorsTrash(treeBody);
                root = TreePermanent.getInventoryTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.WORKFLOW)) {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getWorkflowViewTree(folders, controllerId, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.NOTICEBOARD)) {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getBoardViewTree(folders, controllerId, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.REPORT)) {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getReportViewTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.DOCUMENTATION)) {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getDocuViewTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (types.size() == 1 && types.iterator().next().equals(TreeType.LOCK)) {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getLockViewTree(folders, controllerId, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else if (types.stream().allMatch(Set.of(TreeType.WORKINGDAYSCALENDAR, TreeType.NONWORKINGDAYSCALENDAR)::contains)) {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getCalendarViewTree(folders, getCurrentAccount().getSOSAuthDetailedFolderPermissions());
            } else {
                folders = TreePermanent.initFoldersByFoldersForViews(treeBody);
                root = TreePermanent.getTree(folders, jocPermittedFolders, controllerPermittedFolders);
            }

            TreeView entity = new TreeView();
            if (root != null) {
                entity.getFolders().add(root);
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
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
        if (folders != null && !folders.isEmpty() && folders.stream().anyMatch(folder -> folder.getFolder() == null || folder.getFolder()
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