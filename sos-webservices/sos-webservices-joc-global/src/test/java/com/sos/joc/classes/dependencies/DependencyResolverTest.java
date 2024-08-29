package com.sos.joc.classes.dependencies;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DependencyResolverTest {
    
    private static final String JSON_WORKFLOW = "{\"TYPE\":\"Workflow\",\"version\":\"1.7.2\",\"timeZone\":\"Europe/Berlin\",\"instructions\":[{\"TYPE\":\"AddOrder\",\"workflowName\":\"wf_test\"}]}";

    private static final String JSON_BOARD_INSTRUCTION = "{\"jobNames\": [\"job3\"], \"jobLabels\": [\"job3\"], \"postNotices\": [\"board_example\"], \"noticeBoardNames\": [\"board_example\"]}";
    private static final String JSON_ADDORDER_INSTRUCTION = "{\"addOrders\": [\"wf_test\", \"wf_test_2_expect\"]}";
    private static final String JSON_LOCK_INSTRUCTION = "{\"locks\": {\"lock_test\": -1}, \"lockIds\": [\"lock_test\"], \"jobNames\": [\"job\"], \"jobLabels\": [\"job\"]}";

    private static final String JSON_FOS = "{\"TYPE\":\"FileWatch\",\"version\":\"1.7.2\",\"workflowName\":\"wf_fos_test\",\"agentName\":\"primaryAgent\",\"directoryExpr\":\"\\\"C:\\\\\\\\tmp\\\"\",\"pattern\":\"echo*.log\",\"timeZone\":\"UTC\",\"delay\":2,\"title\":\"fos title\"}";
    private static final String JSON_SCHEDULE = "{\"version\":\"1.0.0\",\"workflowNames\":[\"wf_test\"],\"submitOrderToControllerWhenPlanned\":true,\"planOrderAutomatically\":true,\"calendars\":[{\"calendarName\":\"working-days\",\"timeZone\":\"Europe/Berlin\",\"periods\":[{\"singleStart\":\"20:00:00\",\"whenHoliday\":\"SUPPRESS\"}]}],\"orderParameterisations\":[]}";
    private static final String JSON_JOBTEMPLATE = "{\"version\":\"1.7.2\",\"executable\":{\"TYPE\":\"ShellScriptExecutable\",\"script\":\"echo 'it works!'\"},\"graceTimeout\":1,\"arguments\":{},\"jobResourceNames\":[\"jr_test\"]}";

    private static final String JSON_BOARD = "{\"TYPE\":\"Board\",\"postOrderToNoticeId\":\"replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')\",\"endOfLife\":\"$js7EpochMilli + 1 * 60 * 1000\",\"expectOrderToNoticeId\":\"replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')\",\"version\":\"1.7.2\"}";
    private static final String JSON_JOBRESOURCE = "{\"TYPE\":\"JobResource\",\"version\":\"1.0.0\",\"arguments\":{\"testArg1\":\"\\\"testVal1\\\"\"},\"title\":\"jr title\"}";
    private static final String JSON_SCRIPT = "{\"version\":\"1.7.2\",\"script\":\"echo $1\"}";
    private static final String JSON_LOCK = "{\"TYPE\":\"Lock\",\"version\":\"1.7.2\",\"limit\":1}";

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyResolverTest.class);

    @Test
    public void testGetValuesFromJson () {
        JsonObject jsonObj = null;
        List<String> values = null;
        
        jsonObj = DependencyResolver.jsonObjectFromString(JSON_BOARD_INSTRUCTION);
        values = DependencyResolver.getValuesFromInstructions(jsonObj, DependencyResolver.INSTRUCTION_BOARDS_SEARCH);
        values.stream().forEach(value -> LOGGER.trace(value));
        assertEquals(1, values.size());
        
        jsonObj = DependencyResolver.jsonObjectFromString(JSON_ADDORDER_INSTRUCTION);
        values = DependencyResolver.getValuesFromInstructions(jsonObj, DependencyResolver.INSTRUCTION_ADDORDERS_SEARCH);
        values.stream().forEach(value -> LOGGER.trace(value));
        assertEquals(2, values.size());
        
        jsonObj = DependencyResolver.jsonObjectFromString(JSON_LOCK_INSTRUCTION);
        values = DependencyResolver.getValuesFromInstructions(jsonObj, DependencyResolver.INSTRUCTION_LOCKS_SEARCH);
        values.stream().forEach(value -> LOGGER.trace(value));
        assertEquals(1, values.size());
        
        values = new ArrayList<String>();
        jsonObj = DependencyResolver.jsonObjectFromString(JSON_FOS);
        DependencyResolver.getValuesRecursively("", jsonObj, DependencyResolver.WORKFLOWNAME_SEARCH, values);
        values.stream().forEach(value -> LOGGER.trace(value));
        assertEquals(1, values.size());
        
        values = new ArrayList<String>();
        jsonObj = DependencyResolver.jsonObjectFromString(JSON_SCHEDULE);
        DependencyResolver.getValuesRecursively("", jsonObj, DependencyResolver.WORKFLOWNAMES_SEARCH, values);
        values.stream().forEach(value -> LOGGER.trace(value));
        assertEquals(1, values.size());
        
        values = new ArrayList<String>();
        jsonObj = DependencyResolver.jsonObjectFromString(JSON_JOBTEMPLATE);
        DependencyResolver.getValuesRecursively("", jsonObj, DependencyResolver.JOBRESOURCENAMES_SEARCH, values);
        values.stream().forEach(value -> LOGGER.trace(value));
        assertEquals(1, values.size());
    }
    
}
