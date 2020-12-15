package com.sos.joc.deploy.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bouncycastle.bcpg.sig.Exportable;

import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.instruction.ForkJoin;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.Instructions;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.instruction.OptionalInstructions;
import com.sos.jobscheduler.model.job.ExecutableScript;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.workflow.Branch;
import com.sos.jobscheduler.model.workflow.Jobs;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.ConfigurationFilter;
import com.sos.joc.model.publish.ControllerId;
import com.sos.joc.model.publish.DepHistory;
import com.sos.joc.model.publish.DeployConfig;
import com.sos.joc.model.publish.DeployConfigDelete;
import com.sos.joc.model.publish.DeployConfiguration;
import com.sos.joc.model.publish.DeployConfigurationDelete;
import com.sos.joc.model.publish.DeployDelete;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployableObjects;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.DeploymentVersion;
import com.sos.joc.model.publish.DraftConfig;
import com.sos.joc.model.publish.DraftConfiguration;
import com.sos.joc.model.publish.ExcludeConfiguration;
import com.sos.joc.model.publish.ExportDeployables;
import com.sos.joc.model.publish.ExportFile;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.ExportForBackup;
import com.sos.joc.model.publish.ExportForSigning;
import com.sos.joc.model.publish.ExportReleasables;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.ReleasableConfig;
import com.sos.joc.model.publish.ReleasedConfig;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.model.publish.SetVersionsFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.model.publish.Signature;

public class DeploymentTestUtils {

    public static Workflow createForkJoinWorkflow() {
        return createForkJoinWorkflow("2.0.0-SNAPSHOT", "/test/ForkJoinWorkflow");
    }
    
    public static Workflow createForkJoinWorkflow(String versionId, String path) {
        Workflow workflow = new Workflow();
        workflow.setVersionId(versionId);
        workflow.setPath(path);
        
        ForkJoin forkJoinInstruction = createForkJoinInstruction();

        Jobs jobs = new Jobs();
        jobs.setAdditionalProperty("jobBranch1", createJob("agent1", "@echo off\\necho USERNAME=%USERNAME%"));
        jobs.setAdditionalProperty("jobBranch2", createJob("agent1", "@echo off\\necho HOST=%COMPUTERNAME%"));
        jobs.setAdditionalProperty("jobBranch3", createJob("agent1", "@echo off\\necho USER_HOME=%USERPROFILE%"));
        jobs.setAdditionalProperty("jobAfterJoin", createJob("agent1", "@echo off\\necho TEMP=%TEMP%"));
        workflow.setJobs(jobs);
        
        List<Branch> branches = new ArrayList<Branch>();
        Branch branch1 = new Branch();
        List<Instruction> branch1Instructions = new ArrayList<Instruction>();
        branch1Instructions.add(createJobInstruction("agent1", "jobBranch1", new Integer[] { 0, 100 }, new Integer[] { 1 }));
        branch1.setWorkflow(new Instructions(branch1Instructions));
        branch1.setId("BRANCH1");
        branches.add(branch1);
        Branch branch2 = new Branch();
        List<Instruction> branch2Instructions = new ArrayList<Instruction>();
        branch2Instructions.add(createJobInstruction("agent1", "jobBranch2", new Integer[] { 0, 101 }, new Integer[] { 1, 2 }));
        branch2.setWorkflow(new Instructions(branch2Instructions));
        branch2.setId("BRANCH2");
        branches.add(branch2);
        Branch branch3 = new Branch();
        List<Instruction> branch3Instructions = new ArrayList<Instruction>();
        branch3Instructions.add(createJobInstruction("agent1", "jobBranch3", new Integer[] { 0, 102 }, new Integer[] { 1, 2, 3 }));
        branch3.setWorkflow(new Instructions(branch3Instructions));
        branch3.setId("BRANCH3");
        branches.add(branch3);
        forkJoinInstruction.setBranches(branches);

        NamedJob afterForkJoin = createJobInstruction("agent1", "jobAfterJoin", new Integer[] { 0 }, new Integer[] { 1, 99 });

        List<Instruction> workflowInstructions = new ArrayList<Instruction>();
        workflowInstructions.add(forkJoinInstruction);
        workflowInstructions.add(afterForkJoin);
        workflow.setInstructions(workflowInstructions);

        return workflow;
    }

