package com.sos.joc.classes.inventory;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.model.order.BlockPosition;


public class WorkflowTest {
    
    private static Workflow wWithSegments;
    private static Workflow wWithOutSegments;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Path jsons = Paths.get("src/test/resources/json/");
        wWithSegments = Globals.objectMapper.readValue(jsons.resolve("segmentWorkflow.json").toFile(), Workflow.class);
        wWithOutSegments = Globals.objectMapper.readValue(jsons.resolve("noSegmentWorkflow.json").toFile(), Workflow.class);
    }

    //@Test
    public void addWorkflowPositionsTest() throws StreamReadException, DatabindException, IOException {
        Workflow wWithPos1 = WorkflowsHelper.addWorkflowPositions(wWithSegments);
        System.out.println(Globals.prettyPrintObjectMapper.writeValueAsString(wWithPos1));
    }
    
    @Test
    public void getLabelToPositionsMapTest() throws StreamReadException, DatabindException, IOException {
        Map<String, List<Object>> labelToPositionsMap1 = WorkflowsHelper.getLabelToPositionsMap(wWithSegments);
        Map<String, List<Object>> labelToPositionsMap2 = WorkflowsHelper.getLabelToPositionsMap(wWithOutSegments);
//        System.out.println(labelToPositionsMap1);
//        System.out.println(labelToPositionsMap2);
        assertTrue(labelToPositionsMap1.size() == 8);
        assertTrue(labelToPositionsMap2.size() == 11);
    }
    
    @Test
    public void getWorkflowBlockPositionsTest() throws StreamReadException, DatabindException, IOException {
        Set<BlockPosition> blockPositions1 = WorkflowsHelper.getWorkflowBlockPositions(wWithSegments.getInstructions());
        Set<BlockPosition> blockPositions2 = WorkflowsHelper.getWorkflowBlockPositions(wWithOutSegments.getInstructions());
//        System.out.println(blockPositions1);
//        System.out.println(blockPositions2);
        assertTrue(blockPositions1.size() == 2);
        assertTrue(blockPositions2.size() == 5);
    }
    
    @Test
    public void getCaseWhenPositionsTest() throws StreamReadException, DatabindException, IOException {
        Set<String> casePositions1 = WorkflowsHelper.getCaseWhenPositions(wWithSegments.getInstructions());
        Set<String> casePositions2 = WorkflowsHelper.getCaseWhenPositions(wWithOutSegments.getInstructions());
//        System.out.println(casePositions1);
//        System.out.println(casePositions2);
        assertTrue(casePositions1.iterator().next().equals("4"));
        assertTrue(casePositions2.iterator().next().equals("1/options:2/options:1"));
    }

}
