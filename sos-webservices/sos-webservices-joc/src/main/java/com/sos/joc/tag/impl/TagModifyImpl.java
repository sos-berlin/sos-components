package com.sos.joc.tag.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.tag.rename.RequestFilter;
import com.sos.joc.tag.resource.ITagModify;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("tag")
public class TagModifyImpl extends JOCResourceImpl implements ITagModify {

    private static final String API_CALL = "./tag/rename";

    @Override
    public JOCDefaultResponse postTagRename(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RequestFilter.class);
            RequestFilter modifyTag = Globals.objectMapper.readValue(filterBytes, RequestFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(modifyTag.getAuditLog(), CategoryType.INVENTORY);
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            session.setAutoCommit(false);
            session.beginTransaction();
            
            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
            DBItemInventoryTag tag = dbLayer.getTag(modifyTag.getName());
            if (tag == null) {
               throw new DBMissingDataException("Couldn't find tag with name '" + modifyTag.getName() + "'");
            }
            SOSCheckJavaVariableName.test("tag name: ", modifyTag.getNewName());
            tag.setName(modifyTag.getNewName());
            Date now = Date.from(Instant.now());
            tag.setModified(now);
            dbLayer.getSession().update(tag);
            JocInventory.postTagEvent(modifyTag.getName());
            JocInventory.postTagEvent(modifyTag.getNewName());
            
            Globals.commit(session);
            return JOCDefaultResponse.responseStatusJSOk(now);
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}