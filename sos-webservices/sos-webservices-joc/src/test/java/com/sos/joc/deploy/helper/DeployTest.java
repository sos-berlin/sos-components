package com.sos.joc.deploy.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.problem.Problem;
import js7.base.web.Uri;
import js7.data.agent.AgentId;
import js7.data.item.VersionId;
import js7.data.lock.LockId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JSimpleItem;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.lock.JLock;
import js7.proxy.javaapi.JControllerApi;
import reactor.core.publisher.Flux;

public class DeployTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployTest.class);

    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:5444";
    private static final Uri AGENT_URI = Uri.of("http://localhost:4445");

    private static final String SIGNATURE_TYPE_PGP = "PGP";
    private static final Path PRIVATE_KEY_PGP = Paths.get("src/test/resources/sos.private-pgp-key.asc");
    private static final Path PUBLIC_KEY_PGP = Paths.get("src/test/resources/sos.public-pgp-key.asc");

    private static final Path WORKFLOW_WITH_FORK = Paths.get("src/test/resources/deploy/helper/workflow_fork.workflow.json");
    private static final Path WORKFLOW_WITH_LOCK = Paths.get("src/test/resources/deploy/helper/workflow_lock.workflow.json");
    private static final Path WORKFLOW_WITH_JAVA_JOB = Paths.get("src/test/resources/deploy/helper/workflow_java.workflow.json");
    private static final Path WORKFLOW_WITH_ORDER_PARAMETERS = Paths.get("src/test/resources/deploy/helper/workflow_order_parameters.workflow.json");

    @Ignore
    @Test
    public void testDeployAgent() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            addOrChangeAgent(api, "agent", AGENT_URI);
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    @Ignore
    @Test
    public void testDeployLock() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            Lock lock = new Lock();
            lock.setId("my_lock");
            lock.setLimit(1);

            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            addOrChangeSimpleItem(api, JLock.of(LockId.of(lock.getId()), lock.getLimit()));
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    @Ignore
    @Test
    public void testDeployJava() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            deployWorkflow(api, WORKFLOW_WITH_JAVA_JOB, "1");
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    @Ignore
    @Test
    public void testDeployWorkflowWithFork() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            deployWorkflow(api, WORKFLOW_WITH_FORK, "1");
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    @Ignore
    @Test
    public void testDeployWorkflowWithLock() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            deployWorkflow(api, WORKFLOW_WITH_LOCK, "1");
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    @Ignore
    @Test
    public void testDeployWorkflowWithOrderParameters() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            deployWorkflow(api, WORKFLOW_WITH_ORDER_PARAMETERS, "3");
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    private void deployWorkflow(JControllerApi api, Path workflow, String versionId) throws Exception {
        // 1 - redefine Version (e.g. when already deployed)
        Workflow w = Globals.objectMapper.readValue(Files.readAllBytes(workflow), Workflow.class);
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
        addOrChangeSignedItem(api, workflowOriginal, workflowSigned, SIGNATURE_TYPE_PGP, versionId);
    }

    private void addOrChangeAgent(JControllerApi api, String agentId, Uri agentUri) throws InterruptedException, ExecutionException {
        Either<Problem, Void> answer = api.updateItems(Flux.fromIterable(Collections.singleton(JAgentRef.of(AgentId.of(agentId), agentUri))).map(
                JUpdateItemOperation::addOrChange)).get();
        LOGGER.info("[addOrChangeAgent][" + agentId + "]" + SOSString.toString(answer));
    }

    private void addOrChangeSignedItem(JControllerApi api, String contentOriginal, String contentSigned, String signatureType, String versionId)
            throws InterruptedException, ExecutionException {
        Set<JUpdateItemOperation> operations = new HashSet<JUpdateItemOperation>();
        JUpdateItemOperation operation = JUpdateItemOperation.addOrChange(SignedString.of(contentOriginal, signatureType, contentSigned));
        operations.add(operation);

        Either<Problem, Void> answer = api.updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(versionId))), Flux
                .fromIterable(operations))).get();
        LOGGER.info("[addOrChangeSignedItem][" + CONTROLLER_URI_PRIMARY + "]" + SOSString.toString(answer));
    }

    private void addOrChangeSimpleItem(JControllerApi api, JSimpleItem item) throws InterruptedException, ExecutionException {
        Set<JUpdateItemOperation> operations = new HashSet<JUpdateItemOperation>();
        JUpdateItemOperation operation = JUpdateItemOperation.addOrChangeSimple(item);
        operations.add(operation);

        Either<Problem, Void> answer = api.updateItems(Flux.concat(Flux.fromIterable(operations))).get();
        LOGGER.info("[addOrChangeSimpleItem][" + CONTROLLER_URI_PRIMARY + "]" + SOSString.toString(answer));
    }
}
