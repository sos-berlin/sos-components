package com.sos.js7.job.jocapi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.commons.auth.HttpClientAuthConfig;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.js7.job.UnitTestJobHelper;
import com.sos.js7.job.jocapi.helper.TestApiExecutorOIDCJob;
import com.sos.js7.job.jocapi.helper.TestApiExecutorOIDCJobArguments;

import js7.data_for_java.order.JOutcome;

public class ApiExecutorOIDCTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExecutorOIDCTest.class);

    @Ignore
    @Test
    public void testInventoryExportFolder() throws Exception {
        setAgentProperties();

        Map<String, Object> args = new HashMap<>();
        args.put("api_url", "/inventory/export/folder");
        args.put("body", SOSPath.readFile(Path.of("src/test/resources/jocapi/inventory_export_folder.json")));

        UnitTestJobHelper<TestApiExecutorOIDCJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorOIDCJob(null));
        // optional settings
        h.getStepConfig().setControllerId("js7");
        h.getStepConfig().setOrderId("test_order");
        h.getStepConfig().setJobName("test_job");

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testOrderLog() throws Exception {
        setAgentProperties();

        Map<String, Object> args = new HashMap<>();
        args.put("api_url", "/order/log");
        args.put("body", SOSPath.readFile(Path.of("src/test/resources/jocapi/order_log.json")));

        UnitTestJobHelper<TestApiExecutorOIDCJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorOIDCJob(null));
        // optional settings
        h.getStepConfig().setControllerId("js7");
        h.getStepConfig().setOrderId("test_order");
        h.getStepConfig().setJobName("test_job");

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testTaskLog() throws Exception {
        setAgentProperties();

        Map<String, Object> args = new HashMap<>();
        args.put("api_url", "/task/log");
        args.put("body", SOSPath.readFile(Path.of("src/test/resources/jocapi/task_log.json")));

        UnitTestJobHelper<TestApiExecutorOIDCJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorOIDCJob(null));
        // optional settings
        h.getStepConfig().setControllerId("js7");
        h.getStepConfig().setOrderId("test_order");
        h.getStepConfig().setJobName("test_job");

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testInventorySearch() throws Exception {
        setAgentProperties();

        Map<String, Object> args = new HashMap<>();
        args.put("api_url", "/inventory/search");
        args.put("body", SOSPath.readFile(Path.of("src/test/resources/jocapi/inventory_search.json")));
        // to execute this test successfully set issuer, clientId, clientSecret and issuer as these are not allowed to commit to Github.com
//        args.put("issuer", "YOUR_ISSUER");
//        args.put("clientId", "YOUR_CLIENT_ID");
//        args.put("clientSecret", "YOUR_CLIENT_SECRET");
//        args.put("identityService", "AzureClientFlow");
        args.put("oidcTrustStorePath", "C:\\sp\\java\\jdk-17\\lib\\security\\cacerts");
        args.put("oidcTrustStorePasswd", "changeit");
        args.put("oidcTrustStoreType", "PKCS12");

        UnitTestJobHelper<TestApiExecutorOIDCJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorOIDCJob(null));
        // optional settings
        h.getStepConfig().setControllerId("js7");
        h.getStepConfig().setOrderId("test_order");
        h.getStepConfig().setJobName("test_job");

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @SuppressWarnings("unused")
    private HttpClientAuthConfig getAuthConfig(String user, String password) {
        if (SOSString.isEmpty(user)) {
            return null;
        }
        return new HttpClientAuthConfig(user, password);
    }

    private static void setAgentProperties() {
        Path privateConfPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources");
        System.setProperty("js7.config-directory", privateConfPath.toString());
        System.setProperty("JS7_AGENT_CONFIG_DIR", privateConfPath.toString());
    }

}
