package com.sos.joc.tags.group.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.InventoryTagGroupDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.inventory.InventoryGroupAddEvent;
import com.sos.joc.event.bean.inventory.InventoryGroupDeleteEvent;
import com.sos.joc.event.bean.inventory.InventoryGroupsEvent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.tag.group.RequestFilters;
import com.sos.joc.tags.resource.ITagsModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import jakarta.ws.rs.Path;

@Path("tags/groups")
public class GroupsModifyImpl extends JOCResourceImpl implements ITagsModify {
    
    private static final String API_CALL = "./tags/groups";
    private enum Action {
        ADD, DELETE, ORDERING
    }

    @Override
    public JOCDefaultResponse postTagsAdd(String accessToken, byte[] filterBytes) {
        return postTagsModify(Action.ADD, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postTagsDelete(String accessToken, byte[] filterBytes) {
        return postTagsModify(Action.DELETE, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postTagsOrdering(String accessToken, byte[] filterBytes) {
        return postTagsModify(Action.ORDERING, accessToken, filterBytes);
    }
    
    private JOCDefaultResponse postTagsModify(Action action, String accessToken, byte[] filterBytes) {
        try {
            RequestFilters modifyTags = initModifyRequest(action, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(modifyTags.getAuditLog(), CategoryType.INVENTORY);
            Stream<JOCEvent> events = postTagsModify(action, modifyTags);
            events.forEach(evt -> EventBus.getInstance().post(evt));

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private RequestFilters initModifyRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException,
            IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, RequestFilters.class);
        return Globals.objectMapper.readValue(filterBytes, RequestFilters.class);
    }

    private JOCDefaultResponse initPermissions(String accessToken) {
        return initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
    }
    
    private Stream<JOCEvent> postTagsModify(Action action, RequestFilters modifyTags) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL + "/" + action.name().toLowerCase());
            session.setAutoCommit(false);
            session.beginTransaction();

            Stream<JOCEvent> events = postTagsModify(action, modifyTags, session);

            Globals.commit(session);
            return events;
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Stream<JOCEvent> postTagsModify(Action action, RequestFilters modifyTags, SOSHibernateSession session) throws Exception {
        InventoryTagGroupDBLayer dbLayer = new InventoryTagGroupDBLayer(session);
        Stream<JOCEvent> events = Stream.empty();
        
        switch (action) {
        case ADD:
            List<DBItemInventoryTagGroup> dbGroups = dbLayer.getGroups(modifyTags.getGroups());
            modifyTags.getGroups().removeAll(dbGroups.stream().map(DBItemInventoryTagGroup::getName).collect(Collectors.toSet()));
            Date date = Date.from(Instant.now());
            
            if (!modifyTags.getGroups().isEmpty()) {
                int maxGroupsOrdering = dbLayer.getMaxGroupsOrdering();
                for (String group : modifyTags.getGroups()) {
                    SOSCheckJavaVariableName.test("group name: ", group);
                    DBItemInventoryTagGroup item = new DBItemInventoryTagGroup();
                    item.setName(group);
                    item.setModified(date);
                    item.setOrdering(++maxGroupsOrdering);
                    dbLayer.getSession().save(item);
                }
                events = modifyTags.getGroups().stream().map(InventoryGroupAddEvent::new);
            }
            break;

        case DELETE:
            List<DBItemInventoryTagGroup> dbGroups2 = dbLayer.getGroups(modifyTags.getGroups());
            //set groupId = 0 for Workflow, job, order tags
            dbLayer.deleteGroupIds(dbGroups2.stream().map(DBItemInventoryTagGroup::getId).collect(Collectors.toList()), null);
            dbLayer.deleteGroups(dbGroups2);
            events = modifyTags.getGroups().stream().map(InventoryGroupDeleteEvent::new);
            break;

        case ORDERING:
            List<DBItemInventoryTagGroup> dbAllTags = dbLayer.getAllGroups();
            Map<String, DBItemInventoryTagGroup> mappedByName = dbAllTags.stream().collect(Collectors.toMap(DBItemInventoryTagGroup::getName, Function
                    .identity()));
            int ordering = 1;
            for (String name : modifyTags.getGroups()) {
                DBItemInventoryTagGroup dbItem = mappedByName.remove(name);
                if (dbItem == null) {
                    continue;
                }
                if (ordering != dbItem.getOrdering()) {
                    dbItem.setOrdering(ordering);
                    dbLayer.getSession().update(dbItem);
                }
                ordering++;
            }
            for (DBItemInventoryTagGroup dbItem : mappedByName.values().stream().sorted(Comparator.comparingInt(DBItemInventoryTagGroup::getOrdering))
                    .collect(Collectors.toCollection(LinkedList::new))) {
                if (ordering != dbItem.getOrdering()) {
                    dbItem.setOrdering(ordering);
                    dbLayer.getSession().update(dbItem);
                }
                ordering++;
            }
            events = Stream.of(new InventoryGroupsEvent());
            break;
        }
        return events;
    }

}