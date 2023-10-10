package com.sos.joc.deploy.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.problem.Problem;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.item.VersionId;
import js7.data.lock.LockPath;
import js7.data.order.OrderId;
import js7.data.subagent.SubagentId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUnsignedSimpleItem;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.lock.JLock;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerApi;
import reactor.core.publisher.Flux;

public class DeployTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployTest.class);

    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:5444";
    private static final Uri AGENT_URI = Uri.of("http://localhost:4445");

    private static final String SIGNATURE_TYPE_PGP = "PGP";
    private static final Path PRIVATE_KEY_PGP = Paths.get("src/test/resources/sos.private-pgp-key.asc");
    private static final Path PUBLIC_KEY_PGP = Paths.get("src/test/resources/sos.public-pgp-key.asc");

    private static final Path WORKFLOW_WITH_FAIL = Paths.get("src/test/resources/deploy/helper/workflow_fail.workflow.json");
    private static final Path WORKFLOW_WITH_FORK = Paths.get("src/test/resources/deploy/helper/workflow_fork.workflow.json");
    private static final Path WORKFLOW_WITH_LOCK = Paths.get("src/test/resources/deploy/helper/workflow_lock.workflow.json");
    private static final Path WORKFLOW_WITH_JAVA_JOB = Paths.get("src/test/resources/deploy/helper/workflow_java.workflow.json");
    private static final Path WORKFLOW_WITH_ARGUMENTS = Paths.get("src/test/resources/deploy/helper/workflow_arguments.workflow.json");

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
            lock.setPath("my_lock");
            lock.setLimit(1);

            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            addOrChangeSimpleItem(api, JLock.of(LockPath.of(lock.getPath()), lock.getLimit()));
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
    public void testDeployWorkflowWithFail() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            deployWorkflow(api, WORKFLOW_WITH_FAIL, "1");
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
    public void testDeployWorkflowWithArguments() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            deployWorkflowWithoutSerialisation(api, WORKFLOW_WITH_ARGUMENTS, "ee25c611-7852-40ca-b376-a4f7f228a688");
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    @Ignore
    @Test
    public void testAddOrders() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();
        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);
            int i = 0;
            for (i = 0; i < 1; i++) {
                if (i % 10 == 0) {
                    TimeUnit.SECONDS.sleep(1);
                }
                addOrder(api, i);
            }
            long sleep = 5;
            if (i > 100) {
                if (i <= 1_000) {
                    sleep = 30;
                } else {
                    sleep = 60;
                }
            }
            TimeUnit.SECONDS.sleep(sleep);
            LOGGER.info("stop");
        } catch (Throwable e) {
            throw e;
        } finally {
            proxy.close();
        }
    }

    private void addOrder(JControllerApi api, int counter) {
        try {
            String orderId = "test-" + SOSDate.format(new Date(), "yyyyMMddHHmmss") + "_" + counter;
            JFreshOrder order = JFreshOrder.of(OrderId.of(orderId), WorkflowPath.of("shell"), Optional.empty(), Collections.emptyMap(), true);
            api.addOrder(order).thenAccept(either -> {
                if (either.isRight()) {
                    LOGGER.info(String.format("[created]%s", orderId));
                } else {
                    LOGGER.error(String.format("[failed][%s]%s", orderId, SOSString.toString(either.getLeft())));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deployWorkflowWithoutSerialisation(JControllerApi api, Path workflow, String versionId) throws Exception {
        // 1 - redefine Version (e.g. when already deployed)
        String workflowOriginal = new String(Files.readAllBytes(workflow));
        workflowOriginal = workflowOriginal.replaceAll("\"versionId\": \"to_replace\",", "\"versionId\": \"" + versionId + "\",");

        deployWorkflow(api, workflowOriginal, versionId);
    }

    private void deployWorkflow(JControllerApi api, Path workflow, String versionId) throws Exception {
        // 1 - redefine Version (e.g. when already deployed)
        Workflow w = Globals.objectMapper.readValue(Files.readAllBytes(workflow), Workflow.class);
        w.setVersionId(versionId);

        String workflowOriginal = Globals.objectMapper.writeValueAsString(w);
        deployWorkflow(api, workflowOriginal, versionId);
    }

    private void deployWorkflow(JControllerApi api, String workflowOriginal, String versionId) throws Exception {
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
        SubagentId subAgentId = SubagentId.of(agentId);
        AgentPath agentPath = AgentPath.of(agentId);
        Either<Problem, Void> answer = api.updateItems(Flux.fromIterable(Arrays.asList(JUpdateItemOperation.addOrChangeSimple(JAgentRef.of(agentPath,
                subAgentId)), JUpdateItemOperation.addOrChangeSimple(JSubagentItem.of(subAgentId, agentPath, agentUri, false))))).get();
        LOGGER.info("[addOrChangeAgent][" + agentId + "]" + SOSString.toString(answer));
    }

    private void addOrChangeSignedItem(JControllerApi api, String contentOriginal, String contentSigned, String signatureType, String versionId)
            throws InterruptedException, ExecutionException {
        Set<JUpdateItemOperation> operations = new HashSet<JUpdateItemOperation>();
        JUpdateItemOperation operation = JUpdateItemOperation.addOrChangeSigned(SignedString.of(contentOriginal, signatureType, contentSigned));
        operations.add(operation);

        Either<Problem, Void> answer = api.updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(versionId))), Flux
                .fromIterable(operations))).get();
        LOGGER.info("[addOrChangeSignedItem][" + CONTROLLER_URI_PRIMARY + "]" + SOSString.toString(answer));
    }

    private void addOrChangeSimpleItem(JControllerApi api, JUnsignedSimpleItem item) throws InterruptedException, ExecutionException {
        Set<JUpdateItemOperation> operations = new HashSet<JUpdateItemOperation>();
        JUpdateItemOperation operation = JUpdateItemOperation.addOrChangeSimple(item);
        operations.add(operation);

        Either<Problem, Void> answer = api.updateItems(Flux.concat(Flux.fromIterable(operations))).get();
        LOGGER.info("[addOrChangeSimpleItem][" + CONTROLLER_URI_PRIMARY + "]" + SOSString.toString(answer));
    }
}