    public static Workflow createIfElseWorkflow() {
        return createIfElseWorkflow("2.0.0-SNAPSHOT", "/test/IfElseWorkflow");
    }
    
    public static Workflow createIfElseWorkflow(String versionId, String path) {
        Workflow workflow = new Workflow();
        workflow.setVersionId(versionId);
        workflow.setPath(path);
        // workflow.setId(wfId);
        List<Instruction> thenInstructions = new ArrayList<Instruction>();
        List<Instruction> elseInstructions = new ArrayList<Instruction>();

        Jobs jobs = new Jobs();
        jobs.setAdditionalProperty("job1", createJob("agent1", "@echo off\\necho USERNAME=%USERNAME%"));
        jobs.setAdditionalProperty("job2", createJob("agent1", "@echo off\\necho HOST=%COMPUTERNAME%"));
        jobs.setAdditionalProperty("job3", createJob("agent1", "@echo off\\necho USER_HOME=%USERPROFILE%"));
        jobs.setAdditionalProperty("job4", createJob("agent1", "@echo off\\necho TEMP=%TEMP%"));
        workflow.setJobs(jobs);
        

        NamedJob job1 = createJobInstruction("agent1", "job1", new Integer[] { 0, 100 }, new Integer[] { 1, 2 });
        NamedJob job2 = createJobInstruction("agent1", "job2", new Integer[] { 0, 101, 102 }, new Integer[] { 1, 3, 4 });
        NamedJob job3 = createJobInstruction("agent2", "job3", new Integer[] { 0, 103 }, new Integer[] { 1, 5, 6 });
        NamedJob job4 = createJobInstruction("agent2", "job4", new Integer[] { 0, 104, 105 }, new Integer[] { -1, 1, 99 });

        IfElse ifInstruction = createIfInstruction("variable(key='myParam', default='0')");

        thenInstructions.add(job1);
        thenInstructions.add(job2);
        ifInstruction.setThen(new Instructions(thenInstructions));

        elseInstructions.add(job3);
        elseInstructions.add(job4);
        ifInstruction.setElse(new OptionalInstructions(elseInstructions));

        List<Instruction> workflowInstructions = new ArrayList<Instruction>();
        workflowInstructions.add(ifInstruction);
        workflow.setInstructions(workflowInstructions);
        return workflow;
    }

    public static IfElse createIfInstruction(String condition) {
        IfElse ifInstruction = new IfElse();
        ifInstruction.setPredicate(condition);
        return ifInstruction;
    }

    public static ForkJoin createForkJoinInstruction() {
        ForkJoin forkJoinInstruction = new ForkJoin();
        return forkJoinInstruction;
    }

    public static NamedJob createJobInstruction(String agentName, String jobName, Integer[] successes, Integer[] errors) {
        NamedJob job = new NamedJob();
        job.setJobName(jobName);
        return job;
    }

    public static Set<Workflow> createWorkflowsforDeployment() {
        Set<Workflow> workflows = new HashSet<Workflow>();
        String commitVersionId = "version_test";
        for (int i = 1; i <= 100; i++) {
            if (i <= 50) {
                workflows.add(DeploymentTestUtils.createIfElseWorkflow(commitVersionId, "/myWorkflows/ifElseWorkflow/workflow_" + i));
            } else {
                workflows.add(DeploymentTestUtils.createForkJoinWorkflow(commitVersionId, "/myWorkflows/forkJoinWorkflows/workflow_" + i));
            }
        }
        return workflows;
    }
    
    public static Set<Workflow> createSingleWorkflowsforDeployment() {
        Set<Workflow> workflows = new HashSet<Workflow>();

        String commitVersionId = UUID.randomUUID().toString();
        workflows.add(DeploymentTestUtils.createIfElseWorkflow(commitVersionId, "/myWorkflows/ifElseWorkflow/workflow_1"));
        return workflows;
    }
    
    public static JSObject createJsObjectForDeployment(Workflow workflow) {
        return createJsObjectForDeployment(workflow, null);        
    }

