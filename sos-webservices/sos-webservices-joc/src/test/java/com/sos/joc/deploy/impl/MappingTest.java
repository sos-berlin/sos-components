package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.mapper.FilterAttributesMapper;
import com.sos.sign.model.instruction.IfElse;
import com.sos.sign.model.instruction.NamedJob;
import com.sos.sign.model.workflow.Workflow;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MappingTest {

    private static final String IF_ELSE_JSON =
            "{\"TYPE\":\"Workflow\",\"path\":\"/test/IfElseWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\","
            + "\"instructions\":[{\"TYPE\":\"If\",\"then\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\"},"
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"job2\"}]},\"else\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job3\"},"
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"job4\"}]}}]}";
    private static final String FORK_JOIN_JSON =
            "{\"TYPE\":\"Workflow\",\"path\":\"/test/ForkJoinWorkflow\",\"versionId\":\"2.0.0-SNAPSHOT\","
            + "\"instructions\":[{\"TYPE\":\"Fork\",\"branches\":[{\"id\":\"BRANCH1\",\"workflow\":{\"instructions\":["
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch1\"}]}},{\"id\":\"BRANCH2\",\"workflow\":{\"instructions\":["
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch2\"}]}},{\"id\":\"BRANCH3\",\"workflow\":{\"instructions\":["
            + "{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobBranch3\"}]}}]},{\"TYPE\":\"Execute.Named\",\"jobName\":\"jobAfterJoin\"}]}";
    private static final Logger LOGGER = LoggerFactory.getLogger(MappingTest.class);
    final String FROM_DEP_DATE = "deploymentDate >= :fromDate"; 
    final String TO_DEP_DATE = "deploymentDate < :toDate"; 


    @Test
    public void test01WorkflowToJsonString() {
        Workflow ifElseWorkflow = DeploymentTestUtils.createIfElseWorkflow();
        Workflow forkJoinWorkflow = DeploymentTestUtils.createForkJoinWorkflow();
        String workflowJson = null;
        try {
            workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(ifElseWorkflow);
            assertNotNull(workflowJson);
            LOGGER.trace("IfElse Workflow JSON created successfully!");
            LOGGER.trace(workflowJson);
            workflowJson = null;
            workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(forkJoinWorkflow);
            LOGGER.trace("ForkJoin Workflow JSON created successfully!");
            LOGGER.trace(workflowJson);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

//    @Test
//    public void test02WorkflowToControllerObject() {
//        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
//        Workflow ifElseWorkflow = null;
//        try {
//            ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
//        } catch (JsonParseException | JsonMappingException e) {
//            Assert.fail(e.toString());
//        } catch (IOException e) {
//            Assert.fail(e.toString());
//        }
//        ControllerObject jsObject = new ControllerObject();
//        jsObject.setContent(ifElseWorkflow);
//        Assert.assertEquals("/test/IfElseWorkflow", ((com.sos.sign.model.workflow.Workflow) jsObject.getContent()).getPath());
//        LOGGER.info("IfElse Workflow JSON mapped to java object successfully!");
//    }

    @Test
    public void test04JsonStringToWorkflow() {
        try {
            Workflow ifElseWorkflow = Globals.prettyPrintObjectMapper.readValue(IF_ELSE_JSON, Workflow.class);
            Workflow forkJoinWorkflow = Globals.prettyPrintObjectMapper.readValue(FORK_JOIN_JSON, Workflow.class);

            IfElse ifElse = ifElseWorkflow.getInstructions().get(0).cast();
            NamedJob mj = ifElse.getThen().getInstructions().get(0).cast();
            Assert.assertEquals("testJsonStringToWorkflow: firstJobOfThen1", "job1", mj.getJobName());

            String firstJobOfThen = ifElseWorkflow.getInstructions().get(0).cast(IfElse.class).getThen().getInstructions().get(0).cast(NamedJob.class)
                    .getJobName();
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
    public void test05MapDepHistoryCompactFilter () throws JsonProcessingException {
        ShowDepHistoryFilter filter = DeploymentTestUtils.createDefaultShowDepHistoryCompactFilter();
        LOGGER.trace("ALL properties:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
    }

  @Test
  public void test06MapDepHistoryDetailFilter () throws JsonProcessingException {
      ShowDepHistoryFilter filter = DeploymentTestUtils.createDefaultShowDepHistoryDetailFilter();
      LOGGER.trace("ALL properties:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByFromToAndPath();
      LOGGER.trace("EXAMPLE 1:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByDeploymentDateAndPath();
      LOGGER.trace("EXAMPLE 2:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByDeleteDateAndPath();
      LOGGER.trace("EXAMPLE 3:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByDeleteOperationAndPath();
      LOGGER.trace("EXAMPLE 4:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByCommitIdAndFolder();
      LOGGER.trace("EXAMPLE 5:\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
  }

//  @Test
    public void test07MapRedeployFilter () throws JsonProcessingException {
        RedeployFilter filter = DeploymentTestUtils.createDefaultRedeployFilter();
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
    }
  
    /*
     * No Unit test. DB connection needed to test query parameters
     * */
//    @Test
    public void test08GetDeploymentHistoryDBLayerDeployTest () throws SOSHibernateException {
        ShowDepHistoryFilter filter = DeploymentTestUtils.createShowDepHistoryFilterByDeploymentDateAndPath();

        Set<String> allowedControllers = Collections.singleton(filter.getDetailFilter().getControllerId());
        Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getDetailFilter(), allowedControllers);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(
                presentFilterAttributes.stream()
                .map(item -> new String (item + " = :" + item))
                .collect(Collectors.joining(" and ", " where ", "")));
 
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/sp_hibernate.cfg.xml"));
        factory.setAutoCommit(true);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        SOSHibernateSession session = factory.openStatelessSession();
         Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());
        presentFilterAttributes.stream().forEach(item -> query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item)));

        LOGGER.trace("Create hql via StringBuilder using streams");
        LOGGER.trace(hql.toString());
        LOGGER.trace("Get property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item -> LOGGER.trace(item.getName() + ": " + query.getParameterValue(item.getName()).toString()));
        LOGGER.trace("Replace hql in StringBuilder with property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item ->  
        hql.replace(hql.indexOf(":" + item.getName()), hql.indexOf(":" + item.getName()) + item.getName().length() + 1, 
                query.getParameterValue(item.getName()).toString()));
        LOGGER.trace("Replaced hql:\n" + hql.toString());
        StringBuilder hql2 = new StringBuilder("where controllerId = :controllerId and account = :account");
        LOGGER.trace("Original : " + hql2.toString());
        LOGGER.trace("hql2.indexOf(\":controllerId\") : " + hql2.indexOf(":controllerId"));
        LOGGER.trace("hql2.lastIndexOf(\":controllerId\") : " + hql2.lastIndexOf(":controllerId"));
        hql2.replace(hql2.indexOf(":controllerId"), hql2.indexOf(":controllerId") + ":controllerId".length(), "testsuite");
        LOGGER.trace("Replace 1: " + hql2.toString());
        hql2 = hql2.replace(hql2.indexOf(":account"), hql2.lastIndexOf(":account") + ":account".length(), "ME!");
        LOGGER.trace("Replace 2: " + hql2.toString());
        session.close();
    }
    
    /*
     * No Unit test. DB connection needed to test query parameters
     * */
//    @Test
    public void test09GetDeploymentHistoryFromToDBLayerDeployTest() throws SOSHibernateException {
        ShowDepHistoryFilter filter = DeploymentTestUtils.createShowDepHistoryFilterByFromToAndPath();

        Set<String> allowedControllers = Collections.singleton(filter.getDetailFilter().getControllerId());
        Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getDetailFilter(), allowedControllers);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(presentFilterAttributes.stream().map(item -> {
            if ("from".equals(item)) {
                return FROM_DEP_DATE;
            } else if ("to".equals(item)) {
                return TO_DEP_DATE;
            } else {
                return item + " = :" + item;
            }
        }).collect(Collectors.joining(" and ", " where ", "")));
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/sp_hibernate.cfg.xml"));
        factory.setAutoCommit(true);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        SOSHibernateSession session = factory.openStatelessSession();
        Query<DBItemDeploymentHistory> query = session.createQuery(hql.toString());

        presentFilterAttributes.stream().forEach(item -> {
            switch (item) {
            case "from":
            case "to":
                query.setParameter(item + "Date", FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item),
                        TemporalType.TIMESTAMP);
                break;
            case "deploymentDate":
            case "deleteDate":
                query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item), TemporalType.TIMESTAMP);
                break;
            default:
                query.setParameter(item, FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item));
                break;
            }
        });

        LOGGER.trace("Create hql via StringBuilder using streams");
        LOGGER.trace(hql.toString());
        LOGGER.trace("Get property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item -> LOGGER.trace(item.getName() + ": " + query.getParameterValue(item.getName()).toString()));
        LOGGER.trace("Replace hql in StringBuilder with property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item -> hql.replace(hql.indexOf(":" + item.getName()), hql.indexOf(":" + item.getName()) + item
                .getName().length() + 1, "'" + query.getParameterValue(item.getName()).toString() + "'"));
        LOGGER.trace("Replaced hql:\n" + hql.toString());
        StringBuilder hql2 = new StringBuilder("where controllerId = :controllerId and account = :account");
        LOGGER.trace("hql2.indexOf(\":controllerId\") : " + hql2.indexOf(":controllerId"));
        LOGGER.trace("hql2.lastIndexOf(\":controllerId\") : " + hql2.lastIndexOf(":controllerId"));
        LOGGER.trace("Original : " + hql2.toString());
        hql2.replace(hql2.indexOf(":controllerId"), hql2.indexOf(":controllerId") + ":controllerId".length(), "testsuite");
        LOGGER.trace("Replace 1: " + hql2.toString());
        hql2 = hql2.replace(hql2.indexOf(":account"), hql2.lastIndexOf(":account") + ":account".length(), "ME!");
        LOGGER.trace("Replace 2: " + hql2.toString());
        session.close();
    }
    
    @Test
    public void test10MapRedeployFilter () throws JsonProcessingException {
        RedeployFilter filter = DeploymentTestUtils.createDefaultRedeployFilter();
//        ExcludeConfiguration exclude = new ExcludeConfiguration();
//        exclude.setPath("/myWorkflows/myIfElseWorkflow/workflow_12");
//        exclude.setDeployType(DeployType.WORKFLOW);
//        filter.getExcludes().add(exclude);
        LOGGER.trace("RedeployFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(filter));
        
        Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter);
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(
                presentFilterAttributes.stream()
                .map(item -> {
                    if("folder".equals(item)) {
                        return "folder = :folder or folder like :likeFolder";
                    } else if ("likeFolder".equals(item)) {
                        return null;
                    } else {
                        return item + " = :" + item;
                    }
                }).filter(item -> item != null)
                .collect(Collectors.joining(" and ", " where ", "")));
        LOGGER.trace(hql.toString());
    }

    @Test
    public void test11MapDeployFilter () throws JsonProcessingException {
        LOGGER.trace("DeployFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleDeployFilter()));
    }

    @Test
    public void test12MapExportFilterForSigning () throws JsonProcessingException {
        LOGGER.trace("ExportFilter forSigning=true Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleExportFilter(true)));
    }

    @Test
    public void test13MapExportForBackupFilter () throws JsonProcessingException {
        LOGGER.trace("ExportFilter forSigning=false Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleExportFilter(false)));
    }

    @Test
    public void test14MapSetVersionFilter () throws JsonProcessingException {
        LOGGER.trace("SetVersionFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleSetVersionFilter()));
    }

    @Test
    public void test15MapSetVersionsFilter () throws JsonProcessingException {
        LOGGER.trace("SetVersionsFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleSetVersionsFilter()));
    }

    @Test
    public void test16MapPathFilter () throws JsonProcessingException {
        LOGGER.trace("PathFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExamplePathFilter()));
    }

    @Test
    public void test17MapPathResponse () throws JsonProcessingException {
        LOGGER.trace("PathResponse Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExamplePathResponse()));
    }

    @Test
    public void test18GenerateRootCAFilter () throws JsonProcessingException {
        LOGGER.trace("GenerateRootCaFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createGenerateCaFilter()));
    }

    @Test
    public void test19SetRootCAFilter () throws JsonProcessingException {
        LOGGER.trace("SetRootCaFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createSetRootCaFilter()));
    }

    @Test
    public void test20RepositoryCopyToFilter () throws JsonProcessingException {
        LOGGER.trace("Repository - CopyToFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryCopyToFilter()));
    }

    @Test
    public void test21RepositoryDeleteFromFilter () throws JsonProcessingException {
        LOGGER.info("Repository - DeleteFromFilter Example");
        LOGGER.info("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryDeleteFromFilter()));
    }

    @Test
    public void test22RepositoryReadFromFilter () throws JsonProcessingException {
        LOGGER.trace("Repository - ReadFromFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryReadFromFilter(true)));
    }

    @Test
    public void test23RepositoryResponseFolder () throws Exception {
        LOGGER.trace("Repository - ResponseFolder Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createResponseFolder(getClass() ,true)));
    }

    @Ignore
    @Test
    public void test24RepositoryUpdateFromFilter () throws JsonProcessingException {
        LOGGER.trace("Repository - UpdateFromFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryUpdateFromFilter()));
    }

}