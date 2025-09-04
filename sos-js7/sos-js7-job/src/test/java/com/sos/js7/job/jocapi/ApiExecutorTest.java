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
import com.sos.js7.job.jocapi.helper.TestApiExecutorJob;
import com.sos.js7.job.jocapi.helper.TestApiExecutorJobArguments;
import com.sos.js7.job.jocapi.helper.TestApiExecutorUploadJob;
import com.sos.js7.job.jocapi.helper.TestApiExecutorUploadJobArguments;

import js7.data_for_java.order.JOutcome;

public class ApiExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExecutorTest.class);

    @Ignore
    @Test
    public void testInventoryExportFolder() throws Exception {
        setAgentProperties();

        Map<String, Object> args = new HashMap<>();
        args.put("api_url", "/inventory/export/folder");
        args.put("body", SOSPath.readFile(Path.of("src/test/resources/jocapi/inventory_export_folder.json")));

        UnitTestJobHelper<TestApiExecutorJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorJob(null));
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

        UnitTestJobHelper<TestApiExecutorJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorJob(null));
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

        UnitTestJobHelper<TestApiExecutorJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorJob(null));
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

        UnitTestJobHelper<TestApiExecutorJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorJob(null));
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
    public void testUploadMulitpartFormdata() throws Exception {
        setAgentProperties();
        Path path = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources/jocapi/test.zip");
        // Path path = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources/jocapi/test.tar.gz");

        Map<String, Object> args = new HashMap<>();
        args.put("api_url", "/inventory/import");
        args.put("format", "ZIP");
        // args.put("format", "TAR_GZ");
        args.put("overwrite", true);
        args.put("file", path);

        UnitTestJobHelper<TestApiExecutorUploadJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorUploadJob(null));

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
