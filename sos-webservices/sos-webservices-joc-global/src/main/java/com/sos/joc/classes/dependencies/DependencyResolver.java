package com.sos.joc.classes.dependencies;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class DependencyResolver {


    /*
     * This class contains utility methods to determine, read and write related configuration objects
     * 
     * */
    
    private static Logger LOGGER = LoggerFactory.getLogger(DependencyResolver.class);
    
    public static final String INSTRUCTION_LOCKS_SEARCH = "lockIds";
    public static final String INSTRUCTION_BOARDS_SEARCH = "noticeBoardNames";
    public static final String INSTRUCTION_ADDORDERS_SEARCH = "addOrders";
    public static final String WORKFLOWNAME_SEARCH = "workflowName";
    public static final String WORKFLOWNAMES_SEARCH = "workflowNames";
    public static final String JOBRESOURCENAMES_SEARCH = "jobResourceNames";
    
    public static ReferencedDbItem resolve(SOSHibernateSession session, ConfigurationObject inventoryObject)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        return resolve(session, inventoryObject.getName(), inventoryObject.getObjectType());
    }

    public static ReferencedDbItem resolve(SOSHibernateSession session, String name, ConfigurationType type)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        DBItemInventoryConfiguration dbItem = null;
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        if(!ConfigurationType.FOLDER.equals(type)) {
            List<DBItemInventoryConfiguration> results = dbLayer.getConfigurationByName(name, type.intValue());
            if(!results.isEmpty()) {
                dbItem = results.get(0); 
            }
        }
        ReferencedDbItem resolvedItem = resolveReferencedBy(session, dbItem);
        resolveReferences(resolvedItem, session);
        return resolvedItem;
    }

    public static ReferencedDbItem resolveReferencedBy(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem) throws SOSHibernateException {
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
    
    public static void resolveReferences (ReferencedDbItem item, SOSHibernateSession session) throws JsonMappingException, JsonProcessingException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        String json = item.getReferencedItem().getContent();
        List<DBItemInventoryConfiguration> results = null;
        switch (item.getReferencedItem().getTypeAsEnum()) {
        // determine references in configurations json
        case WORKFLOW:
            DBItemSearchWorkflow wfSearch = dbLayer.getSearchWorkflow(item.getId(), null);
            JsonObject instructions = jsonObjectFromString(wfSearch.getInstructions());
            JsonObject workflow = jsonObjectFromString(json);
            //Lock
            List<String> lockIds = getValuesFromInstructions(instructions, INSTRUCTION_LOCKS_SEARCH);
            for(String lockId : lockIds) {
                if(lockId.contains("\"")) {
                    lockId = lockId.replaceAll("\"","");
                }
                results = dbLayer.getConfigurationByName(lockId, ConfigurationType.LOCK.intValue());
                if(!results.isEmpty()) {
                    item.getReferences().add(results.get(0));
                }
            }
            //JobResource
            List<String> wfInstructionJobResourceNames = getValuesFromInstructions(instructions, JOBRESOURCENAMES_SEARCH);
            for(String jobResourceName : wfInstructionJobResourceNames) {
                if(jobResourceName.contains("\"")) {
                    jobResourceName = jobResourceName.replaceAll("\"","");
                }
                results = dbLayer.getConfigurationByName(jobResourceName, ConfigurationType.JOBRESOURCE.intValue());
                if(!results.isEmpty()) {
                    item.getReferences().add(results.get(0));
                }
            }
            List<String> wfJobResourceNames = new ArrayList<String>(); 
            getValuesRecursively("", workflow, INSTRUCTION_ADDORDERS_SEARCH, wfJobResourceNames);
            if(!wfJobResourceNames.isEmpty()) {
                for (String jobResource : wfJobResourceNames) {
                    if(jobResource.contains("\"")) {
                        jobResource = jobResource.replaceAll("\"","");
                    }
                    results = dbLayer.getConfigurationByName(jobResource, ConfigurationType.JOBRESOURCE.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(results.get(0));
                    }
                }
            }
            //NoticeBoards
            List<String> boardNames = getValuesFromInstructions(instructions, INSTRUCTION_BOARDS_SEARCH);
            for(String boardName : boardNames) {
                if(boardName.contains("\"")) {
                    boardName = boardName.replaceAll("\"","");
                }
                results = dbLayer.getConfigurationByName(boardName, ConfigurationType.NOTICEBOARD.intValue());
                if(!results.isEmpty()) {
                    item.getReferences().add(results.get(0));
                }
            }
            //Workflow
            List<String> wfInstructionWorkflowNames = getValuesFromInstructions(instructions, INSTRUCTION_ADDORDERS_SEARCH);
            for(String workflowName : wfInstructionWorkflowNames) {
                if(workflowName.contains("\"")) {
                    workflowName = workflowName.replaceAll("\"","");
                }
                results = dbLayer.getConfigurationByName(workflowName, ConfigurationType.WORKFLOW.intValue());
                if(!results.isEmpty()) {
                    item.getReferences().add(results.get(0));
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
                    if(wf.contains("\"")) {
                        wf = wf.replaceAll("\"","");
                    }
                    results = dbLayer.getConfigurationByName(wf, ConfigurationType.WORKFLOW.intValue());
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
                    if(jobresource.contains("\"")) {
                        jobresource = jobresource.replaceAll("\"","");
                    }
                    results = dbLayer.getConfigurationByName(jobresource, ConfigurationType.JOBRESOURCE.intValue());
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
                    if(wf.contains("\"")) {
                        wf = wf.replaceAll("\"","");
                    }
                    results = dbLayer.getConfigurationByName(wf, ConfigurationType.WORKFLOW.intValue());
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
    
    public static void updateDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException {
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
    
    public static void insertOrRenewDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException {
        ReferencedDbItem references = resolveReferencedBy(session, inventoryDbItem);
        InventoryDBLayer layer = new InventoryDBLayer(session);
        // store new dependencies
        layer.insertOrReplaceDependencies(references.getReferencedItem(), convert(references));
    }

    public static ReferencedDbItem getReferencedDbItems(SOSHibernateSession session, DBItemInventoryConfiguration item,
            List<DBItemInventoryDependency> dependencies) throws SOSHibernateException {
        ReferencedDbItem referencedItem = new ReferencedDbItem(item);
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        Map<Long, List<Long>> groupedDependencies = dependencies.stream()
                .filter(dependency -> dependency.getInvId().equals(item.getId()))
                .collect(Collectors.groupingBy(DBItemInventoryDependency::getInvId, 
                        Collectors.mapping(DBItemInventoryDependency::getInvDependencyId, Collectors.toList())));
        referencedItem.getReferencedBy().addAll(dbLayer.getConfigurations(groupedDependencies.get(item.getId())));
        return referencedItem;
    }

    public static List<DBItemInventoryDependency> getStoredDependencies(SOSHibernateSession session, ReferencedDbItem references)
            throws SOSHibernateException {
        return getStoredDependencies(session, references.getReferencedItem());
    }

    public static List<DBItemInventoryDependency> getStoredDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryObject)
            throws SOSHibernateException {
        List<DBItemInventoryDependency> dependencies = new ArrayList<DBItemInventoryDependency>();
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        dependencies = dbLayer.getDependencies(inventoryObject);
        return dependencies;
    }

    public static List<DBItemInventoryDependency> convert(ReferencedDbItem reference) {
        return reference.getReferencedBy().stream().map(item -> {
            DBItemInventoryDependency dependency = new DBItemInventoryDependency();
            dependency.setInvId(reference.getReferencedItem().getId());
            dependency.setDependencyType(item.getTypeAsEnum());
            dependency.setInvDependencyId(item.getId());
            dependency.setPublished(item.getDeployed() || item.getReleased());
            return dependency;
        }).collect(Collectors.toList());
    }
    
    public static ReferencedDbItem convert(SOSHibernateSession session, DBItemInventoryConfiguration invCfg, List<DBItemInventoryDependency> dependencies)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        ReferencedDbItem newDbItem = null;
        if(dependencies != null && !dependencies.isEmpty()) {
            newDbItem = new ReferencedDbItem(dbLayer.getConfiguration(dependencies.get(0).getInvId()));
            for(DBItemInventoryDependency dependency : dependencies) {
                // TODO: conversion
                DBItemInventoryConfiguration newItem = dbLayer.getConfiguration(dependency.getInvDependencyId());
                newDbItem.getReferencedBy().add(newItem);
            }
        } else {
            newDbItem = new ReferencedDbItem(dbLayer.getConfiguration(invCfg.getId()));
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
                            values.add(((JsonString)object).toString().replaceAll("\"", ""));
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
