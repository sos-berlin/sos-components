package com.sos.joc.tag.job.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryJobTag;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.tag.resource.ITagModify;

import jakarta.ws.rs.Path;

@Path("tag/job")
public class TagModifyImpl extends ATagsModifyImpl<DBItemInventoryJobTag> implements ITagModify {

    private static final String API_CALL = "./tag/job/rename";

    @Override
    public JOCDefaultResponse postRename(String accessToken, byte[] filterBytes) {
        return postTagRename(ResponseObject.JOBTAGS, API_CALL, accessToken, filterBytes, new InventoryJobTagDBLayer(null));
    }

}