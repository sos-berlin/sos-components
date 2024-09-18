package com.sos.joc.tags.job.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryJobTag;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.tags.resource.ITagsModify;

import jakarta.ws.rs.Path;

@Path("job/tags")
public class TagsModifyImpl extends ATagsModifyImpl<DBItemInventoryJobTag> implements ITagsModify {
    
    private static final String API_CALL = "./job/tags";
    private InventoryJobTagDBLayer dbLayer = new InventoryJobTagDBLayer(null);

    @Override
    public JOCDefaultResponse postTagsAdd(String accessToken, byte[] filterBytes) {
        return postTagsModify(API_CALL, Action.ADD, accessToken, filterBytes, dbLayer);
    }

    @Override
    public JOCDefaultResponse postTagsDelete(String accessToken, byte[] filterBytes) {
        return postTagsModify(API_CALL, Action.DELETE, accessToken, filterBytes, dbLayer);
    }

    @Override
    public JOCDefaultResponse postTagsOrdering(String accessToken, byte[] filterBytes) {
        return postTagsModify(API_CALL, Action.ORDERING, accessToken, filterBytes, dbLayer);
    }

}