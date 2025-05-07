package com.sos.joc.inventory.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.inventory.impl.common.AReadTag;
import com.sos.joc.inventory.resource.ITagResource;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class TagResourceImpl extends AReadTag implements ITagResource {

    @Override
    public JOCDefaultResponse readTag(final String accessToken, byte[] inBytes) {
        return readTag(IMPL_PATH, false, new InventoryTagDBLayer(null), accessToken, inBytes);
    }

    @Override
    public JOCDefaultResponse readTrashTag(final String accessToken, byte[] inBytes) {
        return readTag(TRASH_IMPL_PATH, true, new InventoryTagDBLayer(null), accessToken, inBytes);
    }
    
    @Override
    public JOCDefaultResponse readJobTag(final String accessToken, byte[] inBytes) {
        return readTag(IMPL_PATH_JOB, false, new InventoryJobTagDBLayer(null), accessToken, inBytes);
    }

    @Override
    public JOCDefaultResponse readTrashJobTag(final String accessToken, byte[] inBytes) {
        return readTag(TRASH_IMPL_PATH_JOB, true, new InventoryJobTagDBLayer(null), accessToken, inBytes);
    }

}
