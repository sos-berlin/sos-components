package com.sos.joc.tags.job.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryJobTag;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.tags.resource.ITags;
import com.sos.joc.tags.resource.ITagsUsedBy;

import jakarta.ws.rs.Path;

@Path("tags/job")
public class TagsImpl extends ATagsModifyImpl<DBItemInventoryJobTag> implements ITags, ITagsUsedBy {

    private static final String API_CALL = "./tags/job";
    private static final String API_USEDBY_CALL = "./tags/job/used";
    private InventoryJobTagDBLayer dbLayer = new InventoryJobTagDBLayer(null);

    @Override
    public JOCDefaultResponse postTags(String accessToken) {
        return postTagsOrGroups(ResponseObject.JOBTAGS, API_CALL, accessToken, dbLayer);
    }
    
    @Override
    public JOCDefaultResponse postUsedBy(String accessToken, byte[] filterBytes) {
        return postUsedBy(API_USEDBY_CALL, accessToken, filterBytes, dbLayer);
    }
    
}