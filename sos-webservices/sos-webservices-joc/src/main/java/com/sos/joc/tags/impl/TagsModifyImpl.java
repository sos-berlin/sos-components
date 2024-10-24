package com.sos.joc.tags.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.tags.resource.ITagsModify;

import jakarta.ws.rs.Path;

@Path("tags")
public class TagsModifyImpl extends ATagsModifyImpl<DBItemInventoryTag> implements ITagsModify {
    
    private static final String API_CALL = "./tags";
    private InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(null);

    @Override
    public JOCDefaultResponse postTagsAdd(String accessToken, byte[] filterBytes) {
        return postTagsModify(ResponseObject.INVTAGS, API_CALL, Action.ADD, accessToken, filterBytes, dbLayer);
    }

    @Override
    public JOCDefaultResponse postTagsDelete(String accessToken, byte[] filterBytes) {
        return postTagsModify(ResponseObject.INVTAGS, API_CALL, Action.DELETE, accessToken, filterBytes, dbLayer);
    }

    @Override
    public JOCDefaultResponse postTagsOrdering(String accessToken, byte[] filterBytes) {
        return postTagsModify(ResponseObject.INVTAGS, API_CALL, Action.ORDERING, accessToken, filterBytes, dbLayer);
    }

}