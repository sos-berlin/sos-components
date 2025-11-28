package com.sos.joc.classes.dependencies;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.dependencies.callables.ReferenceCallable;
import com.sos.joc.classes.dependencies.items.ReferencedDbItem;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryDependency;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.dependencies.DBLayerDependencies;
import com.sos.joc.db.search.DBItemSearchWorkflow;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
//import com.sos.joc.model.inventory.dependencies.get.ResponseItem;
import com.sos.joc.model.inventory.dependencies.update.ResponseItem;

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
    public static final String JOBRESOURCES_SEARCH = "jobResources";
    public static final String LOCKNAME_SEARCH = "lockName";
    public static final String CALENDARNAME_SEARCH = "calendarName";
    public static final String NW_CALENDARS_SEARCH = "nonWorkingDayCalendars";
    public static final String JOBTEMPLATE_SEARCH = "jobTemplate";
    public static final String INCLUDESCRIPT_SEARCH = "scripts";
    public static final String SCRIPT_SEARCH = "script";
    private static final String threadNamePrefix = "Thread-DepResolver-";
    private static final AtomicInteger threadNameSuffix = new AtomicInteger();

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
            add(ConfigurationType.INCLUDESCRIPT.intValue());
            add(ConfigurationType.WORKINGDAYSCALENDAR.intValue());
            add(ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
        }
      });

    
    public static ReferencedDbItem resolve(SOSHibernateSession session, String name, ConfigurationType type)
            throws SOSHibernateException, IOException {
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

    // with db access for dependency resolution of a single or few items or to use with threading
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
            List<DBItemInventoryConfiguration> calendars = dbLayer.getUsedCalendarsByCalendarName(inventoryDbItem.getName());
            if (calendars != null) {
                cfg.getReferencedBy().addAll(calendars);
            }
            break;
        case JOBTEMPLATE:
            List<DBItemInventoryConfiguration> workflowsByJobTemplate = dbLayer.getUsedWorkflowsByJobTemplateName(inventoryDbItem.getName());
            if(workflowsByJobTemplate != null) {
                cfg.getReferencedBy().addAll(workflowsByJobTemplate);
            }
            break;
        case INCLUDESCRIPT:
            // TODO: 
            List<DBItemInventoryConfiguration> isWorkflowsOrJobTemplatesByIncludeScript = dbLayer.getWorkflowsAndJobTemplatesWithIncludedScripts();
            Set<DBItemInventoryConfiguration> isWorkflows = isWorkflowsOrJobTemplatesByIncludeScript.stream()
                    .filter(item -> item.getTypeAsEnum().equals(ConfigurationType.WORKFLOW)).collect(Collectors.toSet());
            Set<DBItemInventoryConfiguration> isJobTemplates = isWorkflowsOrJobTemplatesByIncludeScript.stream()
                    .filter(item -> item.getTypeAsEnum().equals(ConfigurationType.JOBTEMPLATE)).collect(Collectors.toSet());
            resolveIncludeScriptByWorkflow(cfg, isWorkflows);
            resolveIncludeScriptByJobTemplate(cfg, isJobTemplates);
            break;
        default:
            break;
        }
        return cfg;
    }
    
    // with minimal db access for dependency resolution of a collection of items
    public static ReferencedDbItem resolveReferencedBy(DBItemInventoryConfiguration inventoryDbItem,
            Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems) throws SOSHibernateException, IOException {
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
            resolveNonWorkingDaysCalendarsFromCalendars(cfg, groupedItems);
        case NONWORKINGDAYSCALENDAR:
            resolveAllCalendarReferencedBySchedule(cfg, groupedItems);
            break;
        case JOBTEMPLATE:
            resolveJobTemplateReferencedByWorkflow(cfg, groupedItems.get(ConfigurationType.WORKFLOW).values());
            break;
        case INCLUDESCRIPT:
            resolveIncludeScriptByWorkflow(cfg, groupedItems.get(ConfigurationType.WORKFLOW).values());
            resolveIncludeScriptByJobTemplate(cfg, groupedItems.get(ConfigurationType.JOBTEMPLATE).values());
            break;
        default:
            break;
        }
        return cfg;
    }
    
    public static void resolveIncludeScriptByWorkflow(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> workflows) {
        Predicate<String> hasScriptInclude = Pattern.compile(JsonConverter.scriptIncludeComments + JsonConverter.scriptInclude + "[ \t]+"
                + item.getReferencedItem().getName() + "\\s*").asPredicate();
        for(DBItemInventoryConfiguration wf : workflows) {
            if (hasScriptInclude.test(wf.getContent())) {
                try {
                    Workflow w = Globals.objectMapper.readValue(wf.getContent(), Workflow.class);
                    if (w.getJobs() != null) {
                        w.getJobs().getAdditionalProperties().forEach((jobName, job) -> {
                            if (job.getExecutable() != null && ExecutableType.ShellScriptExecutable.equals(job.getExecutable().getTYPE())) {
                                ExecutableScript es = job.getExecutable().cast();
                                if (es.getScript() != null && hasScriptInclude.test(es.getScript())) {
                                    String[] scriptLines = es.getScript().split("\n");
                                    for (int i = 0; i < scriptLines.length; i++) {
                                        String line = scriptLines[i];
                                        if (hasScriptInclude.test(line)) {
                                            Matcher m = JsonConverter.scriptIncludePattern.matcher(line);
                                            if (m.find()) {
                                                if (item.getReferencedItem().getName().equals(m.group(3))) {
                                                    item.getReferencedBy().add(wf);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (JsonProcessingException e) {
                    // do nothing !
                }
            }
        }
    }
    
    public static void resolveIncludeScriptByJobTemplate(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> jobTemplates) {
        Predicate<String> hasScriptInclude = Pattern.compile(JsonConverter.scriptIncludeComments + JsonConverter.scriptInclude + "[ \t]+"
                + item.getReferencedItem().getName() + "\\s*").asPredicate();
        for(DBItemInventoryConfiguration jobTemplate : jobTemplates) {
            if (hasScriptInclude.test(jobTemplate.getContent())) {
                try {
                    JobTemplate jt = Globals.objectMapper.readValue(jobTemplate.getContent(), JobTemplate.class);
                    if (jt.getExecutable() != null && ExecutableType.ShellScriptExecutable.equals(jt.getExecutable().getTYPE())) {
                        ExecutableScript es = jt.getExecutable().cast();
                        if (es.getScript() != null && hasScriptInclude.test(es.getScript())) {
                            String[] scriptLines = es.getScript().split("\n");
                            for (int i = 0; i < scriptLines.length; i++) {
                                String line = scriptLines[i];
                                if (hasScriptInclude.test(line)) {
                                    Matcher m = JsonConverter.scriptIncludePattern.matcher(line);
                                    if (m.find()) {
                                        if (item.getReferencedItem().getName().equals(m.group(3))) {
                                            item.getReferencedBy().add(jobTemplate);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (JsonProcessingException e) {
                    // do nothing !
                }
            }
        }
    }
    
    public static void resolveLockReferencedByWorkflow(ReferencedDbItem item, Collection<DBItemInventoryConfiguration> workflows) {
        for(DBItemInventoryConfiguration cfg : workflows) {
            String json = cfg.getContent();
            JsonObject workflow = jsonObjectFromString(json);
            //IncludeScript
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

    public static void resolveAllCalendarReferencedBySchedule(ReferencedDbItem item, 
            Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems) throws JsonMappingException, JsonProcessingException {
        Collection<DBItemInventoryConfiguration> cfgs = groupedItems.get(ConfigurationType.SCHEDULE).values();
        for(DBItemInventoryConfiguration schedule : cfgs) {
            Set<String> scheduleCalendars = new HashSet<String>();
            Schedule s = JocInventory.convertSchedule(schedule.getContent(), Schedule.class);
            Optional.ofNullable(s.getCalendars()).ifPresent(cals -> cals.forEach(cal -> {
                scheduleCalendars.add(cal.getCalendarName());
                Optional.ofNullable(cal.getExcludes()).map(Frequencies::getNonWorkingDayCalendars).stream()
                    .forEach(nwCal -> scheduleCalendars.add(cal.getCalendarName()));
            }));
            Optional.ofNullable(s.getNonWorkingDayCalendars()).ifPresent(cals -> cals.forEach(cal -> scheduleCalendars.add(cal.getCalendarName())));
            if(!scheduleCalendars.isEmpty()) {
                for(String cal : scheduleCalendars) {
                    DBItemInventoryConfiguration wCalItem = groupedItems.get(ConfigurationType.WORKINGDAYSCALENDAR).get(cal);
                    if(wCalItem != null) {
                        item.getReferences().add(wCalItem);
                    }
                    DBItemInventoryConfiguration nwCalItem = groupedItems.get(ConfigurationType.NONWORKINGDAYSCALENDAR).get(cal);
                    if(nwCalItem != null) {
                        item.getReferences().add(nwCalItem);
                    }
                }
            }
        }
    }
    
    public static void resolveNonWorkingDaysCalendarsFromCalendars(ReferencedDbItem item, Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems)
            throws JsonParseException, JsonMappingException, IOException {
        Collection<DBItemInventoryConfiguration> cfgs = groupedItems.get(ConfigurationType.WORKINGDAYSCALENDAR).values();
        Set<String> nonWorkingDaysCalendars = new HashSet<String>();
        for(DBItemInventoryConfiguration calendar : cfgs) {
            Calendar workingDayCal = (Calendar)JocInventory.content2IJSObject(calendar.getContent(), ConfigurationType.WORKINGDAYSCALENDAR);
            Optional.ofNullable(workingDayCal.getExcludes()).map(Frequencies::getNonWorkingDayCalendars)
                .ifPresent(cals -> nonWorkingDaysCalendars.addAll(cals));
        }
        if(!nonWorkingDaysCalendars.isEmpty()) {
            groupedItems.get(ConfigurationType.NONWORKINGDAYSCALENDAR).entrySet().stream()
                .filter(entry ->  nonWorkingDaysCalendars.contains(entry.getKey()))
                .forEach(entry ->item.getReferences().add(entry.getValue()));
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

    // with db access for dependency resolution of a single or few items or to use with threading
    public static void resolveReferences (ReferencedDbItem item, SOSHibernateSession session) throws IOException {
        // this method is in use
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        String json = item.getReferencedItem().getContent();
        List<DBItemInventoryConfiguration> results = null;
        JsonObject wfsearchInstructions = null;
        JsonObject wfsearchJobs = null;
        JsonObject wfsearchScripts = null;
        switch (item.getReferencedItem().getTypeAsEnum()) {
        // determine references in configurations json
        case WORKFLOW:
            DBItemSearchWorkflow wfSearch = dbLayer.getSearchWorkflow(item.getId(), null);
            if(wfSearch != null) {
                wfsearchInstructions = jsonObjectFromString(wfSearch.getInstructions());
                wfsearchJobs = jsonObjectFromString(wfSearch.getJobs());
                wfsearchScripts = jsonObjectFromString(wfSearch.getJobsScripts());
            }
            JsonObject workflow = jsonObjectFromString(json);
            //Lock
            if(wfsearchInstructions != null) {
                List<String> lockIds = getValuesFromObject(wfsearchInstructions, INSTRUCTION_LOCKS_SEARCH);
                if (!lockIds.isEmpty()) {
                    // check from instructions info from WindowsSearch
                    for(String lockId : lockIds) {
                        results = dbLayer.getConfigurationByName(lockId.replaceAll("\"",""), ConfigurationType.LOCK.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                } else {
                    // fallback: if result in WindowsSearch is empty check again directly in json
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
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
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
            if(wfsearchJobs != null) {
                // check from instructions info from WindowsSearch
                List<String> wfSearchJobsJobResourceNames = getValuesFromObject(wfsearchJobs, JOBRESOURCENAMES_SEARCH);
                if(!wfSearchJobsJobResourceNames.isEmpty()) {
                    for(String jobResourceName : wfSearchJobsJobResourceNames) {
                        results = dbLayer.getConfigurationByName(jobResourceName.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                } else {
                    List<String> wfSearchJobsJobResources = getValuesFromObject(wfsearchJobs, JOBRESOURCES_SEARCH);
                    if(!wfSearchJobsJobResources.isEmpty()) {
                        for(String jobResourceName : wfSearchJobsJobResources) {
                            results = dbLayer.getConfigurationByName(jobResourceName.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                            if(!results.isEmpty()) {
                                item.getReferences().add(results.get(0));
                            }
                        }
                    } else {
                        // fallback: if result in WindowsSearch is empty check again directly in json
                        List<String> wfJobResourceNames = new ArrayList<String>(); 
                        getValuesRecursively("", workflow, JOBRESOURCENAMES_SEARCH, wfJobResourceNames);
                        if(!wfJobResourceNames.isEmpty()) {
                            for (String jobResource : wfJobResourceNames) {
                                results = dbLayer.getConfigurationByName(jobResource.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                                if(!results.isEmpty()) {
                                    item.getReferences().add(results.get(0));
                                }
                            }
                        }
                    }
                }
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
                List<String> wfJobResourceNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, JOBRESOURCENAMES_SEARCH, wfJobResourceNames);
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
            if(wfsearchInstructions != null) {
                // check from instructions info from WindowsSearch
                List<String> boardNames = getValuesFromObject(wfsearchInstructions, INSTRUCTION_BOARDS_SEARCH);
                if(!boardNames.isEmpty()) {
                    for(String boardName : boardNames) {
                        results = dbLayer.getConfigurationByName(boardName.replaceAll("\"",""), ConfigurationType.NOTICEBOARD.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                } else {
                    // fallback: if result in WindowsSearch is empty check again directly in json
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
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
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
            if(wfsearchInstructions != null) {
                // check from instructions info from WindowsSearch
                List<String> wfInstructionWorkflowNames = getValuesFromObject(wfsearchInstructions, INSTRUCTION_ADDORDERS_SEARCH);
                if(!wfInstructionWorkflowNames.isEmpty()) {
                    for(String workflowName : wfInstructionWorkflowNames) {
                        results = dbLayer.getConfigurationByName(workflowName.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                } else {
                    // fallback: if result in WindowsSearch is empty check again directly in json
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
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
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
            if(wfsearchScripts != null) {
                List<String> wfSearchJobScriptNames = getValuesFromObject(wfsearchScripts, INCLUDESCRIPT_SEARCH);
                for(String script : wfSearchJobScriptNames) {
                    Matcher m = JsonConverter.scriptIncludePattern.matcher(script);
                    if (m.find()) {
                        String scriptName = m.group(2);
                        results = dbLayer.getConfigurationByName(scriptName, ConfigurationType.INCLUDESCRIPT.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(results.get(0));
                        }
                    }
                }
            } else {
                if(json.contains("##!include")) {
                    List<String> wfJobScriptNames = new ArrayList<String>();
                    getValuesRecursively("", workflow, SCRIPT_SEARCH, wfJobScriptNames);
                    if(!wfJobScriptNames.isEmpty()) {
                        for(String script : wfJobScriptNames) {
                            Matcher m = JsonConverter.scriptIncludePattern.matcher(script);
                            if (m.find()) {
                                String scriptName = m.group(2);
                                results = dbLayer.getConfigurationByName(scriptName, ConfigurationType.INCLUDESCRIPT.intValue());
                                if(!results.isEmpty()) {
                                    item.getReferences().add(results.get(0));
                                }
                            }
                        }
                    }
                }
            }
            break;
        case SCHEDULE:
            // Workflow
            // no instructions are available in WindowsSearch check always directly in json
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
            // Calendars
            Set<String> scheduleCalendars = new HashSet<String>();
            Schedule s = JocInventory.convertSchedule(json, Schedule.class);
            Optional.ofNullable(s.getCalendars()).ifPresent(cals -> cals.forEach(cal -> {
                scheduleCalendars.add(cal.getCalendarName());
                Optional.ofNullable(cal.getExcludes()).map(Frequencies::getNonWorkingDayCalendars).stream()
                    .forEach(nwCal -> scheduleCalendars.add(cal.getCalendarName()));
            }));
            Optional.ofNullable(s.getNonWorkingDayCalendars()).ifPresent(cals -> cals.forEach(cal -> scheduleCalendars.add(cal.getCalendarName())));
            if(!scheduleCalendars.isEmpty()) {
                for(String cal : scheduleCalendars) {
                    results = dbLayer.getConfigurationByName(cal.replaceAll("\"",""), ConfigurationType.WORKINGDAYSCALENDAR.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().addAll(results);
                    }
                }
            }
            break;
        case JOBTEMPLATE:
            // jobResource
            // no instructions are available in WindowsSearch check always directly in json
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
            // include script
            if(json.contains("##!include")) {
                List<String> wfJobScriptNames = new ArrayList<String>();
                getValuesRecursively("", jobTemplate, SCRIPT_SEARCH, wfJobScriptNames);
                if(!wfJobScriptNames.isEmpty()) {
                    for(String script : wfJobScriptNames) {
                        Matcher m = JsonConverter.scriptIncludePattern.matcher(script);
                        if (m.find()) {
                            String scriptName = m.group(2);
                            results = dbLayer.getConfigurationByName(scriptName, ConfigurationType.INCLUDESCRIPT.intValue());
                            if(!results.isEmpty()) {
                                item.getReferences().add(results.get(0));
                            }
                        }
                    }
                }
            }
            break;
        case FILEORDERSOURCE:
            // Workflow
            // no instructions are available in WindowsSearch check always directly in json
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
        case WORKINGDAYSCALENDAR:
            // Calendars
            Set<String> nwCalendars = new HashSet<String>();
            Calendar calendar = (Calendar)JocInventory.content2IJSObject(json, ConfigurationType.WORKINGDAYSCALENDAR);
            Optional.ofNullable(calendar.getExcludes()).map(Frequencies::getNonWorkingDayCalendars)
                .ifPresent(cals -> cals.forEach(cal -> nwCalendars.add(cal)));
            if(!nwCalendars.isEmpty()) {
                for(String cal : nwCalendars) {
                    results = dbLayer.getConfigurationByName(cal, ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
                    results.stream().filter(dbItem -> ConfigurationType.NONWORKINGDAYSCALENDAR.equals(dbItem.getTypeAsEnum()))
                        .findFirst().ifPresent(nwCal ->item.getReferences().add(nwCal));
                }
            }
            break;
            
        default:
            break;
        }
    }
    
    // with db access for dependency resolution of a single or few items or to use with threading
    public static void resolveReferences (ResponseItem item, SOSHibernateSession session)
            throws JsonMappingException, JsonProcessingException {
        // this method is in use
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        String json = Globals.objectMapper.writeValueAsString(item.getDependency().getConfiguration());
        List<DBItemInventoryConfiguration> results = null;
        JsonObject instructions = null;
        JsonObject jobScripts = null;
        switch (item.getDependency().getObjectType()) {
        // determine references in configurations json
        case WORKFLOW:
            DBItemSearchWorkflow wfSearch = dbLayer.getSearchWorkflow(item.getDependency().getId(), null);
            if(wfSearch != null) {
                instructions = jsonObjectFromString(wfSearch.getInstructions());
                jobScripts = jsonObjectFromString(wfSearch.getJobsScripts());
            }
            JsonObject workflow = jsonObjectFromString(json);
            //Lock
            if(instructions != null) {
                List<String> lockIds = getValuesFromObject(instructions, INSTRUCTION_LOCKS_SEARCH);
                for(String lockId : lockIds) {
                    results = dbLayer.getConfigurationByName(lockId.replaceAll("\"",""), ConfigurationType.LOCK.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
                    }
                }
            } else {
                List<String> wfLockNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, LOCKNAME_SEARCH, wfLockNames);
                if(!wfLockNames.isEmpty()) {
                    for (String lock : wfLockNames) {
                        results = dbLayer.getConfigurationByName(lock.replaceAll("\"",""), ConfigurationType.LOCK.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(new ResponseItem(convert(results.get(0))));
                        }
                    }
                }
            }
            //JobResource
            if(instructions != null) {
                List<String> wfInstructionJobResourceNames = getValuesFromObject(instructions, JOBRESOURCENAMES_SEARCH);
                for(String jobResourceName : wfInstructionJobResourceNames) {
                    results = dbLayer.getConfigurationByName(jobResourceName.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
                    }
                }
            } else {
                List<String> wfJobResourceNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, JOBRESOURCENAMES_SEARCH, wfJobResourceNames);
                if(!wfJobResourceNames.isEmpty()) {
                    for (String jobResource : wfJobResourceNames) {
                        results = dbLayer.getConfigurationByName(jobResource.replaceAll("\"",""), ConfigurationType.JOBRESOURCE.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(new ResponseItem(convert(results.get(0))));
                        }
                    }
                }
            }
            //NoticeBoards
            if(instructions != null) {
                List<String> boardNames = getValuesFromObject(instructions, INSTRUCTION_BOARDS_SEARCH);
                for(String boardName : boardNames) {
                    results = dbLayer.getConfigurationByName(boardName.replaceAll("\"",""), ConfigurationType.NOTICEBOARD.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
                    }
                }
            } else {
                List<String> wfBoardNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, INSTRUCTION_BOARDS_SEARCH, wfBoardNames);
                if(!wfBoardNames.isEmpty()) {
                    for (String board : wfBoardNames) {
                        results = dbLayer.getConfigurationByName(board.replaceAll("\"",""), ConfigurationType.NOTICEBOARD.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(new ResponseItem(convert(results.get(0))));
                        }
                    }
                }
            }
            //Workflow
            if(instructions != null) {
                List<String> wfInstructionWorkflowNames = getValuesFromObject(instructions, INSTRUCTION_ADDORDERS_SEARCH);
                for(String workflowName : wfInstructionWorkflowNames) {
                    results = dbLayer.getConfigurationByName(workflowName.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                    if(!results.isEmpty()) {
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
                    }
                }
            } else {
                List<String> wfWorkflowNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, WORKFLOWNAME_SEARCH, wfWorkflowNames);
                if(!wfWorkflowNames.isEmpty()) {
                    for (String wf : wfWorkflowNames) {
                        results = dbLayer.getConfigurationByName(wf.replaceAll("\"",""), ConfigurationType.WORKFLOW.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(new ResponseItem(convert(results.get(0))));
                        }
                    }
                }
            }
            // ScriptIncludes
            if(jobScripts != null) {
                List<String> wfSearchJobScriptNames = getValuesFromObject(jobScripts, INCLUDESCRIPT_SEARCH);
                for(String script : wfSearchJobScriptNames) {
                    Matcher m = JsonConverter.scriptIncludePattern.matcher(script);
                    if (m.find()) {
                        String scriptName = m.group(2);
                        results = dbLayer.getConfigurationByName(scriptName, ConfigurationType.INCLUDESCRIPT.intValue());
                        if(!results.isEmpty()) {
                            item.getReferences().add(new ResponseItem(convert(results.get(0))));
                        }
                    }
                }
            } else {
                if(json.contains("##!include")) {
                    List<String> wfJobScriptNames = new ArrayList<String>();
                    getValuesRecursively("", workflow, SCRIPT_SEARCH, wfJobScriptNames);
                    if(!wfJobScriptNames.isEmpty()) {
                        for(String script : wfJobScriptNames) {
                            Matcher m = JsonConverter.scriptIncludePattern.matcher(script);
                            if (m.find()) {
                                String scriptName = m.group(2);
                                results = dbLayer.getConfigurationByName(scriptName, ConfigurationType.INCLUDESCRIPT.intValue());
                                if(!results.isEmpty()) {
                                    item.getReferences().add(new ResponseItem(convert(results.get(0))));
                                }
                            }
                        }
                    }
                }
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
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
                    }
                }
            }
            // Calendars
            Set<String> scheduleCalendars = new HashSet<String>();
            Schedule s = JocInventory.convertSchedule(json, Schedule.class);
            Optional.ofNullable(s.getCalendars()).ifPresent(cals -> cals.forEach(cal -> {
                scheduleCalendars.add(cal.getCalendarName());
                Optional.ofNullable(cal.getExcludes()).map(Frequencies::getNonWorkingDayCalendars).stream()
                    .forEach(nwCal -> scheduleCalendars.add(cal.getCalendarName()));
            }));
            Optional.ofNullable(s.getNonWorkingDayCalendars()).ifPresent(cals -> cals.forEach(cal -> scheduleCalendars.add(cal.getCalendarName())));
            if(!scheduleCalendars.isEmpty()) {
                for(String cal : scheduleCalendars) {
                    results = dbLayer.getConfigurationByName(cal.replaceAll("\"",""), ConfigurationType.WORKINGDAYSCALENDAR.intValue());
                    if(!results.isEmpty()) {
                        results.forEach(result -> item.getReferences().add(new ResponseItem(convert(result))));
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
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
                    }
                }
            }
            // include scripts
            if(json.contains("##!include")) {
                List<String> wfJobScriptNames = new ArrayList<String>();
                getValuesRecursively("", jobTemplate, SCRIPT_SEARCH, wfJobScriptNames);
                if(!wfJobScriptNames.isEmpty()) {
                    for(String script : wfJobScriptNames) {
                        Matcher m = JsonConverter.scriptIncludePattern.matcher(script);
                        if (m.find()) {
                            String scriptName = m.group(2);
                            results = dbLayer.getConfigurationByName(scriptName, ConfigurationType.INCLUDESCRIPT.intValue());
                            if(!results.isEmpty()) {
                                item.getReferences().add(new ResponseItem(convert(results.get(0))));
                            }
                        }
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
                        item.getReferences().add(new ResponseItem(convert(results.get(0))));
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
            throws IOException {
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
            // Calendars
            Set<String> scheduleCalendars = new HashSet<String>();
            Schedule s = JocInventory.convertSchedule(json, Schedule.class);
            Optional.ofNullable(s.getCalendars()).ifPresent(cals -> cals.forEach(cal -> {
                scheduleCalendars.add(cal.getCalendarName());
                Optional.ofNullable(cal.getExcludes()).map(Frequencies::getNonWorkingDayCalendars).stream()
                    .forEach(nwCal -> scheduleCalendars.add(cal.getCalendarName()));
            }));
            Optional.ofNullable(s.getNonWorkingDayCalendars()).ifPresent(cals -> cals.forEach(cal -> scheduleCalendars.add(cal.getCalendarName())));
            if(!scheduleCalendars.isEmpty()) {
                for(String cal : scheduleCalendars) {
                    DBItemInventoryConfiguration wCalItem = groupedItems.get(ConfigurationType.WORKINGDAYSCALENDAR).get(cal);
                    if(wCalItem != null) {
                        item.getReferences().add(wCalItem);
                    }
                    DBItemInventoryConfiguration nwCalItem = groupedItems.get(ConfigurationType.NONWORKINGDAYSCALENDAR).get(cal);
                    if(nwCalItem != null) {
                        item.getReferences().add(nwCalItem);
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
        case WORKINGDAYSCALENDAR:
            // Calendars
            Set<String> nwCalendars = new HashSet<String>();
            Calendar calendar = (Calendar)JocInventory.content2IJSObject(json, ConfigurationType.WORKINGDAYSCALENDAR);
            Optional.ofNullable(calendar.getExcludes()).map(Frequencies::getNonWorkingDayCalendars)
                .ifPresent(cals -> cals.forEach(cal -> nwCalendars.add(cal)));
            if(!nwCalendars.isEmpty()) {
                for(String cal : nwCalendars) {
                    DBItemInventoryConfiguration nwCalItem = groupedItems.get(ConfigurationType.NONWORKINGDAYSCALENDAR).get(cal);
                    if(nwCalItem != null) {
                        item.getReferences().add(nwCalItem);
                    }
                }
            }
            break;
        default:
            break;
        }
        return item;
    }
    
    public static void updateDependencies(DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        new Thread(() -> {
            try {
                SOSHibernateSession session = null;
                try {
                    session = Globals.createSosHibernateStatelessConnection("DependencyResolver");
                    insertOrRenewDependencies(session, inventoryDbItem);
                } finally {
                    Globals.disconnect(session);
                }
            } catch (Exception e) {
                //TODO use ProblemHelper
                LOGGER.error("", e);
            }
        }, threadNamePrefix + Math.abs(threadNameSuffix.incrementAndGet() % 1000)).start();
        
    }
    
    public static void updateDependencies(List<DBItemInventoryConfiguration> allCfgs)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        updateDependencies(allCfgs, false);
    }

    public static void updateDependencies(List<DBItemInventoryConfiguration> allCfgs, boolean withPool)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        new Thread(() -> {
            try {
                SOSHibernateSession session = null;
                try {
                    session = Globals.createSosHibernateStatelessConnection("DependencyResolver");
                    insertOrRenewDependencies(session, allCfgs, withPool);
                } finally {
                    Globals.disconnect(session);
                }
            } catch (Exception e) {
                //TODO use ProblemHelper
                LOGGER.error("", e);
            }
        }, threadNamePrefix + Math.abs(threadNameSuffix.incrementAndGet() % 1000)).start();
    }
    
    public static void insertOrRenewDependencies(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem) throws SOSHibernateException, IOException {
        // this method is in use (JocInventory update and insert methods)
            ReferencedDbItem references = resolveReferencedBy(session, inventoryDbItem);
            resolveReferences(references, session);
            DBLayerDependencies layer = new DBLayerDependencies(session);
            // store new dependencies
            layer.insertOrReplaceDependencies(references.getReferencedItem(), convert(references, session));
    }

    public static void insertOrRenewDependencies(SOSHibernateSession session, List<DBItemInventoryConfiguration> allCfgs, boolean withPool)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        // this method is in use
        Set<ReferencedDbItem> referencedItems = new HashSet<ReferencedDbItem>();
        List<ReferenceCallable> callables = allCfgs.stream().map(item -> new ReferenceCallable(item)).collect(Collectors.toList());
        if(!callables.isEmpty()) {
            ExecutorService executorService = null;
            try {
                if(withPool) {
                    Integer maxPoolSize = readMaxPoolSize(Globals.getHibernateConfFile());
                    if(maxPoolSize != null) {
                        executorService = Executors.newFixedThreadPool(Math.min(callables.size(), maxPoolSize/2));
                    } else {
                        executorService = Executors.newFixedThreadPool(1);
                    }
                } else {
                    executorService = Executors.newFixedThreadPool(1);
                }
                for (Future<ReferencedDbItem> result : executorService.invokeAll(callables)) {
                    try {
                        referencedItems.add(result.get());
                    } catch (ExecutionException e) {
                        if (e.getCause() != null) {
                            LOGGER.error("", e.getCause());
                        } else {
                            LOGGER.error("", e);
                        }
                    }
                } 
            } finally {
                executorService.shutdown();
            }
            DBLayerDependencies layer = new DBLayerDependencies(session);
            referencedItems.stream()
                .filter(item -> !(item.getReferencedBy().isEmpty() && item.getReferences().isEmpty()))
                .forEach(ref -> {
                    try {
                        layer.insertOrReplaceDependencies(ref.getReferencedItem(), convert(ref, layer.getSession()));
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                });
        }
    }

    private static Integer readMaxPoolSize (Path hibernateConfigFile) {
        try {
            if(hibernateConfigFile != null) {
                String cfg = Files.readString(hibernateConfigFile);
                if(cfg.contains("hibernate.hikari.maximumPoolSize")) {
                    Configuration config = new Configuration();
                    config.configure(hibernateConfigFile.toUri().toURL());
                    Object key = config.getProperties().get("hibernate.hikari.maximumPoolSize");
                    if(key != null) {
                        return Integer.valueOf(key.toString());
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    // this method is in use
    public static List<DBItemInventoryDependency> getStoredDependencies(SOSHibernateSession session,
            DBItemInventoryConfiguration inventoryObject) throws SOSHibernateException {
        List<DBItemInventoryDependency> dependencies = new ArrayList<DBItemInventoryDependency>();
        DBLayerDependencies dbLayer = new DBLayerDependencies(session);
        dependencies = dbLayer.getDependencies(inventoryObject);
        return dependencies;
    }
    
    public static List<DBItemInventoryDependency> getStoredDependencies (DBItemInventoryConfiguration item,
            List<DBItemInventoryDependency> allDependencies) {
        return allDependencies.stream().filter(dep -> dep.getInvId().equals(item.getId())).collect(Collectors.toList());
    }
    
    // this method is currently in use
    private static Set<DBItemInventoryDependency> convert(ReferencedDbItem reference, SOSHibernateSession session) {
        Set<DBItemInventoryDependency> dependencies = new HashSet<DBItemInventoryDependency>();
        InventoryDBLayer dblayer = new InventoryDBLayer(session);
        dependencies.addAll(reference.getReferencedBy().stream().map(item -> {
            DBItemInventoryDependency dependency = new DBItemInventoryDependency();
            dependency.setInvId(reference.getReferencedItem().getId());
            dependency.setInvType(reference.getReferencedItem().getTypeAsEnum());
            dependency.setDependencyType(item.getTypeAsEnum());
            dependency.setInvDependencyId(item.getId());
            // check if item is already deployed or released
            dependency.setPublished(isPublished(dblayer, item));
            dependency.setInvEnforce(!dependency.getPublished());
            dependency.setDepEnforce(!dependency.getPublished());
            return dependency;
        }).collect(Collectors.toSet()));
        dependencies.addAll(reference.getReferences().stream().map(item -> {
            DBItemInventoryDependency dependency = new DBItemInventoryDependency();
            dependency.setInvId(item.getId());
            dependency.setInvType(item.getTypeAsEnum());
            dependency.setDependencyType(reference.getReferencedItem().getTypeAsEnum());
            dependency.setInvDependencyId(reference.getReferencedItem().getId());
            dependency.setPublished(isPublished(dblayer, reference.getReferencedItem()));
            dependency.setInvEnforce(!dependency.getPublished());
            dependency.setDepEnforce(!dependency.getPublished());
            
            return dependency;
        }).collect(Collectors.toSet()));
        return dependencies;
    }
    
    // this method is in use
    public static ReferencedDbItem convert(SOSHibernateSession session, DBItemInventoryConfiguration invCfg, List<DBItemInventoryDependency> dependencies)
            throws SOSHibernateException, IOException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        ReferencedDbItem newDbItem = new ReferencedDbItem(invCfg);
        if(dependencies != null && !dependencies.isEmpty()) {
            for(DBItemInventoryDependency dependency : dependencies) {
                DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(dependency.getInvDependencyId());
                if(cfg != null) {
                    newDbItem.getReferencedBy().add(cfg);
                }
            }
        }
        resolveReferences(newDbItem, session);
        return newDbItem;
    }
    
//    public static ReferencedDbItem convert(DBItemInventoryConfiguration invCfg, List<DBItemInventoryDependency> dependencies,
//            Map<Long, DBItemInventoryConfiguration> cfgs, SOSHibernateSession session) throws SOSHibernateException,
//            IOException {
//        ReferencedDbItem newDbItem = new ReferencedDbItem(invCfg);
//        if(dependencies != null && !dependencies.isEmpty()) {
//            for(DBItemInventoryDependency dependency : dependencies) {
//                DBItemInventoryConfiguration cfg = cfgs.get(dependency.getInvDependencyId());
//                if(cfg != null) {
//                    newDbItem.getReferencedBy().add(cfg);
//                }
//            }
//        }
//        resolveReferences(newDbItem, session);
//        return newDbItem;
//    }

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
    public static List<String> getValuesFromObject(JsonObject jsonObj, String searchKey) {
        List<String> foundValues = new ArrayList<String>();
        if (jsonObj.containsKey(searchKey)) {
            JsonArray params = jsonObj.getJsonArray(searchKey);
            for (int i = 0; i < params.size(); i++) {
                String value = params.getString(i);
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
                        if(nextKey.contains(searchKey)) {
                            values.add(((JsonString)object).getString());
                        }
                    }
                }
            } else {
                // value is neither object nor array and next key is the search key, ends the recursion
                if(nextKey.equals(searchKey) || currentKey.equals(searchKey)) {
                    values.add(value.toString().replaceAll("\"", ""));
                }
            }
        }
    }

    public static ConfigurationObject convert(DBItemInventoryConfiguration item) {
        ConfigurationObject object = new ConfigurationObject();
        try {
            object.setConfiguration(JocInventory.content2IJSObject(item.getContent(), item.getTypeAsEnum()));
        } catch (IOException e) {
            return null;
        }
        object.setId(item.getId());
        object.setConfigurationDate(item.getModified());
        object.setDeleted(false);
        object.setDeliveryDate(Date.from(Instant.now()));
        object.setDeployed(item.getDeployed());
        object.setReleased(item.getReleased());
        object.setName(item.getName());
        object.setPath(item.getPath());
        object.setValid(item.getValid());
        object.setObjectType(item.getTypeAsEnum());
        return object;
    }
    
    private static boolean isPublished(InventoryDBLayer dbLayer, DBItemInventoryConfiguration config) {
        try {
            if(JocInventory.isDeployable(config.getTypeAsEnum())) {
                return dbLayer.isDeployed(config.getId());
            } else if (JocInventory.isReleasable(config.getTypeAsEnum())) {
                return dbLayer.isReleased(config.getId());
            }
        } catch (SOSHibernateException e) {}
        return false;
    }
}
