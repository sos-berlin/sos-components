package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.command.UpdateRepo;
import com.sos.jobscheduler.model.deploy.Signature;
import com.sos.jobscheduler.model.deploy.SignatureType;
import com.sos.jobscheduler.model.deploy.SignedObject;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.publish.JSObject;

public class MappingTest {

    private static final String IF_ELSE_JSON =
            "{\"TYPE\":\"Workflow\",\"path\":\"/test/IfElseWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\",\"instructions\":[{\"TYPE\":\"If\",\"then\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"job2\"}],\"else\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job3\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"job4\"}]}]}";
    private static final String FORK_JOIN_JSON =
            "{\"TYPE\":\"Workflow\",\"path\":\"/test/ForkJoinWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\",\"instructions\":[{\"TYPE\":\"Fork\",\"branches\":[{\"id\":\"BRANCH1\",\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch1\"}]},{\"id\":\"BRANCH2\",\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"/test/jobBranch2\"}]},{\"id\":\"BRANCH3\",\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch3\"}]}]},{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobAfterJoin\"}]}";
    private static final String AGENT_REF_JSON =
            "{\"TYPE\":\"AgentRef\",\"path\":\"/test/Agent\",\"versionId\":\"2.0.0-SNAPSHOT\",\"uri\":\"http://localhost:4223\"}";
    private static final Logger LOGGER = LoggerFactory.getLogger(MappingTest.class);

    @Test
    public void test1WorkflowToJsonString() {
        Workflow ifElseWorkflow = DeploymentTestUtils.createIfElseWorkflow();
        Workflow forkJoinWorkflow = DeploymentTestUtils.createForkJoinWorkflow();
        ObjectMapper om = new ObjectMapper();
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
        ObjectMapper om = new ObjectMapper();
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
        ObjectMapper om = new ObjectMapper();
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
        ObjectMapper om = new ObjectMapper();
        try {
            Workflow ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
            Workflow forkJoinWorkflow = om.readValue(FORK_JOIN_JSON, Workflow.class);

            IfElse ifElse = ifElseWorkflow.getInstructions().get(0).cast();
            NamedJob mj = ifElse.getThen().get(0).cast();
            Assert.assertEquals("testJsonStringToWorkflow: firstJobOfThen1", "job1", mj.getJobName());

            String firstJobOfThen = ifElseWorkflow.getInstructions().get(0).cast(IfElse.class).getThen().get(0).cast(NamedJob.class).getJobName();
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
    @Ignore
    public void testUpdateRepo() {
        SignedObject signedObject = new SignedObject();
        String version = "testVersion1";
        signedObject.setString(
                "{\"TYPE\": \"AgentRef\", \"path\": \"/test-agent-2\", \"versionId\": \"" + version + "\", \"uri\":\"http://localhost:41420\"}");
        Signature signature = new Signature();
        signature.setSignatureString("-----BEGIN PGP SIGNATURE-----\r\n" + "\r\n"
                + "iQEzBAEBCAAdFiEEzMIAKBvtuj6rEL9KUGGKu7ZjpJ0FAl5eWN8ACgkQUGGKu7Zj\r\n"
                + "pJ2f4Af9HLFEGbDQeq4SfNwznY1AA0xSalHCSqsZA7K5t85uaG3qkkIDxmr+UDPI\r\n"
                + "4Oa7Bx3gMvrBY/lJrwARwGLkXIdGmIyuXt2VvRU+yTfZK4oI5BSgqkKkkRVEwLL+\r\n"
                + "OrEPDquVRs/smh1df5mzUdx7kRaViLVD4ZqYp+87Qoqd7JbKdf3X7MV4A23efiY/\r\n"
                + "Ko+ZW2nnVuFmplt4E0ZZw7XHa7HHWw2iYyKCcNLNWuwI0AL1ecG5kmQMUXrg1NiZ\r\n"
                + "QRYs8SDPl+qTv3Jop+45Vh9OKuh6qkuanEjQcwqnRzoaXIahZSJg1MlmNrRRR1pa\r\n" + "BJ3nqv0B4WUvaGgZA3GPkYaStS6PpA==\r\n" + "=KGoC\r\n"
                + "-----END PGP SIGNATURE-----");
        signature.setTYPE(SignatureType.PGP);
        signedObject.setSignature(signature);
        UpdateRepo updateRepo = new UpdateRepo();
        updateRepo.setVersionId(version);
        updateRepo.getChange().add(signedObject);
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            LOGGER.info("***************************  Request Body  ***************************");
            LOGGER.info(om.writeValueAsString(updateRepo));
            SOSRestApiClient httpClient = new SOSRestApiClient();
            httpClient.setAllowAllHostnameVerifier(false);
            httpClient.setBasicAuthorization("VGVzdDp0ZXN0");
            httpClient.addHeader("Accept", "application/json");
            httpClient.addHeader("Content-Type", "application/json");
            // httpClient.addHeader("userId", "test");
            String response = httpClient.postRestService(UriBuilder.fromPath("http://localhost:4222/master/api/command").build(), om
                    .writeValueAsString(updateRepo));
            LOGGER.info("*****************************  Response  *****************************");
            LOGGER.info(response);
        } catch (IllegalArgumentException | UriBuilderException | SOSException | IOException e) {
            LOGGER.error(e.toString());
        } finally {
            LOGGER.info("************************** End Update Repo  **************************\n");
        }
    }
    
}
