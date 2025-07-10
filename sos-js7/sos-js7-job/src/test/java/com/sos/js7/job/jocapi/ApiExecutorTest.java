package com.sos.js7.job.jocapi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.js7.job.UnitTestJobHelper;
import com.sos.js7.job.jocapi.helper.TestApiExecutorJob;
import com.sos.js7.job.jocapi.helper.TestApiExecutorJobArguments;

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

        UnitTestJobHelper<TestApiExecutorJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorJob());
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

        UnitTestJobHelper<TestApiExecutorJobArguments> h = new UnitTestJobHelper<>(new TestApiExecutorJob());
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
    public void deprecatedTestApiExecutorExport() throws Exception {
        setAgentProperties();

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Outfile", Paths.get(System.getProperty("user.dir")).resolve("target/exported").resolve("export_calendars.zip").toString()
                .replace('\\', '/'));
        ApiExecutor ex = new ApiExecutor(null, headers);
        String accessToken = ex.login().getAccessToken();
        // String requestBody = "{\"useShortPath\": false, \"exportFile\": {\"filename\": \"export_calendars.zip\", \"format\": \"ZIP\"}, \"shallowCopy\":
        // {\"objectTypes\": [\"WORKINGDAYSCALENDAR\",\"NONWORKINGDAYSCALENDAR\"],\"folders\": [\"/Calendars\"],\"recursive\": true, \"onlyValidObjects\":
        // false, \"withoutDrafts\": false, \"withoutDeployed\": false, \"withoutReleased\": false}}";
        // ApiResponse response = ex.post(accessToken, "/inventory/export/folder", requestBody);
        LOGGER.info("File created!");
        ex.logout(accessToken);
    }

    @Ignore
    @Test
    public void deprecatedTestApiExecutorOrderLog() throws Exception {
        setAgentProperties();

        ApiExecutor ex = new ApiExecutor(null);
        String accessToken = ex.login().getAccessToken();
        String requestBody = "{\"controllerId\":\"controller_270\",\"historyId\":264}";
        ApiResponse response = ex.post(accessToken, "/order/log", requestBody);
        LOGGER.info("Order log:\n" + response.getResponseBody());
        ex.logout(accessToken);
    }

    private static void setAgentProperties() {
        Path privateConfPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources");
        System.setProperty("js7.config-directory", privateConfPath.toString());
        System.setProperty("JS7_AGENT_CONFIG_DIR", privateConfPath.toString());
    }

}
