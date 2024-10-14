package com.sos.joc.classes.tag;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.inventory.InventoryTagAddEvent;
import com.sos.joc.event.bean.inventory.InventoryTagDeleteEvent;
import com.sos.joc.event.bean.inventory.InventoryTagsEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.tag.Tags;
import com.sos.joc.model.tag.TagsUsedBy;
import com.sos.joc.model.tag.common.RequestFilters;
import com.sos.joc.model.tag.common.RequestFolder;
import com.sos.joc.model.tag.group.Groups;
import com.sos.joc.model.tag.rename.RequestFilter;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

public abstract class ATagsModifyImpl<T extends IDBItemTag> extends JOCResourceImpl {

    protected enum Action {
        ADD, DELETE, ORDERING
    }
    
    protected enum ResponseObject {
        TAGS, GROUPS
    }

    private Stream<JOCEvent> postTagsModify(String apiCall, Action action, RequestFilters modifyTags, ATagDBLayer<T> dbLayer) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall + "/" + action.name().toLowerCase());
            session.setAutoCommit(false);
            session.beginTransaction();
            dbLayer.setSession(session);

            Stream<JOCEvent> events = postTagsModify(action, modifyTags, dbLayer);

            Globals.commit(session);
            return events;
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private Stream<JOCEvent> postTagsModify(Action action, RequestFilters modifyTags, ATagDBLayer<T> dbLayer) throws Exception {
        Set<GroupedTag> groupedTags = modifyTags.getTags() == null ? Collections.emptySet() : modifyTags.getTags().stream().map(GroupedTag::new)
                .collect(Collectors.toSet());
        Set<String> tags = groupedTags.stream().map(GroupedTag::getTag).collect(Collectors.toSet());
        Stream<JOCEvent> events = Stream.empty();
        
        switch (action) {
        case ADD:
            Set<T> result = insert(groupedTags, dbLayer.getTags(tags), Date.from(Instant.now()), dbLayer);
            events = result.stream().map(T::getName).map(InventoryTagAddEvent::new);
            break;

        case DELETE:
            List<T> dbTags = dbLayer.getTags(tags);
            // IMPORTANT! first taggings, then tags
            dbLayer.deleteTaggingsByTagIds(dbTags.stream().map(T::getId).collect(Collectors.toList())); // TODO events for Workflows
            dbLayer.deleteTags(dbTags);

            events = tags.stream().map(InventoryTagDeleteEvent::new);
            break;

        case ORDERING:
            List<T> dbAllTags = dbLayer.getAllTags();
            Map<String, T> mappedByName = dbAllTags.stream().collect(Collectors.toMap(T::getName, Function.identity()));
            int ordering = 1;
            for (String name : tags) {
                T dbItem = mappedByName.remove(name);
                if (dbItem == null) {
                    continue;
                }
                if (ordering != dbItem.getOrdering()) {
                    dbItem.setOrdering(ordering);
                    dbLayer.getSession().update(dbItem);
                }
                ordering++;
            }
            for (T dbItem : mappedByName.values().stream().sorted(Comparator.comparingInt(T::getOrdering)).collect(Collectors.toCollection(
                    LinkedList::new))) {
                if (ordering != dbItem.getOrdering()) {
                    dbItem.setOrdering(ordering);
                    dbLayer.getSession().update(dbItem);
                }
                ordering++;
            }
            events = Stream.of(new InventoryTagsEvent());
            break;
        }
        return events;
    }
    
    public Set<T> insert(Collection<GroupedTag> groupedTags, List<T> oldDBTags, Date date, ATagDBLayer<T> dbLayer) throws SOSHibernateException {
        if (groupedTags == null || groupedTags.isEmpty()) {
            return new HashSet<>();
        }

        Set<GroupedTag> alreadyExistingTagsInDB = oldDBTags.stream().map(T::getName).map(GroupedTag::new).collect(Collectors.toSet());
        Set<GroupedTag> groupedTagsCopy = new HashSet<>(groupedTags);
        groupedTagsCopy.removeAll(alreadyExistingTagsInDB); // contains only new tags with their groups
        Set<T> result = new HashSet<>();
        if (!groupedTagsCopy.isEmpty()) {
            // select groups, getGroups from DB of these groups and insert to DB if necessary
            Set<String> groups = groupedTagsCopy.stream().map(GroupedTag::getGroup).filter(Optional::isPresent).map(Optional::get).collect(Collectors
                    .toSet());
            List<DBItemInventoryTagGroup> dbGroups = groups.isEmpty() ? Collections.emptyList() : dbLayer.getGroups(groups);
            Map<String, Long> dbGroupsMap = dbGroups.stream().collect(Collectors.toMap(DBItemInventoryTagGroup::getName,
                    DBItemInventoryTagGroup::getId));
            groups.removeAll(dbGroupsMap.keySet()); // groups contains only new groups

            if (!groups.isEmpty()) {
                int maxGroupsOrdering = dbLayer.getMaxGroupsOrdering();
                for (String group : groups) {
                    DBItemInventoryTagGroup item = new DBItemInventoryTagGroup();
                    item.setName(group);
                    item.setModified(date);
                    item.setOrdering(++maxGroupsOrdering);
                    dbLayer.getSession().save(item);
                    dbGroupsMap.put(group, item.getId());
                    // TODO events
                }
            }

            int maxOrdering = dbLayer.getMaxOrdering();
            Class<T> typedDbItemClazz = createTypedDBItemClass();
            for (GroupedTag groupedTag : groupedTagsCopy) {
                groupedTag.checkJavaNameRules();
                T item = createTypedDBItem(typedDbItemClazz);
                item.setId(null);
                item.setModified(date);
                item.setName(groupedTag.getTag());
                item.setOrdering(++maxOrdering);
                if (groupedTag.getGroup().isPresent()) {
                    item.setGroupId(dbGroupsMap.getOrDefault(groupedTag.getGroup().get(), 0L));
                } else {
                    item.setGroupId(0L);
                }
                dbLayer.getSession().save(item);
                result.add((T) item);
            }
        }
        return result;
    }

    private Tags postTags(String apiCall, ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            dbLayer.setSession(session);
            Tags tags = new Tags();
            tags.setTags(dbLayer.getAllTagNames());
            tags.setDeliveryDate(Date.from(Instant.now()));
            return tags;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Groups postGroups(String apiCall, ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            dbLayer.setSession(session);
            Groups groups = new Groups();
            groups.setGroups(dbLayer.getAllGroupNames());
            groups.setDeliveryDate(Date.from(Instant.now()));
            return groups;
        } finally {
            Globals.disconnect(session);
        }
    }

    protected JOCDefaultResponse postTags(String apiCall, String accessToken, ATagDBLayer<T> dbLayer) {
        return postTagsOrGroups(ResponseObject.TAGS, apiCall, accessToken, dbLayer);
    }
    
    protected JOCDefaultResponse postGroups(String apiCall, String accessToken, ATagDBLayer<T> dbLayer) {
        return postTagsOrGroups(ResponseObject.GROUPS, apiCall, accessToken, dbLayer);
    }
    
    protected JOCDefaultResponse postTagsOrGroups(ResponseObject responseObject, String apiCall, String accessToken, ATagDBLayer<T> dbLayer) {
        try {
            initLogging(apiCall, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            switch (responseObject) {
            case TAGS:
                Tags tags = postTags(apiCall, dbLayer);
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(tags));
            default: //case GROUPS:
                Groups groups = postGroups(apiCall, dbLayer);
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(groups));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    protected JOCDefaultResponse postUsedBy(String apiCall, String accessToken, byte[] filterBytes, ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            initLogging(apiCall, filterBytes, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            JsonValidator.validateFailFast(filterBytes, RequestFolder.class);
            RequestFolder in =  Globals.objectMapper.readValue(filterBytes, RequestFolder.class);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            dbLayer.setSession(session);
            
            TagsUsedBy entity = new TagsUsedBy();
            entity.setAdditionalProperties(getUsedBy(in.getFolders(), folderPermissions.getListOfFolders(), dbLayer));
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
    
    protected JOCDefaultResponse postTagsModify(String apiCall, Action action, String accessToken, byte[] filterBytes, ATagDBLayer<T> dbLayer) {
        try {
            RequestFilters modifyTags = initModifyRequest(apiCall, action, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(modifyTags.getAuditLog(), CategoryType.INVENTORY);
            Stream<JOCEvent> events = postTagsModify(apiCall, action, modifyTags, dbLayer);
            events.forEach(evt -> EventBus.getInstance().post(evt));

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    protected JOCDefaultResponse postTagRename(String apiCall, String accessToken, byte[] filterBytes, ATagDBLayer<T> dbLayer) {
        return postTagRename(ResponseObject.TAGS, apiCall, accessToken, filterBytes, dbLayer);
    }
    
    private JOCDefaultResponse postTagRename(ResponseObject responseObject, String apiCall, String accessToken, byte[] filterBytes,
            ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            initLogging(apiCall, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RequestFilter.class);
            RequestFilter modifyTag = Globals.objectMapper.readValue(filterBytes, RequestFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String objectName = ResponseObject.TAGS.equals(responseObject) ? "tag" : "group";
            
            storeAuditLog(modifyTag.getAuditLog(), CategoryType.INVENTORY);
            
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            session.setAutoCommit(false);
            session.beginTransaction();
            
            dbLayer.setSession(session);
            T tag = dbLayer.getTag(modifyTag.getName());
            if (tag == null) {
               throw new DBMissingDataException("Couldn't find " + objectName + " with name '" + modifyTag.getName() + "'");
            }
            SOSCheckJavaVariableName.test(objectName + " name: ", modifyTag.getNewName());
            tag.setName(modifyTag.getNewName());
            Date now = Date.from(Instant.now());
            tag.setModified(now);
            dbLayer.getSession().update(tag);
            Globals.commit(session);
            
            switch (responseObject) {
            case TAGS:
                EventBus.getInstance().post(new InventoryTagAddEvent(modifyTag.getNewName()));
                EventBus.getInstance().post(new InventoryTagDeleteEvent(modifyTag.getName()));
                break;
            default: //case GROUPS
                //TODO events
                break;
            }
            
            
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

    private T createTypedDBItem(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> createTypedDBItemClass() {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) pt.getActualTypeArguments()[0];
    }

    private JOCDefaultResponse initPermissions(String accessToken) {
        return initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
    }

    private RequestFilters initModifyRequest(String apiCall, Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException,
            IOException {
        initLogging(apiCall + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, RequestFilters.class);
        return Globals.objectMapper.readValue(filterBytes, RequestFilters.class);
    }
    
    private Map<String, Set<String>> getUsedBy(Collection<Folder> folders, Set<Folder> permittedFolders, ATagDBLayer<T> dbLayer)
            throws SOSHibernateException {
        return dbLayer.getTagsByFolders(folders, true).stream().filter(i -> folderIsPermitted(i.getFolder(), permittedFolders)).collect(Collectors
                .groupingBy(InventoryTagItem::getNullableName, Collectors.mapping(InventoryTagItem::getPath, Collectors.toSet())));
    }
    
    public static <T extends IDBItemTag> void checkAndAssignGroup(Map<String, GroupedTag> groupedTags, ATagDBLayer<T> dbLayer, String type) {
        
        Map<String, GroupedTag> dbGroupedTagsMap = dbLayer.getGroupedTags(groupedTags.keySet()).stream().collect(Collectors.toMap(GroupedTag::getTag,
                Function.identity()));
        for (Map.Entry<String, GroupedTag> gt : groupedTags.entrySet()) {
            GroupedTag dbInvGroupedTag = dbGroupedTagsMap.get(gt.getKey());
            if (dbInvGroupedTag != null) {
                if (dbInvGroupedTag.hasGroup()) {
                    if (gt.getValue().hasGroup()) {
                        if (!gt.getValue().getGroup().equals(dbInvGroupedTag.getGroup())) {
                            throw new JocBadRequestException(String.format("The tag '%s' has already the group '%s'", gt.getKey(), dbInvGroupedTag
                                    .getGroup().get()));
                        }
                    } else {
                        gt.getValue().setGroup(dbInvGroupedTag.getGroup().get());
                    }
                } else if (gt.getValue().hasGroup()) {
                    throw new JocBadRequestException(String.format("The tag '%s' is already used as %s tag without a group", gt.getKey(), type));
                }
            }
        }
    }

}