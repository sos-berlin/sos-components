package com.sos.joc.classes.dependencies;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.job.Job;
import com.sos.joc.Globals;
import com.sos.joc.classes.dependencies.callables.ReferenceCallable;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DependencyResolver {


    /*
     * This class contains utility methods to determine, read and write related configuration objects for a given configuration
     * 
     * */
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyResolver.class);
    
    public static final String INSTRUCTION_LOCKS_SEARCH = "lockIds";
    public static final String INSTRUCTION_BOARDS_SEARCH = "noticeBoardNames";
    public static final String INSTRUCTION_ADDORDERS_SEARCH = "addOrders";
    public static final String WORKFLOWNAME_SEARCH = "workflowName";
    public static final String WORKFLOWNAMES_SEARCH = "workflowNames";
    public static final String JOBRESOURCENAMES_SEARCH = "jobResourceNames";
    public static final String LOCKNAME_SEARCH = "lockName";
    public static final String CALENDARNAME_SEARCH = "calendarName";
    public static final String JOBTEMPLATE_SEARCH = "jobTemplate";

    public static final List<Integer> dependencyTypes = Collections.unmodifiableList(new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;
        {
            add(ConfigurationType.WORKFLOW.intValue());
            add(ConfigurationType.FILEORDERSOURCE.intValue());
            add(ConfigurationType.JOBRESOURCE.intValue());
            add(ConfigurationType.JOBTEMPLATE.intValue());
            add(ConfigurationType.LOCK.intValue());
            add(ConfigurationType.NOTICEBOARD.intValue());
            add(ConfigurationType.SCHEDULE.intValue());
            add(ConfigurationType.WORKINGDAYSCALENDAR.intValue());
            add(ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
        }
      });

    
    public static ReferencedDbItem resolve(SOSHibernateSession session, String name, ConfigurationType type)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        DBItemInventoryConfiguration dbItem = null;
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        List<DBItemInventoryConfiguration> allConfigs = dbLayer.getConfigurationsByType(DependencyResolver.dependencyTypes);
        if(!ConfigurationType.FOLDER.equals(type)) {
            Optional<DBItemInventoryConfiguration> optItem = allConfigs.stream()
                    .filter(item -> item.getName().equals(name) && item.getTypeAsEnum().equals(type)).findFirst();
            if(optItem.isPresent()) {
                dbItem = optItem.get();
            }
        }
        Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems = allConfigs.stream() .collect(
                Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum, 
                        Collectors.toMap(DBItemInventoryConfiguration::getName, Function.identity())));
        DependencyResolver.dependencyTypes.forEach(ctype -> groupedItems.putIfAbsent(ConfigurationType.fromValue(ctype), Collections.emptyMap()));

        ReferencedDbItem resolvedItem = resolveReferencedBy(dbItem, groupedItems);
        resolveReferences(resolvedItem, groupedItems);
        return resolvedItem;
    }

    // with db access for dependency resolution of a single or few items ot to use with threading
    public static ReferencedDbItem resolveReferencedBy(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem) throws SOSHibernateException {
        // this method is in use
        ReferencedDbItem cfg = new ReferencedDbItem(inventoryDbItem);
        cfg.setReferencedItem(inventoryDbItem);
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        switch(inventoryDbItem.getTypeAsEnum()) {
        case LOCK:
            List<DBItemInventoryConfiguration> workflowsWithLocks =  dbLayer.getUsedWorkflowsByLockId(inventoryDbItem.getName());
            if(workflowsWithLocks != null) {
                cfg.getReferencedBy().addAll(workflowsWithLocks);
            }
            break;
        case JOBRESOURCE:
            List<DBItemInventoryConfiguration> workflowsWithJobResource =  dbLayer.getUsedWorkflowsByJobResource(inventoryDbItem.getName());
            if(workflowsWithJobResource != null) {
                cfg.getReferencedBy().addAll(workflowsWithJobResource);
            }
            List<DBItemInventoryConfiguration> jobTemplatesByJobResource = dbLayer.getUsedJobTemplatesByJobResource(inventoryDbItem.getName());
            if(jobTemplatesByJobResource != null) {
                cfg.getReferencedBy().addAll(jobTemplatesByJobResource);
            }
            break;
        case NOTICEBOARD:
            List<DBItemInventoryConfiguration> workflowsWithNoticeBoards =  dbLayer.getUsedWorkflowsByBoardName(inventoryDbItem.getName());
            if(workflowsWithNoticeBoards != null) {
                cfg.getReferencedBy().addAll(workflowsWithNoticeBoards);
            }
            break;
        case WORKFLOW:
            List<DBItemInventoryConfiguration> fileOrderSources = dbLayer.getUsedFileOrderSourcesByWorkflowName(inventoryDbItem.getName());
            if (fileOrderSources != null) {
                cfg.getReferencedBy().addAll(fileOrderSources);
            }
            List<DBItemInventoryConfiguration> workflowSchedules = dbLayer.getUsedSchedulesByWorkflowName(inventoryDbItem.getName());
            if(workflowSchedules != null) {
                cfg.getReferencedBy().addAll(workflowSchedules);
            }
            List<DBItemInventoryConfiguration> workflowsWithInstruction = dbLayer.getUsedWorkflowsByAddOrdersWorkflowName(
                    inventoryDbItem.getName());
            if(workflowsWithInstruction != null) {
                cfg.getReferencedBy().addAll(workflowsWithInstruction);
            }
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            List<DBItemInventoryConfiguration> schedulesBasedOnCalendar = dbLayer.getUsedSchedulesByCalendarName(inventoryDbItem.getName());
            if(schedulesBasedOnCalendar != null) {
                cfg.getReferencedBy().addAll(schedulesBasedOnCalendar);
            }
            break;
        case JOBTEMPLATE:
            List<DBItemInventoryConfiguration> workflowsByJobTemplate = dbLayer.getUsedWorkflowsByJobTemplateName(inventoryDbItem.getName());
            if(workflowsByJobTemplate != null) {
                cfg.getReferencedBy().addAll(workflowsByJobTemplate);
            }
            break;
        default:
            break;
        }
        return cfg;
    }
    
    // with minimal db access for dependency resolution of a collection of items
    public static ReferencedDbItem resolveReferencedBy(DBItemInventoryConfiguration inventoryDbItem, Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        // this method is currently not in use
        ReferencedDbItem cfg = new ReferencedDbItem(inventoryDbItem);
        cfg.setReferencedItem(inventoryDbItem);
        switch(inventoryDbItem.getTypeAsEnum()) {
        case LOCK:
            resolveLockReferencedByWorkflow(cfg, groupedItems.get(ConfigurationType.WORKFLOW).values());
            break;
        case JOBRESOURCE:
            resolveJobResourcesReferencedByWorkflow(cfg, groupedItems.get(ConfigurationType.WORKFLOW).values());
            break;
        case NOTICEBOARD:
            resolveBoardReferencedByWorkflow(cfg, groupedItems.get(ConfigurationType.WORKFLOW).values());
            break;
        case WORKFLOW:
            resolveWorkflowReferencedBy(cfg, groupedItems);
            break;
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
            resolveCalendarReferencedBySchedule(cfg, groupedItems.get(ConfigurationType.SCHEDULE).values());
            break;
        case JOBTEMPLATE:
            resolveJobTemplateReferencedByWorkflow(cfg, groupedItems.get(ConfigurationType.WORKFLOW).values());
            break;
        default:
            break;
        }
        return cfg;
    }
    
    public static void resolveLockReferencedByWorkflow(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> workflows) {
        for(DBItemInventoryConfiguration cfg : workflows) {
            String json = cfg.getContent();
            JsonObject workflow = jsonObjectFromString(json);
            //Lock
            List<String> wfLockNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, LOCKNAME_SEARCH, wfLockNames);
            if(!wfLockNames.isEmpty()) {
                for (String lock : wfLockNames) {
                    if(lock.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(cfg);
                    }
                }
            }
        }
    }
    
    public static void resolveJobResourcesReferencedByWorkflow(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> cfgs) {
        for(DBItemInventoryConfiguration cfg : cfgs) {
            String json = cfg.getContent();
            JsonObject workflow = jsonObjectFromString(json);
            List<String> wfJobResourceNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, INSTRUCTION_ADDORDERS_SEARCH, wfJobResourceNames);
            if(!wfJobResourceNames.isEmpty()) {
                for (String jobResource : wfJobResourceNames) {
                    if(jobResource.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(cfg);
                    }
                }
            }
        }
    }

    public static void resolveBoardReferencedByWorkflow(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> cfgs) {
        for(DBItemInventoryConfiguration cfg : cfgs) {
            String json = cfg.getContent();
            JsonObject workflow = jsonObjectFromString(json);
            List<String> wfBoardNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, INSTRUCTION_BOARDS_SEARCH, wfBoardNames);
            if(!wfBoardNames.isEmpty()) {
                for (String board : wfBoardNames) {
                    if(board.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(cfg);
                    }
                }
            }
        }
    }

    public static void resolveWorkflowReferencedBy(ReferencedDbItem item, Map<ConfigurationType, Map<String, DBItemInventoryConfiguration>> groupedItems) {
        // resolve workflows (add order instructions)
        for(DBItemInventoryConfiguration cfg : groupedItems.get(ConfigurationType.WORKFLOW).values()) {
            String json = cfg.getContent();
            JsonObject workflow = jsonObjectFromString(json);
            List<String> wfWorkflowNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, WORKFLOWNAME_SEARCH, wfWorkflowNames);
            if(!wfWorkflowNames.isEmpty()) {
                for (String wf : wfWorkflowNames) {
                    if(wf.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(cfg);
                    }
                }
            }
        }
        // file order sources
        for(DBItemInventoryConfiguration fos : groupedItems.get(ConfigurationType.FILEORDERSOURCE).values()) {
            String json = fos.getContent();
            JsonObject fosItem = jsonObjectFromString(json);
            List<String> fosWorkflows = new ArrayList<String>();
            getValuesRecursively("", fosItem, WORKFLOWNAME_SEARCH, fosWorkflows);
            if(!fosWorkflows.isEmpty()) {
                for(String wf : fosWorkflows) {
                    if(wf.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(fos);
                    }
                }
            }
            
        }
        // schedules
        for(DBItemInventoryConfiguration schedule : groupedItems.get(ConfigurationType.SCHEDULE).values()) {
            String json = schedule.getContent();
            JsonObject scheduleItem = jsonObjectFromString(json);
            List<String> scheduleWorkflows = new ArrayList<String>();
            getValuesRecursively("", scheduleItem, WORKFLOWNAMES_SEARCH, scheduleWorkflows);
            if(!scheduleWorkflows.isEmpty()) {
                for(String wf : scheduleWorkflows) {
                    if(wf.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(schedule);
                    }
                }
            }
        }
    }

    public static void resolveCalendarReferencedBySchedule(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> cfgs) {
        for(DBItemInventoryConfiguration schedule : cfgs) {
            String json = schedule.getContent();
            JsonObject scheduleItem = jsonObjectFromString(json);
            List<String> wfCalendarNames = new ArrayList<String>(); 
            getValuesRecursively("", scheduleItem, CALENDARNAME_SEARCH, wfCalendarNames);
            if(!wfCalendarNames.isEmpty()) {
                for (String cal : wfCalendarNames) {
                    if(cal.replaceAll("\"","").equals(item.getName())) {
                        item.getReferencedBy().add(schedule);
                    }
                }
            }
        }
    }

    public static void resolveJobTemplateReferencedByWorkflow(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> cfgs)
            throws JsonMappingException, JsonProcessingException {
        for(DBItemInventoryConfiguration cfg : cfgs) {
            Workflow workflow = Globals.objectMapper.readValue(cfg.getContent(), Workflow.class);
            if(workflow.getJobs() != null) {
                Map<String,Job> jobs = workflow.getJobs().getAdditionalProperties();
                for(Job job : jobs.values()) {
                    if(job.getJobTemplate() != null) {
                        if(job.getJobTemplate().getName().equals(item.getName())) {
                            item.getReferencedBy().add(cfg);
                        }
                    }
                }
            }
        }
    }

    // with db access for dependency resolution of a single or few items or to use in with threading
    public static void resolveReferences (ReferencedDbItem item, SOSHibernateSession session)
            throws JsonMappingException, JsonProcessingException {
        // this method is in use
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        String json = item.getReferencedItem().getContent();
        List<DBItemInventoryConfiguration> results = null;
        JsonObject instructions = null;
        switch (item.getReferencedItem().getTypeAsEnum()) {
        // determine references in configurations json
        case WORKFLOW:
            DBItemSearchWorkflow wfSearch = dbLayer.getSearchWorkflow(item.getId(), null);
            if(wfSearch != null) {
                instructions = jsonObjectFromString(wfSearch.getInstructions());
            }
            JsonObject workflow = jsonObjectFromString(json);
            //Lock
            if(instructions != null) {
                List<String> lockIds = getValuesFromInstructions(instructions, INSTRUCTION_LOCKS_SEARCH);
                for(String lockId : lockIds) {
                    results = dbLayer.getConfigurationByName(lockId.replaceAll("\"",""), ConfigurationType.LOCK.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            } else {
                List<String> wfLockNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, LOCKNAME_SEARCH, wfLockNames);
                if(!wfLockNames.isEmpty()) {
                    for (String lock : wfLockNames) {
                        results = dbLayer.getConfigurationByName(lock.replaceAll("\"",""), ConfigurationType.LOCK.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                }
            }
            //JobResource
            if(instructions != null) {
                List<String> wfInstructionJobResourceNames = getValuesFromInstructions(instructions, JOBRESOURCENAMES_SEARCH);
                for(String jobResourceName : wfInstructionJobResourceNames) {
                    results = dbLayer.getConfigurationByName(jobResourceName.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            } else {
                List<String> wfJobResourceNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, INSTRUCTION_ADDORDERS_SEARCH, wfJobResourceNames);
                if(!wfJobResourceNames.isEmpty()) {
                    for (String jobResource : wfJobResourceNames) {
                        results = dbLayer.getConfigurationByName(jobResource.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                }
            }
            //NoticeBoards
            if(instructions != null) {
                List<String> boardNames = getValuesFromInstructions(instructions, INSTRUCTION_BOARDS_SEARCH);
                for(String boardName : boardNames) {
                    results = dbLayer.getConfigurationByName(boardName.replaceAll("\"",""), ConfigurationType.NOTICEBOARD.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            } else {
                List<String> wfBoardNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, INSTRUCTION_BOARDS_SEARCH, wfBoardNames);
                if(!wfBoardNames.isEmpty()) {
                    for (String board : wfBoardNames) {
                        results = dbLayer.getConfigurationByName(board.replaceAll("\"",""), ConfigurationType.NOTICEBOARD.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                }
            }
            //Workflow
            if(instructions != null) {
                List<String> wfInstructionWorkflowNames = getValuesFromInstructions(instructions, INSTRUCTION_ADDORDERS_SEARCH);
                for(String workflowName : wfInstructionWorkflowNames) {
                    results = dbLayer.getConfigurationByName(workflowName.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            } else {
                List<String> wfWorkflowNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, WORKFLOWNAME_SEARCH, wfWorkflowNames);
                if(!wfWorkflowNames.isEmpty()) {
                    for (String wf : wfWorkflowNames) {
                        results = dbLayer.getConfigurationByName(wf.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                }
            }
            // ScriptIncludes
            if(json.contains("##!include")) {
                
            }
            break;
        case SCHEDULE:
            // Workflow
            JsonObject schedule = jsonObjectFromString(json);
            List<String> scheduleWorkflows = new ArrayList<String>();
            getValuesRecursively("", schedule, WORKFLOWNAMES_SEARCH, scheduleWorkflows);
            if(!scheduleWorkflows.isEmpty()) {
                for(String wf : scheduleWorkflows) {
                    results = dbLayer.getConfigurationByName(wf.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            }
            break;
        case JOBTEMPLATE:
            // jobResource
            JsonObject jobTemplate = jsonObjectFromString(json);
            List<String> jobTemplateJobResources = new ArrayList<String>();
            getValuesRecursively("", jobTemplate, JOBRESOURCENAMES_SEARCH, jobTemplateJobResources);
            if(!jobTemplateJobResources.isEmpty()) {
                for(String jobresource : jobTemplateJobResources) {
                    results = dbLayer.getConfigurationByName(jobresource.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            }
            break;
        case FILEORDERSOURCE:
            // Workflow
            JsonObject fos = jsonObjectFromString(json);
            List<String> fosWorkflows = new ArrayList<String>();
            getValuesRecursively("", fos, WORKFLOWNAME_SEARCH, fosWorkflows);
            if(!fosWorkflows.isEmpty()) {
                for(String wf : fosWorkflows) {
                    results = dbLayer.getConfigurationByName(wf.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            }
            break;
        default:
            break;
        }
    }
    
    // with no further db access for dependency resolution of a collection of items
    public static ReferencedDbItem resolveReferences (ReferencedDbItem item, Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems)
            throws JsonMappingException, JsonProcessingException {
        // this method is currently not in use
        String json = item.getReferencedItem().getContent();
        switch (item.getReferencedItem().getTypeAsEnum()) {
        // determine references in configurations json
        case WORKFLOW:
            JsonObject workflow = jsonObjectFromString(json);
            //Lock
            List<String> wfLockNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, LOCKNAME_SEARCH, wfLockNames);
            if(!wfLockNames.isEmpty()) {
                for (String lock : wfLockNames) {
                    DBItemInventoryConfiguration lockItem = groupedItems.get(ConfigurationType.LOCK).get(lock);
                    if(lockItem != null) {
                        item.getReferences().add(lockItem);
                    }
                }
            }
            //JobResource
            List<String> wfJobResourceNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, JOBRESOURCENAMES_SEARCH, wfJobResourceNames);
            if(!wfJobResourceNames.isEmpty()) {
                for (String jobResource : wfJobResourceNames) {
                    DBItemInventoryConfiguration jobResourceItem = groupedItems.get(ConfigurationType.JOBRESOURCE).get(jobResource);
                    if(jobResourceItem != null) {
                        item.getReferences().add(jobResourceItem);
                    }
                }
            }
            //NoticeBoards
            List<String> wfBoardNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, INSTRUCTION_BOARDS_SEARCH, wfBoardNames);
            if(!wfBoardNames.isEmpty()) {
                for (String board : wfBoardNames) {
                    DBItemInventoryConfiguration boardItem = groupedItems.get(ConfigurationType.NOTICEBOARD).get(board);
                    if(boardItem != null) {
                        item.getReferences().add(boardItem);
                    }
                }
            }
            //Workflow
            List<String> wfWorkflowNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, WORKFLOWNAME_SEARCH, wfWorkflowNames);
            if(!wfWorkflowNames.isEmpty()) {
                for (String wf : wfWorkflowNames) {
                    DBItemInventoryConfiguration workflowItem = groupedItems.get(ConfigurationType.WORKFLOW).get(wf);
                    if(workflowItem != null) {
                        item.getReferences().add(workflowItem);
                    }
                }
            }
            // ScriptIncludes
            if(json.contains("##!include")) {
                
            }
            break;
        case SCHEDULE:
            JsonObject schedule = jsonObjectFromString(json);
            // Workflow
            List<String> scheduleWorkflows = new ArrayList<String>();
            getValuesRecursively("", schedule, WORKFLOWNAMES_SEARCH, scheduleWorkflows);
            if(!scheduleWorkflows.isEmpty()) {
                for(String wf : scheduleWorkflows) {
                    DBItemInventoryConfiguration workflowItem = groupedItems.get(ConfigurationType.WORKFLOW).get(wf);
                    if(workflowItem != null) {
                        item.getReferences().add(workflowItem);
                    }
                }
            }
            break;
        case JOBTEMPLATE:
            // jobResource
            JsonObject jobTemplate = jsonObjectFromString(json);
            List<String> jobTemplateJobResources = new ArrayList<String>();
            getValuesRecursively("", jobTemplate, JOBRESOURCENAMES_SEARCH, jobTemplateJobResources);
            if(!jobTemplateJobResources.isEmpty()) {
                for(String jobresource : jobTemplateJobResources) {
                    DBItemInventoryConfiguration jobresourceItem = groupedItems.get(ConfigurationType.JOBRESOURCE).get(jobresource);
                    if(jobresourceItem != null) {
                        item.getReferences().add(jobresourceItem);
                    }
                }
            }
            break;
        case FILEORDERSOURCE:
            // Workflow
            JsonObject fos = jsonObjectFromString(json);
            List<String> fosWorkflows = new ArrayList<String>();
            getValuesRecursively("", fos, WORKFLOWNAME_SEARCH, fosWorkflows);
            if(!fosWorkflows.isEmpty()) {
                for(String wf : fosWorkflows) {
                    DBItemInventoryConfiguration workflowItem = groupedItems.get(ConfigurationType.WORKFLOW).get(wf);
                    if(workflowItem != null) {
                        item.getReferences().add(workflowItem);
                    }
                }
            }
            break;
        default:
            break;
        }
        return item;
    }
    
    /**
     * This method is used by JocInventory at each store operation
     * @param session
     * @param inventoryDbItem
     * @throws SOSHibernateException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    public static void updateDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        // this method is in use (JocInventory update and insert methods)
        boolean ownTransaction = false;
        if (!session.isAutoCommit() && !session.isTransactionOpened()) {
            session.beginTransaction();
            ownTransaction = true;
        }
        insertOrRenewDependencies(session, inventoryDbItem);
        if(ownTransaction) {
            session.commit();
        }
    }
    
    public static void updateDependencies(SOSHibernateSession session, List<DBItemInventoryConfiguration> allCfgs)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        // this method is in use
        boolean ownTransaction = false;
        if (!session.isAutoCommit() && !session.isTransactionOpened()) {
            session.beginTransaction();
            ownTransaction = true;
        }
        insertOrRenewDependencies(session, allCfgs);
        if(ownTransaction) {
            session.commit();
        }
    }
    
    /**
     * This method is used by the DependencyResolver as used in JocInventory at each store operation
     * @param session
     * @param inventoryDbItem
     * @throws SOSHibernateException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    public static void insertOrRenewDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        // this method is in use (JocInventory update and insert methods)
        ReferencedDbItem references = resolveReferencedBy(session, inventoryDbItem);
        resolveReferences(references, session);
        InventoryDBLayer layer = new InventoryDBLayer(session);
        // store new dependencies
        layer.insertOrReplaceDependencies(references.getReferencedItem(), convert(references, session));
    }

    /**
     * This method is used by the DependencyResolver as used in ./inventory/dependencies/update API for processing multiple objects 
     *  with a minimum of db accesses 
     *  
     * @param session
     * @param allCfgs
     * @throws SOSHibernateException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     * @throws InterruptedException
     */
    public static void insertOrRenewDependencies(SOSHibernateSession session, List<DBItemInventoryConfiguration> allCfgs)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        // this method is in use
        Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems = allCfgs.stream() .collect(
                Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum, 
                        Collectors.toMap(DBItemInventoryConfiguration::getName, Function.identity())));
        DependencyResolver.dependencyTypes.forEach(ctype -> groupedItems.putIfAbsent(ConfigurationType.fromValue(ctype), Collections.emptyMap()));
        InventoryDBLayer layer = new InventoryDBLayer(session);

        List<ReferenceCallable> callables = allCfgs.stream().map(item -> new ReferenceCallable(item, groupedItems)).collect(Collectors.toList());
        if(!callables.isEmpty()) {
            ExecutorService executorService = Executors.newFixedThreadPool(Math.min(callables.size(), 5));
            for (Future<ReferencedDbItem> result : executorService.invokeAll(callables)) {
                try {
                    ReferencedDbItem item = result.get();
                    // store new dependencies if direct or indirect references are present
                    if(!(item.getReferencedBy().isEmpty() && item.getReferences().isEmpty())) {
                        layer.insertOrReplaceDependencies(item.getReferencedItem(), convert(item, session));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
    }


    /**
     * This method is used by the DependencyResolver as used in ./inventory/dependencies/update API for processing multiple objects 
     *  with a minimum of db accesses 
     * @param session
     * @param inventoryDbItem
     * @param allCfgs
     * @throws SOSHibernateException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    public static void insertOrRenewDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem,
            Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems)
                    throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        // this method is currently not in use (method above with callables is currently used in the api)
        ReferencedDbItem references = resolveReferencedBy(inventoryDbItem, groupedItems);
        resolveReferences(references, groupedItems);
        // store new dependencies
        InventoryDBLayer layer = new InventoryDBLayer(session);
        layer.insertOrReplaceDependencies(references.getReferencedItem(), convert(references, session));

    }
    
    public static List<DBItemInventoryDependency> getStoredDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryObject)
            throws SOSHibernateException {
        // this method is in use
        List<DBItemInventoryDependency> dependencies = new ArrayList<DBItemInventoryDependency>();
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        dependencies = dbLayer.getDependencies(inventoryObject);
        return dependencies;
    }

    public static List<DBItemInventoryDependency> convert(ReferencedDbItem reference, SOSHibernateSession session) {
        // this method is currently not in use
        return reference.getReferencedBy().stream().map(item -> {
            DBItemInventoryDependency dependency = new DBItemInventoryDependency();
            dependency.setInvId(reference.getReferencedItem().getId());
            dependency.setDependencyType(item.getTypeAsEnum());
            dependency.setInvDependencyId(item.getId());
            dependency.setPublished(item.getDeployed() || item.getReleased());
            if(item.getDeployed()) {
                InventoryDBLayer dblayer = new InventoryDBLayer(session);
                try {
                    DBItemDeploymentHistory latestDeployed =  dblayer.getLatestActiveDepHistoryItem(reference.getReferencedItem().getId());
                    dependency.setDepDependencyId(latestDeployed.getId());
                    dependency.setControllerId(latestDeployed.getControllerId());
                } catch (SOSHibernateException e) {
                    throw new JocSosHibernateException(e);
                }
            }
            return dependency;
        }).collect(Collectors.toList());
    }
    
    public static ReferencedDbItem convert(SOSHibernateSession session, DBItemInventoryConfiguration invCfg, List<DBItemInventoryDependency> dependencies)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        // this method is in use
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        ReferencedDbItem newDbItem = new ReferencedDbItem(invCfg);
        if(dependencies != null && !dependencies.isEmpty()) {
            for(DBItemInventoryDependency dependency : dependencies) {
                DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(dependency.getInvId());
                if(cfg != null) {
                    newDbItem.getReferencedBy().add(cfg);
                }
            }
        }
        resolveReferences(newDbItem, session);
        return newDbItem;
    }
    
    public static JsonObject jsonObjectFromString(String jsonAsString) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonAsString));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
    
    /**
     * This method processes a flat json structure as given from the INSTRUCTIONS column of the SEARCH_WORKFLOWS Table
     *  (fast)
     * 
     * @param jsonObj
     * @param searchKey
     * @return a List of string values found to the given search key
     */
    public static List<String> getValuesFromInstructions(JsonObject jsonObj, String searchKey) {
        List<String> foundValues = new ArrayList<String>();
        if (jsonObj.containsKey(searchKey)) {
            JsonArray params = jsonObj.getJsonArray(searchKey);
            for (int i = 0; i < params.size(); i++) {
                String value = params.get(i).toString();
                if(value != null) {
                    foundValues.add(value);
                }
            }
        }
        return foundValues;
    }
    
    /**
     * This method processes the complex json structure from the CONTENT column of the INV_CONFIGURATIONS or the INV_CONTENT
     *  column from the DEP_HISTORY table.  As the method is called recursively it will not return any values. instead it will
     *  propagate the @param values List
     *  (slow)
     *  
     * @param previousKey initial key as a starting point in the json structure, for full structure processing set initial key to ""
     * @param currentObject
     * @param searchKey the key name to be searched
     * @param values List of values that are propagated while processing recursively
     * 
     */
    public static void getValuesRecursively(String previousKey, JsonObject currentObject, String searchKey, List<String> values) {
        // iterate each key
        for (String currentKey : currentObject.keySet()) {
            // build the next key
            String nextKey = previousKey == null || previousKey.isEmpty() ? currentKey : previousKey + "-" + currentKey;
            Object value = currentObject.get(currentKey);
            if (value instanceof JsonObject) {
                // if current value is object, call recursively with next key and value
                getValuesRecursively(nextKey, (JsonObject) value, searchKey, values);
            } else if (value instanceof JsonArray) {
                // if current value is array, iterate it
                JsonArray array = (JsonArray) value;
                for (int i = 0; i < array.size(); i++) {
                    Object object = array.get(i);
                    if(object instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) object;
                        // assuming current array member is object, call recursively with next key + index and current member
                        getValuesRecursively(nextKey + "-" + i, jsonObject, searchKey, values);
                        // TODO: might need to handle special case of current member being array
                    } else if (object instanceof JsonString) {
                        // assuming current array member is a string and next key is the search key, ends the recursion
                        if(nextKey.equals(searchKey)) {
                            values.add(((JsonString)object).getString());
                        }
                    }
                }
            } else {
                // value is neither object nor array and next key is the search key, ends the recursion
                if(nextKey.equals(searchKey)) {
                    values.add(value.toString().replaceAll("\"", ""));
                }
            }
        }
    }

}
