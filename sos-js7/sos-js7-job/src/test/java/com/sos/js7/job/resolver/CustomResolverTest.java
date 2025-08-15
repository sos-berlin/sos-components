package com.sos.js7.job.resolver;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;
import com.sos.js7.job.helper.TestJob;
import com.sos.js7.job.helper.TestJobArguments;

import js7.data_for_java.order.JOutcome;

/** Performance Tests:<br/>
 * - see com.sos.js7.job.generator.resolver.ResolverGenerator<br/>
 * - Test of 1000 resolvers:<br/>
 * -- Registration by agent: ~600ms<br/>
 * -- 300 arguments (arguments from the JobResources and only some of them have o prefix)<br/>
 * --- running with DEBUG level and EmptyJob (ScriptEngine) <= 1s<br/>
 * -- 1000 arguments (all arguments from a JobResource and use different 1000 prefixes) - see com.sos.js7.job.generator.json.JsonGenerator<br/>
 * --- running with DEBUG level and EmptyJob ~ 2s<br/>
 * --- executed with INFO level and EmptyJob (ScriptEngine) <= 1s<br/>
 * -- 100 order preparation maps, 10 parameters pro map (10 prefixes used per iteration) - see com.sos.js7.job.generator.json.JsonGenerator<br/>
 * --- executed with INFO level and EmptyJob (Java) = 1s<br/>
 */
public class CustomResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomResolverTest.class);

    /** To load the custom resolvers the environment variable JS7_AGENT_LIBDIR should be set */
    @Ignore
    @Test
    public void test() throws Exception {
        String authMethodsAsStringList = "PASSWORD;PUBLICKEY";
        String authMethodsAsBase64EncodedStringList = "KEYBOARD_INTERACTIVE;base64:UEFTU1dPUkQ=;base64:UFVCTElDS0VZ;";

        Map<String, Object> args = new HashMap<>();
        args.put("auth_methods", authMethodsAsStringList);
        args.put("auth_methods", Arrays.asList(authMethodsAsStringList.split(";")));
        args.put("auth_methods", authMethodsAsBase64EncodedStringList);
        args.put("auth_methods", Arrays.asList(authMethodsAsBase64EncodedStringList.split(";")));

        // args.put("test", "upper:xyz");
        // args.put("test2", "apath:xyz");
        // args.put("path", "");
        args.put("map_string_values", getMapStrings());
        args.put("map_path_values", getMapPaths());
        args.put("log_all_arguments", Boolean.valueOf(true));
        args.put("log_level", "DEBUG");
        args.put("base1", "base64:xxx");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testList() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put(TestJob.ARG_NAME_LIST_SINGLETONMAP_UNDECLARED, getListOfSingletonMaps());
        args.put(TestJob.ARG_NAME_LIST_SINGLETONMAP_DECLARED, getListOfSingletonMaps());

        args.put("log_all_arguments", Boolean.valueOf(true));
        args.put("log_level", "DEBUG");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    private Map<String, String> getMapStrings() {
        Map<String, String> m = new HashMap<>();
        m.put("p1", "x");
        m.put("p2", "upper:my_string");
        return m;
    }

    private Map<String, Object> getMapPaths() {
        Map<String, Object> m = new HashMap<>();
        m.put("p1", Paths.get("x"));
        m.put("p1", "upper:my_path");
        return m;
    }

    private List<Map<String, Object>> getListOfSingletonMaps() {
        List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
        l.add(new HashMap<>(Collections.singletonMap("username", "base64:dXNlcm5hbWU=")));
        l.add(new HashMap<>(Collections.singletonMap("counter", "base64:Y291bnRlcg==")));
        // l.add(new HashMap<>(Collections.singletonMap("bad", "base64:bad_value")));
        l.add(new HashMap<>(Collections.singletonMap("username", "base64:Y291bnRlcg==")));
        l.add(new HashMap<>(Collections.singletonMap("y", "base64:yyyy")));
        return l;
    }

}
