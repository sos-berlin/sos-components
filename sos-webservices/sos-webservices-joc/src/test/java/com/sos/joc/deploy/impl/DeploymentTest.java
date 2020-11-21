package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.openpgp.PGPException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeploymentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentTest.class);
    private static final String PUBLICKEY_RESOURCE_PATH = "/test_public.asc";
    private static final String PRIVATEKEY_RESOURCE_PATH = "/test_private.asc";
    private static final String PRIVATE_RSA_PKCS8_KEY_PATH = "src/test/resources/sp2.key";
    private static final String PRIVATE_RSA_KEY_PATH = "src/test/resources/sp.key";
    private static final String TARGET_FILENAME = "bundle_js_workflows.zip";
    private static final String TARGET_FILENAME_SINGLE = "bundle_js_single_workflow.zip";
    private static ObjectMapper om = UpDownloadMapper.initiateObjectMapper();

    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.info("**************************  Deployment Tests started  *******************************");
        LOGGER.info("");
        LOGGER.info("**************************  Using PGP Keys  *****************************************");
        LOGGER.info("");
        om.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("**************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test01ExportWorkflowsToArchiveFile() throws IOException {
        LOGGER.info("*************************  export workflows to zip file Test ************************");
        Set<Workflow> workflows = DeploymentTestUtils.createWorkflowsforDeployment();
        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
        for (Workflow workflow : workflows) {
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            jsObjectsToExport.add(jsObject);
        }
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.info("Archive bundle_js_single_workflow.zip succefully created in ./target/created_test_files!");
        LOGGER.info("************************ End Test export workflows to zip file  *********************");
        LOGGER.info("");
    }

