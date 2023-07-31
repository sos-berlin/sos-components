package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.persistence.TemporalType;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
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
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.agent.transfer.AgentExportFilter;
import com.sos.joc.model.agent.transfer.AgentImportFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.release.ReleasableRecallFilter;
import com.sos.joc.model.joc.VersionsFilter;
import com.sos.joc.model.notification.DeleteNotificationFilter;
import com.sos.joc.model.notification.ReadNotificationFilter;
import com.sos.joc.model.notification.StoreNotificationFilter;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.publish.ShowDepHistoryFilter;
import com.sos.joc.model.publish.folder.ExportFolderFilter;
import com.sos.joc.model.publish.git.AddCredentialsFilter;
import com.sos.joc.model.publish.git.commands.CloneFilter;
import com.sos.joc.model.settings.StoreSettingsFilter;
import com.sos.joc.publish.mapper.FilterAttributesMapper;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;
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

    @Test
    public void test02WorkflowToControllerObject() {
        Workflow ifElseWorkflow = null;
        try {
            ifElseWorkflow = Globals.objectMapper.readValue(IF_ELSE_JSON, Workflow.class);
        } catch (JsonParseException | JsonMappingException e) {
            Assert.fail(e.toString());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        ControllerObject jsObject = new ControllerObject();
        jsObject.setContent(ifElseWorkflow);
        Assert.assertEquals("/test/IfElseWorkflow", ((com.sos.sign.model.workflow.Workflow) jsObject.getContent()).getPath());
        LOGGER.trace("IfElse Workflow JSON mapped to java object successfully!");
    }

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
            LOGGER.trace("Workflow JSONs mapped to java object and children checked successfully!");
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
        LOGGER.trace("Repository - CopyToFilter Folder Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryCopyToFilterFolderExample()));
        LOGGER.trace("Repository - CopyToFilter Files Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryCopyToFilterFilesExample()));
    }

    @Test
    public void test21RepositoryDeleteFromFilter () throws JsonProcessingException {
        LOGGER.trace("Repository - DeleteFromFilter Folder Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryDeleteFromFilterFolderExample()));
        LOGGER.trace("Repository - DeleteFromFilter Files rollout env Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryDeleteFromFilterFilesRolloutExample()));
        LOGGER.trace("Repository - DeleteFromFilter Files local env Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryDeleteFromFilterFilesLocalExample()));
    }

    @Test
    public void test22RepositoryReadFromFilter () throws JsonProcessingException {
        LOGGER.trace("Repository - ReadFromFilter Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryReadFromRolloutFilter(true)));
    }

    @Ignore
    @Test
    public void test23RepositoryResponseFolder () throws Exception {
        LOGGER.trace("Repository - ResponseFolder Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createResponseFolder(getClass() ,true)));
    }

    @Test
    public void test24RepositoryUpdateFromFilter () throws JsonProcessingException {
        LOGGER.trace("Repository - UpdateFromFilter Folder rollout Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryUpdateFromFilterFolderExample()));
        LOGGER.trace("Repository - UpdateFromFilter Files rollout Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryUpdateFromFilterFilesRolloutExample()));
        LOGGER.trace("Repository - UpdateFromFilter Files local Example");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createRepositoryUpdateFromFilterFilesLocalExample()));
    }
    
    @Test
    public void test25GitCredentials () throws JsonProcessingException {
        LOGGER.trace("GitCredentials - with password");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleGitCredentialsPassword()));
        LOGGER.trace("GitCredentials - with personal access token (pat)");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleGitCredentialsAccessToken()));
        LOGGER.trace("GitCredentials - with path to key file (ssh)");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleGitCredentialsKeyfilePath()));
        LOGGER.trace("AddCredentialsFilter");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleAddGitCredentialsFilter()));
        LOGGER.trace("RemoveCredentialsFilter");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleRemoveGitCredentialsFilter()));
    }
    
    @Test
    public void test26GitCommandsFilter () throws JsonProcessingException {
        LOGGER.trace("Git Commands - Add all");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleAddAllFilter()));
        LOGGER.trace("Git Commands - commit");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleCommitFilter()));
        LOGGER.trace("Git Commands - checkout (branch)");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleCheckoutBranchFilter()));
        LOGGER.trace("Git Commands - checkout (tag)");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleCheckoutTagFilter()));
        LOGGER.trace("Git Commands - clone");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleCloneFilter()));
        LOGGER.trace("Git Commands - pull");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExamplePullFilter()));
        LOGGER.trace("Git Commands - push");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExamplePushFilter()));
        LOGGER.trace("Git Commands - tag");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleTagFilter()));
        LOGGER.trace("Git Commands - reset");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleResetAllFilter()));
        LOGGER.trace("Git Commands - restore");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleRestoreAllFilter()));
        LOGGER.trace("Git Commands - log");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleLogFilter()));
        LOGGER.trace("Git Commands - status");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createExampleStatusFilter()));
        LOGGER.trace("Git Command Response - status");
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createGitCommandResponse()));
    }

    @Test
    public void test27LicenseInfoFilter () throws JsonProcessingException {
        LOGGER.trace("\n" + Globals.prettyPrintObjectMapper.writeValueAsString(DeploymentTestUtils.createLicenseInfo()));
    }
    
    @Test
    public void test27ValidateWithRegex() {
        CloneFilter cloneFilter = DeploymentTestUtils.createExampleCloneFilter();
        boolean validatedSuccessfully = false;
        try {
            LOGGER.trace(Globals.objectMapper.writeValueAsString(cloneFilter));
            JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(cloneFilter), CloneFilter.class);
            validatedSuccessfully = true;
        } catch (SOSJsonSchemaException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(validatedSuccessfully);
        AddCredentialsFilter addCredFilter = DeploymentTestUtils.createExampleAddGitCredentialsFilter();
        try {
            LOGGER.trace(Globals.objectMapper.writeValueAsString(addCredFilter));
            JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(addCredFilter), AddCredentialsFilter.class);
            validatedSuccessfully = true;
        } catch (SOSJsonSchemaException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(validatedSuccessfully);
    }
    
    @Test
    public void test28ExportFolder() throws JsonProcessingException {
        ExportFolderFilter filter = DeploymentTestUtils.createExportFolderShallowCopyFilter();
        LOGGER.trace("shallow copy");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(filter));
        filter = DeploymentTestUtils.createExportFolderForSigningFilter();
        LOGGER.trace("for signing");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(filter));
    }
    
    @Test
    public void test29ReleasableRecallFilter() throws JsonProcessingException {
        ReleasableRecallFilter filter = DeploymentTestUtils.createReleasableRecallFilter();
        LOGGER.trace("ReleasableRecallFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(filter));
    }
    
    @Test
    public void test30NotificationFilters() throws JsonProcessingException {
        ReadNotificationFilter readFilter = DeploymentTestUtils.createReadNotificationFilter();
        StoreNotificationFilter storeFilter = DeploymentTestUtils.createStoreNotificationFilter();
        DeleteNotificationFilter deleteFilter = DeploymentTestUtils.createDeleteNotificationFilter();
        LOGGER.trace("ReadNotificationFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(readFilter));
        LOGGER.trace("StoreNotificationFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(storeFilter));
        LOGGER.trace("DeleteNotificationFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(deleteFilter));
    }
    
    @Test
    public void test31SettingsFilter() throws JsonProcessingException {
        StoreSettingsFilter storeFilter = DeploymentTestUtils.createStoreSettingsFilter();
        LOGGER.trace("StoreSettingsFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(storeFilter));
    }
    
    @Test
    public void test32AgentExportFilter() throws JsonProcessingException {
        AgentExportFilter agentExportFilter = DeploymentTestUtils.createAgentExportFilter();
        LOGGER.trace("AgentExportFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(agentExportFilter));
    }
    
    @Test
    public void test33AgentImportFilter() throws JsonProcessingException {
        AgentImportFilter agentImportFilter = DeploymentTestUtils.createAgentImportFilter();
        LOGGER.trace("AgentImportFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(agentImportFilter));
    }
    
    @Test
    public void test34VersionsFilter() throws JsonProcessingException {
        VersionsFilter versionsFilter = DeploymentTestUtils.createVersionsFilter();
        LOGGER.trace("VersionsFilter");
        LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(versionsFilter));
    }
    
    @Test
    public void test35CloneAlias() throws JsonMappingException, JsonProcessingException {
        boolean validated = false;
        try {
            JsonValidator.validate("{\"folder\":\"/Github-Test\",\"remoteUri\":\"https://github.com/ztak0120/JS7-GITHUB.git\",\"category\":\"ROLLOUT\"}".getBytes(), CloneFilter.class);
            validated = true;
        }catch (Exception e) {
            validated = false;
        }
        assertTrue(validated);
        try {
            JsonValidator.validate("{\"folder\":\"/Github-Test\",\"remoteUrl\":\"https://github.com/ztak0120/JS7-GITHUB.git\",\"category\":\"ROLLOUT\"}".getBytes(), CloneFilter.class);
            validated = true;
        }catch (Exception e) {
            validated = false;
        }
        assertTrue(validated);
    }
    
    @Test
    public void test36ParseWorkflowNames () {
        // pattern: ^.*workflowNames\"\:\[(.*?)\].*$
        String regex = "^.*workflowNames\\\"\\:\\[(.*?)\\].*$";
        String json = "{\"version\":\"1.4.0\",\"workflowNames\":[\"scheduled\", \"scheduled2\"],\"title\":\"blah\",\"submitOrderToControllerWhenPlanned\":true,\"planOrderAutomatically\":true,\"calendars\":[{\"calendarName\":\"My_Working_days\",\"timeZone\":\"Europe/Berlin\",\"periods\":[{\"singleStart\":\"14:00:00\",\"whenHoliday\":\"SUPPRESS\"}]}],\"orderParameterisations\":[]}";
        List<String> workflows = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if(matcher.matches()) {
            String workflowNamesArray = matcher.group(1);
            String[] workflowNamesSplitted = workflowNamesArray.split(",");
            for(int i=0; i < workflowNamesSplitted.length; i++) {
                workflows.add(workflowNamesSplitted[i].trim().substring(1, workflowNamesSplitted[i].trim().length() -1));
            }
        }
        assertTrue(workflows.size() == 2);
    }

    @Test
    public void test37DeploymentDescriptorExample() throws JsonProcessingException {
//        DeploymentDescriptor descriptor = DeploymentTestUtils.createDeploymentDescriptorSchemaExample();
//        LOGGER.info("DeploymentDescriptor");
//        String json = Globals.prettyPrintObjectMapper.writeValueAsString(descriptor);
//        LOGGER.info( "\n" + json);
        boolean valid = false;
        String test ="{\"descriptor\" : {\"descriptorId\" : \"example_05\",\"title\" : \"Example of the current schema of a deployment descriptor\",\"account\" : \"sp\",\"scheduled\" : \"2023-03-06T13:46:26.643+00:00\",\"created\" : \"2023-02-20T13:46:26.643+00:00\"},\"license\" : {\"licenseKeyFile\" : \"licenses/sos.pem\",\"licenseBinFile\" : \"licenses/js7-license.jar\"},\"agents\" : {\"controllerRefs\" : [ {\"controllerId\" : \"testsuite\",\"members\" : [ {\"agentId\" : \"agent_001\",\"target\" : {\"connection\" : {\"host\" : \"centostest-primary\",\"port\" : 22},\"authentication\" : {\"method\" : \"publickey\",\"user\" : \"sos\",\"keyFile\" : \"/home/sos/.ssh/sos_rsa\"},\"packageLocation\" : \"/tmp\",\"execPre\" : \"StopService\",\"execPost\" : \"StartService\",\"makeService\" : true,\"forceSudo\" : true},\"media\" : {\"release\" : \"2.5.1\",\"tarball\" : \"2.5.1/js7_agent_unix.2.5.1.tar.gz\"},\"installation\" : {\"home\" : \"/opt/sos-berlin.com/js7/agent-primary\",\"data\" : \"/var/sos-berlin.com/js7/agent-primary\",\"homeOwner\" : \"sos1:sos1\",\"dataOwner\" : \"sos2:sos2\",\"runUser\" : \"sos2\",\"httpPort\" : \"localhost:31445\",\"httpsPort\" : \"centostest-primary.sos:31443\",\"javaHome\" : \"/opt/jdk-11.0.2\",\"javaOptions\" : \"-Xmx125m -Djava.security.egd=file:///dev/urandom\"},\"configuration\" : {\"certificates\" : {\"keyStore\" : \"agents/instances/agent_001/config/private/https-keystore.p12\",\"keyStorePassword\" : \"jobscheduler\",\"keyPassword\" : \"jobscheduler\",\"keyAlias\" : \"centostest-primary\",\"trustStore\" : \"agents/instances/agent_001/config/private/https-truststore.p12\",\"trustStorePassword\" : \"jobscheduler\"},\"templates\" : [ \"agents/templates/https/config\" ]}}, {\"agentId\" : \"agent_002\",\"target\" : {\"connection\" : {\"host\" : \"centostest-secondary\",\"port\" : 22},\"authentication\" : {\"method\" : \"publickey\",\"user\" : \"sos\",\"keyFile\" : \"/home/sos/.ssh/sos_rsa\"},\"packageLocation\" : \"/tmp\",\"execPre\" : \"StopService\",\"execPost\" : \"StartService\",\"makeService\" : true,\"forceSudo\" : true},\"media\" : {\"release\" : \"2.5.1\",\"tarball\" : \"2.5.1/js7_agent_unix.2.5.1.tar.gz\"},\"installation\" : {\"home\" : \"/opt/sos-berlin.com/js7/agent-secondary\",\"data\" : \"/var/sos-berlin.com/js7/agent-secondary\",\"homeOwner\" : \"sos1:sos1\",\"dataOwner\" : \"sos2:sos2\",\"runUser\" : \"sos2\",\"httpPort\" : \"localhost:32445\",\"httpsPort\" : \"centostest-secondary.sos:32443\",\"javaHome\" : \"/opt/jdk-11.0.2\",\"javaOptions\" : \"-Xmx256m -Djava.security.egd=file:///dev/urandom\"},\"configuration\" : {\"certificates\" : {\"keyStore\" : \"agents/instances/agent_002/config/private/https-keystore.p12\",\"keyStorePassword\" : \"jobscheduler\",\"keyPassword\" : \"jobscheduler\",\"keyAlias\" : \"centostest-primary\",\"trustStore\" : \"agents/instances/agent_002/config/private/https-truststore.p12\",\"trustStorePassword\" : \"jobscheduler\"},\"templates\" : [ \"agents/templates/https/config\" ]}} ]} ]},\"controllers\" : [ {\"jocRef\" : \"cluster\",\"controllerId\" : \"testsuite\",\"primary\" : {\"target\" : {\"connection\" : {\"host\" : \"centostest-primary\",\"port\" : 22},\"authentication\" : {\"method\" : \"publickey\",\"user\" : \"sos\",\"keyFile\" : \"/home/sos/.ssh/sos_rsa\"},\"packageLocation\" : \"/tmp\",\"execPre\" : \"StopService\",\"execPost\" : \"StartService\",\"makeService\" : true,\"forceSudo\" : true},\"media\" : {\"release\" : \"2.5.1\",\"tarball\" : \"2.5.1/js7_controller_unix.2.5.1.tar.gz\"},\"installation\" : {\"home\" : \"/opt/sos-berlin.com/js7/controller-primary\",\"data\" : \"/var/sos-berlin.com/js7/controller-primary\",\"homeOwner\" : \"sos1:sos1\",\"dataOwner\" : \"sos2:sos2\",\"runUser\" : \"sos2\",\"httpPort\" : \"localhost:21444\",\"httpsPort\" : \"centostest-primary.sos:21443\",\"javaHome\" : \"/opt/jdk-11.0.2\",\"javaOptions\" : \"-Xmx256m -Djava.security.egd=file:///dev/urandom\"},\"configuration\" : {\"certificates\" : {\"cert\" : \"controllers/instances/cluster.primary/config/private/centostest-primary.crt\",\"keyStore\" : \"controllers/instances/cluster.primary/config/private/https-keystore.p12\",\"keyStorePassword\" : \"jobscheduler\",\"keyPassword\" : \"jobscheduler\",\"keyAlias\" : \"centostest-primary\",\"trustStore\" : \"controllers/instances/cluster.primary/config/private/https-truststore.p12\",\"trustStorePassword\" : \"jobscheduler\"},\"templates\" : [ \"controllers/templates/https.primary/config\" ]}},\"secondary\" : {\"target\" : {\"connection\" : {\"host\" : \"centostest-secondary\",\"port\" : 22},\"authentication\" : {\"method\" : \"publickey\",\"user\" : \"sos\",\"keyFile\" : \"/home/sos/.ssh/sos_rsa\"},\"packageLocation\" : \"/tmp\",\"execPre\" : \"StopService\",\"execPost\" : \"StartService\",\"makeService\" : true,\"forceSudo\" : true},\"media\" : {\"release\" : \"2.5.1\",\"tarball\" : \"2.5.1/js7_controller_unix.2.5.1.tar.gz\"},\"installation\" : {\"home\" : \"/opt/sos-berlin.com/js7/controller-secondary\",\"data\" : \"/var/sos-berlin.com/js7/controller-secondary\",\"homeOwner\" : \"sos1:sos1\",\"dataOwner\" : \"sos2:sos2\",\"runUser\" : \"sos2\",\"httpPort\" : \"localhost:22444\",\"httpsPort\" : \"centostest-secondary.sos:22443\",\"javaHome\" : \"/opt/jdk-11.0.2\",\"javaOptions\" : \"-Xmx256m -Djava.security.egd=file:///dev/urandom\"},\"configuration\" : {\"certificates\" : {\"cert\" : \"controllers/instances/cluster.secondary/config/private/centostest-secondary.crt\",\"keyStore\" : \"controllers/instances/cluster.secondary/config/private/https-keystore.p12\",\"keyStorePassword\" : \"jobscheduler\",\"keyPassword\" : \"jobscheduler\",\"keyAlias\" : \"centostest-secondary\",\"trustStore\" : \"controllers/instances/cluster.secondary/config/private/https-truststore.p12\",\"trustStorePassword\" : \"jobscheduler\"},\"templates\" : [ \"controllers/templates/https.secondary/config\" ]}}} ],\"joc\" : [ {\"members\" : {\"clusterId\" : \"cluster\",\"instances\" : [ {\"instanceId\" : 1,\"target\" : {\"connection\" : {\"host\" : \"centostest-primary\",\"port\" : 22},\"authentication\" : {\"method\" : \"publickey\",\"user\" : \"sos\",\"keyFile\" : \"/home/sos/.ssh/sos_rsa\"},\"packageLocation\" : \"/tmp\",\"execPre\" : \"StopService\",\"execPost\" : \"StartService\",\"makeService\" : true,\"forceSudo\" : true},\"media\" : {\"release\" : \"2.5.1\",\"tarball\" : \"2.5.1/js7_joc_linux.2.5.1.tar.gz\"},\"installation\" : {\"setupDir\" : \"/opt/sos-berlin.com/js7/joc-primary.setup\",\"title\" : \"PRIMARY JOC COCKPIT\",\"securityLevel\" : \"medium\",\"dbmsConfig\" : \"joc/templates/dbms/h2/response/hibernate.cfg.xml\",\"dbmsDriver\" : \"joc/templates/dbms/h2/response/h2-1.4.200.jar\",\"dbmsInit\" : \"byJoc\",\"isUser\" : true,\"isPreserveEnv\" : true,\"home\" : \"/opt/sos-berlin.com/js7/joc-primary\",\"data\" : \"/var/sos-berlin.com/js7/joc-primary\",\"homeOwner\" : \"sos1:sos1\",\"dataOwner\" : \"sos2:sos2\",\"runUser\" : \"sos2\",\"httpPort\" : \"localhost:11446\",\"httpsPort\" : \"centostest-primary.sos:11443\",\"javaHome\" : \"/opt/jdk-11.0.2\",\"javaOptions\" : \"-Xmx256m -Djava.security.egd=file:///dev/urandom\"},\"configuration\" : {\"responseDir\" : \"joc/templates/dbms/h2/response\",\"certificates\" : {\"cert\" : \"joc/instances/cluster.primary/resources/centostest-primary.crt\",\"keyStore\" : \"joc/instances/cluster.primary/resources/https-keystore.p12\",\"keyStorePassword\" : \"jobscheduler\",\"keyPassword\" : \"jobscheduler\",\"keyAlias\" : \"centostest-primary\",\"trustStore\" : \"joc/instances/cluster.primary/resources/https-truststore.p12\",\"trustStorePassword\" : \"jobscheduler\"},\"templates\" : [ \"joc/templates/https/resources\" ],\"startFiles\" : {\"httpIni\" : \"joc/templates/https/start.d/http.ini\",\"httpsIni\" : \"joc/templates/https/start.d/https.ini\",\"sslIni\" : \"joc/templates/https/start.d/ssl.ini\"}}}, {\"instanceId\" : 2,\"target\" : {\"connection\" : {\"host\" : \"centostest-secondary\",\"port\" : 22},\"authentication\" : {\"method\" : \"publickey\",\"user\" : \"sos\",\"keyFile\" : \"/home/sos/.ssh/sos_rsa\"},\"packageLocation\" : \"/tmp\",\"execPre\" : \"StopService\",\"execPost\" : \"StartService\",\"makeService\" : true,\"forceSudo\" : true},\"media\" : {\"release\" : \"2.5.1\",\"tarball\" : \"2.5.1/js7_joc_linux.2.5.1.tar.gz\"},\"installation\" : {\"setupDir\" : \"/sos-berlin.com/js7/opt/joc-secondary.setup\",\"title\" : \"SECONDARY JOC COCKPIT\",\"securityLevel\" : \"medium\",\"dbmsConfig\" : \"joc/templates/dbms/h2/response/hibernate.cfg.xml\",\"dbmsDriver\" : \"joc/templates/dbms/h2/response/h2-1.4.200.jar\",\"dbmsInit\" : \"byJoc\",\"isUser\" : true,\"isPreserveEnv\" : true,\"home\" : \"/opt/sos-berlin.com/js7/joc-secondary\",\"data\" : \"/var/sos-berlin.com/js7/joc-secondary\",\"homeOwner\" : \"sos1:sos1\",\"dataOwner\" : \"sos2:sos2\",\"runUser\" : \"sos2\",\"httpPort\" : \"localhost:12446\",\"httpsPort\" : \"centostest-secondary.sos:12443\",\"javaHome\" : \"/opt/jdk-11.0.2\",\"javaOptions\" : \"-Xmx256m -Djava.security.egd=file:///dev/urandom\"},\"configuration\" : {\"responseDir\" : \"joc/templates/dbms/h2/response\",\"certificates\" : {\"cert\" : \"joc/instances/cluster.secondary/resources/centostest-secondary.crt\",\"keyStore\" : \"joc/instances/cluster.secondary/resources/https-keystore.p12\",\"keyStorePassword\" : \"jobscheduler\",\"keyPassword\" : \"jobscheduler\",\"keyAlias\" : \"centostest-secondary\",\"trustStore\" : \"joc/instances/cluster.secondary/resources/https-truststore.p12\",\"trustStorePassword\" : \"jobscheduler\"},\"templates\" : [ \"joc/templates/https/resources\" ],\"startFiles\" : {\"httpIni\" : \"joc/templates/https/start.d/http.ini\",\"httpsIni\" : \"joc/templates/https/start.d/https.ini\",\"sslIni\" : \"joc/templates/https/start.d/ssl.ini\"}}} ]}} ]}";
        try {
            Validator.validate(ConfigurationType.DEPLOYMENTDESCRIPTOR, test.getBytes());
            valid = true;
        } catch (SOSJsonSchemaException | JocConfigurationException | SOSHibernateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOGGER.info( "json valid: " + valid);
        assertTrue(valid);
    }
    
    @Test
    public void testTarOutputStream () throws IOException {
        // file path of the archive file to write
        String targetFilePath = "target/created_test_files/targetArchive.tar.gz";
        // create parent folder if not exists
        if(!Files.exists(Paths.get("target/created_test_files"))) {
            Files.createDirectory(Paths.get("target/created_test_files"));
        }
        File targetFile = new File(targetFilePath);
        // file path of the source file to put into the archive
        String sourceFilePath = "src/test/resources/12345678901234567890123456789012345678901234567890/1234567890123456789012345678901234567890/shortFilenameWithLongPath.txt";
        // file path of the target file in the archive
        String pathInArchive = "12345678901234567890123456789012345678901234567890/1234567890123456789012345678901234567890/shortFilenameWithLongPath.txt";
        File source = Paths.get(sourceFilePath).toFile();
        InputStream in = Files.newInputStream(Paths.get(sourceFilePath));
        long fileLength = source.length();
        // create TAR outputStream with POSIX credentials
        GZIPOutputStream gzipOut = null;
        BufferedOutputStream bOut = null;
        TarArchiveOutputStream tarArchiveOut = null;
        // get Name property of the entry of the newly created archive
        String pathInArchiveAfter;
        try {
            bOut = new BufferedOutputStream(new FileOutputStream(targetFile));
            gzipOut = new GZIPOutputStream(bOut);
            tarArchiveOut = new TarArchiveOutputStream(gzipOut);
            tarArchiveOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tarArchiveOut.putArchiveEntry(new TarArchiveEntry(source, pathInArchive));
            // write the target archives entry
            for (int i = 0; i < fileLength; i++) {
                tarArchiveOut.write(in.read());
            }
            // flush and close the tar output stream (save the file)
            tarArchiveOut.closeArchiveEntry();
            tarArchiveOut.flush();
        } finally {
            if (tarArchiveOut != null) {
                try {
                    tarArchiveOut.finish();
                    tarArchiveOut.close();
                } catch (Exception e) {}
            }
            if (gzipOut != null) {
                try {
                    gzipOut.flush();
                    gzipOut.close();
                } catch (Exception e) {}
            }
            if (bOut != null) {
                try {
                    bOut.flush();
                    bOut.close();
                } catch (Exception e) {}
            }
        }
        // read the newly created tar file
        GZIPInputStream gzipInputStream = null;
        TarArchiveInputStream tarArchiveIn = null;
        InputStream tarIn = null;
        try {
            tarIn = Files.newInputStream(Paths.get(targetFilePath));
            gzipInputStream = new GZIPInputStream(tarIn);
            tarArchiveIn = new TarArchiveInputStream(gzipInputStream);
            LOGGER.debug("In File Path: " + sourceFilePath);
            LOGGER.debug("archive path: " + targetFilePath);
            LOGGER.debug("path inside archive: " + pathInArchive);
            TarArchiveEntry entry = tarArchiveIn.getNextTarEntry();
            pathInArchiveAfter = entry.getName();
            LOGGER.debug("path inside archive after store: " + pathInArchiveAfter);
        } finally {
            if (tarIn != null) {
                try {
                    tarIn.close();
                } catch (Exception e) {}
            }
            if (tarArchiveIn != null) {
                try {
                    tarArchiveIn.close();
                } catch (Exception e) {}
            }
            if (gzipInputStream != null) {
                try {
                    gzipInputStream.close();
                } catch (Exception e) {}
            }
        }
    }
    
}