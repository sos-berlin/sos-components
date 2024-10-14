package com.sos.joc.tags.group.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.tags.group.resource.IGroups;

import jakarta.ws.rs.Path;

@Path("tags/groups")
public class GroupsImpl extends ATagsModifyImpl<DBItemInventoryTagGroup> implements IGroups {

    private static final String API_CALL = "./tags/groups";

    @Override
    public JOCDefaultResponse postGroups(String accessToken) {
        return postGroups(API_CALL, accessToken, new InventoryTagGroupDBLayer(null));
    }
}