package com.sos.joc.tags.group.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.db.history.DBItemHistoryOrderTag;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagsEvent;
import com.sos.joc.event.bean.inventory.InventoryTagsEvent;
import com.sos.joc.exceptions.JocBadRequestException;
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
    public JOCDefaultResponse postRename(String accessToken, byte[] filterBytes) {
        return postTagRename(ResponseObject.GROUPS, API_CALL_RENAME, accessToken, filterBytes, new InventoryTagGroupDBLayer(null));
    }

    @Override
    public JOCDefaultResponse assignTags(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL_STORE, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, GroupTagsFilter.class);
            GroupTagsFilter modifyTags = Globals.objectMapper.readValue(filterBytes, GroupTagsFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(modifyTags.getAuditLog());
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
            
            Set<String> tags = modifyTags.getTags().stream().map(GroupedTag::new).map(GroupedTag::getTag).collect(Collectors.toSet());

            Set<String> addedTags =  new HashSet<>(tags);
            addedTags.removeAll(oldTags);
            oldTags.removeAll(tags); //deletedTags
            
            Set<JOCEvent> events = new HashSet<>();
            
            dbLayer.deleteGroupIds(Collections.singletonList(groupId), oldTags, events);
            
            List<IDBItemTag> dbTagItems = new ArrayList<>();
            int size = 0;
            dbTagItems.addAll(new InventoryTagDBLayer(session).getTags(addedTags));
            size = dbTagItems.size() - size;
            if (size > 0) {
                events.add(new InventoryTagsEvent());
            }
            dbTagItems.addAll(new InventoryJobTagDBLayer(session).getTags(addedTags));
            size = dbTagItems.size() - size;
            if (size > 0) {
                events.add(new InventoryJobTagsEvent());
            }
            dbTagItems.addAll(new InventoryOrderTagDBLayer(session).getTags(addedTags));
            Date now = Date.from(Instant.now());
            
            for (IDBItemTag dbTagItem : dbTagItems) {
                dbTagItem.setGroupId(groupId);
                dbTagItem.setModified(now);
                session.update(dbTagItem);
            }
            
            List<DBItemHistoryOrderTag> historyOrderTags =  OrderTags.getTagsByTagNames(addedTags, session);
            for (DBItemHistoryOrderTag dbTagItem : historyOrderTags) {
                dbTagItem.setGroupId(groupId);
                session.update(dbTagItem);
            }
            
            Globals.commit(session);
            events.forEach(e -> EventBus.getInstance().post(e));
            
            return responseStatusJSOk(now);
        } catch (Exception e) {
            Globals.rollback(session);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

}