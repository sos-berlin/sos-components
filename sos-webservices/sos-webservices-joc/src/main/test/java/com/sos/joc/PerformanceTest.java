package com.sos.joc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.exception.SOSException;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.job.ExecutableScript;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.jobscheduler.model.workflow.Jobs;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.classes.JOCJsonCommand;

public class PerformanceTest {

    public static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.INDENT_OUTPUT, true);
    private String agentRefPath = "/test3/agent";
    private String agentUri = "http://OH:41420";
    private String workflowPathBase = "/test3/workflow";
    private Integer numOfJobs = 1;
    private Integer numOfWorkflows = 100000;
    private Integer sleep = 5;
    private String deployPath = "C:/Program Files/sos-berlin.com/jobscheduler/scheduler.2.0.1.oh/var/config/live";
    private String jobSchedulerUrl = "http://localhost:40421";

    
    @Ignore
    @Test
    public void testCreateOrders() throws IOException, URISyntaxException, SOSException, InterruptedException {
        
        Instant now = Instant.now();
        //now = now.plusSeconds(60*60);
        Long scheduledFor = null;//now.getEpochSecond() * 1000;
        
        JOCJsonCommand command = new JOCJsonCommand(new URI(jobSchedulerUrl), null);
        command.setAutoCloseHttpClient(false);
        command.setUriBuilderForOrders();
        command.setSocketTimeout(30000);
        command.addHeader("Content-Type", "application/json");
        command.addHeader("Accept", "application/json");
        
        for (int i = 0; i < numOfWorkflows; i++) {
            String workflowPath = String.format("%s%06d", workflowPathBase, i+1);
            String orderId = String.format("ORDER-ID%06d", i+1);
            FreshOrder order = new FreshOrder(orderId, workflowPath, scheduledFor, null);
            String jsonStringOfOrder = objectMapper.writeValueAsString(order);
            System.out.println(jsonStringOfOrder);
            try {
                String response = command.postRestService(command.getURI(), jsonStringOfOrder);
                int httpReplyCode = command.statusCode();
                String status = String.format("%d %s: %s", httpReplyCode, command.getHttpResponse().getStatusLine().getReasonPhrase(), response);
                if (httpReplyCode != 201) {
                    System.err.println(status);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            Thread.sleep(100);
        }
        
        command.closeHttpClient();
        System.out.println("all orders are scheduled for: " + now);
    }
    
    @Ignore
    @Test
    public void testCreateWorkflows() throws IOException, URISyntaxException, SOSException, InterruptedException {
        
        Path livePath = Paths.get(deployPath);
        
        StringBuilder jobScript = new StringBuilder();
        jobScript.append("@echo off").append("\r\n");
        jobScript.append("echo %DATE% %TIME% - %SCHEDULER_PARAM_JOBNAME% started in workflow %SCHEDULER_PARAM_WORKFLOW%").append("\r\n");
        jobScript.append("echo %DATE% %TIME% - %SCHEDULER_PARAM_JOBNAME% waiting for ").append(sleep).append("s").append("\r\n");
        //jobScript.append("waitfor SomethingThatNeverHappens /t ").append(sleep).append(" >nul 2>nul").append("\r\n");
        jobScript.append("ping -n ").append((sleep+1)).append(" localhost >nul 2>nul").append("\r\n");
        jobScript.append("echo %DATE% %TIME% - %SCHEDULER_PARAM_JOBNAME% finished").append("\r\n");
        
        AgentRef agentRef = createAgentRef(agentRefPath, agentUri);
        deployAgentRef(agentRef, livePath);
        
        for (int i = 0; i < numOfWorkflows; i++) {
            String workflowPath = String.format("%s%06d", workflowPathBase, i+1);
            Workflow workflow = createWorkflow(workflowPath, agentRefPath, jobScript.toString(), numOfJobs);
            deployWorkflow(workflow, livePath);
//            if ((i+1) % 1000 == 0) {
//                Thread.sleep(60 * 1000);
//            }
        }
    }

    private Workflow createWorkflow(String workflowPath, String agentRefPath, String jobScript, Integer numOfJobs) {
        List<Instruction> instructions = new ArrayList<Instruction>();
        Jobs jobs = new Jobs();
        for (int i = 0; i < numOfJobs; i++) {
            String jobName = String.format("job%02d", i+1);
            instructions.add(createJobInstruction(jobName, workflowPath));
            jobs.getAdditionalProperties().put(jobName, createTestJob(agentRefPath, jobScript));
        }
        return new Workflow(workflowPath, null, instructions, jobs);
    }
    
    private void deployWorkflow(Workflow workflow, Path deployPath) throws IOException, InterruptedException {
        Path p = deployPath.resolve(workflow.getPath().substring(1) + ".workflow.json");
        if (!Files.exists(p.getParent())) {
            Files.createDirectories(p.getParent());
        }
        System.out.println(objectMapper.writeValueAsString(workflow));
        Files.write(p, objectMapper.writeValueAsBytes(workflow));
        Thread.sleep(100);
    }

    private NamedJob createJobInstruction(String jobName, String workflowPath) {
        Variables params = new Variables();
        params.getAdditionalProperties().put("workflow", workflowPath);
        params.getAdditionalProperties().put("jobName", jobName);
        return new NamedJob(jobName, null, params);
    }

    private Job createTestJob(String agentRefPath, String jobScript) {
        ExecutableScript executable = new ExecutableScript();
        executable.setScript(jobScript);
        Job job = new Job();
        job.setAgentRefPath(agentRefPath);
        job.setExecutable(executable);
        job.setTaskLimit(1); //required
        return job;
    }

    private AgentRef createAgentRef(String agentRefPath, String agentUri) {
        return new AgentRef(agentRefPath, null, agentUri);
    }

    private void deployAgentRef(AgentRef agentRef, Path deployPath) throws IOException, InterruptedException {
        Path p = deployPath.resolve(agentRef.getPath().substring(1) + ".agentref.json");
        if (!Files.exists(p.getParent())) {
            Files.createDirectories(p.getParent());
        }
        System.out.println(objectMapper.writeValueAsString(agentRef));
        Files.write(p, objectMapper.writeValueAsBytes(agentRef));
        Thread.sleep(25);
    }

}