//    @Test
//    public void test01aExportWorkflowToArchiveFile() throws IOException {
//        LOGGER.info("*************************  export single workflow to zip file Test ******************");
//        Set<Workflow> workflows = DeploymentTestUtils.createSingleWorkflowsforDeployment();
//        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
//        for (Workflow workflow : workflows) {
//            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
//            jsObjectsToExport.add(jsObject);
//        }
//        exportSingleWorkflow(jsObjectsToExport);
//        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME_SINGLE)));
//        LOGGER.info("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
//        LOGGER.info("************************ End Test export workflows to zip file  *********************");
//        LOGGER.info("");
//    }

    @Test
    public void test02ImportWorkflowsfromArchiveFile() throws IOException {
        LOGGER.info("*************************  import workflows from zip file Test **********************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("************************* End Test import workflows from zip file  ******************");
        LOGGER.info("");
    }

    @Test
    public void test03ImportWorkflowsFromSignAndUpdateArchiveFile() throws IOException, PGPException {
        LOGGER.info("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       sign Workflows **************************************");
        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowPGP(workflow);
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
    public void test04ImportWorkflowsandSignaturesFromArchiveFile() throws IOException, PGPException {
        Set<JSObject> jsObjects = new HashSet<JSObject>();
        LOGGER.info("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       import signatures ***********************************");
        Set<SignaturePath> signatures = importSignaturePaths();
        assertEquals(100, signatures.size());
        LOGGER.info(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    workflow.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.info("mapping signatures to workflows was successful!");
        LOGGER.info("*************************  verify signatures ****************************************");
        int countVerified = 0;
        int countNotVerified = 0;
        for (JSObject jsObject : jsObjects) {
            if (verifySignaturePGP((Workflow) jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.info(String.format("%1$d workflow signatures verified successfully", countVerified));
        LOGGER.info(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.info("************************* End Test import and verify ********************************");
    }

    @Test
    public void test05ImportWorkflowsFromSignAndUpdateArchiveFile()
            throws IOException, DataLengthException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
        LOGGER.info("");
        LOGGER.info("**************************  Using RSA Keys and a generate X.509 Certificate  ********");
        LOGGER.info("********************************  PKCS12  *******************************************");
        LOGGER.info("");
        LOGGER.info("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       sign Workflows **************************************");
        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowRSA(workflow);
            LOGGER.info("Base64 MIME encoded: " + signature.getSignatureString());
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
    public void test06ImportWorkflowsandSignaturesFromArchiveFile()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Set<JSObject> jsObjects = new HashSet<JSObject>();
        LOGGER.info("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       import signatures ***********************************");
        Set<SignaturePath> signatures = importSignaturePaths();
        assertEquals(100, signatures.size());
        LOGGER.info(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    workflow.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.info("mapping signatures to workflows was successful!");
        LOGGER.info("*************************  verify signatures ****************************************");
        LOGGER.info("*************************  verify signatures with extracted Public Key **************");
        int countVerified = 0;
        int countNotVerified = 0;
        String privateKey = new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_KEY_PATH)));
        KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(privateKey);
        for (JSObject jsObject : jsObjects) {
            if (verifySignatureWithRSAKey(kp.getPublic(), (Workflow) jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.info(String.format("%1$d workflow signatures verified with extracted PublicKey successfully", countVerified));
        LOGGER.info(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.info("*************************  verify signatures with generated Certificate  ************");
        countVerified = 0;
        countNotVerified = 0;
        Certificate cert = KeyUtil.generateCertificateFromKeyPair(kp);
        for (JSObject jsObject : jsObjects) {
            if (verifySignatureWithX509Certificate(cert, (Workflow) jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.info(String.format("%1$d workflow signatures verified with generated Certificate successfully", countVerified));
        LOGGER.info(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.info("************************* End Test import and verify ********************************");
        LOGGER.info("");
    }

    @Test
    public void test07ImportWorkflowsFromSignAndUpdateArchiveFile()
            throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
        LOGGER.info("");
        LOGGER.info("**************************  Using RSA Keys and a generate X.509 Certificate  ********");
        LOGGER.info("********************************  PKCS8  ********************************************");
        LOGGER.info("");
        LOGGER.info("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       sign Workflows **************************************");
        Set<JSObject> jsObjectsToExport = new HashSet<JSObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowRSAPKCS8(workflow);
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
    public void test08ImportWorkflowsandSignaturesFromArchiveFile()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Set<JSObject> jsObjects = new HashSet<JSObject>();
        LOGGER.info("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       import signatures ***********************************");
        Set<SignaturePath> signatures = importSignaturePaths();
        assertEquals(100, signatures.size());
        LOGGER.info(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            JSObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    workflow.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.info("mapping signatures to workflows was successful!");
        LOGGER.info("*************************  verify signatures ****************************************");
        LOGGER.info("*************************  verify signatures with extracted Public Key **************");
        int countVerified = 0;
        int countNotVerified = 0;
        String privateKey = new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_PKCS8_KEY_PATH)));
        KeyPair kp = KeyUtil.getKeyPairFromPrivatKeyString(privateKey);
        for (JSObject jsObject : jsObjects) {
            if (verifySignatureWithRSAKey(kp.getPublic(), (Workflow) jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.info(String.format("%1$d workflow signatures verified with extracted PublicKey successfully", countVerified));
        LOGGER.info(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.info("*************************  verify signatures with generated Certificate  ************");
        countVerified = 0;
        countNotVerified = 0;
        Certificate cert = KeyUtil.generateCertificateFromKeyPair(kp);
        for (JSObject jsObject : jsObjects) {
            if (verifySignatureWithX509Certificate(cert, (Workflow) jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.info(String.format("%1$d workflow signatures verified with generated Certificate successfully", countVerified));
        LOGGER.info(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.info("************************* End Test import and verify ********************************");
        LOGGER.info("");
    }

    @Test
    @Ignore
    /* This is NO Unit test!
     * This is an integration Test with hibernate and a DB!
     * to run this test, adjust path to your hibernate configuration file
     * adjust account and security level of your keyPair
     * Make sure your X509Certificate is known to your controller
     * uncomment the Ignore annotation
     **/
    public void test10VerfiySignatureFromDBItem() throws SOSHibernateException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, 
            SignatureException, NoSuchProviderException, IOException {
        LOGGER.info("******************************  VerfiySignatureFromDBItem Test  *********************");
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/sp_hibernate.cfg.xml"));
        factory.setAutoCommit(true);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        SOSHibernateSession session = factory.openStatelessSession();
        DBLayerDeploy dbLayer = new DBLayerDeploy(session);
        DBLayerKeys dbLayerKeys = new DBLayerKeys(session);
        List<Long> depIds = new ArrayList<Long>();
        depIds.add(116L);
        List<DBItemDeploymentHistory> items = dbLayer.getFilteredDeployments(depIds);
        DBItemDeploymentHistory historyItem = items.get(0);
        X509Certificate certificate = KeyUtil.getX509Certificate(dbLayerKeys.getKeyPair("root", JocSecurityLevel.LOW).getCertificate());
        Boolean verified = VerifySignature.verifyX509(certificate, historyItem.getContent(), historyItem.getSignedContent());
        LOGGER.info("verified: " + verified);

        LOGGER.info("*************************** VerfiySignatureFromDBItem Test finished *****************");
    }

    private void exportWorkflows(Set<JSObject> jsObjectsToExport) throws IOException {
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        Boolean notExists = Files.notExists(Paths.get("target").resolve("created_test_files"));
        if (notExists) {
            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
            LOGGER.info("subfolder \"created_test_files\" created in target folder.");
        }
        out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME));
        zipOut = new ZipOutputStream(new BufferedOutputStream(out), StandardCharsets.UTF_8);
        for (JSObject jsObjectToExport : jsObjectsToExport) {
            Workflow workflow = (Workflow) jsObjectToExport.getContent();
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

    @SuppressWarnings("unused")
    private void exportSingleWorkflow(Set<JSObject> jsObjectsToExport) throws IOException {
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        Boolean notExists = Files.notExists(Paths.get("target").resolve("created_test_files"));
        if (notExists) {
            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
            LOGGER.info("subfolder \"created_test_files\" created in target folder.");
        }
        out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME_SINGLE));
        zipOut = new ZipOutputStream(new BufferedOutputStream(out), StandardCharsets.UTF_8);
        for (JSObject jsObjectToExport : jsObjectsToExport) {
            Workflow workflow = (Workflow) jsObjectToExport.getContent();
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
        LOGGER.info("archive to read from exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME)));
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

    @SuppressWarnings("unused")
    private Set<Workflow> importSingleWorkflow() throws IOException {
        Set<Workflow> workflows = new HashSet<Workflow>();
        LOGGER.info("archive to read from exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME_SINGLE)));
        InputStream fileStream = Files.newInputStream(Paths.get("target/created_test_files").resolve(TARGET_FILENAME_SINGLE));
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

    private Set<SignaturePath> importSignaturePaths() throws IOException {
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
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
                SignaturePath signaturePath = new SignaturePath();
                signaturePath.setObjectPath("/" + entryName.substring(0, entryName.indexOf(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION
                        .value())));
                Signature signature = new Signature();
                signature.setSignatureString(outBuffer.toString());
                signaturePath.setSignature(signature);
                signaturePaths.add(signaturePath);
            }
        }
        return signaturePaths;
    }

    private Signature signWorkflowPGP(Workflow workflow) throws IOException, PGPException {
        Signature signature = new Signature();
        String passphrase = null;
        InputStream privateKeyInputStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = om.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson);
        signature.setSignatureString(SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    @SuppressWarnings("unused")
    private Signature signSingleWorkflowPGP(Workflow workflow) throws IOException, PGPException {
        Signature signature = new Signature();
        String passphrase = null;
        InputStream privateKeyInputStream = getClass().getResourceAsStream("/sos.private-pgp-key.asc");
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = om.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson);
        signature.setSignatureString(SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    private Signature signWorkflowRSA(Workflow workflow)
            throws DataLengthException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException, IOException {
        Signature signature = new Signature();
        String privateKey = new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_KEY_PATH)));
        String workflowJson = om.writeValueAsString(workflow);
        signature.setSignatureString(SignObject.signX509(privateKey, workflowJson));
        return signature;
    }

    private Signature signWorkflowRSAPKCS8(Workflow workflow)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Signature signature = new Signature();
        PrivateKey privateKey = KeyUtil.getPrivateKeyFromString(new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_PKCS8_KEY_PATH))));
        String workflowJson = om.writeValueAsString(workflow);
        signature.setSignatureString(SignObject.signX509(privateKey, workflowJson));
        return signature;
    }

    @SuppressWarnings("unused")
    private Signature signAgentRefPGP(AgentRef agent) throws IOException, PGPException {
        Signature signature = new Signature();
        String passphrase = null;
        InputStream privateKeyInputStream = getClass().getResourceAsStream(PRIVATEKEY_RESOURCE_PATH);
        InputStream originalInputStream = null;
        String agentJson = null;
        agentJson = om.writeValueAsString(agent);
        originalInputStream = IOUtils.toInputStream(agentJson);
        signature.setSignatureString(SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    @SuppressWarnings("unused")
    private Signature signAgentRefRSA(AgentRef agent)
            throws IOException, DataLengthException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException  {
        Signature signature = new Signature();
        String privateKey = new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_KEY_PATH)));
        String agentJson = om.writeValueAsString(agent);
        signature.setSignatureString(SignObject.signX509(privateKey, agentJson));
        return signature;
    }

    private Boolean verifySignaturePGP(Workflow workflow, String signatureString) throws IOException, PGPException {
        InputStream publicKeyInputStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        InputStream signatureInputStream = IOUtils.toInputStream(signatureString);
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = om.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson);
        Boolean isVerified = null;
        isVerified = VerifySignature.verifyPGP(publicKeyInputStream, originalInputStream, signatureInputStream);
        return isVerified;
    }

    private Boolean verifySignatureWithRSAKey(PublicKey publicKey, Workflow workflow, String signatureString)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        String workflowJson = om.writeValueAsString(workflow);
        return VerifySignature.verifyX509(publicKey, workflowJson, signatureString);
    }

    private Boolean verifySignatureWithX509Certificate(Certificate certificate, Workflow workflow, String signatureString)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException {
        String workflowJson = null;
        workflowJson = om.writeValueAsString(workflow);
        return VerifySignature.verifyX509(certificate, workflowJson, signatureString);
    }

}
