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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.IDBItemTag;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.common.ATagDBLayer;
import com.sos.joc.db.inventory.items.InventoryTagItem;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.inventory.InventoryTagAddEvent;
import com.sos.joc.event.bean.inventory.InventoryTagDeleteEvent;
import com.sos.joc.event.bean.inventory.InventoryTagsEvent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.tag.Tags;
import com.sos.joc.model.tag.TagsUsedBy;
import com.sos.joc.model.tag.common.RequestFilters;
import com.sos.joc.model.tag.common.RequestFolder;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

public abstract class ATagsModifyImpl<T extends IDBItemTag> extends JOCResourceImpl {

    protected enum Action {
        ADD, DELETE, ORDERING
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
        Set<String> tags = modifyTags.getTags() == null ? Collections.emptySet() : modifyTags.getTags();
        Stream<JOCEvent> events = Stream.empty();
        
        switch (action) {
        case ADD:
            Set<T> result = add(tags, Date.from(Instant.now()), dbLayer);
            events = result.stream().map(T::getName).map(InventoryTagAddEvent::new);
            break;

        case DELETE:
            if (dbLayer instanceof InventoryJobTagDBLayer) {
                //change jobtags in workflow json
                List<Long> workflowIds = ((InventoryJobTagDBLayer) dbLayer).getWorkflowIdsHavingTags(tags.stream().collect(Collectors.toList()));
                InventoryDBLayer dbInvLayer = new InventoryDBLayer(dbLayer.getSession());
                for (Long workflowId : workflowIds) {
                    DBItemInventoryConfiguration conf = JocInventory.getConfiguration(dbInvLayer, workflowId, null, null, folderPermissions);
                    Workflow worfklow = WorkflowConverter.convertInventoryWorkflow(conf.getContent());
                    boolean tagsFound = false;
                    if (worfklow.getJobs() != null && worfklow.getJobs().getAdditionalProperties() != null) {
                        for(Job job : worfklow.getJobs().getAdditionalProperties().values()) {
                            if (job.getJobTags() != null) {
                                if (job.getJobTags().removeAll(tags)) {
                                    tagsFound = true;
                                    if (job.getJobTags().isEmpty()) {
                                        job.setJobTags(null);
                                    }
                                }
                            }
                        }
                    }
                    if (tagsFound) {
                        conf.setContent(JocInventory.toString(worfklow));
                        dbLayer.getSession().update(conf);
                    }
                }
            }
            
            List<T> dbTags = dbLayer.getTags(tags);
            Set<String> alreadyExistingTags = dbTags.stream().map(T::getName).collect(Collectors.toSet());
            tags.retainAll(alreadyExistingTags);
            // IMPORTANT! first taggings, then tags
            dbLayer.deleteTaggings(tags); // TODO events for Workflows
            dbLayer.deleteTags(tags);

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

    public Set<T> add(Set<String> tags, Date date, ATagDBLayer<T> dbLayer) throws SOSHibernateException {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }
        List<T> dbTags = dbLayer.getTags(tags);
        Set<String> alreadyExistingTags = dbTags.stream().map(T::getName).collect(Collectors.toSet());
        tags.removeAll(alreadyExistingTags);
        int maxOrdering = dbLayer.getMaxOrdering();
        Set<T> result = new HashSet<>();
        Class<T> typedDbItemClazz = createTypedDBItemClass();
        for (String name : tags) {
            SOSCheckJavaVariableName.test("tag name: ", name);
            T item = createTypedDBItem(typedDbItemClazz);
            item.setId(null);
            item.setModified(date);
            item.setName(name);
            item.setOrdering(++maxOrdering);
            dbLayer.getSession().save(item);
            result.add((T) item);
        }
        return result;
    }

    private Tags postTags(String apiCall, ATagDBLayer<T> dbLayer) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(apiCall);
            dbLayer.setSession(session);

            Tags entity = new Tags();
            entity.setTags(dbLayer.getAllTagNames());
            entity.setDeliveryDate(Date.from(Instant.now()));

            return entity;
        } finally {
            Globals.disconnect(session);
        }
    }

    protected JOCDefaultResponse postTags(String apiCall, String accessToken, ATagDBLayer<T> dbLayer) {
        try {
            initLogging(apiCall, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Tags entity = postTags(apiCall, dbLayer);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
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

}