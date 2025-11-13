package com.sos.joc.note.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryNote;
import com.sos.joc.db.inventory.InventoryNotesDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.note.NoteResponse;
import com.sos.joc.model.note.common.NoteIdentifier;
import com.sos.joc.note.resource.INote;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("note")
public class NoteImpl extends JOCResourceImpl implements INote {

    private static final String API_CALL_READ = "./note";
    private static final String API_CALL_DELETE = "./note/delete";

    @Override
    public JOCDefaultResponse read(String accessToken, byte[] body) {
        SOSHibernateSession session = null;
        try {
            body = initLogging(API_CALL_READ, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, NoteIdentifier.class);
            NoteIdentifier in = Globals.objectMapper.readValue(body, NoteIdentifier.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getView()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
            InventoryNotesDBLayer dbLayer = new InventoryNotesDBLayer(session);

            DBItemInventoryNote dbItem = dbLayer.getNote(in);
            if (dbItem == null) {
                throw new DBMissingDataException(String.format("Couldn't find note of %s: %s", in.getObjectType().name().toLowerCase(), in.getName()));

            } else {
                NoteResponse note = Globals.objectMapper.readValue(dbItem.getContent(), NoteResponse.class);
                note.setName(in.getName());
                note.setObjectType(in.getObjectType());
                note.setDeliveryDate(Date.from(Instant.now()));
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(note));
            }
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    @Override
    public JOCDefaultResponse delete(String accessToken, byte[] body) {
        SOSHibernateSession session = null;
        try {
            body = initLogging(API_CALL_DELETE, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, NoteIdentifier.class);
            NoteIdentifier in = Globals.objectMapper.readValue(body, NoteIdentifier.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            InventoryNotesDBLayer dbLayer = new InventoryNotesDBLayer(session);

            DBItemInventoryNote dbItem = dbLayer.getNote(in);
            if (dbItem != null) {
                session.delete(dbItem);
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}
