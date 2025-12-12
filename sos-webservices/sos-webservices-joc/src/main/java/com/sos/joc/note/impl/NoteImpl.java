package com.sos.joc.note.impl;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryNote;
import com.sos.joc.db.inventory.InventoryNotesDBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.inventory.InventoryNoteDeleteEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.note.DisplayPreferencesRequest;
import com.sos.joc.model.note.NoteResponse;
import com.sos.joc.model.note.common.Author;
import com.sos.joc.model.note.common.ModifyRequest;
import com.sos.joc.model.note.common.NoteIdentifier;
import com.sos.joc.note.resource.INote;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("note")
public class NoteImpl extends JOCResourceImpl implements INote {

    private static final String API_CALL_READ = "./note";
    private static final String API_CALL_DELETE = "./note/delete";
    private static final String API_CALL_PREFS = "./note/preferences";

    @Override
    public JOCDefaultResponse read(String accessToken, byte[] body) {
        SOSHibernateSession session = null;
        try {
            body = initLogging(API_CALL_READ, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, NoteIdentifier.class);
            NoteIdentifier in = Globals.objectMapper.readValue(body, NoteIdentifier.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkObjectType(in.getObjectType());
            in.setName(JocInventory.pathToName(in.getName()));
            session = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
            InventoryNotesDBLayer dbLayer = new InventoryNotesDBLayer(session);
            InventorySearchItem invItem = getInvItem(dbLayer, in, folderPermissions);
            DBItemInventoryNote dbItem = dbLayer.getNote(invItem.getId());
            NoteResponse note = getNoteResponse(dbItem, in, invItem.getPath());
            note.setDeliveryDate(Date.from(Instant.now()));
            note.setNoteId(dbItem.getId());
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(note));
        } catch (DBMissingDataException e) {
            return responseStatus434JSError(e, true);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static NoteResponse getNoteResponse(DBItemInventoryNote dbItem, NoteIdentifier in, String path) throws JsonMappingException, JsonProcessingException {
        if (dbItem == null) {
            throw new DBMissingDataException(String.format("Couldn't find note of %s: %s", in.getObjectType().name().toLowerCase(), in.getName()));

        } else {
            NoteResponse note = Globals.objectMapper.readValue(dbItem.getContent(), NoteResponse.class);
            note.setName(in.getName());
            note.setObjectType(in.getObjectType());
            note.setPath(path);
            return note;
        }
    }
    
    protected static InventorySearchItem getInvItem(InventoryNotesDBLayer dbLayer, NoteIdentifier in, SOSAuthFolderPermissions folderPermissions) {
        InventorySearchItem invItem = dbLayer.getInvItem(in);
        if (invItem == null) {
            throw new DBMissingDataException(String.format("Couldn't find %s: %s", in.getObjectType().name().toLowerCase(), in.getName()));
        }
        if (!folderPermissions.isPermittedForFolder(invItem.getFolder())) {
            throw new JocFolderPermissionsException(invItem.getFolder());
        }
        return invItem;
    }
    
    protected static void checkObjectType(ConfigurationType type) {
        if (!JocInventory.isDeployable(type) && !JocInventory.isReleasable(type)) {
            throw new JocBadRequestException("Invalid object type");
         }
    }
    
    protected static Author newAuthor(String userName) {
        Author a = new Author();
        a.setUserName(userName);
        return a;
    }

    @Override
    public JOCDefaultResponse delete(String accessToken, byte[] body) {
        SOSHibernateSession session = null;
        try {
            body = initLogging(API_CALL_DELETE, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, ModifyRequest.class);
            ModifyRequest in = Globals.objectMapper.readValue(body, ModifyRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkObjectType(in.getObjectType());
            in.setName(JocInventory.pathToName(in.getName()));
            storeAuditLog(in.getAuditLog());
            session = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            InventoryNotesDBLayer dbLayer = new InventoryNotesDBLayer(session);
            InventorySearchItem invItem = getInvItem(dbLayer, in, folderPermissions);
            DBItemInventoryNote dbItem = dbLayer.getNote(invItem.getId());
            if (dbItem != null) {
                session.delete(dbItem);
                EventBus.getInstance().post(new InventoryNoteDeleteEvent(invItem.getPath(), in.getObjectType().value()));
            }
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    @Override
    public JOCDefaultResponse setPreferences(String accessToken, byte[] body) {
        SOSHibernateSession session = null;
        try {
            body = initLogging(API_CALL_PREFS, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, DisplayPreferencesRequest.class);
            DisplayPreferencesRequest in = Globals.objectMapper.readValue(body, DisplayPreferencesRequest.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkObjectType(in.getObjectType());
            in.setName(JocInventory.pathToName(in.getName()));
            session = Globals.createSosHibernateStatelessConnection(API_CALL_PREFS);
            InventoryNotesDBLayer dbLayer = new InventoryNotesDBLayer(session);
            InventorySearchItem invItem = getInvItem(dbLayer, in, folderPermissions);
            DBItemInventoryNote dbItem = dbLayer.getNote(invItem.getId());
            NoteResponse note = getNoteResponse(dbItem, in, invItem.getPath());
            
            if (!in.getDisplayPreferences().equals(note.getMetadata().getDisplayPreferences())) {
                Date now = Date.from(Instant.now());
                
                note.getMetadata().setDisplayPreferences(in.getDisplayPreferences());
                note.getMetadata().setModified(now);
                note.getMetadata().setModifiedBy(newAuthor(getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname()));
                
                dbItem.setContent(Globals.objectMapper.writeValueAsString(note));
                dbItem.setModified(now);
                
                session.update(dbItem);
            }
            
            note.setDeliveryDate(Date.from(Instant.now()));
            note.setNoteId(dbItem.getId());
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(note));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}
