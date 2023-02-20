package com.sos.joc.deploy.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.descriptor.DeploymentDescriptor;
import com.sos.inventory.model.descriptor.Descriptor;
import com.sos.inventory.model.descriptor.License;
import com.sos.inventory.model.descriptor.agent.AgentDescriptor;
import com.sos.inventory.model.descriptor.agent.AgentsDescriptor;
import com.sos.inventory.model.descriptor.agent.ControllerRefDescriptor;
import com.sos.inventory.model.descriptor.common.Authentication;
import com.sos.inventory.model.descriptor.common.Authentication.Method;
import com.sos.inventory.model.descriptor.common.Certificates;
import com.sos.inventory.model.descriptor.common.Connection;
import com.sos.inventory.model.descriptor.common.Installation;
import com.sos.inventory.model.descriptor.common.Media;
import com.sos.inventory.model.descriptor.common.Target;
import com.sos.inventory.model.descriptor.controller.ControllerClusterDescriptor;
import com.sos.inventory.model.descriptor.controller.ControllerDescriptor;
import com.sos.inventory.model.descriptor.joc.JocDescriptor;
import com.sos.inventory.model.descriptor.joc.JocInstallation;
import com.sos.inventory.model.descriptor.joc.JocInstallation.DbmsInit;
import com.sos.inventory.model.descriptor.joc.JocInstallation.SecurityLevel;
import com.sos.inventory.model.descriptor.joc.JocInstanceDescriptor;
import com.sos.inventory.model.descriptor.joc.Members;
import com.sos.inventory.model.descriptor.joc.StartFiles;
import com.sos.joc.Globals;
import com.sos.joc.model.agent.transfer.AgentExportFilter;
import com.sos.joc.model.agent.transfer.AgentImportFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.path.PathFilter;
import com.sos.joc.model.inventory.path.PathResponse;
import com.sos.joc.model.inventory.release.Releasable;
import com.sos.joc.model.inventory.release.ReleasableRecallFilter;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.joc.model.joc.LicenseType;
import com.sos.joc.model.joc.VersionsFilter;
import com.sos.joc.model.notification.DeleteNotificationFilter;
import com.sos.joc.model.notification.ReadNotificationFilter;
import com.sos.joc.model.notification.StoreNotificationFilter;
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
import com.sos.joc.model.publish.folder.ExportFolderFilter;
import com.sos.joc.model.publish.folder.ExportFolderForSigning;
import com.sos.joc.model.publish.folder.ExportFolderShallowCopy;
import com.sos.joc.model.publish.git.AddCredentialsFilter;
import com.sos.joc.model.publish.git.GitCredentials;
import com.sos.joc.model.publish.git.RemoveCredentialsFilter;
import com.sos.joc.model.publish.git.commands.CheckoutFilter;
import com.sos.joc.model.publish.git.commands.CloneFilter;
import com.sos.joc.model.publish.git.commands.CommitFilter;
import com.sos.joc.model.publish.git.commands.CommonFilter;
import com.sos.joc.model.publish.git.commands.GitCommandResponse;
import com.sos.joc.model.publish.git.commands.TagFilter;
import com.sos.joc.model.publish.repository.Category;
import com.sos.joc.model.publish.repository.Configurations;
import com.sos.joc.model.publish.repository.CopyToFilter;
import com.sos.joc.model.publish.repository.DeleteFromFilter;
import com.sos.joc.model.publish.repository.ReadFromFilter;
import com.sos.joc.model.publish.repository.ResponseFolder;
import com.sos.joc.model.publish.repository.ResponseFolderItem;
import com.sos.joc.model.publish.repository.UpdateFromFilter;
import com.sos.joc.model.settings.StoreSettingsFilter;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.publish.repository.util.RepositoryUtil;
import com.sos.sign.model.instruction.ForkJoin;
import com.sos.sign.model.instruction.IfElse;
import com.sos.sign.model.instruction.Instruction;
import com.sos.sign.model.instruction.Instructions;
import com.sos.sign.model.instruction.NamedJob;
import com.sos.sign.model.instruction.OptionalInstructions;
import com.sos.sign.model.job.ExecutableScript;
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
        filter.setObjectType(ConfigurationType.WORKFLOW);
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
        cred.setUsername("sp");
        cred.setEmail("sp@test.example.com");
        cred.setPassword("myExamplePasswd");
        cred.setGitServer("my-example-remote.git-host.net");
        return cred;
    }

    public static GitCredentials createExampleGitCredentialsAccessToken() {
        GitCredentials cred = new GitCredentials();
        cred.setGitAccount("myExampleGitAccount");
        cred.setUsername("sp");
        cred.setEmail("sp@test.example.com");
        cred.setPersonalAccessToken("ExaM29pl4evLx9ebhWzo");
        cred.setGitServer("my-example-remote.git-host.net");
        return cred;
    }

    public static GitCredentials createExampleGitCredentialsKeyfilePath() {
        GitCredentials cred = new GitCredentials();
        cred.setGitAccount("myExampleGitAccount");
        cred.setUsername("sp");
        cred.setEmail("sp@test.example.com");
        cred.setKeyfilePath("/example/path/to/keyfile");
        cred.setGitServer("my-example-remote.git-host.net");
        return cred;
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
        removeCredFilter.getGitServers().add("github.com");
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
        cloneFilter.setRemoteUrl("git@github.com:sos-berlin/JS7Demo.git");
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

    public static GitCommandResponse createGitCommandResponse () {
        GitCommandResponse response = new GitCommandResponse();
        response.setCommand("git status -s");
        response.setStdOut(" M sp_git_test.txt");
        response.setStdErr(null);
        response.setExitCode(0);
        return response;
    }

    public static Js7LicenseInfo createLicenseInfo() {
        Js7LicenseInfo info = new Js7LicenseInfo();
        info.setValid(true);
        info.setType(LicenseType.COMMERCIAL_VALID);
        Calendar cal = new GregorianCalendar();
        cal.set(2020, 0, 12, 9, 33, 42);
        info.setValidFrom(Date.from(cal.toInstant()));
        cal.set(2023, 0, 12, 9, 32, 41);
        info.setValidUntil(Date.from(cal.toInstant()));
        return info;
    }
    
    public static ExportFolderFilter createExportFolderShallowCopyFilter() {
        ExportFolderFilter filter = new ExportFolderFilter();
        ExportFolderShallowCopy shallowCopy = new ExportFolderShallowCopy();
        ExportFile exportFile = new ExportFile();
        exportFile.setFilename("test_export_folder.zip");
        exportFile.setFormat(ArchiveFormat.ZIP);
        filter.setExportFile(exportFile);
        List<String> folders = new ArrayList<String>();
        folders.add("/JS7Demo");
        folders.add("/Examples.Unix");
        shallowCopy.setFolders(folders);
        shallowCopy.setRecursive(true);
        List<ConfigurationType> types = new ArrayList<ConfigurationType>();
        types.add(ConfigurationType.WORKFLOW);
        types.add(ConfigurationType.FILEORDERSOURCE);
        types.add(ConfigurationType.JOBRESOURCE);
        types.add(ConfigurationType.LOCK);
        types.add(ConfigurationType.WORKINGDAYSCALENDAR);
        types.add(ConfigurationType.SCHEDULE);
        shallowCopy.setObjectTypes(types);
        filter.setShallowCopy(shallowCopy);
        return filter;
    }

    public static ExportFolderFilter createExportFolderForSigningFilter() {
        ExportFolderFilter filter = new ExportFolderFilter();
        ExportFolderForSigning forSigning = new ExportFolderForSigning();
        ExportFile exportFile = new ExportFile();
        exportFile.setFilename("test_export_folder.zip");
        exportFile.setFormat(ArchiveFormat.ZIP);
        filter.setExportFile(exportFile);
        List<String> folders = new ArrayList<String>();
        folders.add("/JS7Demo");
        folders.add("/Examples.Unix");
        forSigning.setFolders(folders);
        forSigning.setRecursive(true);
        List<ConfigurationType> types = new ArrayList<ConfigurationType>();
        types.add(ConfigurationType.WORKFLOW);
        types.add(ConfigurationType.FILEORDERSOURCE);
        types.add(ConfigurationType.JOBRESOURCE);
        types.add(ConfigurationType.LOCK);
        forSigning.setObjectTypes(types);
        forSigning.setControllerId("testsuite");
        filter.setForSigning(forSigning);
        return filter;
    }
    
    public static ReleasableRecallFilter createReleasableRecallFilter() {
        ReleasableRecallFilter filter = new ReleasableRecallFilter();
        Releasable releasable = new Releasable();
        releasable.setName("mySchedule");
        releasable.setObjectType(ConfigurationType.SCHEDULE);
        filter.getReleasables().add(releasable);
        return filter;
    }
    
    public static ReadNotificationFilter createReadNotificationFilter() {
        ReadNotificationFilter filter = new ReadNotificationFilter();
        filter.setForceRelease(false);
        return filter;
    }
    
    public static DeleteNotificationFilter createDeleteNotificationFilter() {
        DeleteNotificationFilter filter = new DeleteNotificationFilter();
        filter.setRelease(false);
        return filter;
    }
    
    public static StoreNotificationFilter createStoreNotificationFilter() {
        StoreNotificationFilter filter = new StoreNotificationFilter();
        filter.setConfiguration("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n"
                + "<Configurations>\r\n"
                + "    <Fragments>\r\n"
                + "        <MessageFragments>\r\n"
                + "            <Message name=\"command_on_failure\"><![CDATA[${MON_OS_HISTORY_ID} Type: ${MON_N_TYPE}, Return Code: ${MON_OS_RETURN_CODE}, Error Code: ${MON_OS_ERROR_CODE}, Error Message: ${MON_OS_ERROR_TEXT}, Warning: ${MON_OS_WARN_TEXT}, Controller ID: ${MON_O_CONTROLLER_ID}, Agent URI: ${MON_OS_AGENT_URI}, Order ID: ${MON_O_ORDER_ID}, Workflow Path: ${MON_O_WORKFLOW_PATH}, Workflow Title: ${MON_O_WORKFLOW_TITLE}, Job Name: ${MON_OS_JOB_NAME}, Job Title: ${MON_OS_JOB_TITLE}, Job Label: ${MON_OS_JOB_LABEL}, Job Criticality: ${MON_OS_JOB_CRITICALITY}, Order History Status: ${MON_O_SEVERITY}, Order Start Time: ${MON_O_START_TIME}, Order End Time: ${MON_O_END_TIME}, Order Step Start Time: ${MON_OS_START_TIME}, Order Step End Time: ${MON_OS_END_TIME}, Order History ID: ${MON_O_HISTORY_ID}, Order Step History ID: ${MON_OS_HISTORY_ID}]]></Message>\r\n"
                + "            <Message name=\"command_on_success\"><![CDATA[${MON_OS_HISTORY_ID} Type: ${MON_N_TYPE}, Return Code: ${MON_OS_RETURN_CODE}, Error Code: ${MON_OS_ERROR_CODE}, Error Message: ${MON_OS_ERROR_TEXT}, Warning: ${MON_OS_WARN_TEXT}, Controller ID: ${MON_O_CONTROLLER_ID}, Agent URI: ${MON_OS_AGENT_URI}, Order ID: ${MON_O_ORDER_ID}, Workflow Path: ${MON_O_WORKFLOW_PATH}, Workflow Title: ${MON_O_WORKFLOW_TITLE}, Job Name: ${MON_OS_JOB_NAME}, Job Title: ${MON_OS_JOB_TITLE}, Job Label: ${MON_OS_JOB_LABEL}, Job Criticality: ${MON_OS_JOB_CRITICALITY}, Order History Status: ${MON_O_SEVERITY}, Order Start Time: ${MON_O_START_TIME}, Order End Time: ${MON_O_END_TIME}, Order Step Start Time: ${MON_OS_START_TIME}, Order Step End Time: ${MON_OS_END_TIME}, Order History ID: ${MON_O_HISTORY_ID}, Order Step History ID: ${MON_OS_HISTORY_ID}]]></Message>\r\n"
                + "            <Message name=\"mail_on_failure\"><![CDATA[<body>\r\n"
                + "    <style type=\"text/css\">.tg  {border-collapse:collapse;border-spacing:0;border-color:#aaa;}.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aaa;color:#333;background-color:#fff;}.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aaa;color:#fff;background-color:#f38630;}</style>\r\n"
                + "    <table class=\"tg\">\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Error/Warning</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Return&nbsp;Code:</td>\r\n"
                + "            <td>${MON_OS_RETURN_CODE}</td>\r\n"
                + "            <td>Message:</td>\r\n"
                + "            <td>${MON_OS_ERROR_TEXT}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Error&nbsp;Code:</td>\r\n"
                + "            <td>${MON_OS_ERROR_CODE}</td>\r\n"
                + "            <td>Warning:</td>\r\n"
                + "            <td>${MON_OS_WARN_TEXT}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Controller</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Controller&nbsp;ID:</td>\r\n"
                + "            <td>${MON_O_CONTROLLER_ID}</td>\r\n"
                + "            <td>Agent&nbsp;URI:</td>\r\n"
                + "            <td>${MON_OS_AGENT_URI}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Order</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Order&nbsp;ID:</td>\r\n"
                + "            <td colspan=\"3\">\r\n"
                + "                <a href=\"${JOC_HREF_ORDER}\">${MON_O_ORDER_ID}</a>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Order&nbsp;Log:</td>\r\n"
                + "            <td colspan=\"3\">\r\n"
                + "                <a href=\"${JOC_HREF_ORDER_LOG}\">${JOC_HREF_ORDER_LOG}</a>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Workflow&nbsp;Path:</td>\r\n"
                + "            <td>\r\n"
                + "                <a href=\"${JOC_HREF_WORKFLOW}\">${MON_O_WORKFLOW_PATH}</a>\r\n"
                + "            </td>\r\n"
                + "            <td>Workflow&nbsp;Title:</td>\r\n"
                + "            <td>${MON_O_WORKFLOW_TITLE}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Job&nbsp;Name:</td>\r\n"
                + "            <td>\r\n"
                + "                <a href=\"${JOC_HREF_JOB}\">${MON_OS_JOB_NAME}</a>\r\n"
                + "            </td>\r\n"
                + "            <td>Job&nbsp;Title:</td>\r\n"
                + "            <td>${MON_OS_JOB_TITLE}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Job&nbsp;Label:</td>\r\n"
                + "            <td>${MON_OS_JOB_LABEL}</td>\r\n"
                + "            <td>Job&nbsp;Criticality:</td>\r\n"
                + "            <td>${MON_OS_JOB_CRITICALITY}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Job&nbsp;Log:</td>\r\n"
                + "            <td colspan=\"3\">\r\n"
                + "                <a href=\"${JOC_HREF_JOB_LOG}\">${JOC_HREF_JOB_LOG}</a>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Order&nbsp;History</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Status:</td>\r\n"
                + "            <td>${MON_O_SEVERITY}</td>\r\n"
                + "            <td>Time&nbsp;elapsed:</td>\r\n"
                + "            <td>${MON_O_TIME_ELAPSED}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Start&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_O_START_TIME}</td>\r\n"
                + "            <td>End&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_O_END_TIME}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Order&nbsp;Step&nbsp;History</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Status:</td>\r\n"
                + "            <td>${MON_OS_SEVERITY}</td>\r\n"
                + "            <td>Time&nbsp;elapsed:</td>\r\n"
                + "            <td>${MON_OS_TIME_ELAPSED}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Start&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_OS_START_TIME}</td>\r\n"
                + "            <td>End&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_OS_END_TIME}</td>\r\n"
                + "        </tr>\r\n"
                + "    </table>\r\n"
                + "</body>]]></Message>\r\n"
                + "            <Message name=\"mail_on_success\"><![CDATA[<body>\r\n"
                + "    <style type=\"text/css\">.tg  {border-collapse:collapse;border-spacing:0;border-color:#bbb;}.tg td {font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#bbb;color:#594F4F;background-color:#E0FFEB;}.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#bbb;color:#493F3F;background-color:#9DE0AD}</style>\r\n"
                + "    <table class=\"tg\">\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Controller</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Controller&nbsp;ID:</td>\r\n"
                + "            <td>${MON_O_CONTROLLER_ID}</td>\r\n"
                + "            <td>Agent&nbsp;URI:</td>\r\n"
                + "            <td>${MON_OS_AGENT_URI}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Order</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Order&nbsp;ID:</td>\r\n"
                + "            <td colspan=\"3\">\r\n"
                + "                <a href=\"${JOC_HREF_ORDER}\">${MON_O_ORDER_ID}</a>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Order&nbsp;Log:</td>\r\n"
                + "            <td colspan=\"3\">\r\n"
                + "                <a href=\"${JOC_HREF_ORDER_LOG}\">${JOC_HREF_ORDER_LOG}</a>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Workflow&nbsp;Path:</td>\r\n"
                + "            <td>\r\n"
                + "                <a href=\"${JOC_HREF_WORKFLOW}\">${MON_O_WORKFLOW_PATH}</a>\r\n"
                + "            </td>\r\n"
                + "            <td>Workflow&nbsp;Title:</td>\r\n"
                + "            <td>${MON_O_WORKFLOW_TITLE}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Job&nbsp;Name:</td>\r\n"
                + "            <td>\r\n"
                + "                <a href=\"${JOC_HREF_JOB}\">${MON_OS_JOB_NAME}</a>\r\n"
                + "            </td>\r\n"
                + "            <td>Job&nbsp;Title:</td>\r\n"
                + "            <td>${MON_OS_JOB_TITLE}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Job&nbsp;Label:</td>\r\n"
                + "            <td>${MON_OS_JOB_LABEL}</td>\r\n"
                + "            <td>Job&nbsp;Criticality:</td>\r\n"
                + "            <td>${MON_OS_JOB_CRITICALITY}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Job&nbsp;Log:</td>\r\n"
                + "            <td colspan=\"3\">\r\n"
                + "                <a href=\"${JOC_HREF_JOB_LOG}\">${JOC_HREF_JOB_LOG}</a>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Order&nbsp;History</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Status:</td>\r\n"
                + "            <td>${MON_O_SEVERITY}</td>\r\n"
                + "            <td>Time&nbsp;elapsed:</td>\r\n"
                + "            <td>${MON_O_TIME_ELAPSED}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Start&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_O_START_TIME}</td>\r\n"
                + "            <td>End&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_O_END_TIME}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <th colspan=\"4\">Order&nbsp;Step&nbsp;History</th>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Status:</td>\r\n"
                + "            <td>${MON_OS_SEVERITY}</td>\r\n"
                + "            <td>Time&nbsp;elapsed:</td>\r\n"
                + "            <td>${MON_OS_TIME_ELAPSED}</td>\r\n"
                + "        </tr>\r\n"
                + "        <tr>\r\n"
                + "            <td>Start&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_OS_START_TIME}</td>\r\n"
                + "            <td>End&nbsp;Time&nbsp;UTC:</td>\r\n"
                + "            <td>${MON_OS_END_TIME}</td>\r\n"
                + "        </tr>\r\n"
                + "    </table>\r\n"
                + "</body>]]></Message>\r\n"
                + "        </MessageFragments>\r\n"
                + "        <MonitorFragments>\r\n"
                + "            <CommandFragment name=\"command_on_failure\">\r\n"
                + "                <MessageRef ref=\"command_on_failure\"/>\r\n"
                + "                <Command><![CDATA[echo \"${MESSAGE}\" >> /tmp/notification.log]]></Command>\r\n"
                + "            </CommandFragment>\r\n"
                + "            <CommandFragment name=\"command_on_success\">\r\n"
                + "                <MessageRef ref=\"command_on_success\"/>\r\n"
                + "                <Command><![CDATA[echo \"${MESSAGE}\" >> /tmp/notification.log]]></Command>\r\n"
                + "            </CommandFragment>\r\n"
                + "            <MailFragment charset=\"ISO-8859-1\" content_type=\"text/html\" encoding=\"7bit\" job_resources=\"eMailDefault\" name=\"mail_on_failure\" priority=\"Normal\">\r\n"
                + "                <MessageRef ref=\"mail_on_failure\"/>\r\n"
                + "                <Subject><![CDATA[JS7 JobScheduler Notification: ${MON_N_TYPE} - Order ID: ${MON_O_ORDER_ID} - Workflow: ${MON_O_WORKFLOW_PATH}]]></Subject>\r\n"
                + "                <To><![CDATA[info@example.com]]></To>\r\n"
                + "            </MailFragment>\r\n"
                + "            <MailFragment charset=\"ISO-8859-1\" content_type=\"text/html\" encoding=\"7bit\" job_resources=\"eMailDefault\" name=\"mail_on_success\" priority=\"Normal\">\r\n"
                + "                <MessageRef ref=\"mail_on_success\"/>\r\n"
                + "                <Subject><![CDATA[JS7 JobScheduler Notification: ${MON_N_TYPE} - Order ID: ${MON_O_ORDER_ID} - Workflow: ${MON_O_WORKFLOW_PATH}]]></Subject>\r\n"
                + "                <To><![CDATA[info@example.com]]></To>\r\n"
                + "            </MailFragment>\r\n"
                + "        </MonitorFragments>\r\n"
                + "        <ObjectFragments>\r\n"
                + "            <Workflows name=\"any\">\r\n"
                + "                <Workflow>\r\n"
                + "                    <WorkflowJob criticality=\"ALL\"/>\r\n"
                + "                </Workflow>\r\n"
                + "            </Workflows>\r\n"
                + "            <Workflows name=\"none\">\r\n"
                + "                <Workflow path=\"unknown\"/>\r\n"
                + "            </Workflows>\r\n"
                + "        </ObjectFragments>\r\n"
                + "    </Fragments>\r\n"
                + "    <Notifications>\r\n"
                + "        <Notification name=\"notify_on_failure\" type=\"ERROR WARNING\">\r\n"
                + "            <NotificationMonitors>\r\n"
                + "                <CommandFragmentRef ref=\"command_on_failure\">\r\n"
                + "                    <MessageRef ref=\"command_on_failure\"/>\r\n"
                + "                </CommandFragmentRef>\r\n"
                + "                <MailFragmentRef ref=\"mail_on_failure\"/>\r\n"
                + "            </NotificationMonitors>\r\n"
                + "            <NotificationObjects>\r\n"
                + "                <WorkflowsRef ref=\"none\"/>\r\n"
                + "            </NotificationObjects>\r\n"
                + "        </Notification>\r\n"
                + "        <Notification name=\"notify_on_success\" type=\"SUCCESS\">\r\n"
                + "            <NotificationMonitors>\r\n"
                + "                <CommandFragmentRef ref=\"command_on_success\">\r\n"
                + "                    <MessageRef ref=\"command_on_success\"/>\r\n"
                + "                </CommandFragmentRef>\r\n"
                + "                <MailFragmentRef ref=\"mail_on_success\"/>\r\n"
                + "            </NotificationMonitors>\r\n"
                + "            <NotificationObjects>\r\n"
                + "                <WorkflowsRef ref=\"none\"/>\r\n"
                + "            </NotificationObjects>\r\n"
                + "        </Notification>\r\n"
                + "        <Notification name=\"notify_on_failure_gui\" type=\"ERROR WARNING\">\r\n"
                + "            <NotificationMonitors/>\r\n"
                + "            <NotificationObjects>\r\n"
                + "                <WorkflowsRef ref=\"any\"/>\r\n"
                + "            </NotificationObjects>\r\n"
                + "        </Notification>\r\n"
                + "    </Notifications>\r\n"
                + "</Configurations>");
        return filter;
    }
    
    public static StoreSettingsFilter createStoreSettingsFilter () {
        StoreSettingsFilter filter = new StoreSettingsFilter();
        filter.setConfigurationItem("{\"status\":[\"info\",\"debug\",\"error\",\"warn\"],\"isEnable\":false}");
        return filter;
    }
    
    public static AgentExportFilter createAgentExportFilter() {
        AgentExportFilter filter = new AgentExportFilter();
        ExportFile file = new ExportFile();
        file.setFilename("myAgentExport.zip");
        file.setFormat(ArchiveFormat.ZIP);
        filter.setExportFile(file);
        List<String> agentIds = Arrays.asList("agent_cluster_001", "agent_001", "agent_002", "agent_003", "agent_006"); 
        filter.setAgentIds(agentIds);
        return filter;
    }
    
    public static AgentImportFilter createAgentImportFilter() {
        AgentImportFilter filter = new AgentImportFilter();
        filter.setControllerId("testsuite");
        filter.setOverwrite(false);
        filter.setFormat(ArchiveFormat.ZIP);
        return filter;
    }
    
    public static VersionsFilter createVersionsFilter() {
        VersionsFilter filter = new VersionsFilter();
        filter.setAgentIds(Arrays.asList("primaryAgentID","standaloneAgentID","agent_cluster_001"));
        filter.setControllerIds(Arrays.asList("controller", "standalone", "testsuite"));
        return filter;
    }
    
    public static DeploymentDescriptor createDeploymentDescriptorSchemaExample () {
        DeploymentDescriptor depDescriptor = new DeploymentDescriptor();

        Descriptor descr = new Descriptor();
        descr.setAccount("sp");
        descr.setDescriptorId("example_04");
        descr.setTitle("Example of the current schema of a deployment descriptor");
        Instant now = Instant.now();
        descr.setCreated(Date.from(now));
        Instant scheduledFor = now.plusSeconds(TimeUnit.DAYS.toSeconds(14));
        descr.setScheduled(Date.from(scheduledFor));
        depDescriptor.setDescriptor(descr);
        
        License license = new License();
        license.setLicenseBinFile("licenses/js7-license.jar");
        license.setLicenseKeyFile("licenses/sos.pem");
        depDescriptor.setLicense(license);
        
        depDescriptor.setControllers(createControllersDescriptor());
        depDescriptor.setAgents(createAgentsDescriptor());
        depDescriptor.setJoc(createJocsDescriptor());
        
        return depDescriptor;
    }
    
    private static List<ControllerClusterDescriptor>createControllersDescriptor() {
        List<ControllerClusterDescriptor> controllers = new ArrayList<ControllerClusterDescriptor>();
        ControllerClusterDescriptor cc1Descriptor = new ControllerClusterDescriptor();
        
        cc1Descriptor.setJocRef("cluster");
        cc1Descriptor.setControllerId("testsuite");
        
        ControllerDescriptor primaryControllerDesc = new ControllerDescriptor();
        
        com.sos.inventory.model.descriptor.controller.Configuration cfgPrimary = new com.sos.inventory.model.descriptor.controller.Configuration();
        Certificates primaryCertificates = new Certificates();
        primaryCertificates.setKeyStore("controllers/instances/cluster.primary/config/private/https-keystore.p12");
        primaryCertificates.setKeyStorePassword("jobscheduler");
        primaryCertificates.setKeyPassword("jobscheduler");
        primaryCertificates.setKeyAlias("centostest-primary");
        primaryCertificates.setTrustStore("controllers/instances/cluster.primary/config/private/https-truststore.p12");
        primaryCertificates.setTrustStorePassword("jobscheduler");
        cfgPrimary.setCertificates(primaryCertificates);
        cfgPrimary.setControllerCert("controllers/instances/cluster.primary/config/private/centostest-primary.crt");
        
        List<String> primaryTemplates = new ArrayList<String>();
        primaryTemplates.add("controllers/templates/https.primary/config");
        cfgPrimary.setTemplates(primaryTemplates);
        
        primaryControllerDesc.setConfiguration(cfgPrimary);

        Installation installationPrimary = new Installation();
        installationPrimary.setData("/var/sos-berlin.com/js7/controller-primary");
        installationPrimary.setDataOwner("sos2:sos2");
        installationPrimary.setHome("/opt/sos-berlin.com/js7/controller-primary");
        installationPrimary.setHomeOwner("sos1:sos1");
        installationPrimary.setHttpPort("localhost:21444");
        installationPrimary.setHttpsPort("centostest-primary.sos:21443");
        installationPrimary.setJavaHome("/opt/jdk-11.0.2");
        installationPrimary.setJavaOptions("-Xmx256m -Djava.security.egd=file:///dev/urandom");
        installationPrimary.setRunUser("sos2");
        primaryControllerDesc.setInstallation(installationPrimary);

        Media primaryMedia = new Media();
        primaryMedia.setRelease("2.5.1");
        primaryMedia.setTarball("2.5.1/js7_controller_unix.2.5.1.tar.gz");
        primaryControllerDesc.setMedia(primaryMedia);


        Target primaryTarget = new Target();

        Authentication primaryAuth = new Authentication();
        primaryAuth.setKeyFile("/home/sos/.ssh/sos_rsa");
        primaryAuth.setMethod(Method.PUBLICKEY);
        primaryAuth.setUser("sos");
        primaryTarget.setAuthentication(primaryAuth);
        
        Connection primaryConnection = new Connection();
        primaryConnection.setHost("centostest-primary");
        primaryConnection.setPort(22);
        primaryTarget.setConnection(primaryConnection);
        
        primaryTarget.setExecPost("StartService");
        primaryTarget.setExecPre("StopService");
        primaryTarget.setMakeService(true);
        primaryTarget.setPackageLocation("/tmp");
        primaryControllerDesc.setTarget(primaryTarget);

        cc1Descriptor.setPrimary(primaryControllerDesc);

        ControllerDescriptor secondaryControllerDesc = new ControllerDescriptor();

        com.sos.inventory.model.descriptor.controller.Configuration cfgSecondary = new com.sos.inventory.model.descriptor.controller.Configuration();
        Certificates secondaryCertificates = new Certificates();
        secondaryCertificates.setKeyStore("controllers/instances/cluster.secondary/config/private/https-keystore.p12");
        secondaryCertificates.setKeyStorePassword("jobscheduler");
        secondaryCertificates.setKeyPassword("jobscheduler");
        secondaryCertificates.setKeyAlias("centostest-secondary");
        secondaryCertificates.setTrustStore("controllers/instances/cluster.secondary/config/private/https-truststore.p12");
        secondaryCertificates.setTrustStorePassword("jobscheduler");
        cfgSecondary.setCertificates(secondaryCertificates);
        cfgSecondary.setControllerCert("controllers/instances/cluster.secondary/config/private/centostest-secondary.crt");

        List<String> secondaryTemplates = new ArrayList<String>();
        secondaryTemplates.add("controllers/templates/https.secondary/config");
        cfgSecondary.setTemplates(secondaryTemplates);
        
        secondaryControllerDesc.setConfiguration(cfgSecondary);

        Installation installationSecondary = new Installation();
        installationSecondary.setData("/var/sos-berlin.com/js7/controller-secondary");
        installationSecondary.setDataOwner("sos2:sos2");
        installationSecondary.setHome("/opt/sos-berlin.com/js7/controller-secondary");
        installationSecondary.setHomeOwner("sos1:sos1");
        installationSecondary.setHttpPort("localhost:22444");
        installationSecondary.setHttpsPort("centostest-secondary.sos:22443");
        installationSecondary.setJavaHome("/opt/jdk-11.0.2");
        installationSecondary.setJavaOptions("-Xmx256m -Djava.security.egd=file:///dev/urandom");
        installationSecondary.setRunUser("sos2");
        secondaryControllerDesc.setInstallation(installationSecondary);
        
        Media secondaryMedia = new Media();
        secondaryMedia.setRelease("2.5.1");
        secondaryMedia.setTarball("2.5.1/js7_controller_unix.2.5.1.tar.gz");
        secondaryControllerDesc.setMedia(secondaryMedia);
        
        Target secondaryTarget = new Target();
        Authentication secondaryAuth = new Authentication();
        secondaryAuth.setKeyFile("/home/sos/.ssh/sos_rsa");
        secondaryAuth.setMethod(Method.PUBLICKEY);
        secondaryAuth.setUser("sos");
        secondaryTarget.setAuthentication(secondaryAuth);
        
        Connection secondaryConnection = new Connection();
        secondaryConnection.setHost("centostest-secondary");
        secondaryConnection.setPort(22);
        secondaryTarget.setConnection(secondaryConnection);
        
        secondaryTarget.setExecPost("StartService");
        secondaryTarget.setExecPre("StopService");
        secondaryTarget.setMakeService(true);
        secondaryTarget.setPackageLocation("/tmp");
        secondaryControllerDesc.setTarget(secondaryTarget);
        
        cc1Descriptor.setSecondary(secondaryControllerDesc);
        controllers.add(cc1Descriptor);
        return controllers;
    }
    
    private static AgentsDescriptor createAgentsDescriptor() {
        AgentsDescriptor agentsDescriptor = new AgentsDescriptor();
        if(agentsDescriptor.getControllerRefs() == null) {
            agentsDescriptor.setControllerRefs(new ArrayList<ControllerRefDescriptor>());
        }
        ControllerRefDescriptor ref1Descriptor = new ControllerRefDescriptor();
        ref1Descriptor.setControllerId("testsuite");
        if(ref1Descriptor.getMembers() == null) {
            ref1Descriptor.setMembers(new ArrayList<AgentDescriptor>());
        }
        
        AgentDescriptor agent1Desc = new AgentDescriptor();
        agent1Desc.setAgentId("agent_001");
        
        com.sos.inventory.model.descriptor.agent.Configuration cfgAgent1 = new com.sos.inventory.model.descriptor.agent.Configuration();

        Certificates agent1Certs = new Certificates();
        agent1Certs.setKeyStore("agents/instances/agent_001/config/private/https-keystore.p12");
        agent1Certs.setKeyStorePassword("jobscheduler");
        agent1Certs.setKeyPassword("jobscheduler");
        agent1Certs.setKeyAlias("centostest-primary");
        agent1Certs.setTrustStore("agents/instances/agent_001/config/private/https-truststore.p12");
        agent1Certs.setTrustStorePassword("jobscheduler");
        cfgAgent1.setCertificates(agent1Certs);
        
        List<String> agent1Templates = new ArrayList<String>();
        agent1Templates.add("agents/templates/https/config");
        cfgAgent1.setTemplates(agent1Templates);
        
        agent1Desc.setConfiguration(cfgAgent1);
        
        Installation agent1Install = new Installation();
        agent1Install.setData("/var/sos-berlin.com/js7/agent-primary");
        agent1Install.setDataOwner("sos2:sos2");
        agent1Install.setHome("/opt/sos-berlin.com/js7/agent-primary");
        agent1Install.setHomeOwner("sos1:sos1");
        agent1Install.setHttpPort("localhost:31445");
        agent1Install.setHttpsPort("centostest-primary.sos:31443");
        agent1Install.setJavaHome("/opt/jdk-11.0.2");
        agent1Install.setJavaOptions("-Xmx125m -Djava.security.egd=file:///dev/urandom");
        agent1Install.setRunUser("sos2");
        agent1Desc.setInstallation(agent1Install);
        
        Media agent1Media = new Media();
        agent1Media.setRelease("2.5.1");
        agent1Media.setTarball("2.5.1/js7_agent_unix.2.5.1.tar.gz");
        agent1Desc.setMedia(agent1Media);
        
        Target agent1Target = new Target();
        Authentication agent1Auth = new Authentication();
        agent1Auth.setKeyFile("/home/sos/.ssh/sos_rsa");
        agent1Auth.setMethod(Authentication.Method.PUBLICKEY);
        agent1Auth.setUser("sos");
        agent1Target.setAuthentication(agent1Auth);
        
        Connection agent1Connection = new Connection();
        agent1Connection.setHost("centostest-primary");
        agent1Connection.setPort(22);
        agent1Target.setConnection(agent1Connection);
        
        agent1Target.setExecPost("StartService");
        agent1Target.setExecPre("StopService");
        agent1Target.setMakeService(true);
        agent1Target.setPackageLocation("/tmp");
        agent1Desc.setTarget(agent1Target);

        ref1Descriptor.getMembers().add(agent1Desc);
        
        AgentDescriptor agent2Desc = new AgentDescriptor();
        agent2Desc.setAgentId("agent_002");
        
        com.sos.inventory.model.descriptor.agent.Configuration cfgAgent2 = new com.sos.inventory.model.descriptor.agent.Configuration();
        
        Certificates agent2Certs = new Certificates();
        agent2Certs.setKeyStore("agents/instances/agent_002/config/private/https-keystore.p12");
        agent2Certs.setKeyStorePassword("jobscheduler");
        agent2Certs.setKeyPassword("jobscheduler");
        agent2Certs.setKeyAlias("centostest-primary");
        agent2Certs.setTrustStore("agents/instances/agent_002/config/private/https-truststore.p12");
        agent2Certs.setTrustStorePassword("jobscheduler");
        cfgAgent2.setCertificates(agent2Certs);
        
        List<String> agent2Templates = new ArrayList<String>();
        agent2Templates.add("agents/templates/https/config");
        cfgAgent2.setTemplates(agent2Templates);
        
        agent2Desc.setConfiguration(cfgAgent2);
        
        Installation agent2Install = new Installation();
        agent2Install.setData("/var/sos-berlin.com/js7/agent-secondary");
        agent2Install.setDataOwner("sos2:sos2");
        agent2Install.setHome("/opt/sos-berlin.com/js7/agent-secondary");
        agent2Install.setHomeOwner("sos1:sos1");
        agent2Install.setHttpPort("localhost:32445");
        agent2Install.setHttpsPort("centostest-secondary.sos:32443");
        agent2Install.setJavaHome("/opt/jdk-11.0.2");
        agent2Install.setJavaOptions("-Xmx256m -Djava.security.egd=file:///dev/urandom");
        agent2Install.setRunUser("sos2");
        agent2Desc.setInstallation(agent2Install);
        
        Media agent2Media = new Media();
        agent2Media.setRelease("2.5.1");
        agent2Media.setTarball("2.5.1/js7_agent_unix.2.5.1.tar.gz");
        agent2Desc.setMedia(agent2Media);
        
        Target agent2Target = new Target();
        Authentication agent2Auth = new Authentication();
        agent2Auth.setKeyFile("/home/sos/.ssh/sos_rsa");
        agent2Auth.setMethod(Authentication.Method.PUBLICKEY);
        agent2Auth.setUser("sos");
        agent2Target.setAuthentication(agent2Auth);
        
        Connection agent2Connection = new Connection();
        agent2Connection.setHost("centostest-secondary");
        agent2Connection.setPort(22);
        agent2Target.setConnection(agent2Connection);
        
        agent2Target.setExecPost("StartService");
        agent2Target.setExecPre("StopService");
        agent2Target.setMakeService(true);
        agent2Target.setPackageLocation("/tmp");
        agent2Desc.setTarget(agent2Target);
        
        ref1Descriptor.getMembers().add(agent2Desc);
        agentsDescriptor.getControllerRefs().add(ref1Descriptor);
        return agentsDescriptor;
    }
    
    private static List<JocDescriptor> createJocsDescriptor() {
        List<JocDescriptor> jocs = new ArrayList<JocDescriptor>();
        JocDescriptor jocClusterDescriptor = new JocDescriptor();
        Members jocMembers = new Members();
        
        jocMembers.setClusterId("cluster");
        if(jocMembers.getInstances() == null) {
            jocMembers.setInstances(new ArrayList<JocInstanceDescriptor>());
        }
        jocClusterDescriptor.setMembers(jocMembers);
        
        JocInstanceDescriptor joc1Descr = new JocInstanceDescriptor();
        joc1Descr.setOrdering(1);
        joc1Descr.setInstanceId("" + joc1Descr.getOrdering());
        
        com.sos.inventory.model.descriptor.joc.Configuration joc1Cfg = new com.sos.inventory.model.descriptor.joc.Configuration();
        
        Certificates joc1Certs = new Certificates();
        joc1Certs.setKeyStore("joc/instances/cluster.primary/resources/https-keystore.p12");
        joc1Certs.setKeyStorePassword("jobscheduler");
        joc1Certs.setKeyPassword("jobscheduler");
        joc1Certs.setKeyAlias("centostest-primary");
        joc1Certs.setTrustStore("joc/instances/cluster.primary/resources/https-truststore.p12");
        joc1Certs.setTrustStorePassword("jobscheduler");
        joc1Cfg.setCertificates(joc1Certs);

        joc1Cfg.setJocCert("joc/instances/cluster.primary/resources/centostest-primary.crt");
        
        StartFiles joc1StartFiles = new StartFiles();
        joc1StartFiles.setHttpIni("joc/templates/https/start.d/http.ini");
        joc1StartFiles.setHttpsIni("joc/templates/https/start.d/https.ini");
        joc1StartFiles.setSslIni("joc/templates/https/start.d/ssl.ini");
        joc1Cfg.setStartFiles(joc1StartFiles);
        
        List<String> joc1Templates = new ArrayList<String>();
        joc1Templates.add("joc/templates/https/resources");
        joc1Cfg.setTemplates(joc1Templates);
        
        joc1Cfg.setResponseDir("joc/templates/dbms/h2/response");
        joc1Descr.setConfiguration(joc1Cfg);
        
        JocInstallation joc1Installation = new JocInstallation();
        joc1Installation.setData("/var/sos-berlin.com/js7/joc-primary");
        joc1Installation.setDataOwner("sos2:sos2");
        joc1Installation.setDbmsConfig("joc/templates/dbms/h2/response/hibernate.cfg.xml");
        joc1Installation.setDbmsDriver("joc/templates/dbms/h2/response/h2-1.4.200.jar");
        joc1Installation.setDbmsInit(DbmsInit.BY_JOC);
        joc1Installation.setHome("/opt/sos-berlin.com/js7/joc-primary");
        joc1Installation.setHomeOwner("sos1:sos1");
        joc1Installation.setHttpPort("localhost:11446");
        joc1Installation.setHttpsPort("centostest-primary.sos:11443");
        joc1Installation.setIsPreserveEnv(true);
        joc1Installation.setIsUser(true);
        joc1Installation.setJavaHome("/opt/jdk-11.0.2");
        joc1Installation.setJavaOptions("-Xmx256m -Djava.security.egd=file:///dev/urandom");
        joc1Installation.setRunUser("sos2");
        joc1Installation.setSecurityLevel(SecurityLevel.MEDIUM);
        joc1Installation.setSetupDir("/opt/sos-berlin.com/js7/joc-primary.setup");
        joc1Installation.setTitle("PRIMARY JOC COCKPIT");
        joc1Descr.setInstallation(joc1Installation);
        
        Media joc1Media = new Media();
        joc1Media.setRelease("2.5.1");
        joc1Media.setTarball("2.5.1/js7_joc_linux.2.5.1.tar.gz");
        joc1Descr.setMedia(joc1Media);
        
        Target joc1Target = new Target();
        
        Authentication joc1Authentication = new Authentication();
        joc1Authentication.setKeyFile("/home/sos/.ssh/sos_rsa");
        joc1Authentication.setMethod(Authentication.Method.PUBLICKEY);
        joc1Authentication.setUser("sos");
        joc1Target.setAuthentication(joc1Authentication);
        
        Connection joc1Connection = new Connection();
        joc1Connection.setHost("centostest-primary");
        joc1Connection.setPort(22);
        joc1Target.setConnection(joc1Connection);
        
        joc1Target.setExecPost("StartService");
        joc1Target.setExecPre("StopService");
        joc1Target.setMakeService(true);
        joc1Target.setPackageLocation("/tmp");
        joc1Descr.setTarget(joc1Target);
        
        jocMembers.getInstances().add(joc1Descr);
        
        JocInstanceDescriptor joc2Descr = new JocInstanceDescriptor();
        joc2Descr.setOrdering(2);
        joc2Descr.setInstanceId("" + joc2Descr.getOrdering());
        
        com.sos.inventory.model.descriptor.joc.Configuration joc2Cfg = new com.sos.inventory.model.descriptor.joc.Configuration();
        Certificates joc2Certs = new Certificates();
        joc2Certs.setKeyStore("joc/instances/cluster.secondary/resources/https-keystore.p12");
        joc2Certs.setKeyStorePassword("jobscheduler");
        joc2Certs.setKeyPassword("jobscheduler");
        joc2Certs.setKeyAlias("centostest-secondary");
        joc2Certs.setTrustStore("joc/instances/cluster.secondary/resources/https-truststore.p12");
        joc2Certs.setTrustStorePassword("jobscheduler");
        joc2Cfg.setCertificates(joc2Certs);

        joc2Cfg.setJocCert("joc/instances/cluster.secondary/resources/centostest-secondary.crt");
        
        StartFiles joc2StartFiles = new StartFiles();
        joc2StartFiles.setHttpIni("joc/templates/https/start.d/http.ini");
        joc2StartFiles.setHttpsIni("joc/templates/https/start.d/https.ini");
        joc2StartFiles.setSslIni("joc/templates/https/start.d/ssl.ini");
        joc2Cfg.setStartFiles(joc2StartFiles);
        
        List<String> joc2Templates = new ArrayList<String>();
        joc2Templates.add("joc/templates/https/resources");
        joc2Cfg.setTemplates(joc2Templates);
        
        joc2Cfg.setResponseDir("joc/templates/dbms/h2/response");
        joc2Descr.setConfiguration(joc2Cfg);
        
        JocInstallation joc2Installation = new JocInstallation();
        joc2Installation.setData("/var/sos-berlin.com/js7/joc-secondary");
        joc2Installation.setDataOwner("sos2:sos2");
        joc2Installation.setDbmsConfig("joc/templates/dbms/h2/response/hibernate.cfg.xml");
        joc2Installation.setDbmsDriver("joc/templates/dbms/h2/response/h2-1.4.200.jar");
        joc2Installation.setDbmsInit(DbmsInit.BY_JOC);
        joc2Installation.setHome("/opt/sos-berlin.com/js7/joc-secondary");
        joc2Installation.setHomeOwner("sos1:sos1");
        joc2Installation.setHttpPort("localhost:12446");
        joc2Installation.setHttpsPort("centostest-secondary.sos:12443");
        joc2Installation.setIsPreserveEnv(true);
        joc2Installation.setIsUser(true);
        joc2Installation.setJavaHome("/opt/jdk-11.0.2");
        joc2Installation.setJavaOptions("-Xmx256m -Djava.security.egd=file:///dev/urandom");
        joc2Installation.setRunUser("sos2");
        joc2Installation.setSecurityLevel(SecurityLevel.MEDIUM);
        joc2Installation.setSetupDir("/sos-berlin.com/js7/opt/joc-secondary.setup");
        joc2Installation.setTitle("SECONDARY JOC COCKPIT");
        joc2Descr.setInstallation(joc2Installation);
        
        Media joc2Media = new Media();
        joc2Media.setRelease("2.5.1");
        joc2Media.setTarball("2.5.1/js7_joc_linux.2.5.1.tar.gz");
        joc2Descr.setMedia(joc2Media);
        
        Target joc2Target = new Target();
        
        Authentication joc2Authentication = new Authentication();
        joc2Authentication.setKeyFile("/home/sos/.ssh/sos_rsa");
        joc2Authentication.setMethod(Authentication.Method.PUBLICKEY);
        joc2Authentication.setUser("sos");
        joc2Target.setAuthentication(joc2Authentication);
        
        Connection joc2Connection = new Connection();
        joc2Connection.setHost("centostest-secondary");
        joc2Connection.setPort(22);
        joc2Target.setConnection(joc2Connection);
        
        joc2Target.setExecPost("StartService");
        joc2Target.setExecPre("StopService");
        joc2Target.setMakeService(true);
        joc2Target.setPackageLocation("/tmp");
        joc2Descr.setTarget(joc2Target);
        
        jocMembers.getInstances().add(joc2Descr);
        jocClusterDescriptor.setMembers(jocMembers);
        jocs.add(jocClusterDescriptor);
        return jocs;
    }

}
