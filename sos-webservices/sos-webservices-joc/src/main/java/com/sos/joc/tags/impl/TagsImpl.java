package com.sos.joc.tags.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.tags.resource.ITags;
import com.sos.joc.tags.resource.ITagsUsedBy;

import jakarta.ws.rs.Path;

@Path("tags")
public class TagsImpl extends ATagsModifyImpl<DBItemInventoryTag> implements ITags, ITagsUsedBy {

    private static final String API_CALL = "./tags";
    private static final String API_USEDBY_CALL = "./tags/used";
    private InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(null);

    @Override
    public JOCDefaultResponse postTags(String accessToken) {
        return postTagsOrGroups(ResponseObject.INVTAGS, API_CALL, accessToken, dbLayer);
    }
    
    @Override
    public JOCDefaultResponse postUsedBy(String accessToken, byte[] filterBytes) {
        return postUsedBy(API_USEDBY_CALL, accessToken, filterBytes, dbLayer);
    }

}