package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.pgp.util.sign.SignObject;
import com.sos.pgp.util.verify.VerifySignature;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeploymentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentTest.class);
    private static final String PUBLICKEY_PATH = "src/test/resources/test_public.asc";
    private static final String PRIVATEKEY_PATH = "src/test/resources/test_private.asc";
    private static final String PUBLICKEY_RESOURCE_PATH = "/test_public.asc";
    private static final String PRIVATEKEY_RESOURCE_PATH = "/test_private.asc";
    private static final String TARGET_FILENAME = "bundle_js_workflows.zip";
    private static ObjectMapper om = new ObjectMapper();
//    private Set<Workflow> workflows = new HashSet<Workflow>();

    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.info("**************************  Deployment Tests started  *******************************");
        LOGGER.info("");
        om.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("**************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test1ExportWorkflowsToArchiveFile () throws IOException {
        LOGGER.info("*************************  export workflows to zip file Test ************************");
        Set<Workflow> workflows = DeploymentTestUtils.createWorkflowsforDeployment();
        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
        for(Workflow workflow : workflows) {
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            jsObjectsToExport.add(jsObject);
        }
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.info("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
        LOGGER.info("************************ End Test export workflows to zip file  *********************");
        LOGGER.info("");            
    }

    @Test
    public void test2ImportWorkflowsfromArchiveFile () throws IOException {
        LOGGER.info("*************************  import workflows from zip file Test **********************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("************************* End Test import workflows from zip file  ******************");
        LOGGER.info("");            
    }
    
    @Test
    public void test3ImportWorkflowsFromSignAndUpdateArchiveFile () throws IOException, PGPException {
        LOGGER.info("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       sign Workflows **************************************");
        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
        int counterSigned = 0;
        for(Workflow workflow : workflows) {
            Signature signature = signWorkflow(workflow);
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
            assertNotNull(jsObject.getSignedContent());
            counterSigned++;
            jsObjectsToExport.add(jsObject);
        }
        assertEquals(100, counterSigned);
        LOGGER.info(String.format("%1$d workflows signed", counterSigned));
        LOGGER.info("*************************       export signature to zip file ************************");
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.info("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
        LOGGER.info("Archive contains the workflows and their signatures.");
        LOGGER.info("************************* End Test import workflows from zip file  ******************");
        LOGGER.info("");            
    }

    @Test
    public void test4ImportWorkflowsandSignaturesFromArchiveFile () throws IOException, PGPException {
        Set<JSObject> jsObjects = new HashSet<JSObject>();
        LOGGER.info("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       import signatures ***********************************");
        Set<Signature> signatures = importSignatures();
        assertEquals(100, signatures.size());
        LOGGER.info(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            Signature signature = signatures.stream().filter(signatureFromStream -> signatureFromStream.getObjectPath().equals(workflow.getPath()))
                .map(signatureFromStream -> signatureFromStream).findFirst().get();
            jsObject.setSignedContent(signature.getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.info("mapping signatures to workflows was successful!");
        LOGGER.info("*************************  verify signatures ****************************************");
        int countVerified = 0;
        int countNotVerified = 0;
        Boolean isVerified = false;
        for (JSObject jsObject : jsObjects) {
            if (verifySignature((Workflow)jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.info(String.format("%1$d workflow signatures verified successfully", countVerified));
        LOGGER.info(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.info("************************* End Test import and verify ********************************");
        LOGGER.info("");            
    }
    
    private void exportWorkflows(Set<JSObject> jsObjectsToExport) throws IOException {
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        Boolean notExists = Files.notExists(Paths.get("target").resolve("created_test_files"));
        if(notExists) {
            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
            LOGGER.info("subfolder \"created_test_files\" created in target folder.");
        }
        out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME));
        zipOut = new ZipOutputStream(new BufferedOutputStream(out), Charsets.UTF_8);
        for (JSObject jsObjectToExport : jsObjectsToExport) {
            Workflow workflow = (Workflow)jsObjectToExport.getContent();
            String signature = jsObjectToExport.getSignedContent();
            String zipEntryNameWorkflow = workflow.getPath().substring(1) + ".workflow.json";
            String zipEntryNameWorkflowSignature = workflow.getPath().substring(1) + ".workflow.json.asc";
            
            ZipEntry entryWorkflow = new ZipEntry(zipEntryNameWorkflow);
            zipOut.putNextEntry(entryWorkflow);
            String workflowJson = om.writeValueAsString(workflow);
            zipOut.write(workflowJson.getBytes());
            zipOut.closeEntry();
            
            if (signature != null) {
                ZipEntry entrySignature = new ZipEntry(zipEntryNameWorkflowSignature);
                zipOut.putNextEntry(entrySignature);
                zipOut.write(signature.getBytes());
                zipOut.closeEntry();
            }
        }
        zipOut.flush();
        if (zipOut != null) {
            zipOut.close();
        }
    }

    private Set<Workflow> importWorkflows() throws IOException {
        Set<Workflow> workflows = new HashSet<Workflow>();
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
            if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                workflows.add(om.readValue(outBuffer.toString(), Workflow.class));
            }
        }
        return workflows;
    }
    
    private Set<Signature> importSignatures() throws IOException {
        Set<Signature> signatures = new HashSet<Signature>();
        LOGGER.info("archive to read exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME)));
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
            if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                Signature signature = new Signature();
                signature.setObjectPath("/" + entryName.substring(0, entryName.indexOf(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())));
                signature.setSignatureString(outBuffer.toString());
                signatures.add(signature);
            }
        }
        return signatures;
    }
    
    private Signature signWorkflow (Workflow workflow) throws IOException, PGPException {
        Signature signature = new Signature();
        String passphrase = null;
        InputStream privateKeyInputStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = om.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson);
        signature.setSignatureString(SignObject.sign(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    private Boolean verifySignature(Workflow workflow, String signatureString) throws IOException, PGPException {
        InputStream publicKeyInputStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        InputStream signatureInputStream = IOUtils.toInputStream(signatureString);
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = om.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson);
        Boolean isVerified = null;
        isVerified = VerifySignature.verify(publicKeyInputStream, originalInputStream, signatureInputStream);
        return isVerified;
    }

}
