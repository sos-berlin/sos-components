package com.sos.joc.tags.group.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.tag.group.GroupTagsFilter;
import com.sos.joc.tag.resource.ITagModify;
import com.sos.joc.tags.group.resource.IGroupAssignTags;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("tags/group")
public class GroupModifyImpl extends ATagsModifyImpl<DBItemInventoryTagGroup> implements ITagModify, IGroupAssignTags {

    private static final String API_CALL_RENAME = "./tags/group/rename";
    private static final String API_CALL_STORE = "./tags/group/store";

    @Override
    public JOCDefaultResponse postTagRename(String accessToken, byte[] filterBytes) {
        return postTagRename(API_CALL_RENAME, accessToken, filterBytes, new InventoryTagGroupDBLayer(null));
    }

    @Override
    public JOCDefaultResponse assignTags(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL_STORE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, GroupTagsFilter.class);
            GroupTagsFilter modifyTags = Globals.objectMapper.readValue(filterBytes, GroupTagsFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(modifyTags.getAuditLog(), CategoryType.INVENTORY);
            if (modifyTags.getTags() == null) {
                modifyTags.setTags(Collections.emptySet()); 
            }
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL_STORE);
            session.setAutoCommit(false);
            session.beginTransaction();
            
            InventoryTagGroupDBLayer dbLayer = new InventoryTagGroupDBLayer(session);
            List<DBItemInventoryTagGroup> groups = dbLayer.getGroups(Collections.singleton(modifyTags.getGroup()));
            if (groups.isEmpty()) {
                throw new JocBadRequestException("Couldn't find group '" + modifyTags.getGroup() + "'");
            }
            Long groupId = groups.get(0).getGroupId();
            Set<String> oldTags = dbLayer.getTagsByGroupId(groupId);
            
            Set<String> addedTags =  new HashSet<>(modifyTags.getTags());
            addedTags.removeAll(oldTags);
            oldTags.removeAll(modifyTags.getTags()); //deletedTags
            
            dbLayer.deleteGroupIds(Collections.singletonList(groupId), oldTags);
            
            List<IDBItemTag> dbTagItems = new ArrayList<>();
            dbTagItems.addAll(new InventoryTagDBLayer(session).getTags(addedTags));
            dbTagItems.addAll(new InventoryJobTagDBLayer(session).getTags(addedTags));
            dbTagItems.addAll(new InventoryOrderTagDBLayer(session).getTags(addedTags));
            
            Date now = Date.from(Instant.now());
            
            for (IDBItemTag dbTagItem : dbTagItems) {
                dbTagItem.setGroupId(groupId);
                dbTagItem.setModified(now);
                session.update(dbTagItem);
            }
            // TODO history orders?? dbTagItems.addAll(...)
            
            // TODO events
            
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