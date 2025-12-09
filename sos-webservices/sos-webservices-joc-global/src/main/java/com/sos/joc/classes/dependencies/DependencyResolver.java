package com.sos.joc.classes.dependencies;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Predicate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Frequencies;
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
    public static final String JOBRESOURCES_SEARCH = "jobResources";
    public static final String LOCKNAME_SEARCH = "lockName";
    public static final String CALENDARNAME_SEARCH = "calendarName";
    public static final String NW_CALENDARS_SEARCH = "nonWorkingDayCalendars";
    public static final String JOBTEMPLATE_SEARCH = "jobTemplate";
    public static final String INCLUDESCRIPT_SEARCH = "scripts";
    public static final String SCRIPT_SEARCH = "script";
    private static final String threadNamePrefix = "Thread-DepResolver-";
    private static final AtomicInteger threadNameSuffix = new AtomicInteger();

    public static final Set<Integer> dependencyTypes = Collections.unmodifiableSet(new HashSet<Integer>() {
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
    
    public static final Map<ConfigurationType, Set<ConfigurationType>> referencedTypesByRequestedType = Collections.unmodifiableMap(
            new HashMap<ConfigurationType, Set<ConfigurationType>>() {
        private static final long serialVersionUID = 1L;
        {
            put(ConfigurationType.WORKFLOW, Collections.unmodifiableSet(new HashSet<ConfigurationType>() {
                private static final long serialVersionUID = 1L;
                {
                    add(ConfigurationType.WORKFLOW);
                    add(ConfigurationType.JOBRESOURCE);
                    add(ConfigurationType.LOCK);
                    add(ConfigurationType.NOTICEBOARD);
                    add(ConfigurationType.INCLUDESCRIPT);
                }
            }));
            put(ConfigurationType.FILEORDERSOURCE, Collections.unmodifiableSet(new HashSet<ConfigurationType>() {
                private static final long serialVersionUID = 1L;
                {
                    add(ConfigurationType.WORKFLOW);
                }
            }));
            put(ConfigurationType.SCHEDULE, Collections.unmodifiableSet(new HashSet<ConfigurationType>() {
                private static final long serialVersionUID = 1L;
                {
                    add(ConfigurationType.WORKFLOW);
                    add(ConfigurationType.WORKINGDAYSCALENDAR);
                    add(ConfigurationType.NONWORKINGDAYSCALENDAR);
                }
            }));
            put(ConfigurationType.JOBTEMPLATE, Collections.unmodifiableSet(new HashSet<ConfigurationType>() {
                private static final long serialVersionUID = 1L;
                {
                    add(ConfigurationType.JOBRESOURCE);
                    add(ConfigurationType.INCLUDESCRIPT);
                }
            }));
            put(ConfigurationType.WORKINGDAYSCALENDAR, Collections.unmodifiableSet(new HashSet<ConfigurationType>() {
                private static final long serialVersionUID = 1L;
                {
                    add(ConfigurationType.NONWORKINGDAYSCALENDAR);
                }
            }));
        }
    });
    
    public static ReferencedDbItem createReferencedDbItem (DBItemInventoryConfiguration inventoryDbItem) {
        return new ReferencedDbItem(inventoryDbItem);
    }
    
    public static void resolveReferences (ReferencedDbItem item, SOSHibernateSession session) throws IOException {
        resolveReferences(item, session, Collections.emptyMap());
    }
    
    // with db access for dependency resolution of a single or few items or to use with threading
    public static void resolveReferences (ReferencedDbItem item, SOSHibernateSession session, 
            Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped) throws IOException {
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
                    lockIds.forEach(lockId -> lockId.replaceAll("\"",""));
                    // check from instructions info from WindowsSearch
                    if(allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(lockIds.stream(), ConfigurationType.LOCK.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.LOCK).stream().filter(getPredicate(lockIds)).forEach(item.getReferences()::add);
                    }
                } else {
                    // fallback: if result in WindowsSearch is empty check again directly in json
                    List<String> wfLockNames = new ArrayList<String>(); 
                    getValuesRecursively("", workflow, LOCKNAME_SEARCH, wfLockNames);
                    if(!wfLockNames.isEmpty()) {
                        wfLockNames.forEach(lockName -> lockName.replaceAll("\"",""));
                        if(allItemsGrouped.isEmpty()) {
                            item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfLockNames.stream(), ConfigurationType.LOCK.intValue()));
                        } else {
                            allItemsGrouped.get(ConfigurationType.LOCK).stream().filter(getPredicate(wfLockNames)).forEach(item.getReferences()::add);
                        }
                    }
                }
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
                List<String> wfLockNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, LOCKNAME_SEARCH, wfLockNames);
                if(!wfLockNames.isEmpty()) {
                    wfLockNames.forEach(lockId -> lockId.replaceAll("\"",""));
                    if(allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfLockNames.stream(), ConfigurationType.LOCK.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.LOCK).stream().filter(getPredicate(wfLockNames)).forEach(item.getReferences()::add);
                    }
                }
            }
            //JobResource
            if(wfsearchJobs != null) {
                // check from instructions info from WindowsSearch
                List<String> wfSearchJobsJobResourceNames = getValuesFromObject(wfsearchJobs, JOBRESOURCENAMES_SEARCH);
                if(!wfSearchJobsJobResourceNames.isEmpty()) {
                    wfSearchJobsJobResourceNames.forEach(jobResourceName -> jobResourceName.replaceAll("\"",""));
                    if(allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfSearchJobsJobResourceNames.stream(), ConfigurationType.JOBRESOURCE.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.JOBRESOURCE).stream().filter(getPredicate(wfSearchJobsJobResourceNames)).forEach(item.getReferences()::add);
                    }
                } else {
                    List<String> wfSearchJobsJobResources = getValuesFromObject(wfsearchJobs, JOBRESOURCES_SEARCH);
                    if(!wfSearchJobsJobResources.isEmpty()) {
                        wfSearchJobsJobResources.forEach(jobResourceName -> jobResourceName.replaceAll("\"",""));
                        if(allItemsGrouped.isEmpty()) {
                            item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfSearchJobsJobResources.stream(), ConfigurationType.JOBRESOURCE.intValue()));
                        } else {
                            allItemsGrouped.get(ConfigurationType.JOBRESOURCE).stream().filter(getPredicate(wfSearchJobsJobResources)).forEach(item.getReferences()::add);
                        }
                        
                    } else {
                        // fallback: if result in WindowsSearch is empty check again directly in json
                        List<String> wfJobResourceNames = new ArrayList<String>(); 
                        getValuesRecursively("", workflow, JOBRESOURCENAMES_SEARCH, wfJobResourceNames);
                        if(!wfJobResourceNames.isEmpty()) {
                            wfJobResourceNames.forEach(jobResourceName -> jobResourceName.replaceAll("\"",""));
                            if (allItemsGrouped.isEmpty()) {
                                item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfJobResourceNames.stream(), ConfigurationType.JOBRESOURCE.intValue()));
                            } else {
                                allItemsGrouped.get(ConfigurationType.JOBRESOURCE).stream().filter(getPredicate(wfJobResourceNames)).forEach(item.getReferences()::add);
                            }
                        }
                    }
                }
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
                List<String> wfJobResourceNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, JOBRESOURCENAMES_SEARCH, wfJobResourceNames);
                if(!wfJobResourceNames.isEmpty()) {
                    wfJobResourceNames.forEach(jobResourceName -> jobResourceName.replaceAll("\"",""));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfJobResourceNames.stream(), ConfigurationType.JOBRESOURCE.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.JOBRESOURCE).stream().filter(getPredicate(wfJobResourceNames)).forEach(item.getReferences()::add);
                    }
                }
            }
            //NoticeBoards
            if(wfsearchInstructions != null) {
                // check from instructions info from WindowsSearch
                List<String> boardNames = getValuesFromObject(wfsearchInstructions, INSTRUCTION_BOARDS_SEARCH);
                if(!boardNames.isEmpty()) {
                    boardNames.forEach(boardName -> boardName.replaceAll("\"",""));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(boardNames.stream(), ConfigurationType.NOTICEBOARD.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.NOTICEBOARD).stream().filter(getPredicate(boardNames)).forEach(item.getReferences()::add);
                    }
                } else {
                    // fallback: if result in WindowsSearch is empty check again directly in json
                    List<String> wfBoardNames = new ArrayList<String>(); 
                    getValuesRecursively("", workflow, INSTRUCTION_BOARDS_SEARCH, wfBoardNames);
                    if(!wfBoardNames.isEmpty()) {
                        wfBoardNames.forEach(boardName -> boardName.replaceAll("\"",""));
                        if (allItemsGrouped.isEmpty()) {
                            item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfBoardNames.stream(), ConfigurationType.NOTICEBOARD.intValue()));
                        } else {
                            allItemsGrouped.get(ConfigurationType.NOTICEBOARD).stream().filter(getPredicate(wfBoardNames)).forEach(item.getReferences()::add);
                        }
                    }
                }
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
                List<String> wfBoardNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, INSTRUCTION_BOARDS_SEARCH, wfBoardNames);
                if(!wfBoardNames.isEmpty()) {
                    wfBoardNames.forEach(boardName -> boardName.replaceAll("\"",""));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfBoardNames.stream(), ConfigurationType.NOTICEBOARD.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.NOTICEBOARD).stream().filter(getPredicate(wfBoardNames)).forEach(item.getReferences()::add);
                    }
                }
            }
            //Workflow
            if(wfsearchInstructions != null) {
                // check from instructions info from WindowsSearch
                List<String> wfInstructionWorkflowNames = getValuesFromObject(wfsearchInstructions, INSTRUCTION_ADDORDERS_SEARCH);
                if(!wfInstructionWorkflowNames.isEmpty()) {
                    wfInstructionWorkflowNames.forEach(workflowName -> workflowName.replaceAll("\"",""));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfInstructionWorkflowNames.stream(), ConfigurationType.WORKFLOW.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.WORKFLOW).stream().filter(getPredicate(wfInstructionWorkflowNames)).forEach(item.getReferences()::add);
                    }
                } else {
                    // fallback: if result in WindowsSearch is empty check again directly in json
                    List<String> wfWorkflowNames = new ArrayList<String>(); 
                    getValuesRecursively("", workflow, WORKFLOWNAME_SEARCH, wfWorkflowNames);
                    if(!wfWorkflowNames.isEmpty()) {
                        wfWorkflowNames.forEach(workflowName -> workflowName.replaceAll("\"",""));
                        if (allItemsGrouped.isEmpty()) {
                            item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfWorkflowNames.stream(), ConfigurationType.WORKFLOW.intValue()));
                        } else {
                            allItemsGrouped.get(ConfigurationType.WORKFLOW).stream().filter(getPredicate(wfWorkflowNames)).forEach(item.getReferences()::add);
                        }
                    }
                }
            } else {
                // if no instructions are available in WindowsSearch check again directly in json
                List<String> wfWorkflowNames = new ArrayList<String>(); 
                getValuesRecursively("", workflow, WORKFLOWNAME_SEARCH, wfWorkflowNames);
                if(!wfWorkflowNames.isEmpty()) {
                    wfWorkflowNames.forEach(workflowName -> workflowName.replaceAll("\"",""));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(wfWorkflowNames.stream(), ConfigurationType.WORKFLOW.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.WORKFLOW).stream().filter(getPredicate(wfWorkflowNames)).forEach(item.getReferences()::add);
                    }
                }
            }
            // ScriptIncludes
            if(wfsearchScripts != null) {
                List<String> wfSearchJobScriptNames = getValuesFromObject(wfsearchScripts, INCLUDESCRIPT_SEARCH);
                if(!wfSearchJobScriptNames.isEmpty()) {
                    Stream<String> names = wfSearchJobScriptNames.stream()
                            .map(JsonConverter.scriptIncludePattern::matcher).map(Matcher::results).flatMap(Function.identity())
                            .map(mr -> mr.group(3));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(names, ConfigurationType.INCLUDESCRIPT.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.INCLUDESCRIPT).stream().filter(getPredicate(names.toList())).forEach(item.getReferences()::add);
                    }
                }
            } else {
                if(JsonConverter.hasScriptIncludes.test(json)) {
                    List<String> wfJobScriptNames = new ArrayList<String>();
                    getValuesRecursively("", workflow, SCRIPT_SEARCH, wfJobScriptNames);
                    if(!wfJobScriptNames.isEmpty()) {
                        Stream<String> names = wfJobScriptNames.stream()
                                .map(JsonConverter.scriptIncludePattern::matcher).map(Matcher::results).flatMap(Function.identity())
                                .map(mr -> mr.group(3));
                        if (allItemsGrouped.isEmpty()) {
                            item.getReferences().addAll(dbLayer.getConfigurationsByNames(names, ConfigurationType.INCLUDESCRIPT.intValue()));
                        } else {
                            allItemsGrouped.get(ConfigurationType.INCLUDESCRIPT).stream().filter(getPredicate(names.toList())).forEach(item.getReferences()::add);
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
                scheduleWorkflows.forEach(workflowName -> workflowName.replaceAll("\"",""));
                if (allItemsGrouped.isEmpty()) {
                    item.getReferences().addAll(dbLayer.getConfigurationsByNames(scheduleWorkflows.stream(), ConfigurationType.WORKFLOW.intValue()));
                } else {
                    allItemsGrouped.get(ConfigurationType.WORKFLOW).stream().filter(getPredicate(scheduleWorkflows)).forEach(item.getReferences()::add);
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
                scheduleCalendars.forEach(calendarName -> calendarName.replaceAll("\"",""));
                if (allItemsGrouped.isEmpty()) {
                    item.getReferences().addAll(dbLayer.getConfigurationsByNames(scheduleCalendars.stream(), ConfigurationType.WORKINGDAYSCALENDAR.intValue()));
                } else {
                    if(allItemsGrouped.get(ConfigurationType.WORKINGDAYSCALENDAR) != null) {
                        allItemsGrouped.get(ConfigurationType.WORKINGDAYSCALENDAR).stream().filter(getPredicate(scheduleCalendars)).forEach(item.getReferences()::add);
                    }
                    if(allItemsGrouped.get(ConfigurationType.NONWORKINGDAYSCALENDAR) != null) {
                        allItemsGrouped.get(ConfigurationType.NONWORKINGDAYSCALENDAR).stream().filter(getPredicate(scheduleCalendars)).forEach(item.getReferences()::add);
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
                jobTemplateJobResources.forEach(jobResourceName -> jobResourceName.replaceAll("\"",""));
                if (allItemsGrouped.isEmpty()) {
                    item.getReferences().addAll(dbLayer.getConfigurationsByNames(jobTemplateJobResources.stream(), ConfigurationType.JOBRESOURCE.intValue()));
                } else {
                    allItemsGrouped.get(ConfigurationType.JOBRESOURCE).stream().filter(getPredicate(jobTemplateJobResources)).forEach(item.getReferences()::add);
                    
                }
            }
            // include script
            if(JsonConverter.hasScriptIncludes.test(json)) {
                List<String> wfJobScriptNames = new ArrayList<String>();
                getValuesRecursively("", jobTemplate, SCRIPT_SEARCH, wfJobScriptNames);
                if(!wfJobScriptNames.isEmpty()) {
                    Stream<String> names = wfJobScriptNames.stream()
                            .map(JsonConverter.scriptIncludePattern::matcher).map(Matcher::results).flatMap(Function.identity())
                            .map(mr -> mr.group(3));
                    if (allItemsGrouped.isEmpty()) {
                        item.getReferences().addAll(dbLayer.getConfigurationsByNames(names, ConfigurationType.INCLUDESCRIPT.intValue()));
                    } else {
                        allItemsGrouped.get(ConfigurationType.INCLUDESCRIPT).stream().filter(getPredicate(names.toList())).forEach(item.getReferences()::add);
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
                fosWorkflows.forEach(workflowName -> workflowName.replaceAll("\"",""));
                if (allItemsGrouped.isEmpty()) {
                    item.getReferences().addAll(dbLayer.getConfigurationsByNames(fosWorkflows.stream(), ConfigurationType.WORKFLOW.intValue()));
                } else {
                    allItemsGrouped.get(ConfigurationType.WORKFLOW).stream().filter(getPredicate(fosWorkflows)).forEach(item.getReferences()::add);
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
                nwCalendars.forEach(nwCalendarName -> nwCalendarName.replaceAll("\"",""));
                if (allItemsGrouped.isEmpty()) {
                    item.getReferences().addAll(dbLayer.getConfigurationsByNames(nwCalendars.stream(), ConfigurationType.NONWORKINGDAYSCALENDAR.intValue()));
                } else {
                    allItemsGrouped.get(ConfigurationType.NONWORKINGDAYSCALENDAR).stream().filter(getPredicate(nwCalendars)).forEach(item.getReferences()::add);
                }
            }
            break;
        default:
            break;
        }
    }
    
    private static Predicate<DBItemInventoryConfiguration> getPredicate (Collection<String> names) {
        return cfg -> names.contains(cfg.getName());
    }
    
    public static void updateDependencies(DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        if (inventoryDbItem == null) {
            return;
        }
        if (!dependencyTypes.contains(inventoryDbItem.getType())) {
            return;
        }
        new Thread(() -> {
            try {
                SOSHibernateSession session = null;
                try {
                    session = Globals.createSosHibernateStatelessConnection("DependencyResolver");
                    resolve(session, inventoryDbItem);
                } finally {
                    Globals.disconnect(session);
                }
            } catch (Exception e) {
                //TODO use ProblemHelper
                LOGGER.error("", e);
            }
        }, threadNamePrefix + Math.abs(threadNameSuffix.incrementAndGet() % 1000)).start();
        
    }
    
    public static void updateDependencies(Collection<DBItemInventoryConfiguration> allCfgs)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        if (allCfgs == null) {
            return;
        }
        updateDependencies(allCfgs.stream().filter(i -> dependencyTypes.contains(i.getType())).toList(), false);
    }

    public static void updateDependencies(Collection<DBItemInventoryConfiguration> allCfgs, boolean withPool)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        if (allCfgs == null || allCfgs.isEmpty()) {
            return;
        }
        new Thread(() -> {
            try {
                SOSHibernateSession session = null;
                try {
                    session = Globals.createSosHibernateStatelessConnection("DependencyResolver");
                    resolveThreaded(session, allCfgs, withPool);
                } finally {
                    Globals.disconnect(session);
                }
            } catch (Exception e) {
                //TODO use ProblemHelper
                LOGGER.error("", e);
            }
        }, threadNamePrefix + Math.abs(threadNameSuffix.incrementAndGet() % 1000)).start();
    }
    
    public static void resolve(SOSHibernateSession session, DBItemInventoryConfiguration inventoryDbItem)
            throws SOSHibernateException, IOException {
        if (inventoryDbItem == null) {
            return;
        }
        // this method is in use (JocInventory update and insert methods)
        ReferencedDbItem references = createReferencedDbItem(inventoryDbItem);
        InventoryDBLayer dblayer = new InventoryDBLayer(session);
        resolveReferences(references, session);
        DBLayerDependencies layer = new DBLayerDependencies(session);
        // store new dependencies
        layer.insertOrReplaceDependencies(references.getReferencedItem(), convert(references, session));
    }

    // this method is in use
    public static void resolveThreaded(SOSHibernateSession session, Collection<DBItemInventoryConfiguration> allCfgs, boolean withPool)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException, InterruptedException {
        if (allCfgs == null) {
            return;
        }
        Map<ConfigurationType, Set<DBItemInventoryConfiguration>> allItemsGrouped = allCfgs.stream().collect(Collectors.groupingBy(
                DBItemInventoryConfiguration::getTypeAsEnum, Collectors.toSet()));

        Date threadsStarted = Date.from(Instant.now());
        Stream<ReferencedDbItem> referencedItemStream = Stream.empty();
        Integer poolSize = 1;
        if (withPool) {
            Integer maxPoolSize = readMaxPoolSize(Globals.getHibernateConfFile());
            if (maxPoolSize != null) {
                poolSize = maxPoolSize / 2;
            }
        }
        Predicate<DBItemInventoryConfiguration> onlyReferncingItemsFilter = cfg -> JocInventory.getTypesFromObjectsWithReferences().contains(cfg
                .getType());
        if (poolSize > 1) {
            // filters to reduce allItemsGrouped
            Function<DBItemInventoryConfiguration, Map<ConfigurationType, Set<DBItemInventoryConfiguration>>> toSubMap = item -> {
                Map<ConfigurationType, Set<DBItemInventoryConfiguration>> subMap = new HashMap<>();
                Set<ConfigurationType> allowedTypes = DependencyResolver.referencedTypesByRequestedType.get(item.getTypeAsEnum());
                allItemsGrouped.forEach((K, V) -> {
                    if (allowedTypes.contains(K)) {
                        subMap.put(K, V);
                    }
                });
                return subMap;
            };
            List<ReferenceCallable> callables = allCfgs.stream().filter(onlyReferncingItemsFilter)
                    .map(item -> new ReferenceCallable(item, toSubMap.apply(item))).collect(Collectors.toList());
            if (!callables.isEmpty()) {
                ExecutorService executorService = null;
                Set<ReferencedDbItem> referencedItems = new HashSet<ReferencedDbItem>();
                if(callables.size() == 1) {
                    try {
                        referencedItems.add(callables.get(0).call());
                    } catch (Exception e) {
                        if (e.getCause() != null) {
                            LOGGER.error("", e.getCause());
                        } else {
                            LOGGER.error("", e);
                        }
                    }
                } else {
                    executorService = Executors.newFixedThreadPool(Math.min(callables.size(), poolSize));
                    try {
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
                }
                referencedItemStream = referencedItems.stream();
            }
        } else {
            referencedItemStream = allCfgs.stream().filter(onlyReferncingItemsFilter).map(DependencyResolver::createReferencedDbItem).peek(item -> {
                try {
                    DependencyResolver.resolveReferences(item, session, allItemsGrouped);
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }).filter(Objects::nonNull);
        }
        DBLayerDependencies layer = new DBLayerDependencies(session);
        referencedItemStream.filter(item -> !item.getReferences().isEmpty()).forEach(ref -> {
            try {
                layer.insertOrReplaceDependencies(ref.getReferencedItem(), convert(ref, layer.getSession()));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        });
        Date threadsFinished = Date.from(Instant.now());
        Long duration = ChronoUnit.MILLIS.between(threadsStarted.toInstant(), threadsFinished.toInstant());
        LOGGER.info("update dependencies finished after " + duration + " milliseconds.");
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
        dependencies = dbLayer.getReferencesDependencies(inventoryObject.getId());
        return dependencies;
    }
    
    // this method is currently in use
    private static Set<DBItemInventoryDependency> convert(ReferencedDbItem reference, SOSHibernateSession session) {
        Set<DBItemInventoryDependency> dependencies = new HashSet<DBItemInventoryDependency>();
        InventoryDBLayer dblayer = new InventoryDBLayer(session);
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
    public static ReferencedDbItem resolveInventoryItemFromDependency(SOSHibernateSession session, DBItemInventoryConfiguration invCfg, List<DBItemInventoryDependency> dependencies)
            throws SOSHibernateException, IOException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        ReferencedDbItem newReferencedDbItem = createReferencedDbItem(invCfg);
          if(dependencies != null && !dependencies.isEmpty()) {
              for(DBItemInventoryDependency dependency : dependencies) {
                  DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(dependency.getInvId());
                  if(cfg != null) {
                      newReferencedDbItem.getReferences().add(cfg);
                  }
              }
          }
        return newReferencedDbItem;
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
