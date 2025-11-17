package com.sos.joc.note.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryNote;
import com.sos.joc.db.inventory.InventoryNotesDBLayer;
import com.sos.joc.db.inventory.items.InventorySearchItem;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.inventory.InventoryNoteAddEvent;
import com.sos.joc.event.bean.inventory.InventoryNoteEvent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.note.AddPost;
import com.sos.joc.model.note.NoteResponse;
import com.sos.joc.model.note.common.Author;
import com.sos.joc.model.note.common.Metadata;
import com.sos.joc.model.note.common.Note;
import com.sos.joc.model.note.common.Participant;
import com.sos.joc.model.note.common.Post;
import com.sos.joc.note.resource.IAddPost;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("note")
public class AddPostImpl extends JOCResourceImpl implements IAddPost {

    private static final String API_CALL = "./note/post/add";

    @Override
    public JOCDefaultResponse add(String accessToken, byte[] body) {
        SOSHibernateSession session = null;
        try {
            body = initLogging(API_CALL, body, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(body, AddPost.class);
            AddPost in = Globals.objectMapper.readValue(body, AddPost.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            NoteImpl.checkObjectType(in.getObjectType());
            storeAuditLog(in.getAuditLog());
            in.setName(JocInventory.pathToName(in.getName()));
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryNotesDBLayer dbLayer = new InventoryNotesDBLayer(session);
            InventorySearchItem invItem = NoteImpl.getInvItem(dbLayer, in, folderPermissions);
            
            String user = getJobschedulerUser().getSOSAuthCurrentAccount().getAccountname();
            Date now = Date.from(Instant.now());
            Author author = NoteImpl.newAuthor(user);
            NoteResponse note = new NoteResponse();

            DBItemInventoryNote dbItem = dbLayer.getNote(invItem.getId());
            if (dbItem == null) { // new note
                note = newNote(now, author, in, invItem.getPath());

                dbItem = new DBItemInventoryNote();
                dbItem.setCreated(now);
                dbItem.setModified(now);
                dbItem.setCid(invItem.getId());
                dbItem.setSeverity(in.getSeverity().intValue());
                dbItem.setId(null);
                dbItem.setContent(Globals.objectMapper.writeValueAsString(note));

                session.save(dbItem);
                EventBus.getInstance().post(new InventoryNoteAddEvent(invItem.getPath(), note.getObjectType().value()));

            } else {
                note = Globals.objectMapper.readValue(dbItem.getContent(), NoteResponse.class);
                boolean userFound = false;
                for (Participant participant : note.getParticipants()) {
                    if (user.equals(participant.getUserName())) {
                        userFound = true;
                        participant.setModified(now);
                        participant.setPostCount(participant.getPostCount() + 1);
                        break;
                    }
                }
                if (!userFound) {
                    note.getParticipants().add(newParticipant(1, now, author));
                }

                int newPostId = note.getPosts().stream().mapToInt(Post::getPostId).max().orElse(0) + 1;
                note.getPosts().add(newPost(newPostId, now, author, in));

                Metadata md = note.getMetadata();
                md.setModified(now);
                md.setModifiedBy(author);
                md.setParticipantCount(note.getParticipants().size());
                md.setPostCount(note.getPosts().size());
                if (md.getSeverity().intValue() < in.getSeverity().intValue()) {
                    md.setSeverity(in.getSeverity());
                }
                
                setNoteIdentifier(note, author, in, invItem.getPath());

                dbItem.setContent(Globals.objectMapper.writeValueAsString(note));
                dbItem.setModified(now);
                dbItem.setSeverity(md.getSeverity().intValue());

                session.update(dbItem);
                EventBus.getInstance().post(new InventoryNoteEvent(invItem.getPath(), note.getObjectType().value()));
            }
            
            note.setDeliveryDate(now);
            note.setNoteId(dbItem.getId());
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(note));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

    private static Participant newParticipant(Integer postCount, Date modified, Author author) {
        Participant p = new Participant();
        p.setModified(modified);
        p.setPostCount(postCount);
        p.setUserName(author.getUserName());
        return p;
    }

    private static Post newPost(Integer postId, Date modified, Author author, AddPost in) {
        Post p = new Post();
        p.setAuthor(author);
        p.setContent(in.getContent());
        p.setPosted(modified);
        p.setSeverity(in.getSeverity());
        p.setPostId(postId);
        return p;
    }

    private static Metadata newMetadata(Date modified, Author author) {
        Metadata md = new Metadata();
        md.setCreated(modified);
        md.setCreatedBy(author);
        md.setModified(modified);
        md.setModifiedBy(author);
        md.setParticipantCount(1);
        md.setPostCount(1);
        return md;
    }

    private static NoteResponse newNote(Date modified, Author author, AddPost in, String path) {
        NoteResponse note = new NoteResponse();
        note.setMetadata(newMetadata(modified, author));
        note.setPosts(Collections.singletonList(newPost(1, modified, author, in)));
        note.setParticipants(Collections.singletonList(newParticipant(1, modified, author)));
        setNoteIdentifier(note, author, in, path);
        return note;
    }
    
    private static void setNoteIdentifier(Note note, Author author, AddPost in, String path) {
        note.setName(in.getName());
        note.setPath(path);
        note.setObjectType(in.getObjectType());
    }

}
