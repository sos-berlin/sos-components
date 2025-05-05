package com.sos.joc.tags.group.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.tag.group.GroupTags;
import com.sos.joc.model.tag.group.RequestFilter;
import com.sos.joc.tags.group.resource.IGroup;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("tags/group")
public class GroupImpl extends ATagsModifyImpl<DBItemInventoryTagGroup> implements IGroup {

    private static final String API_CALL = "./tags/group/read";

    @Override
    public JOCDefaultResponse readGroups(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RequestFilter.class);
            RequestFilter in = Globals.objectMapper.readValue(filterBytes, RequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryTagGroupDBLayer dbLayer = new InventoryTagGroupDBLayer(session);
            
            List<DBItemInventoryTagGroup> groups = dbLayer.getGroups(Collections.singleton(in.getGroup()));
            if (groups.isEmpty()) {
                throw new DBMissingDataException("Couldn't find group '" + in.getGroup() + "'");
            }
            
            GroupTags entity = new GroupTags();
            entity.setGroup(in.getGroup());
            entity.setTags(dbLayer.getTagsByGroupId(groups.get(0).getGroupId()));
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