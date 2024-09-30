package com.sos.joc.tag.group.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.tag.resource.ITagModify;

import jakarta.ws.rs.Path;

@Path("tag/group")
public class GroupModifyImpl extends ATagsModifyImpl<DBItemInventoryTagGroup> implements ITagModify {

    private static final String API_CALL = "./tag/group/rename";

    @Override
    public JOCDefaultResponse postTagRename(String accessToken, byte[] filterBytes) {
        return postTagRename(API_CALL, accessToken, filterBytes, new InventoryTagGroupDBLayer(null));
    }

}