package com.sos.joc.tags.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryTag;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.tag.common.RequestFilters;
import com.sos.joc.tags.resource.ITagsModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import jakarta.ws.rs.Path;

@Path("tags")
public class TagsModifyImpl extends JOCResourceImpl implements ITagsModify {

    private static final String API_CALL = "./tags";

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

    public void postTagsModify(Action action, RequestFilters modifyTags, InventoryTagDBLayer dbLayer) throws Exception {
        storeAuditLog(modifyTags.getAuditLog(), CategoryType.INVENTORY);

        Set<String> tags = modifyTags.getTags() == null ? Collections.emptySet() : modifyTags.getTags();

        switch (action) {
        case ADD:
            add(tags, Date.from(Instant.now()), dbLayer);
            break;
            
        case DELETE:
            // IMPORTANT! first taggings, then tags
            dbLayer.deleteTaggings(tags);
            dbLayer.deleteTags(tags);
            break;
            
        case ORDERING:
            List<DBItemInventoryTag> dbTags = dbLayer.getAllTags();
            Map<String, DBItemInventoryTag> mappedByName = dbTags.stream().collect(Collectors.toMap(DBItemInventoryTag::getName, Function
                    .identity()));
            int ordering = 1;
            for (String name : tags) {
                DBItemInventoryTag dbItem = mappedByName.remove(name);
                if (dbItem == null) {
                    continue;
                }
                if (ordering != dbItem.getOrdering()) {
                    dbItem.setOrdering(ordering);
                    dbLayer.getSession().update(dbItem);
                }
                ordering++;
            }
            for (DBItemInventoryTag dbItem : mappedByName.values().stream().sorted(Comparator.comparingInt(DBItemInventoryTag::getOrdering)).collect(
                    Collectors.toCollection(LinkedList::new))) {
                if (ordering != dbItem.getOrdering()) {
                    dbItem.setOrdering(ordering);
                    dbLayer.getSession().update(dbItem);
                }
                ordering++;
            }
            break;
        }
    }
    
    public static Set<DBItemInventoryTag> add(Set<String> tags, Date date, InventoryTagDBLayer dbLayer) throws SOSHibernateException {
        List<DBItemInventoryTag> dbTags = dbLayer.getTags(tags);
        Set<String> alreadyExistingTags = dbTags.stream().map(DBItemInventoryTag::getName).collect(Collectors.toSet());
        tags.removeAll(alreadyExistingTags);
        int maxOrdering = dbLayer.getMaxOrdering();
        Set<DBItemInventoryTag> result = new HashSet<>();
        for (String name : tags) {
            DBItemInventoryTag item = new DBItemInventoryTag();
            item.setId(null);
            item.setModified(date);
            item.setName(name);
            item.setOrdering(++maxOrdering);
            dbLayer.getSession().save(item);
            result.add(item);
        }
        return result;
    }
    
    private JOCDefaultResponse postTagsModify(Action action, String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            RequestFilters modifyTags = initRequest(action, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL + "/" + action.name().toLowerCase());
            session.setAutoCommit(false);
            session.beginTransaction();
            InventoryTagDBLayer dbLayer = new InventoryTagDBLayer(session);
            postTagsModify(action, modifyTags, dbLayer);
            Globals.commit(session);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
    
    private JOCDefaultResponse initPermissions(String accessToken) {
        return initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
    }
    
    private RequestFilters initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, RequestFilters.class);
        return Globals.objectMapper.readValue(filterBytes, RequestFilters.class);
    }

}