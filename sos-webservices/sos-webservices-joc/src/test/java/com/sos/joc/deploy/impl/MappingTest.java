package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.ExcludeConfiguration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.publish.mapper.FilterAttributesMapper;
import com.sos.joc.publish.mapper.UpDownloadMapper;

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
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        String workflowJson = null;
        try {
            om.enable(SerializationFeature.INDENT_OUTPUT);
            workflowJson = om.writeValueAsString(ifElseWorkflow);
            assertNotNull(workflowJson);
            LOGGER.info("IfElse Workflow JSON created successfully!");
            LOGGER.trace(workflowJson);
            workflowJson = null;
            workflowJson = om.writeValueAsString(forkJoinWorkflow);
            LOGGER.info("ForkJoin Workflow JSON created successfully!");
            LOGGER.trace(workflowJson);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void test02WorkflowToControllerObject() {
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        Workflow ifElseWorkflow = null;
        try {
            ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
        } catch (JsonParseException | JsonMappingException e) {
            Assert.fail(e.toString());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        ControllerObject jsObject = new ControllerObject();
        jsObject.setContent(ifElseWorkflow);
        Assert.assertEquals("/test/IfElseWorkflow", ((Workflow) jsObject.getContent()).getPath());
        LOGGER.info("IfElse Workflow JSON mapped to java object successfully!");
    }

    @Test
    public void test04JsonStringToWorkflow() {
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        try {
            Workflow ifElseWorkflow = om.readValue(IF_ELSE_JSON, Workflow.class);
            Workflow forkJoinWorkflow = om.readValue(FORK_JOIN_JSON, Workflow.class);

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
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
        om.setDateFormat(df);
        LOGGER.info("ALL properties:\n" + om.writeValueAsString(filter));
    }

  @Test
  public void test06MapDepHistoryDetailFilter () throws JsonProcessingException {
      ShowDepHistoryFilter filter = DeploymentTestUtils.createDefaultShowDepHistoryDetailFilter();
      ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
      DateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
      om.setDateFormat(df);
      LOGGER.info("ALL properties:\n" + om.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByFromToAndPath();
      LOGGER.info("EXAMPLE 1:\n" + om.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByDeploymentDateAndPath();
      LOGGER.info("EXAMPLE 2:\n" + om.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByDeleteDateAndPath();
      LOGGER.info("EXAMPLE 3:\n" + om.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByDeleteOperationAndPath();
      LOGGER.info("EXAMPLE 4:\n" + om.writeValueAsString(filter));
      filter = DeploymentTestUtils.createShowDepHistoryFilterByCommitIdAndFolder();
      LOGGER.info("EXAMPLE 5:\n" + om.writeValueAsString(filter));
  }

//  @Test
    public void test07MapRedeployFilter () throws JsonProcessingException {
        RedeployFilter filter = DeploymentTestUtils.createDefaultRedeployFilter();
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(filter));
    }
  
    /*
     * No Unit test. DB connection needed to test query parameters
     * */
//    @Test
    public void test08GetDeploymentHistoryDBLayerDeployTest () throws SOSHibernateException {
        ShowDepHistoryFilter filter = DeploymentTestUtils.createShowDepHistoryFilterByDeploymentDateAndPath();

        Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getDetailFilter());
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

        LOGGER.info("Create hql via StringBuilder using streams");
        LOGGER.info(hql.toString());
        LOGGER.info("Get property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item -> LOGGER.info(item.getName() + ": " + query.getParameterValue(item.getName()).toString()));
        LOGGER.info("Replace hql in StringBuilder with property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item ->  
        hql.replace(hql.indexOf(":" + item.getName()), hql.indexOf(":" + item.getName()) + item.getName().length() + 1, 
                query.getParameterValue(item.getName()).toString()));
        LOGGER.info("Replaced hql:\n" + hql.toString());
        StringBuilder hql2 = new StringBuilder("where controllerId = :controllerId and account = :account");
        LOGGER.info("Original : " + hql2.toString());
        LOGGER.info("hql2.indexOf(\":controllerId\") : " + hql2.indexOf(":controllerId"));
        LOGGER.info("hql2.lastIndexOf(\":controllerId\") : " + hql2.lastIndexOf(":controllerId"));
        hql2.replace(hql2.indexOf(":controllerId"), hql2.indexOf(":controllerId") + ":controllerId".length(), "testsuite");
        LOGGER.info("Replace 1: " + hql2.toString());
        hql2 = hql2.replace(hql2.indexOf(":account"), hql2.lastIndexOf(":account") + ":account".length(), "ME!");
        LOGGER.info("Replace 2: " + hql2.toString());
        session.close();
    }
    
    /*
     * No Unit test. DB connection needed to test query parameters
     * */
//    @Test
    public void test09GetDeploymentHistoryFromToDBLayerDeployTest () throws SOSHibernateException {
       ShowDepHistoryFilter filter = DeploymentTestUtils.createShowDepHistoryFilterByFromToAndPath();

        Set<String> presentFilterAttributes = FilterAttributesMapper.getDefaultAttributesFromFilter(filter.getDetailFilter());
        StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_DEP_HISTORY);
        hql.append(
                presentFilterAttributes.stream()
                .map(item -> {
                    if("from".equals(item)) {
                        return FROM_DEP_DATE;
                    } else if("to".equals(item)) {
                        return TO_DEP_DATE;
                    } else {
                        return item + " = :" + item;
                    }
                })
                .collect(Collectors.joining(" and ", " where ", "")));
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
                query.setParameter(item + "Date", FilterAttributesMapper.getValueByFilterAttribute(filter.getDetailFilter(), item), TemporalType.TIMESTAMP);
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

        LOGGER.info("Create hql via StringBuilder using streams");
        LOGGER.info(hql.toString());
        LOGGER.info("Get property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item -> LOGGER.info(item.getName() + ": " + query.getParameterValue(item.getName()).toString()));
        LOGGER.info("Replace hql in StringBuilder with property and value from query.getParameters().stream(): ");
        query.getParameters().stream().forEach(item ->  
        hql.replace(hql.indexOf(":" + item.getName()), hql.indexOf(":" + item.getName()) + item.getName().length() + 1, 
                "'" + query.getParameterValue(item.getName()).toString() + "'"));
        LOGGER.info("Replaced hql:\n" + hql.toString());
        StringBuilder hql2 = new StringBuilder("where controllerId = :controllerId and account = :account");
        LOGGER.info("hql2.indexOf(\":controllerId\") : " + hql2.indexOf(":controllerId"));
        LOGGER.info("hql2.lastIndexOf(\":controllerId\") : " + hql2.lastIndexOf(":controllerId"));
        LOGGER.info("Original : " + hql2.toString());
        hql2.replace(hql2.indexOf(":controllerId"), hql2.indexOf(":controllerId") + ":controllerId".length(), "testsuite");
        LOGGER.info("Replace 1: " + hql2.toString());
        hql2 = hql2.replace(hql2.indexOf(":account"), hql2.lastIndexOf(":account") + ":account".length(), "ME!");
        LOGGER.info("Replace 2: " + hql2.toString());
        session.close();
    }
    
    @Test
    public void test10MapRedeployFilter () throws JsonProcessingException {
        RedeployFilter filter = DeploymentTestUtils.createDefaultRedeployFilter();
//        ExcludeConfiguration exclude = new ExcludeConfiguration();
//        exclude.setPath("/myWorkflows/myIfElseWorkflow/workflow_12");
//        exclude.setDeployType(DeployType.WORKFLOW);
//        filter.getExcludes().add(exclude);
        LOGGER.info("RedeployFilter Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(filter));
        
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
        LOGGER.info(hql.toString());
    }

    @Test
    public void test11MapDeployFilter () throws JsonProcessingException {
        LOGGER.info("DeployFilter Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExampleDeployFilter()));
    }

    @Test
    public void test12MapExportFilterForSigning () throws JsonProcessingException {
        LOGGER.info("ExportFilter forSigning=true Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExampleExportFilter(true)));
    }

    @Test
    public void test13MapExportForBackupFilter () throws JsonProcessingException {
        LOGGER.info("ExportFilter forSigning=false Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExampleExportFilter(false)));
    }

    @Test
    public void test14MapSetVersionFilter () throws JsonProcessingException {
        LOGGER.info("SetVersionFilter Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExampleSetVersionFilter()));
    }

    @Test
    public void test15MapSetVersionsFilter () throws JsonProcessingException {
        LOGGER.info("SetVersionsFilter Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExampleSetVersionsFilter()));
    }

    @Test
    public void test16MapPathFilter () throws JsonProcessingException {
        LOGGER.info("PathFilter Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExamplePathFilter()));
    }

    @Test
    public void test17MapPathResponse () throws JsonProcessingException {
        LOGGER.info("PathResponse Example");
        ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
        om.setDateFormat(df);
        LOGGER.info("\n" + om.writeValueAsString(DeploymentTestUtils.createExamplePathResponse()));
    }

}