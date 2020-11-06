package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.publish.ExcludeConfiguration;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.ReDeployFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.mapper.UpDownloadMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MappingTest {

    private static final String IF_ELSE_JSON =
            "{\"TYPE\":\"Workflow\",\"path\":\"/test/IfElseWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\","
            + "\"instructions\":[{\"TYPE\":\"If\",\"then\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\"},"
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"job2\"}]},\"else\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job3\"},"
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"job4\"}]}}]}";
    private static final String FORK_JOIN_JSON =
            "{\"TYPE\":\"Workflow\",\"path\":\"/test/ForkJoinWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\","
            + "\"instructions\":[{\"TYPE\":\"Fork\",\"branches\":[{\"id\":\"BRANCH1\",\"workflow\":{\"instructions\":["
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch1\"}]}},{\"id\":\"BRANCH2\",\"workflow\":{\"instructions\":["
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch2\"}]}},{\"id\":\"BRANCH3\",\"workflow\":{\"instructions\":["
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch3\"}]}}]},{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobAfterJoin\"}]}";
    private static final String AGENT_REF_JSON =
            "{\"TYPE\":\"AgentRef\",\"path\":\"/test/Agent\",\"versionId\":\"2.0.0-SNAPSHOT\",\"uri\":\"http://localhost:4223\"}";
    private static final Logger LOGGER = LoggerFactory.getLogger(MappingTest.class);

    @Test
    public void test1WorkflowToJsonString() {
        Workflow ifElseWorkflow = DeploymentTestUtils.createIfElseWorkflow();
        Workflow forkJoinWorkflow = DeploymentTestUtils.createForkJoinWorkflow();
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        String workflowJson = null;
        try {
            om.enable(SerializationFeature.INDENT_OUTPUT);
            workflowJson = om.writeValueAsString(ifElseWorkflow);
            assertNotNull(workflowJson);
            LOGGER.info("IfElse Workflow JSON created successfully!");
            LOGGER.trace(workflowJson);
            workflowJson = null;
            workflowJson = om.writeValueAsString(forkJoinWorkflow);
            LOGGER.info("ForkJoin Workflow JSON created successfully!");
            LOGGER.trace(workflowJson);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test2WorkflowToJSObject() {
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        Workflow ifElseWorkflow = null;
        try {
            ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
        } catch (JsonParseException | JsonMappingException e) {
            Assert.fail(e.toString());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        JSObject jsObject = new JSObject();
        jsObject.setContent(ifElseWorkflow);
        Assert.assertEquals("/test/IfElseWorkflow", ((Workflow) jsObject.getContent()).getPath());
        LOGGER.info("IfElse Workflow JSON mapped to java object successfully!");
    }

    @Test
    public void test3AgentRefToJSObject() {
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        AgentRef agent = null;
        try {
            agent = om.readValue(AGENT_REF_JSON, AgentRef.class);
        } catch (JsonParseException | JsonMappingException e) {
            Assert.fail(e.toString());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        JSObject jsObject = new JSObject();
        jsObject.setContent(agent);
        Assert.assertEquals("/test/Agent", ((AgentRef) jsObject.getContent()).getPath());
        LOGGER.info("AgentRef JSON mapped to java object successfully!");
    }

    @Test
    public void test4JsonStringToWorkflow() {
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        try {
            Workflow ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
            Workflow forkJoinWorkflow = om.readValue(FORK_JOIN_JSON, Workflow.class);

            IfElse ifElse = ifElseWorkflow.getInstructions().get(0).cast();
            NamedJob mj = ifElse.getThen().getInstructions().get(0).cast();
            Assert.assertEquals("testJsonStringToWorkflow: firstJobOfThen1", "job1", mj.getJobName());

            String firstJobOfThen = ifElseWorkflow.getInstructions().get(0).cast(IfElse.class).getThen().getInstructions().get(0).cast(NamedJob.class)
                    .getJobName();
            Assert.assertEquals("testJsonStringToWorkflow: firstJobOfThen2", "job1", firstJobOfThen);

            Assert.assertNotNull(ifElseWorkflow);
            Assert.assertNotNull(forkJoinWorkflow);
            LOGGER.info("Workflow JSONs mapped to java object and children checked successfully!");
        } catch (ClassCastException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        } catch (JsonParseException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        } catch (JsonMappingException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    @Test
    public void test5MapDepHistoryFilter () throws JsonProcessingException {
        ShowDepHistoryFilter filter = DeploymentTestUtils.createDefaultShowDepHistoryFilter();
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(filter));
    }

    @Test
    public void test6MapReDeployFilter () throws JsonProcessingException {
        ReDeployFilter filter = DeploymentTestUtils.createDefaultReDeployFilter();
        ExcludeConfiguration exclude = new ExcludeConfiguration();
        exclude.setPath("/myWorkflows/myIfElseWorkflows/workflow_02");
        exclude.setInvConfigurationId(75L);
        filter.getExcludes().add(exclude);
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(filter));
    }
}
