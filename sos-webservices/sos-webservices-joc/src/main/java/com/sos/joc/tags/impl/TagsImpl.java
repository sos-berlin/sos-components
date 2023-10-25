package com.sos.joc.tags.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.tag.Tags;
import com.sos.joc.tags.resource.ITags;

import jakarta.ws.rs.Path;

@Path("tags")
public class TagsImpl extends JOCResourceImpl implements ITags {

    private static final String API_CALL = "./tags";

    @Override
    public JOCDefaultResponse postTags(String accessToken) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            Tags entity = new Tags();
            entity.setTags(new InventoryTagDBLayer(session).getAllTagNames());
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
}