    public static JSObject createJsObjectForDeployment(Workflow workflow, Signature signature) {
        JSObject jsObject = new JSObject();
        jsObject.setObjectType(DeployType.WORKFLOW);
        jsObject.setAccount("ME!");
        jsObject.setComment("Created from JUnit test class \"DeploymentTests\".");
        jsObject.setModified(Date.from(Instant.now()));
        jsObject.setPath(workflow.getPath());
        jsObject.setContent(workflow);
        if(signature != null) {
            jsObject.setSignedContent(signature.getSignatureString());
        }
        return jsObject;        
    }

    public static Job createJob(String agentRef, String script) {
        Job job = new Job();
        job.setAgentName(agentRef);
        job.setTaskLimit(5);
        ExecutableScript executableScript = new ExecutableScript();
        executableScript.setScript(script);
        job.setExecutable(executableScript);
        return job;
    }

    public static ShowDepHistoryFilter createDefaultShowDepHistoryFilter() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        filter.setAccount("root");
        filter.setCommitId("4cbb095d-b998-4091-92f2-4fb8efb58805");
        filter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        filter.setDeploymentDate(Date.from(depDate));
        filter.setDeleteDate(Date.from(depDate));
        filter.setDeployType(DeployType.WORKFLOW.name());
        filter.setFolder("/myWorkflows/myIfElseWorkflows");
        filter.setOperation(OperationType.UPDATE.name());
        filter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        filter.setState(DeploymentState.DEPLOYED.name());
        filter.setFrom("-10d");
        filter.setTo("-5d");
        filter.setVersion("0.0.1");
        filter.setTimeZone("Europe/Berlin");
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByFromToAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        filter.setAccount("root");
        filter.setControllerId("testsuite");
        filter.setDeployType(DeployType.WORKFLOW.name());
        filter.setFolder("/myWorkflows/myIfElseWorkflows");
        filter.setOperation(OperationType.UPDATE.name());
        filter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        filter.setState(DeploymentState.DEPLOYED.name());
        filter.setFrom("-10d");
        filter.setTo("-5d");
        filter.setVersion("0.0.1");
        filter.setTimeZone("Europe/Berlin");
        filter.setCommitId(null);
        filter.setDeploymentDate(null);
        filter.setDeleteDate(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByDeploymentDateAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        filter.setAccount("root");
        filter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        filter.setDeploymentDate(Date.from(depDate));
        filter.setDeployType(DeployType.WORKFLOW.name());
        filter.setFolder("/myWorkflows/myIfElseWorkflows");
        filter.setOperation(OperationType.UPDATE.name());
        filter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        filter.setState(DeploymentState.DEPLOYED.name());
        filter.setVersion("0.0.1");
        filter.setFrom(null);
        filter.setTo(null);
        filter.setTimeZone(null);
        filter.setDeleteDate(null);
        filter.setCommitId(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByDeleteDateAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        filter.setAccount("root");
        filter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        filter.setDeleteDate(Date.from(depDate));
        filter.setDeployType(DeployType.WORKFLOW.name());
        filter.setFolder("/myWorkflows/myIfElseWorkflows");
        filter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        filter.setState(DeploymentState.DEPLOYED.name());
        filter.setVersion("0.0.1");
        filter.setCommitId(null);
        filter.setDeploymentDate(null);
        filter.setOperation(OperationType.DELETE.name());
        filter.setFrom(null);
        filter.setTo(null);
        filter.setTimeZone(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByDeleteOperationAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        filter.setAccount("root");
        filter.setControllerId("testsuite");
        filter.setDeployType(DeployType.WORKFLOW.name());
        filter.setFolder("/myWorkflows/myIfElseWorkflows");
        filter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        filter.setState(DeploymentState.DEPLOYED.name());
        filter.setVersion("0.0.1");
        filter.setCommitId(null);
        filter.setDeploymentDate(null);
        filter.setDeleteDate(null);
        filter.setOperation(OperationType.DELETE.name());
        filter.setFrom(null);
        filter.setTo(null);
        filter.setTimeZone(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByCommitIdAndFolder() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        filter.setAccount("root");
        filter.setCommitId("4cbb095d-b998-4091-92f2-4fb8efb58805");
        filter.setFolder("/myWorkflows/myIfElseWorkflows");
        filter.setFrom(null);
        filter.setTo(null);
        filter.setTimeZone(null);
        filter.setDeploymentDate(null);
        filter.setDeleteDate(null);
        filter.setOperation(null);
        filter.setDeployType(null);
        filter.setState(null);
        filter.setPath(null);
        filter.setControllerId(null);
        filter.setVersion(null);
        return filter;
    }
    
    public static RedeployFilter createDefaultRedeployFilter() {
        RedeployFilter filter = new RedeployFilter();
        filter.setControllerId("js7-cluster");
        filter.setFolder("/myWorkflows/myIfElseWorkflow");
        ExcludeConfiguration exclude = new ExcludeConfiguration();
        exclude.setPath("/myWorkflows/myIfElseWorkflows/workflow_12");
        exclude.setDeployType(ConfigurationType.WORKFLOW);
        filter.getExcludes().add(exclude);
        return filter;
    }
    
    public static DepHistory createDepHistory() {
        DepHistory depHistory = new DepHistory();
        
        return depHistory;
    }

    public static Object getValueByFilterAttribute (ShowDepHistoryFilter filter, String attribute) {
        switch(attribute) {
            case "account":
                return filter.getAccount();
            case "path":
                return filter.getPath();
            case "folder":
                return filter.getFolder();
            case "type":
                return DeployType.fromValue(filter.getDeployType()).intValue();
            case "controllerId":
                return filter.getControllerId();
            case "commitId":
                return filter.getCommitId();
            case "version":
                return filter.getVersion();
            case "operation":
                return OperationType.valueOf(filter.getOperation()).value();
            case "state":
                return DeploymentState.valueOf(filter.getState()).value();
            case "deploymentDate":
                return filter.getDeploymentDate();
            case "deleteDate":
                return filter.getDeleteDate();
            case "from":
                return JobSchedulerDate.getDateFrom(filter.getFrom(), filter.getTimeZone());
            case "to":
                return JobSchedulerDate.getDateTo(filter.getTo(), filter.getTimeZone());
            case "timeZone":
                return filter.getTimeZone();
        }
        return null;
    }

    public static Set<String> extractDefaultShowDepHistoryFilterAttributes (ShowDepHistoryFilter filter) {
        Set<String> filterAttributes = new HashSet<String>();
        if (filter.getAccount() != null) {
            filterAttributes.add("account");
        }
        if (filter.getPath() != null) {
            filterAttributes.add("path");
        }
        if (filter.getFolder() != null) {
            filterAttributes.add("folder");
        }
        if (filter.getDeployType() != null) {
            filterAttributes.add("type");
        }
        if (filter.getControllerId() != null) {
            filterAttributes.add("controllerId");
        }
        if (filter.getCommitId() != null) {
            filterAttributes.add("commitId");
        }
        if (filter.getVersion() != null) {
            filterAttributes.add("version");
        }
        if (filter.getOperation() != null) {
            filterAttributes.add("operation");
        }
        if (filter.getState() != null) {
            filterAttributes.add("state");
        }
        if (filter.getDeploymentDate() != null) {
            filterAttributes.add("deploymentDate");
        }
        if (filter.getDeleteDate() != null) {
            filterAttributes.add("deleteDate");
        }
        if (filter.getFrom() != null) {
            filterAttributes.add("from");
        }
        if (filter.getTo() != null) {
            filterAttributes.add("to");
        }
        return filterAttributes;
    }

    public static DeployFilter createExampleDeployFilter () {
        DeployFilter filter = new DeployFilter();
        
        DeployableObjects toStore = new DeployableObjects();
        filter.setStore(toStore);

        DeployDelete delete = new DeployDelete();
        filter.setDelete(delete);

        ControllerId js7Cluster = new ControllerId();
        js7Cluster.setControllerId("js7-cluster");
        ControllerId standalone = new ControllerId();
        standalone.setControllerId("standalone");

        filter.getControllerIds().add(js7Cluster);
        filter.getControllerIds().add(standalone);
        
        DraftConfig workflow10DraftConfig = new DraftConfig();
        ConfigurationFilter workflow10draft = new ConfigurationFilter();
        workflow10draft.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow10DraftConfig.setDraftConfiguration(workflow10draft);

        DraftConfig workflow16DraftConfig = new DraftConfig();
        ConfigurationFilter workflow16draft = new ConfigurationFilter();
        workflow16draft.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow16DraftConfig.setDraftConfiguration(workflow16draft);
        
        toStore.getDraftConfigurations().add(workflow10DraftConfig);
        toStore.getDraftConfigurations().add(workflow16DraftConfig);
        
        DeployConfig workflow12deployConfig = new DeployConfig();
        DeployConfiguration workflow12deployed = new DeployConfiguration();
        workflow12deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow12deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow12deployConfig.setDeployConfiguration(workflow12deployed);
        toStore.getDeployConfigurations().add(workflow12deployConfig);
        
        DeployConfigDelete toDeleteConfig = new DeployConfigDelete();
        ConfigurationFilter toDelete = new ConfigurationFilter();
        toDelete.setPath("/myWorkflows/forkJoinWorkflows/workflow_88");
        toDelete.setObjectType(ConfigurationType.WORKFLOW);
        toDeleteConfig.setDeployConfiguration(toDelete);
        delete.getDeployConfigurations().add(toDeleteConfig);
        
        return filter;
    }

    public static ExportFilter createExampleExportFilter (boolean forSigning) {
        ExportFilter filter = new ExportFilter();

        ExportFile exportFile = new ExportFile();
        exportFile.setName("test_export");
        exportFile.setFormat(ArchiveFormat.ZIP);
        filter.setExportFile(exportFile);

        ExportDeployables exportDeployables = new ExportDeployables();
        DraftConfig workflow10DraftConfig = new DraftConfig();
        ConfigurationFilter workflow10draft = new ConfigurationFilter();
        workflow10draft.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow10DraftConfig.setDraftConfiguration(workflow10draft);
        exportDeployables.getDraftConfigurations().add(workflow10DraftConfig);
        DraftConfig workflow16DraftConfig = new DraftConfig();
        ConfigurationFilter workflow16draft = new ConfigurationFilter();
        workflow16draft.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow16DraftConfig.setDraftConfiguration(workflow16draft);
        exportDeployables.getDraftConfigurations().add(workflow16DraftConfig);
        DeployConfig workflow12deployConfig = new DeployConfig();
        DeployConfiguration workflow12deployed = new DeployConfiguration();
        workflow12deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow12deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow12deployConfig.setDeployConfiguration(workflow12deployed);
        exportDeployables.getDeployConfigurations().add(workflow12deployConfig);
        
        ExportReleasables exportReleasables = new ExportReleasables();
        ReleasableConfig scheduleCfg = new ReleasableConfig();
        ConfigurationFilter schedule = new ConfigurationFilter();
        schedule.setPath("/mySchedules/newSchedules/mySchedule");
        schedule.setObjectType(ConfigurationType.SCHEDULE);
        scheduleCfg.setDraftConfiguration(schedule);
        exportReleasables.getDraftConfigurations().add(scheduleCfg);
        ReleasedConfig calendarCfg = new ReleasedConfig();
        ConfigurationFilter calendar = new ConfigurationFilter();
        calendar.setPath("/myCalendars/newCalendars/myCalendar");
        calendar.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
        calendarCfg.setReleasedConfiguration(calendar);
        exportReleasables.getReleasedConfigurations().add(calendarCfg);

        if (forSigning) {
            ExportForSigning exportSigning = new ExportForSigning();
            filter.setForSigning(exportSigning);
            exportSigning.setControllerId("testsuite");
            exportSigning.setDeployables(exportDeployables);
        } else {
            ExportForBackup exportBackup = new ExportForBackup();
            filter.setForBackup(exportBackup);
            exportBackup.setDeployables(exportDeployables);
            exportBackup.setReleasables(exportReleasables);
        }
        return filter;
    }

    public static SetVersionFilter createExampleSetVersionFilter () {
        SetVersionFilter filter = new SetVersionFilter();

        DeployConfig workflow10deployConfig = new DeployConfig();
        DeployConfiguration workflow10deployed = new DeployConfiguration();
        workflow10deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow10deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow10deployConfig.setDeployConfiguration(workflow10deployed);
        filter.getDeployConfigurations().add(workflow10deployConfig);
        
        DeployConfig workflow12deployConfig = new DeployConfig();
        DeployConfiguration workflow12deployed = new DeployConfiguration();
        workflow12deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow12deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow12deployConfig.setDeployConfiguration(workflow12deployed);
        filter.getDeployConfigurations().add(workflow12deployConfig);

        DeployConfig workflow14deployConfig = new DeployConfig();
        DeployConfiguration workflow14deployed = new DeployConfiguration();
        workflow14deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_14");
        workflow14deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow14deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow14deployConfig.setDeployConfiguration(workflow14deployed);
        filter.getDeployConfigurations().add(workflow14deployConfig);

        DeployConfig workflow16deployConfig = new DeployConfig();
        DeployConfiguration workflow16deployed = new DeployConfiguration();
        workflow16deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow16deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow16deployConfig.setDeployConfiguration(workflow16deployed);
        filter.getDeployConfigurations().add(workflow16deployConfig);

        DeployConfig workflow18deployConfig = new DeployConfig();
        DeployConfiguration workflow18deployed = new DeployConfiguration();
        workflow18deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_18");
        workflow18deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow18deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow18deployConfig.setDeployConfiguration(workflow18deployed);
        filter.getDeployConfigurations().add(workflow18deployConfig);
        filter.setVersion("myTestVersion-1.0.0");
        return filter;
    }
    
    public static SetVersionsFilter createExampleSetVersionsFilter () {
        SetVersionsFilter filter = new SetVersionsFilter();

        DeploymentVersion dvWf10 = new DeploymentVersion();
        DeployConfiguration workflow10 = new DeployConfiguration();
        workflow10.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10.setObjectType(ConfigurationType.WORKFLOW);
        workflow10.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        dvWf10.setDeployConfiguration(workflow10);
        dvWf10.setVersion("myTestVersion-1.0.0");
        filter.getDeployConfigurations().add(dvWf10);
        
        DeploymentVersion dvWf12 = new DeploymentVersion();
        DeployConfiguration workflow12 = new DeployConfiguration();
        workflow12.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12.setObjectType(ConfigurationType.WORKFLOW);
        workflow12.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        dvWf12.setDeployConfiguration(workflow12);
        dvWf12.setVersion("myTestVersion-1.0.0");
        filter.getDeployConfigurations().add(dvWf12);
        
        DeploymentVersion dvWf14 = new DeploymentVersion();
        DeployConfiguration workflow14 = new DeployConfiguration();
        workflow14.setPath("/myWorkflows/ifElseWorkflow/workflow_14");
        workflow14.setObjectType(ConfigurationType.WORKFLOW);
        workflow14.setCommitId("2a6487a7-091c-446a-b799-67c87b4db6c2");
        dvWf14.setDeployConfiguration(workflow14);
        dvWf14.setVersion("myTestVersion-1.1.0");
        filter.getDeployConfigurations().add(dvWf14);
        
        DeploymentVersion dvWf16 = new DeploymentVersion();
        DeployConfiguration workflow16 = new DeployConfiguration();
        workflow16.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16.setObjectType(ConfigurationType.WORKFLOW);
        workflow16.setCommitId("2a6487a7-091c-446a-b799-67c87b4db6c2");
        dvWf16.setDeployConfiguration(workflow16);
        dvWf16.setVersion("myTestVersion-1.1.0");
        filter.getDeployConfigurations().add(dvWf16);
        
        DeploymentVersion dvWf18 = new DeploymentVersion();
        DeployConfiguration workflow18 = new DeployConfiguration();
        workflow18.setPath("/myWorkflows/ifElseWorkflow/workflow_18");
        workflow18.setObjectType(ConfigurationType.WORKFLOW);
        workflow18.setCommitId("17cd67d9-37cb-4d28-8596-c88eab6a755d");
        dvWf18.setDeployConfiguration(workflow18);
        dvWf18.setVersion("myTestVersion-1.1.12");
        filter.getDeployConfigurations().add(dvWf18);
        
        return filter;
    }
    
}
