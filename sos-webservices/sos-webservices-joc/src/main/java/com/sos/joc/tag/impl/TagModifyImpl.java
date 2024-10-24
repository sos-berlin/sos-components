package com.sos.joc.tag.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.tag.resource.ITagModify;

import jakarta.ws.rs.Path;

@Path("tag")
public class TagModifyImpl extends ATagsModifyImpl<DBItemInventoryTag> implements ITagModify {

    private static final String API_CALL = "./tag/rename";

    @Override
    public JOCDefaultResponse postRename(String accessToken, byte[] filterBytes) {
        return postTagRename(ResponseObject.INVTAGS, API_CALL, accessToken, filterBytes, new InventoryTagDBLayer(null));
    }

}