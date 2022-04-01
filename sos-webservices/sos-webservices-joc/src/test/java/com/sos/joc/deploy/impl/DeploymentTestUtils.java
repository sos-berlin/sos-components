package com.sos.joc.deploy.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.joc.Globals;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.path.PathFilter;
import com.sos.joc.model.inventory.path.PathResponse;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.DepHistory;
import com.sos.joc.model.publish.DepHistoryCompactFilter;
import com.sos.joc.model.publish.DepHistoryDetailFilter;
import com.sos.joc.model.publish.DeployDeleteFilter;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployablesFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.DeploymentVersion;
import com.sos.joc.model.publish.ExportFile;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.ExportForSigning;
import com.sos.joc.model.publish.ExportShallowCopy;
import com.sos.joc.model.publish.GenerateCaFilter;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.ReleasablesFilter;
import com.sos.joc.model.publish.SetRootCaFilter;
import com.sos.joc.model.publish.SetVersionFilter;
import com.sos.joc.model.publish.SetVersionsFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.model.publish.git.AddCredentialsFilter;
import com.sos.joc.model.publish.git.GetCredentialsFilter;
import com.sos.joc.model.publish.git.GitCredentials;
import com.sos.joc.model.publish.git.RemoveCredentials;
import com.sos.joc.model.publish.git.RemoveCredentialsFilter;
import com.sos.joc.model.publish.git.commands.AddAllFilter;
import com.sos.joc.model.publish.git.commands.CheckoutFilter;
import com.sos.joc.model.publish.git.commands.CloneFilter;
import com.sos.joc.model.publish.git.commands.CommitFilter;
import com.sos.joc.model.publish.git.commands.CommonFilter;
import com.sos.joc.model.publish.git.commands.LogFilter;
import com.sos.joc.model.publish.git.commands.PullFilter;
import com.sos.joc.model.publish.git.commands.PushFilter;
import com.sos.joc.model.publish.git.commands.ResetFilter;
import com.sos.joc.model.publish.git.commands.RestoreFilter;
import com.sos.joc.model.publish.git.commands.StatusFilter;
import com.sos.joc.model.publish.git.commands.TagFilter;
import com.sos.joc.model.publish.repository.Category;
import com.sos.joc.model.publish.repository.Configurations;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.model.publish.repository.DeleteFromFilter;
import com.sos.joc.model.publish.repository.ReadFromFilter;
import com.sos.joc.model.publish.repository.ResponseFolder;
import com.sos.joc.model.publish.repository.ResponseFolderItem;
import com.sos.joc.model.publish.repository.UpdateFromFilter;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.sign.model.instruction.ForkJoin;
import com.sos.sign.model.instruction.IfElse;
import com.sos.sign.model.instruction.Instruction;
import com.sos.sign.model.instruction.Instructions;
import com.sos.sign.model.instruction.NamedJob;
import com.sos.sign.model.instruction.OptionalInstructions;
import com.sos.sign.model.job.Job;
import com.sos.sign.model.workflow.Branch;
import com.sos.sign.model.workflow.BranchWorkflow;
import com.sos.sign.model.workflow.Jobs;
import com.sos.sign.model.workflow.Workflow;

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
        branch1.setWorkflow(new BranchWorkflow(branch1Instructions, null));
        branch1.setId("BRANCH1");
        branches.add(branch1);
        Branch branch2 = new Branch();
        List<Instruction> branch2Instructions = new ArrayList<Instruction>();
        branch2Instructions.add(createJobInstruction("agent1", "jobBranch2", new Integer[] { 0, 101 }, new Integer[] { 1, 2 }));
        branch2.setWorkflow(new BranchWorkflow(branch2Instructions, null));
        branch2.setId("BRANCH2");
        branches.add(branch2);
        Branch branch3 = new Branch();
        List<Instruction> branch3Instructions = new ArrayList<Instruction>();
        branch3Instructions.add(createJobInstruction("agent1", "jobBranch3", new Integer[] { 0, 102 }, new Integer[] { 1, 2, 3 }));
        branch3.setWorkflow(new BranchWorkflow(branch3Instructions, null));
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
    
    public static ControllerObject createJsObjectForDeployment(Workflow workflow) {
        return createJsObjectForDeployment(workflow, null);        
    }

    public static ControllerObject createJsObjectForDeployment(Workflow workflow, Signature signature) {
        ControllerObject jsObject = new ControllerObject();
        jsObject.setObjectType(DeployType.WORKFLOW);
        jsObject.setAccount("SP");
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
        job.setAgentPath(agentRef);
        job.setParallelism(5);
        ExecutableScript executableScript = new ExecutableScript();
        executableScript.setScript(script);
        job.setExecutable(executableScript);
        return job;
    }

    public static ShowDepHistoryFilter createDefaultShowDepHistoryCompactFilter() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryCompactFilter compactFilter = new DepHistoryCompactFilter();
        filter.setCompactFilter(compactFilter);
        compactFilter.setAccount("root");
        compactFilter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        compactFilter.setDeploymentDate(Date.from(depDate));
        compactFilter.setDeleteDate(Date.from(depDate));
        compactFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        compactFilter.setFrom("-10d");
        compactFilter.setTo("-5d");
        compactFilter.setTimeZone("Europe/Berlin");
        return filter;
    }
    
    public static ShowDepHistoryFilter createDefaultShowDepHistoryDetailFilter() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryDetailFilter detailFilter = new DepHistoryDetailFilter();
        filter.setDetailFilter(detailFilter);
        detailFilter.setAccount("root");
        detailFilter.setCommitId("4cbb095d-b998-4091-92f2-4fb8efb58805");
        detailFilter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        detailFilter.setDeploymentDate(Date.from(depDate));
        detailFilter.setDeleteDate(Date.from(depDate));
        detailFilter.setDeployType(DeployType.WORKFLOW.name());
        detailFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        detailFilter.setOperation(OperationType.UPDATE.name());
        detailFilter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        detailFilter.setState(DeploymentState.DEPLOYED.name());
        detailFilter.setFrom("-10d");
        detailFilter.setTo("-5d");
        detailFilter.setVersion("0.0.1");
        detailFilter.setTimeZone("Europe/Berlin");
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByFromToAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryDetailFilter detailFilter = new DepHistoryDetailFilter();
        filter.setDetailFilter(detailFilter);
        detailFilter.setAccount("root");
        detailFilter.setControllerId("testsuite");
        detailFilter.setDeployType(DeployType.WORKFLOW.name());
        detailFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        detailFilter.setOperation(OperationType.UPDATE.name());
        detailFilter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        detailFilter.setState(DeploymentState.DEPLOYED.name());
        detailFilter.setFrom("-10d");
        detailFilter.setTo("-5d");
        detailFilter.setVersion("0.0.1");
        detailFilter.setTimeZone("Europe/Berlin");
        detailFilter.setCommitId(null);
        detailFilter.setDeploymentDate(null);
        detailFilter.setDeleteDate(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByDeploymentDateAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryDetailFilter detailFilter = new DepHistoryDetailFilter();
        filter.setDetailFilter(detailFilter);
        detailFilter.setAccount("root");
        detailFilter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        detailFilter.setDeploymentDate(Date.from(depDate));
        detailFilter.setDeployType(DeployType.WORKFLOW.name());
        detailFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        detailFilter.setOperation(OperationType.UPDATE.name());
        detailFilter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        detailFilter.setState(DeploymentState.DEPLOYED.name());
        detailFilter.setVersion("0.0.1");
        detailFilter.setFrom(null);
        detailFilter.setTo(null);
        detailFilter.setTimeZone(null);
        detailFilter.setDeleteDate(null);
        detailFilter.setCommitId(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByDeleteDateAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryDetailFilter detailFilter = new DepHistoryDetailFilter();
        filter.setDetailFilter(detailFilter);
        detailFilter.setAccount("root");
        detailFilter.setControllerId("testsuite");
        Instant depDate = Instant.parse("2020-11-06T06:48:21.00Z");
        detailFilter.setDeleteDate(Date.from(depDate));
        detailFilter.setDeployType(DeployType.WORKFLOW.name());
        detailFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        detailFilter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        detailFilter.setState(DeploymentState.DEPLOYED.name());
        detailFilter.setVersion("0.0.1");
        detailFilter.setCommitId(null);
        detailFilter.setDeploymentDate(null);
        detailFilter.setOperation(OperationType.DELETE.name());
        detailFilter.setFrom(null);
        detailFilter.setTo(null);
        detailFilter.setTimeZone(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByDeleteOperationAndPath() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryDetailFilter detailFilter = new DepHistoryDetailFilter();
        filter.setDetailFilter(detailFilter);
        detailFilter.setAccount("root");
        detailFilter.setControllerId("testsuite");
        detailFilter.setDeployType(DeployType.WORKFLOW.name());
        detailFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        detailFilter.setPath("/myWorkflows/myIfElseWorkflows/workflow_01");
        detailFilter.setState(DeploymentState.DEPLOYED.name());
        detailFilter.setVersion("0.0.1");
        detailFilter.setCommitId(null);
        detailFilter.setDeploymentDate(null);
        detailFilter.setDeleteDate(null);
        detailFilter.setOperation(OperationType.DELETE.name());
        detailFilter.setFrom(null);
        detailFilter.setTo(null);
        detailFilter.setTimeZone(null);
        return filter;
    }
    
    public static ShowDepHistoryFilter createShowDepHistoryFilterByCommitIdAndFolder() {
        ShowDepHistoryFilter filter = new ShowDepHistoryFilter();
        DepHistoryDetailFilter detailFilter = new DepHistoryDetailFilter();
        filter.setDetailFilter(detailFilter);
        detailFilter.setAccount("root");
        detailFilter.setCommitId("4cbb095d-b998-4091-92f2-4fb8efb58805");
        detailFilter.setFolder("/myWorkflows/myIfElseWorkflows");
        detailFilter.setFrom(null);
        detailFilter.setTo(null);
        detailFilter.setTimeZone(null);
        detailFilter.setDeploymentDate(null);
        detailFilter.setDeleteDate(null);
        detailFilter.setOperation(null);
        detailFilter.setDeployType(null);
        detailFilter.setState(null);
        detailFilter.setPath(null);
        detailFilter.setControllerId(null);
        detailFilter.setVersion(null);
        return filter;
    }
    
    public static RedeployFilter createDefaultRedeployFilter() {
        RedeployFilter filter = new RedeployFilter();
        filter.setControllerId("js7-cluster");
        filter.setFolder("/myWorkflows/myIfElseWorkflow");
        filter.setRecursive(true);
        return filter;
    }
    
    public static DepHistory createDepHistory() {
        DepHistory depHistory = new DepHistory();
        
        return depHistory;
    }

////    public static Object getValueByFilterAttribute (ShowDepHistoryFilter filter, String attribute) {
////        switch(attribute) {
////            case "account":
////                return filter.getAccount();
////            case "path":
////                return filter.getPath();
////            case "folder":
////                return filter.getFolder();
////            case "type":
////                return DeployType.fromValue(filter.getDeployType()).intValue();
////            case "controllerId":
////                return filter.getControllerId();
////            case "commitId":
////                return filter.getCommitId();
////            case "version":
////                return filter.getVersion();
////            case "operation":
////                return OperationType.valueOf(filter.getOperation()).value();
////            case "state":
////                return DeploymentState.valueOf(filter.getState()).value();
////            case "deploymentDate":
////                return filter.getDeploymentDate();
////            case "deleteDate":
////                return filter.getDeleteDate();
////            case "from":
////                return JobSchedulerDate.getDateFrom(filter.getFrom(), filter.getTimeZone());
////            case "to":
////                return JobSchedulerDate.getDateTo(filter.getTo(), filter.getTimeZone());
////            case "timeZone":
////                return filter.getTimeZone();
////        }
////        return null;
////    }
////
//    public static Set<String> extractDefaultShowDepHistoryFilterAttributes (ShowDepHistoryFilter filter) {
//        Set<String> filterAttributes = new HashSet<String>();
//        if (filter.getAccount() != null) {
//            filterAttributes.add("account");
//        }
//        if (filter.getPath() != null) {
//            filterAttributes.add("path");
//        }
//        if (filter.getFolder() != null) {
//            filterAttributes.add("folder");
//        }
//        if (filter.getDeployType() != null) {
//            filterAttributes.add("type");
//        }
//        if (filter.getControllerId() != null) {
//            filterAttributes.add("controllerId");
//        }
//        if (filter.getCommitId() != null) {
//            filterAttributes.add("commitId");
//        }
//        if (filter.getVersion() != null) {
//            filterAttributes.add("version");
//        }
//        if (filter.getOperation() != null) {
//            filterAttributes.add("operation");
//        }
//        if (filter.getState() != null) {
//            filterAttributes.add("state");
//        }
//        if (filter.getDeploymentDate() != null) {
//            filterAttributes.add("deploymentDate");
//        }
//        if (filter.getDeleteDate() != null) {
//            filterAttributes.add("deleteDate");
//        }
//        if (filter.getFrom() != null) {
//            filterAttributes.add("from");
//        }
//        if (filter.getTo() != null) {
//            filterAttributes.add("to");
//        }
//        return filterAttributes;
//    }
//
    public static DeployFilter createExampleDeployFilter () {
        DeployFilter filter = new DeployFilter();
        
        DeployablesValidFilter toStore = new DeployablesValidFilter();
        filter.setStore(toStore);

        DeployDeleteFilter delete = new DeployDeleteFilter();
        filter.setDelete(delete);

        filter.getControllerIds().add("js7-cluster");
        filter.getControllerIds().add("standalone");
        
        Config workflow10DraftConfig = new Config();
        Configuration workflow10draft = new Configuration();
        workflow10draft.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow10DraftConfig.setConfiguration(workflow10draft);

        Config workflow16DraftConfig = new Config();
        Configuration workflow16draft = new Configuration();
        workflow16draft.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow16DraftConfig.setConfiguration(workflow16draft);
        
        toStore.getDraftConfigurations().add(workflow10DraftConfig);
        toStore.getDraftConfigurations().add(workflow16DraftConfig);
        
        Config workflow12deployConfig = new Config();
        Configuration workflow12deployed = new Configuration();
        workflow12deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow12deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow12deployConfig.setConfiguration(workflow12deployed);
        toStore.getDeployConfigurations().add(workflow12deployConfig);
        
        Config toDeleteConfig = new Config();
        Configuration toDelete = new Configuration();
        toDelete.setPath("/myWorkflows/forkJoinWorkflows/workflow_88");
        toDelete.setObjectType(ConfigurationType.WORKFLOW);
        toDeleteConfig.setConfiguration(toDelete);
        delete.getDeployConfigurations().add(toDeleteConfig);
        
        return filter;
    }

    public static ExportFilter createExampleExportFilter (boolean forSigning) {
        ExportFilter filter = new ExportFilter();
        
        ExportFile exportFile = new ExportFile();
        exportFile.setFilename("test_export.zip");
        exportFile.setFormat(ArchiveFormat.ZIP);
        filter.setExportFile(exportFile);

        DeployablesFilter deployablesFilter = new DeployablesFilter();
        DeployablesValidFilter deployablesValidFilter = new DeployablesValidFilter();
        Config workflow10DraftConfig = new Config();
        Configuration workflow10draft = new Configuration();
        workflow10draft.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow10DraftConfig.setConfiguration(workflow10draft);
        deployablesFilter.getDraftConfigurations().add(workflow10DraftConfig);
        deployablesValidFilter.getDraftConfigurations().add(workflow10DraftConfig);
        Config workflow16DraftConfig = new Config();
        Configuration workflow16draft = new Configuration();
        workflow16draft.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16draft.setObjectType(ConfigurationType.WORKFLOW);
        workflow16DraftConfig.setConfiguration(workflow16draft);
        deployablesFilter.getDraftConfigurations().add(workflow16DraftConfig);
        deployablesValidFilter.getDraftConfigurations().add(workflow16DraftConfig);
        Config workflow12deployConfig = new Config();
        Configuration workflow12deployed = new Configuration();
        workflow12deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow12deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow12deployConfig.setConfiguration(workflow12deployed);
        deployablesFilter.getDeployConfigurations().add(workflow12deployConfig);
        deployablesValidFilter.getDeployConfigurations().add(workflow12deployConfig);
        
        ReleasablesFilter releasablesFilter = new ReleasablesFilter();
        Config scheduleCfg = new Config();
        Configuration schedule = new Configuration();
        schedule.setPath("/mySchedules/newSchedules/mySchedule");
        schedule.setObjectType(ConfigurationType.SCHEDULE);
        scheduleCfg.setConfiguration(schedule);
        releasablesFilter.getDraftConfigurations().add(scheduleCfg);
        Config calendarCfg = new Config();
        Configuration calendar = new Configuration();
        calendar.setPath("/myCalendars/newCalendars/myCalendar");
        calendar.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
        calendarCfg.setConfiguration(calendar);
        releasablesFilter.getReleasedConfigurations().add(calendarCfg);

        if (forSigning) {
            ExportForSigning exportSigning = new ExportForSigning();
            filter.setForSigning(exportSigning);
            exportSigning.setControllerId("testsuite");
            exportSigning.setDeployables(deployablesValidFilter);
        } else {
            ExportShallowCopy exportShallowCopy = new ExportShallowCopy();
            filter.setShallowCopy(exportShallowCopy);
            exportShallowCopy.setDeployables(deployablesFilter);
            exportShallowCopy.setReleasables(releasablesFilter);
        }
        return filter;
    }

    public static SetVersionFilter createExampleSetVersionFilter () {
        SetVersionFilter filter = new SetVersionFilter();

        Config workflow10deployConfig = new Config();
        Configuration workflow10deployed = new Configuration();
        workflow10deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow10deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow10deployConfig.setConfiguration(workflow10deployed);
        filter.getDeployConfigurations().add(workflow10deployConfig);
        
        Config workflow12deployConfig = new Config();
        Configuration workflow12deployed = new Configuration();
        workflow12deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow12deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow12deployConfig.setConfiguration(workflow12deployed);
        filter.getDeployConfigurations().add(workflow12deployConfig);

        Config workflow14deployConfig = new Config();
        Configuration workflow14deployed = new Configuration();
        workflow14deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_14");
        workflow14deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow14deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow14deployConfig.setConfiguration(workflow14deployed);
        filter.getDeployConfigurations().add(workflow14deployConfig);

        Config workflow16deployConfig = new Config();
        Configuration workflow16deployed = new Configuration();
        workflow16deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow16deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow16deployConfig.setConfiguration(workflow16deployed);
        filter.getDeployConfigurations().add(workflow16deployConfig);

        Config workflow18deployConfig = new Config();
        Configuration workflow18deployed = new Configuration();
        workflow18deployed.setPath("/myWorkflows/ifElseWorkflow/workflow_18");
        workflow18deployed.setObjectType(ConfigurationType.WORKFLOW);
        workflow18deployed.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        workflow18deployConfig.setConfiguration(workflow18deployed);
        filter.getDeployConfigurations().add(workflow18deployConfig);
        filter.setVersion("myTestVersion-1.0.0");
        return filter;
    }
    
    public static SetVersionsFilter createExampleSetVersionsFilter () {
        SetVersionsFilter filter = new SetVersionsFilter();

        DeploymentVersion dvWf10 = new DeploymentVersion();
        Configuration workflow10 = new Configuration();
        workflow10.setPath("/myWorkflows/ifElseWorkflow/workflow_10");
        workflow10.setObjectType(ConfigurationType.WORKFLOW);
        workflow10.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        dvWf10.setConfiguration(workflow10);
        dvWf10.setVersion("myTestVersion-1.0.0");
        filter.getDeployConfigurations().add(dvWf10);
        
        DeploymentVersion dvWf12 = new DeploymentVersion();
        Configuration workflow12 = new Configuration();
        workflow12.setPath("/myWorkflows/ifElseWorkflow/workflow_12");
        workflow12.setObjectType(ConfigurationType.WORKFLOW);
        workflow12.setCommitId("4273b6c6-c354-4fcd-afdb-2758088abe4a");
        dvWf12.setConfiguration(workflow12);
        dvWf12.setVersion("myTestVersion-1.0.0");
        filter.getDeployConfigurations().add(dvWf12);
        
        DeploymentVersion dvWf14 = new DeploymentVersion();
        Configuration workflow14 = new Configuration();
        workflow14.setPath("/myWorkflows/ifElseWorkflow/workflow_14");
        workflow14.setObjectType(ConfigurationType.WORKFLOW);
        workflow14.setCommitId("2a6487a7-091c-446a-b799-67c87b4db6c2");
        dvWf14.setConfiguration(workflow14);
        dvWf14.setVersion("myTestVersion-1.1.0");
        filter.getDeployConfigurations().add(dvWf14);
        
        DeploymentVersion dvWf16 = new DeploymentVersion();
        Configuration workflow16 = new Configuration();
        workflow16.setPath("/myWorkflows/ifElseWorkflow/workflow_16");
        workflow16.setObjectType(ConfigurationType.WORKFLOW);
        workflow16.setCommitId("2a6487a7-091c-446a-b799-67c87b4db6c2");
        dvWf16.setConfiguration(workflow16);
        dvWf16.setVersion("myTestVersion-1.1.0");
        filter.getDeployConfigurations().add(dvWf16);
        
        DeploymentVersion dvWf18 = new DeploymentVersion();
        Configuration workflow18 = new Configuration();
        workflow18.setPath("/myWorkflows/ifElseWorkflow/workflow_18");
        workflow18.setObjectType(ConfigurationType.WORKFLOW);
        workflow18.setCommitId("17cd67d9-37cb-4d28-8596-c88eab6a755d");
        dvWf18.setConfiguration(workflow18);
        dvWf18.setVersion("myTestVersion-1.1.12");
        filter.getDeployConfigurations().add(dvWf18);
        
        return filter;
    }

    public static PathFilter createExamplePathFilter () {
        PathFilter filter = new PathFilter();
        filter.setName("test_wf");
        filter.setObjectType(ConfigurationType.WORKFLOW.toString());
        filter.setUseDrafts(true);
        return filter;
    }

    public static PathResponse createExamplePathResponse () {
        PathResponse response = new PathResponse();
        response.setPath("/_2021_01_18/test_wf");
        response.setDeliveryDate(Date.from(Instant.now()));
        return response;
    }

    public static GenerateCaFilter createGenerateCaFilter () {
        GenerateCaFilter filter = new GenerateCaFilter();
        filter.setDn("CN=SOS root CA, OU=devel, O=SOS, C=DE");
        return filter;
    }

    public static SetRootCaFilter createSetRootCaFilter () {
        SetRootCaFilter filter = new SetRootCaFilter();
        filter.setPrivateKey("-----BEGIN EC PRIVATE KEY-----\nMIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgTDpNzcltg9AKs48Q\n"
                + "SIM79EsKYr75u/4FUFqisAPn3NCgCgYIKoZIzj0DAQehRANCAASXodrfRFak9V8E\ndbvjjtGZbXd9JgEN8ua6PAubf7i4HPLyXMzhE2lpq3sqD68RBgSx2hx548zNfsMT\n"
                + "PDvGPMiL\n-----END EC PRIVATE KEY-----");
        filter.setCertificate("-----BEGIN CERTIFICATE-----\nMIIBqTCCAVCgAwIBAgIGAXquQGFrMAoGCCqGSM49BAMEMFMxFDASBgNVBAMMC1NP\n"
                + "UyByb290IENBMRswGQYDVQQLDBJ3d3cuc29zLWJlcmxpbi5jb20xETAPBgNVBAoM\nCFNPUyBHbWJIMQswCQYDVQQGEwJERTAeFw0yMTA3MTYwNzM4MTJaFw0yNjA3MTYw\n"
                + "NzM4MTJaMFMxFDASBgNVBAMMC1NPUyByb290IENBMRswGQYDVQQLDBJ3d3cuc29z\nLWJlcmxpbi5jb20xETAPBgNVBAoMCFNPUyBHbWJIMQswCQYDVQQGEwJERTBZMBMG\n"
                + "ByqGSM49AgEGCCqGSM49AwEHA0IABJeh2t9EVqT1XwR1u+OO0Zltd30mAQ3y5ro8\nC5t/uLgc8vJczOETaWmreyoPrxEGBLHaHHnjzM1+wxM8O8Y8yIujEDAOMAwGA1Ud\n"
                + "EwQFMAMBAf8wCgYIKoZIzj0EAwQDRwAwRAIgLMwzASeb5rj658bVLYEd7EGa0r5+\ndMdOLvCIbx7GiyYCIBRRTGRIzEZYeJMohVnVFeE9qQg7ZoJrzduqc+AEGZ1f\n"
                + "-----END CERTIFICATE-----");
        return filter;
    }

    public static CopyToFilter createRepositoryCopyToFilterFolderExample () throws JsonProcessingException {
        CopyToFilter filter = new CopyToFilter();
        filter.setControllerId("testsuite");
        filter.setControllerId(null);
        filter.setAuditLog(null);

        Config folder = new Config();
        Configuration cfgFolder = new Configuration();
        cfgFolder.setObjectType(ConfigurationType.FOLDER);
        cfgFolder.setPath("/ProductDemo");
        cfgFolder.setRecursive(true);
        folder.setConfiguration(cfgFolder);
        
        Configurations rollout = new Configurations();
        rollout.getDraftConfigurations().add(folder);
        rollout.getDeployConfigurations().add(folder);
        rollout.getReleasedConfigurations().add(folder);
        
        Configurations local = new Configurations();
        local.getDraftConfigurations().add(folder);
        local.getDeployConfigurations().add(folder);
        local.getReleasedConfigurations().add(folder);
        
        filter.setRollout(rollout);
        filter.setLocal(local);
        return filter;
    }

    public static CopyToFilter createRepositoryCopyToFilterFilesExample () throws JsonProcessingException {
        
        Configurations rollout = new Configurations();
        Configurations local = new Configurations();

        CopyToFilter filter = new CopyToFilter();
        filter.setControllerId("testsuite");
        filter.setAuditLog(null);
        filter.setRollout(rollout);
        filter.setLocal(local);

        Config file = new Config();
        Configuration cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKFLOW);
        cfg.setPath("/ProductDemo/AdmissionTimes/pdAdmissionTimeJob2");
        cfg.setCommitId("0a4b2077-6912-4a98-b5f1-799af9c1d90a");
        file.setConfiguration(cfg);
        rollout.getDeployConfigurations().add(file);

        file = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKFLOW);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicSerialWorkflow");
        file.setConfiguration(cfg);
        rollout.getDraftConfigurations().add(file);
        
        file = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.SCHEDULE);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicSerialWorkflow");
        file.setConfiguration(cfg);
        local.getDraftConfigurations().add(file);

        local.setDeployConfigurations(null);

        file = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicAllDays");
        file.setConfiguration(cfg);
        local.getReleasedConfigurations().add(file);
        
        return filter;
    }

    public static DeleteFromFilter createRepositoryDeleteFromFilterFolderExample () throws JsonProcessingException {
        DeleteFromFilter filter = new DeleteFromFilter();
        filter.setAuditLog(null);
        filter.setCategory(Category.ROLLOUT);
        Config config = new Config();
        Configuration cfg = new Configuration();
        cfg.setObjectType(ConfigurationType.FOLDER);
        cfg.setPath("/ProductDemo");
        cfg.setRecursive(true);
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);
        
        return filter;
    }

    public static DeleteFromFilter createRepositoryDeleteFromFilterFilesRolloutExample () throws JsonProcessingException {
        DeleteFromFilter filter = new DeleteFromFilter();
        filter.setCategory(Category.ROLLOUT);
        filter.setAuditLog(null);
        Config config = new Config();
        Configuration cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKFLOW);
        cfg.setPath("/ProductDemo/AdmissionTimes/pdAdmissionTimeJob2");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        config = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKFLOW);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicSerialWorkflow");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        return filter;
    }

    public static DeleteFromFilter createRepositoryDeleteFromFilterFilesLocalExample () throws JsonProcessingException {
        DeleteFromFilter filter = new DeleteFromFilter();
        filter.setCategory(Category.LOCAL);
        filter.setAuditLog(null);
        Config config = new Config();
        Configuration cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.SCHEDULE);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicSerialWorkflow");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        config = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicAllDays");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        return filter;
    }

    public static ReadFromFilter createRepositoryReadFromRolloutFilter (boolean recursive) throws JsonProcessingException {
        ReadFromFilter filter = new ReadFromFilter();
        filter.setFolder("/ProductDemo");
        filter.setRecursive(recursive);
        filter.setCategory(Category.ROLLOUT);
        return filter;
    }
    
    public static ReadFromFilter createRepositoryReadFromLocalFilter (boolean recursive) throws JsonProcessingException {
        ReadFromFilter filter = new ReadFromFilter();
        filter.setFolder("/ProductDemo");
        filter.setRecursive(recursive);
        filter.setCategory(Category.LOCAL);
        return filter;
    }
    
    public static ResponseFolder createResponseFolder(Class<?> clazz, boolean recursive) throws Exception {
        Path repositories = Paths.get(clazz.getResource("/joc/repositories/rollout").toURI());
        Path repository = Paths.get(clazz.getResource("/joc/repositories/rollout/ProductDemo").toURI());
        TreeSet<java.nio.file.Path> repoTree = RepositoryUtil.readRepositoryAsTreeSet(repository);
        Set<ResponseFolderItem> responseFolderItems = repoTree.stream().filter(path -> Files.isRegularFile(path))
                .map(path -> RepositoryUtil.getResponseFolderItem(repositories, path)).collect(Collectors.toSet());
        final Map<String, Set<ResponseFolderItem>> groupedFolderItems = responseFolderItems.stream().collect(Collectors.groupingBy(
                ResponseFolderItem::getFolder, Collectors.toSet()));
        SortedSet<ResponseFolder> responseFolder = RepositoryUtil.initTreeByFolder(repository, recursive).stream().map(t -> {
                    ResponseFolder r = new ResponseFolder();
                    String path = Globals.normalizePath(RepositoryUtil.subPath(repositories, Paths.get(t.getPath())).toString());
                    r.setPath(path);
                    if (groupedFolderItems.containsKey(path)) {
                        r.getItems().addAll(groupedFolderItems.get(path));
                    }
                    return r;
                }).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ResponseFolder::getPath).reversed())));
        return RepositoryUtil.getTree(
                responseFolder, RepositoryUtil.subPath(repositories, repository), RepositoryUtil.subPath(repositories, repositories), recursive);
    }

    public static UpdateFromFilter createRepositoryUpdateFromFilterFolderExample () throws JsonProcessingException {
        UpdateFromFilter filter = new UpdateFromFilter();
        filter.setAuditLog(null);
        filter.setCategory(Category.ROLLOUT);
        Config config = new Config();
        Configuration cfg = new Configuration();
        cfg.setObjectType(ConfigurationType.FOLDER);
        cfg.setPath("/ProductDemo");
        cfg.setRecursive(true);        
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);
        return filter;
    }

    public static UpdateFromFilter createRepositoryUpdateFromFilterFilesRolloutExample () throws JsonProcessingException {
        UpdateFromFilter filter = new UpdateFromFilter();
        filter.setAuditLog(null);
        filter.setCategory(Category.ROLLOUT);
        Config config = new Config();
        Configuration cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKFLOW);
        cfg.setPath("/ProductDemo/AdmissionTimes/pdAdmissionTimeJob2");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        config = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKFLOW);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicSerialWorkflow");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        return filter;
    }

    public static UpdateFromFilter createRepositoryUpdateFromFilterFilesLocalExample () throws JsonProcessingException {
        UpdateFromFilter filter = new UpdateFromFilter();
        filter.setAuditLog(null);
        filter.setCategory(Category.LOCAL);
        Config config = new Config();
        Configuration cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.SCHEDULE);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicSerialWorkflow");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        config = new Config();
        cfg = new Configuration();
        cfg.setRecursive(null);
        cfg.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
        cfg.setPath("/ProductDemo/CyclicExecution/pdCyclicAllDays");
        config.setConfiguration(cfg);
        filter.getConfigurations().add(config);

        return filter;
    }
    
    public static GitCredentials createExampleGitCredentialsPassword() {
        GitCredentials cred = new GitCredentials();
        cred.setGitAccount("myExampleGitAccount");
        cred.setPassword("myExamplePasswd");
        cred.setGitServer("my-example-remote.git-host.net");
        return cred;
    }

    public static GitCredentials createExampleGitCredentialsAccessToken() {
        GitCredentials cred = new GitCredentials();
        cred.setGitAccount("myExampleGitAccount");
        cred.setPersonalAccessToken("ExaM29pl4evLx9ebhWzo");
        cred.setGitServer("my-example-remote.git-host.net");
        return cred;
    }

    public static GitCredentials createExampleGitCredentialsKeyfilePath() {
        GitCredentials cred = new GitCredentials();
        cred.setGitAccount("myExampleGitAccount");
        cred.setKeyfilePath("/example/path/to/keyfile");
        cred.setGitServer("my-example-remote.git-host.net");
        return cred;
    }

    public static GetCredentialsFilter createExampleGetGitCredentialsFilter() {
        GetCredentialsFilter getCredFilter = new GetCredentialsFilter();
        return getCredFilter;
    }

    public static AddCredentialsFilter createExampleAddGitCredentialsFilter() {
        AddCredentialsFilter addCredFilter = new AddCredentialsFilter();
        List<GitCredentials> credentials = new ArrayList<GitCredentials>();
        credentials.add(createExampleGitCredentialsKeyfilePath());
        addCredFilter.setCredentials(credentials);
        return addCredFilter;
    }

    public static RemoveCredentialsFilter createExampleRemoveGitCredentialsFilter() {
        RemoveCredentialsFilter removeCredFilter = new RemoveCredentialsFilter();
        List<RemoveCredentials> toRemove = new ArrayList<RemoveCredentials>();
        RemoveCredentials cred = new RemoveCredentials();
        cred.setGitAccount("myExampleGitAccount");
        cred.setGitServer("my-example-remote.git-host.net");
        toRemove.add(cred);
        removeCredFilter.setCredentials(toRemove);
        return removeCredFilter;
    }
    
    public static CommonFilter createExampleAddAllFilter () {
        CommonFilter addAllFilter = new CommonFilter();
        addAllFilter.setFolder("/JSDemo");
        addAllFilter.setCategory(Category.ROLLOUT);
        return addAllFilter;
    }

    public static CheckoutFilter createExampleCheckoutBranchFilter () {
        CheckoutFilter checkoutFilter = new CheckoutFilter();
        checkoutFilter.setBranch("master");
        checkoutFilter.setFolder("/JSDemo");
        checkoutFilter.setCategory(Category.ROLLOUT);
        return checkoutFilter;
    }

    public static CheckoutFilter createExampleCheckoutTagFilter () {
        CheckoutFilter checkoutFilter = new CheckoutFilter();
        checkoutFilter.setTag("v1.0.0");
        checkoutFilter.setFolder("/JSDemo");
        checkoutFilter.setCategory(Category.ROLLOUT);
        return checkoutFilter;
    }

    public static CloneFilter createExampleCloneFilter () {
        CloneFilter cloneFilter = new CloneFilter();
        cloneFilter.setRemoteUri("git@github.com:sos-berlin/JS7Demo.git");
        cloneFilter.setFolder("/JSDemo");
        cloneFilter.setCategory(Category.ROLLOUT);
        return cloneFilter;
    }

    public static CommitFilter createExampleCommitFilter () {
        CommitFilter commitFilter = new CommitFilter();
        commitFilter.setFolder("/JSDemo");
        commitFilter.setCategory(Category.ROLLOUT);
        commitFilter.setMessage("This is a commit message.");
        return commitFilter;
    }

    public static CommonFilter createExampleLogFilter () {
        CommonFilter logFilter = new CommonFilter();
        logFilter.setFolder("/JSDemo");
        logFilter.setCategory(Category.ROLLOUT);
        return logFilter;
    }

    public static CommonFilter createExamplePullFilter () {
        CommonFilter pullFilter = new CommonFilter();
        pullFilter.setFolder("/JSDemo");
        pullFilter.setCategory(Category.ROLLOUT);
        return pullFilter;
    }

    public static CommonFilter createExamplePushFilter () {
        CommonFilter pushFilter = new CommonFilter();
        pushFilter.setFolder("/JSDemo");
        pushFilter.setCategory(Category.ROLLOUT);
        return pushFilter;
    }

    public static CommonFilter createExampleResetAllFilter () {
        CommonFilter resetFilter = new CommonFilter();
        resetFilter.setFolder("/JSDemo");
        resetFilter.setCategory(Category.ROLLOUT);
        
        return resetFilter;
    }

    public static CommonFilter createExampleRestoreAllFilter () {
        CommonFilter restoreFilter = new CommonFilter();
        restoreFilter.setFolder("/JSDemo");
        restoreFilter.setCategory(Category.ROLLOUT);
        return restoreFilter;
    }

    public static CommonFilter createExampleStatusFilter () {
        CommonFilter statusFilter = new CommonFilter();
        statusFilter.setFolder("/JSDemo");
        statusFilter.setCategory(Category.ROLLOUT);
        return statusFilter;
    }

    public static TagFilter createExampleTagFilter () {
        TagFilter tagFilter = new TagFilter();
        tagFilter.setFolder("/JSDemo");
        tagFilter.setCommitHash("e58abc37caee531d6f791e0b976a4baf39185186");
        tagFilter.setName("v1.0.0");
        tagFilter.setCategory(Category.ROLLOUT);
        return tagFilter;
    }

}
