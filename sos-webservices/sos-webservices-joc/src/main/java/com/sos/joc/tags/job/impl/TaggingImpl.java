package com.sos.joc.tags.job.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.tag.ATagsModifyImpl;
import com.sos.joc.classes.tag.GroupedTag;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJobTag;
import com.sos.joc.db.inventory.DBItemInventoryJobTagging;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.InventoryJobTagDBLayer;
import com.sos.joc.db.inventory.InventoryOrderTagDBLayer;
import com.sos.joc.db.inventory.InventoryTagDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.inventory.InventoryJobTagAddEvent;
import com.sos.joc.event.bean.inventory.InventoryJobTagEvent;
import com.sos.joc.event.bean.inventory.InventoryTagEvent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.tag.JobsTags;
import com.sos.joc.model.tag.common.JobTags;
import com.sos.joc.model.tag.common.RequestWorkflowJobFilter;
import com.sos.joc.model.tag.tagging.RequestJobFilter;
import com.sos.joc.tags.job.resource.ITagging;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("inventory")
public class TaggingImpl extends JOCResourceImpl implements ITagging {


    @Override
    public JOCDefaultResponse postTagging(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            RequestJobFilter in = initRequest(IMPL_PATH_TAGGING, RequestJobFilter.class, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TAGGING);
            session.setAutoCommit(false);
            session.beginTransaction();
            InventoryJobTagDBLayer dbTagLayer = new InventoryJobTagDBLayer(session);
            
            DBItemInventoryConfiguration config = getConfiguration(in.getPath(), new InventoryDBLayer(session));
            // without checking if job exists because Workflow is not completely stored when this API is called.
//            Workflow workflow = WorkflowConverter.convertInventoryWorkflow(config.getContent());
//            Jobs jobs = workflow.getJobs();
//            if (jobs == null || jobs.getAdditionalProperties() == null) {
//                throw new JocBadRequestException(String.format("Workflow '%s' doesn't have jobs", config.getPath()));
//            }
            Set<InventoryTagEvent> tagEvents = storeTaggings(in.getJobs(), config, dbTagLayer);
            
            Globals.commit(session);
            
            tagEvents.forEach(evt -> EventBus.getInstance().post(evt));
            
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
    
    // used for import
    public static Set<InventoryTagEvent> storeTaggings(Map<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflows,
            Map<String, Long> dbGroupsMap, InventoryJobTagDBLayer dbTagLayer, boolean checkGroupModerate) throws SOSHibernateException {
        Set<InventoryTagEvent> tagEvents = new HashSet<>();

        for (Map.Entry<DBItemInventoryConfiguration, Set<JobTags>> jobTagsPerWorkflow : jobTagsPerWorkflows.entrySet()) {
            tagEvents.addAll(storeTaggings(jobTagsPerWorkflow.getValue(), jobTagsPerWorkflow.getKey(), dbGroupsMap, dbTagLayer, checkGroupModerate));
        }

        return tagEvents;
    }
    
    private static Set<InventoryTagEvent> storeTaggings(Set<JobTags> inJobTags, DBItemInventoryConfiguration config,
            InventoryJobTagDBLayer dbTagLayer) throws SOSHibernateException {
        return storeTaggings(inJobTags, config, null, dbTagLayer, false);
    }
    
    private static Set<InventoryTagEvent> storeTaggings(Set<JobTags> inJobTags, DBItemInventoryConfiguration config, Map<String, Long> dbGroupsMap,
            InventoryJobTagDBLayer dbTagLayer, boolean checkGroupModerate) throws SOSHibernateException {
        Set<InventoryTagEvent> tagEvents = new HashSet<>();

        Set<JobTags> jobTags = inJobTags == null ? Collections.emptySet() : inJobTags;
        Date date = Date.from(Instant.now());
        Set<DBItemInventoryJobTagging> dbTaggings = dbTagLayer.getTaggings(config.getId());
        boolean taggingIsChanged = false;
        
        for (JobTags jobTag : jobTags) {
            
//            if (!jobs.getAdditionalProperties().containsKey(jobTag.getJobName())) {
//                throw new JocBadRequestException(String.format("Workflow '%s' doesn't have the job '%s'", config.getPath(), jobTag.getJobName()));
//            }
            Set<String> tags = jobTag.getJobTags() == null ? Collections.emptySet() : jobTag.getJobTags();
            Validator.testJavaNameRulesAtTags("$.jobs[" + jobTag.getJobName() + "].jobTags: ", tags);
            
            Map<String, GroupedTag> groupedTags = tags.stream().map(GroupedTag::new).distinct().collect(Collectors.toMap(GroupedTag::getTag,
                    Function.identity()));
            List<DBItemInventoryJobTag> dbJobTags = tags.isEmpty() ? Collections.emptyList() : dbTagLayer.getTags(groupedTags.keySet());

            ATagsModifyImpl.checkAndAssignGroup(groupedTags, new InventoryTagDBLayer(dbTagLayer.getSession()), "workflow", checkGroupModerate);
            ATagsModifyImpl.checkAndAssignGroup(groupedTags, new InventoryOrderTagDBLayer(dbTagLayer.getSession()), "order", checkGroupModerate);
            //TODO the same with historyOrderTags??

            Set<DBItemInventoryJobTag> newDbTagItems = new TagsModifyImpl().insert(groupedTags.values(), dbJobTags, dbGroupsMap, date, dbTagLayer);
            
            tagEvents.addAll(newDbTagItems.stream().map(DBItemInventoryJobTag::getName).map(InventoryJobTagAddEvent::new).collect(Collectors.toSet()));
            newDbTagItems.addAll(dbJobTags);
            
            Map<String, Long> tagNameToIdMap = newDbTagItems.stream().collect(Collectors.toMap(DBItemInventoryJobTag::getName,
                    DBItemInventoryJobTag::getId));
            
            Set<DBItemInventoryJobTagging> requestedTaggings = groupedTags.keySet().stream().map(tagName -> {
                DBItemInventoryJobTagging taggingItem = new DBItemInventoryJobTagging();
                taggingItem.setCid(config.getId());
                taggingItem.setJobName(jobTag.getJobName());
                taggingItem.setTagId(tagNameToIdMap.get(tagName));
                taggingItem.setWorkflowName(config.getName());
                taggingItem.setId(null);
                taggingItem.setModified(date);
                return taggingItem;
            }).collect(Collectors.toSet());
            
            for (DBItemInventoryJobTagging requestedTagging : requestedTaggings) {
                if (dbTaggings.contains(requestedTagging)) {
                    dbTaggings.remove(requestedTagging);
                } else {
                    dbTagLayer.getSession().save(requestedTagging);
                    taggingIsChanged = true;
                }
            }
            for (DBItemInventoryJobTagging dbTagging : dbTaggings) {
                if (dbTagging.getJobName().equals(jobTag.getJobName())) {
                    dbTagLayer.getSession().delete(dbTagging);
                    taggingIsChanged = true;
                }
            }
            // TODO delete obsolete Tags from INV_JOB_TAGS table
            if (!dbTaggings.isEmpty()) {
                dbTaggings.removeIf(i -> i.getJobName().equals(jobTag.getJobName()));
            }
        }
        
        if (taggingIsChanged) {
            tagEvents.add(new InventoryJobTagEvent(config.getName())); //TODO event for job tags? workflowname?
        }
        
        return tagEvents;
    }
    
    @Override
    public JOCDefaultResponse postRenameTagging(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            com.sos.joc.model.tag.rename.RequestJobFilter in = initRequest(IMPL_RENAME_TAGGING, com.sos.joc.model.tag.rename.RequestJobFilter.class,
                    accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            SOSCheckJavaVariableName.test("$.newJobName", in.getNewJobName());
            
            String workflowName = JocInventory.pathToName(in.getPath());
            String workflowPath = WorkflowPaths.getPath(workflowName);
            
            checkFolderPermissions(workflowPath);
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_RENAME_TAGGING);
            session.setAutoCommit(false);
            session.beginTransaction();
            new InventoryJobTagDBLayer(session).renameJob(workflowName, in.getJobName(), in.getNewJobName());
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
    
    @Override
    public JOCDefaultResponse postUsed(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            RequestWorkflowJobFilter in = initRequest(IMPL_PATH_TAGS, RequestWorkflowJobFilter.class, accessToken, filterBytes);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getBasicJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_TAGS);
            DBItemInventoryConfiguration config = getConfiguration(in.getPath(), new InventoryDBLayer(session));
            
            InventoryJobTagDBLayer dbLayer = new InventoryJobTagDBLayer(session);
            JobsTags entity = new JobsTags();
            entity.setJobs(dbLayer.getTagsWithGroups(config.getId(), in.getJobNames()).entrySet().stream().map(e -> {
                JobTags jt = new JobTags();
                jt.setJobName(e.getKey());
                jt.setJobTags(e.getValue());
                return jt;
            }).collect(Collectors.toSet()));
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
    
    private <T> T initRequest(String apiCall, Class<T> clazz, String accessToken, byte[] filterBytes) throws Exception {
        filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.INVENTORY);
        JsonValidator.validateFailFast(filterBytes, clazz);
        return Globals.objectMapper.readValue(filterBytes, clazz);
    }
    
    private DBItemInventoryConfiguration getConfiguration(String path, InventoryDBLayer dbLayer) throws Exception {
        com.sos.joc.model.inventory.common.RequestFilter filter = new com.sos.joc.model.inventory.common.RequestFilter();
        filter.setObjectType(ConfigurationType.WORKFLOW);
        filter.setPath(path);
        return JocInventory.getConfiguration(dbLayer, filter, folderPermissions);
    }
    
}