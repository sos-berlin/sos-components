package com.sos.joc.tree.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.tree.TreePermanent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.tree.Tree;
import com.sos.joc.model.tree.TreeFilter;
import com.sos.joc.model.tree.TreeType;
import com.sos.joc.model.tree.TreeView;
import com.sos.joc.tree.resource.ITreeResource;
import com.sos.schema.JsonValidator;

@Path("tree")
public class TreeResourceImpl extends JOCResourceImpl implements ITreeResource {

    private static final String API_CALL = "./tree";

    @Override
    public JOCDefaultResponse postTree(String accessToken, byte[] treeBodyBytes) {
        try {
            initLogging(API_CALL, treeBodyBytes, accessToken);
            JsonValidator.validateFailFast(treeBodyBytes, TreeFilter.class);
            TreeFilter treeBody = Globals.objectMapper.readValue(treeBodyBytes, TreeFilter.class);

            boolean treeForInventoryTrash = (treeBody.getForInventoryTrash() != null && treeBody.getForInventoryTrash());
            boolean treeForInventory = !treeForInventoryTrash && ((treeBody.getForInventory() != null && treeBody.getForInventory()) || (treeBody
                    .getTypes() != null && treeBody.getTypes().contains(TreeType.INVENTORY)));
            
            List<TreeType> types = TreePermanent.getAllowedTypes(treeBody.getTypes(), getJocPermissions(accessToken), getControllerPermissions(
                    treeBody.getControllerId(), accessToken), treeForInventory, treeForInventoryTrash);
            
            String controllerId = (treeForInventory || treeForInventoryTrash) ? "" : treeBody.getControllerId();
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

    private void checkFoldersFilterParam(List<Folder> folders) throws Exception {
        if (folders != null && !folders.isEmpty() && folders.stream().parallel().anyMatch(folder -> folder.getFolder() == null || folder.getFolder()
                .isEmpty())) {
            throw new JocMissingRequiredParameterException("undefined 'folder'");
        }

    }
}