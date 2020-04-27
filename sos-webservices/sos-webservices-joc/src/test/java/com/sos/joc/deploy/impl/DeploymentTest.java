package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.Charsets;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.publish.common.JSObjectFileExtension;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeploymentTest {

    private static final String TARGET_FILENAME = "bundle_js_workflows.zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentTest.class);

    @Test
    public void test1ExportWorkflowsToArchiveFile () {
        LOGGER.info("*************************  export Workflows to zip file  *************************");
        Set<Workflow> workflows = DeploymentTestUtils.createWorkflowsforDeployment();
        ObjectMapper om = new ObjectMapper();
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        try {
            LOGGER.info("ZIP File \"bundle_js_workflows.zip\" containing the json files will be stored in the target directory.");
            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
            LOGGER.info("subfolder \"created_test_files\" created in target folder");
            LOGGER.info("exists: " + Files.exists(Paths.get("target").resolve("created_test_files")));
            out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME));
            zipOut = new ZipOutputStream(new BufferedOutputStream(out), Charsets.UTF_8);
            for (Workflow workflow : workflows) {
                String zipEntryName = workflow.getPath().substring(1) + ".workflow.json"; 
                ZipEntry entry = new ZipEntry(zipEntryName);
                zipOut.putNextEntry(entry);
                String workflowJson = null;
                try {
                    om.enable(SerializationFeature.INDENT_OUTPUT);
                    workflowJson = om.writeValueAsString(workflow);
                    zipOut.write(workflowJson.getBytes());
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                zipOut.closeEntry();
            }
            zipOut.flush();
        } catch (IOException e1) {
            LOGGER.error(e1.getMessage(), e1);
        } finally {
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (Exception e) {}
            }
        }
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.info("************************ End Test export Workflows to zip file  ***********************");
    }

    @Test
    public void test2ImportWorkflowsfromArchiveFile () throws IOException {
        LOGGER.info("*************************  import Workflows from zip file  *************************");
        Set<Workflow> workflows = new HashSet<Workflow>();
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        LOGGER.info("Zip file to read from exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME)));
        InputStream fileStream = Files.newInputStream(Paths.get("target/created_test_files").resolve(TARGET_FILENAME));
        ZipInputStream zipStream = new ZipInputStream(fileStream);
        ZipEntry entry = null;
        while ((entry = zipStream.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            String entryName = entry.getName().replace('\\', '/');
            ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
            byte[] binBuffer = new byte[8192];
            int binRead = 0;
            while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                outBuffer.write(binBuffer, 0, binRead);
            }
            if (("/"+entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                workflows.add(om.readValue(outBuffer.toString(), Workflow.class));
            } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value())) {
                // TODO: add processing for AgentRefs, when Agents are ready
            } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                // TODO: add processing for Locks, when Locks are ready
            }
        }
        assertEquals(100, workflows.size());
        LOGGER.info("************************ End Test import Workflows from zip file  ***********************");
    }
    
}
