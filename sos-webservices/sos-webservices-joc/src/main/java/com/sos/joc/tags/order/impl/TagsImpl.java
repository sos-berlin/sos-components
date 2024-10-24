package com.sos.joc.tags.order.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryOrderTag;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.tags.resource.ITags;

import jakarta.ws.rs.Path;

@Path("tags/order")
public class TagsImpl extends ATagsModifyImpl<DBItemInventoryOrderTag> implements ITags {

    private static final String API_CALL = "./tags/order";
    private InventoryOrderTagDBLayer dbLayer = new InventoryOrderTagDBLayer(null);

    @Override
    public JOCDefaultResponse postTags(String accessToken) {
        return postTagsOrGroups(ResponseObject.ORDERTAGS, API_CALL, accessToken, dbLayer);
    }
    
}