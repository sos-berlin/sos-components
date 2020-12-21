package com.sos.joc.deploy.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.ProxyUser;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.problem.Problem;
import js7.base.web.Uri;
import js7.data.agent.AgentId;
import js7.data.agent.AgentRef;
import js7.data.item.VersionId;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.data.agent.JAgentRef;
import js7.proxy.javaapi.data.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

public class DeployTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployTest.class);

    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:4444";
    private static final Uri AGENT_URI = Uri.of("http://localhost:4445");

    private static final String SIGNATURE_TYPE_PGP = "PGP";
    private static final Path PRIVATE_KEY_PGP = Paths.get("src/test/resources/sos.private-pgp-key.asc");
    private static final Path PUBLIC_KEY_PGP = Paths.get("src/test/resources/sos.public-pgp-key.asc");

    private static final Path WORKFLOW = Paths.get("src/test/resources/deploy/helper/workflow_fork.workflow.json");

    private static JProxyTestClass proxy = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER.info("---------- [@BeforeClass] ----------");
        proxy = new JProxyTestClass();
        LOGGER.info("---------- [@BeforeClass] ----------");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOGGER.info("---------- [@AfterClass] ----------");
        if (proxy != null) {
            proxy.close();
        }
        LOGGER.info("---------- [@AfterClass] ----------");
    }

    @Ignore
    @Test
    public void testDeployWorkflowWithPGP() throws Exception {
        String versionId = "1";

        // 1 - redefine Version (e.g. when already deployed)
        Workflow w = Globals.objectMapper.readValue(Files.readAllBytes(WORKFLOW), Workflow.class);
        w.setVersionId(versionId);

        String workflowOriginal = Globals.objectMapper.writeValueAsString(w);
        LOGGER.info(String.format("[before sign]%s", workflowOriginal));

        // 2- sign workflow
        String workflowSigned = SignObject.signPGP(new String(Files.readAllBytes(PRIVATE_KEY_PGP)), workflowOriginal, null);
        LOGGER.info(String.format("[after sign]%s", workflowSigned));

        // 2.1 - not necessary: verify signed workflow against public key before controller
        LOGGER.info("[VerifySignature]" + VerifySignature.verifyPGP(new String(Files.readAllBytes(PUBLIC_KEY_PGP)), workflowOriginal,
                workflowSigned));

        // 3 - deploy
        JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
        addOrChangeAgent(api, "agent");
        addOrChangeItem(api, workflowOriginal, workflowSigned, SIGNATURE_TYPE_PGP, versionId);

    }

    private void addOrChangeAgent(JControllerApi api, String agentId) throws InterruptedException, ExecutionException {
        JAgentRef agent = JAgentRef.apply(AgentRef.apply(AgentId.of(agentId), AGENT_URI));
        List<JAgentRef> agents = Arrays.asList(agent);

        Either<Problem, Void> answer = api.updateItems(Flux.fromIterable(agents).map(JUpdateItemOperation::addOrChange)).get();
        LOGGER.info("[addOrChangeAgent][" + agentId + "]" + SOSString.toString(answer));

    }

    private void addOrChangeItem(JControllerApi api, String contentOriginal, String contentSigned, String signatureType, String versionId)
            throws InterruptedException, ExecutionException {
        Set<JUpdateItemOperation> items = new HashSet<JUpdateItemOperation>();
        JUpdateItemOperation item = JUpdateItemOperation.addOrChange(SignedString.of(contentOriginal, signatureType, contentSigned));
        items.add(item);

        Either<Problem, Void> answer = api.updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(versionId))), Flux
                .fromIterable(items))).get();
        LOGGER.info("[addOrChangeItem][" + CONTROLLER_URI_PRIMARY + "]" + SOSString.toString(answer));
    }

}
