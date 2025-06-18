package com.sos.joc.classes.tag;

import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryTagGroup;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.inventory.InventoryGroupAddEvent;
import com.sos.joc.event.bean.inventory.InventoryGroupDeleteEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagAddEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagDeleteEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagsEvent;
import com.sos.joc.event.bean.inventory.InventoryTagAddEvent;
import com.sos.joc.event.bean.inventory.InventoryTagDeleteEvent;
import com.sos.joc.event.bean.inventory.InventoryTagsEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.tag.Tags;
import com.sos.joc.model.tag.TagsUsedBy;
import com.sos.joc.model.tag.common.RequestFilters;
import com.sos.joc.model.tag.common.RequestFolder;
import com.sos.joc.model.tag.group.Groups;
import com.sos.joc.model.tag.rename.RequestFilter;
import com.sos.schema.JsonValidator;

public abstract class ATagsModifyImpl<T extends IDBItemTag> extends JOCResourceImpl {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ATagsModifyImpl.class);

    protected enum Action {
        ADD, DELETE, ORDERING
    }
    
    protected enum ResponseObject {
        GROUPS, INVTAGS, JOBTAGS, ORDERTAGS
    }

    private Stream<JOCEvent> postTagsModify(ResponseObject responseObject, String apiCall, Action action, RequestFilters modifyTags,
            ATagDBLayer<T> dbLayer) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall + "/" + action.name().toLowerCase());
            session.setAutoCommit(false);
            session.beginTransaction();
            dbLayer.setSession(session);

            Stream<JOCEvent> events = postTagsModify(responseObject, action, modifyTags, dbLayer);

            Globals.commit(session);
            return events;
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private Stream<JOCEvent> postTagsModify(ResponseObject responseObject, Action action, RequestFilters modifyTags, ATagDBLayer<T> dbLayer)
            throws Exception {
        Map<String, GroupedTag> groupedTags = modifyTags.getTags() == null ? Collections.emptyMap() : modifyTags.getTags().stream().map(
                GroupedTag::new).distinct().collect(Collectors.toMap(GroupedTag::getTag, Function.identity()));
        List<T> dbTags = Collections.emptyList();
        Stream<JOCEvent> events = Stream.empty();
        
        switch (action) {
        case ADD:
            dbTags = modifyTags.getTags().isEmpty() ? Collections.emptyList() : dbLayer.getTags(groupedTags.keySet());
            switch (responseObject) {
            case INVTAGS:
                checkAndAssignGroup(groupedTags, new InventoryJobTagDBLayer(dbLayer.getSession()), "job");
                checkAndAssignGroup(groupedTags, new InventoryOrderTagDBLayer(dbLayer.getSession()), "order");
                break;
            case JOBTAGS:
                checkAndAssignGroup(groupedTags, new InventoryTagDBLayer(dbLayer.getSession()), "workflow");
                checkAndAssignGroup(groupedTags, new InventoryOrderTagDBLayer(dbLayer.getSession()), "order");
                break;
            default:
                break;    
            }
            
            Set<T> result = insert(groupedTags.values(), dbTags, Date.from(Instant.now()), dbLayer);
            switch (responseObject) {
            case INVTAGS:
                events = result.stream().map(T::getName).map(InventoryTagAddEvent::new);
                break;
            case JOBTAGS:
                events = result.stream().map(T::getName).map(InventoryJobTagAddEvent::new);
                break;
            default:
                break;    
            }
            break;

        case DELETE:
            dbTags = dbLayer.getTags(groupedTags.keySet());
            // IMPORTANT! first taggings, then tags
            dbLayer.deleteTaggingsByTagIds(dbTags.stream().map(T::getId).collect(Collectors.toList())); // TODO events for Workflows
            dbLayer.deleteTags(dbTags);

            switch (responseObject) {
            case INVTAGS:
                events = groupedTags.keySet().stream().map(InventoryTagDeleteEvent::new);
                break;
            case JOBTAGS:
                events = groupedTags.keySet().stream().map(InventoryJobTagDeleteEvent::new);
                break;
            default:
                break;    
            }
            break;

        case ORDERING:
            if (!modifyTags.getTags().isEmpty()) {
                List<T> dbAllTags = dbLayer.getAllTags();
                Map<String, T> mappedByName = dbAllTags.stream().collect(Collectors.toMap(T::getName, Function.identity()));
                int ordering = 1;
                for (String groupedTag : modifyTags.getTags()) {
                    String name = new GroupedTag(groupedTag).getTag();
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
                switch (responseObject) {
                case INVTAGS:
                    events = Stream.of(new InventoryTagsEvent());
                    break;
                case JOBTAGS:
                    events = Stream.of(new InventoryJobTagsEvent());
                    break;
                default:
                    break;    
                }
            }
            break;
        }
        return events;
    }
    
    public Set<T> insert(Collection<GroupedTag> groupedTags, List<T> oldDBTags, Date date, ATagDBLayer<T> dbLayer)
            throws SOSHibernateException {
        return insert(groupedTags, oldDBTags, null, date, dbLayer);
    }
    
    public Set<T> insert(Collection<GroupedTag> groupedTags, List<T> oldDBTags, Map<String, Long> dbGroupsMap, Date date, ATagDBLayer<T> dbLayer)
            throws SOSHibernateException {
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
            if (dbGroupsMap == null) {
                List<DBItemInventoryTagGroup> dbGroups = groups.isEmpty() ? Collections.emptyList() : dbLayer.getGroups(groups);
                dbGroupsMap = dbGroups.stream().collect(Collectors.toMap(DBItemInventoryTagGroup::getName,
                        DBItemInventoryTagGroup::getId));
            }
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

    protected JOCDefaultResponse postTagsOrGroups(ResponseObject responseObject, String apiCall, String accessToken, ATagDBLayer<T> dbLayer) {
        try {
            initLogging(apiCall, "{}".getBytes(), accessToken, CategoryType.INVENTORY);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            switch (responseObject) {
            case GROUPS:
                Groups groups = postGroups(apiCall, dbLayer);
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(groups));
            default: //case all tags
                Tags tags = postTags(apiCall, dbLayer);
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(tags));
            }
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    protected JOCDefaultResponse postUsedBy(String apiCall, String accessToken, byte[] filterBytes, ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.INVENTORY);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
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
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    protected JOCDefaultResponse postTagsModify(ResponseObject responseObject, String apiCall, Action action, String accessToken, byte[] filterBytes,
            ATagDBLayer<T> dbLayer) {
        try {
            RequestFilters modifyTags = initModifyRequest(apiCall, action, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(modifyTags.getAuditLog());
            Stream<JOCEvent> events = postTagsModify(responseObject, apiCall, action, modifyTags, dbLayer);
            events.forEach(evt -> EventBus.getInstance().post(evt));

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    protected JOCDefaultResponse postTagRename(ResponseObject responseObject, String apiCall, String accessToken, byte[] filterBytes,
            ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, RequestFilter.class);
            RequestFilter modifyTag = Globals.objectMapper.readValue(filterBytes, RequestFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(modifyTag.getAuditLog());
            
            GroupedTag groupedName = new GroupedTag(modifyTag.getName());
            GroupedTag groupedNewName = new GroupedTag(modifyTag.getNewName());
            
            if (groupedName.toString().equals(groupedNewName.toString())) {
                // nothing to do
                return responseStatusJSOk(Date.from(Instant.now()));
            }
            
            String objectName = ResponseObject.GROUPS.equals(responseObject) ? "group" : "tag";
            
            if (ResponseObject.GROUPS.equals(responseObject)) {
                SOSCheckJavaVariableName.test(objectName + " name: ", modifyTag.getNewName());
            } else {
                groupedNewName.checkJavaNameRules();
            }
            
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            session.setAutoCommit(false);
            session.beginTransaction();
            
            dbLayer.setSession(session);
            T tag = dbLayer.getTag(groupedName.getTag());
            if (tag == null) {
               throw new DBMissingDataException("Couldn't find " + objectName + " with name '" + groupedName.getTag() + "'");
            }
            T newTag = dbLayer.getTag(groupedNewName.getTag());
            if (newTag != null) {
                throw new DBMissingDataException(objectName + " with name '" + groupedName.getTag() + "' already exists.");
             }
            
            Date now = Date.from(Instant.now());
            DBItemInventoryTagGroup dbGroupItem = null;
            Map<String, GroupedTag> groupedTags = new HashMap<>(1);
            groupedTags.put(groupedNewName.getTag(), groupedNewName);

            // consider group
            switch (responseObject) {
            case INVTAGS:
                checkAndAssignGroup(groupedTags, new InventoryJobTagDBLayer(session), "job");
                checkAndAssignGroup(groupedTags, new InventoryOrderTagDBLayer(session), "order");
                groupedNewName = groupedTags.get(groupedNewName.getTag());
                dbGroupItem = getDbGroup(groupedNewName, now, dbLayer);
                break;
            case JOBTAGS:
                checkAndAssignGroup(groupedTags, new InventoryTagDBLayer(session), "workflow");
                checkAndAssignGroup(groupedTags, new InventoryOrderTagDBLayer(session), "order");
                groupedNewName = groupedTags.get(groupedNewName.getTag());
                dbGroupItem = getDbGroup(groupedNewName, now, dbLayer);
                break;
            case ORDERTAGS:
            case GROUPS:
                break;
            }

            tag.setName(groupedNewName.getTag());
            tag.setModified(now);
            if (dbGroupItem != null) {
                tag.setGroupId(dbGroupItem.getId()); 
            } else {
                tag.setGroupId(0L); 
            }
            dbLayer.getSession().update(tag);
            Globals.commit(session);
            
            switch (responseObject) {
            case INVTAGS:
                EventBus.getInstance().post(new InventoryTagAddEvent(modifyTag.getNewName()));
                EventBus.getInstance().post(new InventoryTagDeleteEvent(modifyTag.getName()));
                break;
            case JOBTAGS:
                EventBus.getInstance().post(new InventoryJobTagAddEvent(modifyTag.getNewName()));
                EventBus.getInstance().post(new InventoryJobTagDeleteEvent(modifyTag.getName()));
                break;
            case ORDERTAGS:
                break;
            case GROUPS:
                EventBus.getInstance().post(new InventoryGroupAddEvent(modifyTag.getNewName()));
                EventBus.getInstance().post(new InventoryGroupDeleteEvent(modifyTag.getName()));
                break;
            }
            
            
            return responseStatusJSOk(now);
        } catch (Exception e) {
            Globals.rollback(session);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private DBItemInventoryTagGroup getDbGroup(GroupedTag groupedNewName, Date now, ATagDBLayer<T> dbLayer) throws SOSHibernateException {
        if (groupedNewName.hasGroup()) {
            List<DBItemInventoryTagGroup> dbGroups = dbLayer.getGroups(Collections.singleton(groupedNewName.getGroup().get()));
            if (dbGroups.isEmpty()) {
                // insert group
                int maxGroupsOrdering = dbLayer.getMaxGroupsOrdering();
                DBItemInventoryTagGroup dbGroupItem = new DBItemInventoryTagGroup();
                dbGroupItem.setName(groupedNewName.getGroup().get());
                dbGroupItem.setModified(now);
                dbGroupItem.setOrdering(++maxGroupsOrdering);
                dbLayer.getSession().save(dbGroupItem);
                return dbGroupItem;
            } else {
                return dbGroups.get(0);
            }
        }
        return null;
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
        return initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
    }

    private RequestFilters initModifyRequest(String apiCall, Action action, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(apiCall + "/" + action.name().toLowerCase(), filterBytes, accessToken, CategoryType.INVENTORY);
        JsonValidator.validateFailFast(filterBytes, RequestFilters.class);
        return Globals.objectMapper.readValue(filterBytes, RequestFilters.class);
    }
    
    private Map<String, Set<String>> getUsedBy(Collection<Folder> folders, Set<Folder> permittedFolders, ATagDBLayer<T> dbLayer)
            throws SOSHibernateException {
        return dbLayer.getTagsByFolders(folders, true).stream().filter(i -> folderIsPermitted(i.getFolder(), permittedFolders)).collect(Collectors
                .groupingBy(InventoryTagItem::getNullableName, Collectors.mapping(InventoryTagItem::getPath, Collectors.toSet())));
    }
    
    public static <T extends IDBItemTag> void checkAndAssignGroup(Map<String, GroupedTag> groupedTags, ATagDBLayer<T> dbLayer, String type) {
        checkAndAssignGroup(groupedTags, dbLayer, type, false);
    }
    
    public static <T extends IDBItemTag> void checkAndAssignGroupModerate(Map<String, GroupedTag> groupedTags, ATagDBLayer<T> dbLayer, String type) {
        checkAndAssignGroup(groupedTags, dbLayer, type, true);
    }
    
    public static <T extends IDBItemTag> void checkAndAssignGroup(Map<String, GroupedTag> groupedTags, ATagDBLayer<T> dbLayer, String type,
            boolean withoutJocBadRequestException) {

        Map<String, GroupedTag> dbGroupedTagsMap = dbLayer.getGroupedTags(groupedTags.keySet()).stream().collect(Collectors.toMap(GroupedTag::getTag,
                Function.identity()));
        for (Map.Entry<String, GroupedTag> gt : groupedTags.entrySet()) {
            GroupedTag dbInvGroupedTag = dbGroupedTagsMap.get(gt.getKey());
            if (dbInvGroupedTag != null) {
                if (dbInvGroupedTag.hasGroup()) {
                    if (gt.getValue().hasGroup()) {
                        if (!gt.getValue().getGroup().equals(dbInvGroupedTag.getGroup())) {
                            if (withoutJocBadRequestException) {
                                gt.getValue().setGroup(dbInvGroupedTag.getGroup().get());
                                LOGGER.info(String.format("The tag '%s' has already the group '%s'. The group '%s' will be ignored.", gt.getKey(),
                                        dbInvGroupedTag.getGroup().get(), gt.getValue().getGroup()));
                            } else {
                                throw new JocBadRequestException(String.format("The tag '%s' has already the group '%s'", gt.getKey(), dbInvGroupedTag
                                        .getGroup().get()));
                            }
                        }
                    } else {
                        gt.getValue().setGroup(dbInvGroupedTag.getGroup().get());
                    }
                } else if (gt.getValue().hasGroup()) {
                    if (withoutJocBadRequestException) {
                        gt.getValue().setGroup(null);
                        LOGGER.info(String.format("The tag '%s' is already used as %s tag without a group. The group won't be used.", gt.getKey(), type));
                    } else {
                        throw new JocBadRequestException(String.format("The tag '%s' is already used as %s tag without a group", gt.getKey(), type));
                    }
                }
            }
        }
    }

}