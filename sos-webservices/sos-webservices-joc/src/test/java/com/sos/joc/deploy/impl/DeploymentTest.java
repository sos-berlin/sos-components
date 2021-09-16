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
import java.nio.file.Path;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.controller.model.agent.AgentRef;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.sign.model.workflow.Workflow;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeploymentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentTest.class);
    private static final String PUBLICKEY_RESOURCE_PATH = "/test_public.asc";
    private static final String PRIVATEKEY_RESOURCE_PATH = "/test_private.asc";
    private static final String PRIVATE_RSA_PKCS8_KEY_PATH = "src/test/resources/sp2.key";
    private static final String PRIVATE_RSA_KEY_PATH = "src/test/resources/sp.key";
    private static final String PRIVATE_ECDSA_KEY_RESOURCE_PATH = "/sos.private-ec-key.pem";
    private static final String X509_CERTIFICATE_RESOURCE_PATH = "/sos.certificate-ec-key.pem";
    private static final String TARGET_FILENAME = "bundle_js_workflows.zip";
    private static final String TARGET_FILENAME_SINGLE = "bundle_js_single_workflow.zip";

    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.info("**************************  Deployment Tests started  *******************************");
        LOGGER.info("");
        LOGGER.info("**************************  Using PGP Keys  *****************************************");
        LOGGER.info("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("**************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test01ExportWorkflowsToArchiveFile() throws IOException {
        LOGGER.info("*************************  export workflows to zip file Test ************************");
        Set<Workflow> workflows = DeploymentTestUtils.createWorkflowsforDeployment();
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        for (Workflow workflow : workflows) {
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
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
//        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
//        for (Workflow workflow : workflows) {
//            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
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
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowPGP(workflow);
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
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
        Set<ControllerObject> jsObjects = new HashSet<ControllerObject>();
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
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    jsObject.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.info("mapping signatures to workflows was successful!");
        LOGGER.info("*************************  verify signatures ****************************************");
        int countVerified = 0;
        int countNotVerified = 0;
        for (ControllerObject jsObject : jsObjects) {
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
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowRSA(workflow);
//            LOGGER.info("Base64 MIME encoded: " + signature.getSignatureString());
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
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
        Set<ControllerObject> jsObjects = new HashSet<ControllerObject>();
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
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    jsObject.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
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
        for (ControllerObject jsObject : jsObjects) {
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
        for (ControllerObject jsObject : jsObjects) {
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
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowRSAPKCS8(workflow);
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
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
        Set<ControllerObject> jsObjects = new HashSet<ControllerObject>();
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
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    jsObject.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
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
        for (ControllerObject jsObject : jsObjects) {
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
        for (ControllerObject jsObject : jsObjects) {
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

    @Test
    public void test11ImportWorkflowsFromSignAndUpdateArchiveFile() 
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, PGPException {
        LOGGER.info("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.info("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows2();
        assertEquals(2, workflows.size());
        LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.info("*************************       sign Workflows **************************************");
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowECDSA(workflow);
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
            assertNotNull(jsObject.getSignedContent());
            counterSigned++;
            jsObjectsToExport.add(jsObject);
        }
        assertEquals(2, counterSigned);
        LOGGER.info(String.format("%1$d workflows signed", counterSigned));
        LOGGER.info("*************************       export signature to zip file ************************");
        exportWorkflows(jsObjectsToExport, "export_high_from_gui.zip", ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value());
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve("export_high_from_gui.zip")));
        LOGGER.info("Archive export_high_from_gui.zip succefully created in ./target/created_test_files!");
        LOGGER.info("Archive contains the workflows and their signatures.");
        LOGGER.info("************************* End Test import workflows from zip file  ******************");
        LOGGER.info("");
    }

    @Test
    public void test12ImportWorkflowsFromFolderSignAndUpdateArchiveFiles() 
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, PGPException {
        LOGGER.info("*************************  import sign and update workflows from zip files Test *****");
        LOGGER.info("*************************       import workflows ************************************");
        Path archivesPath = Paths.get("src/test/resources/import_deploy");
        Set<Path> filePaths = Files.list(archivesPath).collect(Collectors.toSet());
        Integer archivesToSignCount = filePaths.size();
        filePaths.stream().forEach(item -> {
            try {
                Set<Workflow> workflows = importWorkflows(item.toString().replace("\\", "/").replace("src/test/resources", ""));
                LOGGER.info(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
                LOGGER.info("*************************       sign Workflows **************************************");
                Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
                int counterSigned = 0;
                for (Workflow workflow : workflows) {
                    Signature signature = signWorkflowECDSA(workflow);
                    ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
                    assertNotNull(jsObject.getSignedContent());
                    counterSigned++;
                    jsObjectsToExport.add(jsObject);
                }
                LOGGER.info(String.format("%1$d workflows signed", counterSigned));
                LOGGER.info("*************************       export signature to zip file ************************");
                exportWorkflows(jsObjectsToExport, item.getFileName().toString(),
                        item.getParent().toString().replace("\\", "/").replace("src/test/resources/",""), 
                        ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value());
                LOGGER.info(String.format("Archive %1$s succefully created in ./target/created_test_files/%2$s!",
                        item.getFileName().toString(), item.getParent().toString().replace("\\", "/").replace("src/test/resources/","")));
                LOGGER.info("Archive contains the workflows and their signatures.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Set<Path> signedArchivesPaths = Files.list(Paths.get("target/created_test_files/import_deploy")).collect(Collectors.toSet());
        assertTrue(archivesToSignCount == signedArchivesPaths.size());
        LOGGER.info(String.format("%1$d archives signed.", signedArchivesPaths.size()));
        LOGGER.info("************************* End Test import workflows from zip file  ******************");
        LOGGER.info("");
    }

    @Test
    @Ignore
    /* This is NO Unit test!
     * This is an integration Test with hibernate and a DB!
     * to run this test, adjust path to your hibernate configuration file
     * uncomment the Ignore annotation
     **/
    public void test13DetermineItemsForCleanup() throws SOSHibernateException {
        LOGGER.info("******************************  Determine Items For Cleanup Test  *******************");
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/sp_hibernate.cfg.xml"));
        factory.setAutoCommit(true);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        SOSHibernateSession session = factory.openStatelessSession();
        DBLayerDeploy dbLayer = new DBLayerDeploy(session);
        // First: get all deployments from history
        List<DBItemDeploymentHistory> items = dbLayer.getAllDeployedConfigurations();
        // Second: map per item name
        Map<String, Set<DBItemDeploymentHistory>> mapPerItemName = 
                items.stream().collect(Collectors.groupingBy(DBItemDeploymentHistory::getName, Collectors.toSet()));
        for (Map.Entry<String, Set<DBItemDeploymentHistory>> entry : mapPerItemName.entrySet()) {
            LOGGER.info("Object withName: " + entry.getKey());
            LOGGER.info("has deployment count of: " + entry.getValue().size());
            if(entry.getValue().size() <= 5) {
                continue;
            } else {
                // Third: map by deployment date 
                Map<Date, List<DBItemDeploymentHistory>> mapPerDate = 
                        entry.getValue().stream().collect(Collectors.groupingBy(DBItemDeploymentHistory::getDeploymentDate));
                
                Set<Date> sortedDates = mapPerDate.keySet().stream().sorted(Comparator.comparing(Date::getTime)).collect(Collectors.toSet());
                sortedDates.stream().sorted(Comparator.naturalOrder()).forEach(item -> LOGGER.info("Nat. Order" + item.toLocaleString()));
                sortedDates.stream().sorted(Comparator.reverseOrder()).forEach(item -> LOGGER.info("Rev. Order" + item.toLocaleString()));
            }
        }
        LOGGER.info("**************************** Determine Items For Cleanup Test finished **************");
    }
    
    private void exportWorkflows(Set<ControllerObject> jsObjectsToExport) throws IOException {
        exportWorkflows(jsObjectsToExport, TARGET_FILENAME, ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value());
    }

    private void exportWorkflows(Set<ControllerObject> jsObjectsToExport, String filename, String signatureFileExtension) throws IOException {
        exportWorkflows(jsObjectsToExport, filename, null, signatureFileExtension);
    }

    private void exportWorkflows(Set<ControllerObject> jsObjectsToExport, String filename, String targetFolder, String signatureFileExtension) throws IOException {
        ZipOutputStream zipOut = null;
        OutputStream out = null;
        Path defaultTargetFolderPath = Paths.get("target").resolve("created_test_files");
        Path targetFolderPath = null;
        if (targetFolder != null) {
            targetFolderPath = defaultTargetFolderPath.resolve(targetFolder);
        } else {
            targetFolderPath = defaultTargetFolderPath;
        }
        Boolean notExists = Files.notExists(targetFolderPath);
        if (notExists) {
            Files.createDirectory(targetFolderPath);
            LOGGER.info(String.format("folder \"%1$s\" created.", targetFolderPath.toString()));
        }
        out = Files.newOutputStream(targetFolderPath.resolve(filename));
        zipOut = new ZipOutputStream(new BufferedOutputStream(out), StandardCharsets.UTF_8);
        for (ControllerObject jsObjectToExport : jsObjectsToExport) {
            com.sos.sign.model.workflow.Workflow workflow = (com.sos.sign.model.workflow.Workflow) jsObjectToExport.getContent();
            String signature = jsObjectToExport.getSignedContent();
            String zipEntryNameWorkflow = workflow.getPath().substring(1) + ".workflow.json";
            String zipEntryNameWorkflowSignature = workflow.getPath().substring(1) + signatureFileExtension;
            ZipEntry entryWorkflow = new ZipEntry(zipEntryNameWorkflow);
            zipOut.putNextEntry(entryWorkflow);
            String workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
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

//    private void exportSingleWorkflow(Set<ControllerObject> jsObjectsToExport) throws IOException {
//        ZipOutputStream zipOut = null;
//        OutputStream out = null;
//        Boolean notExists = Files.notExists(Paths.get("target").resolve("created_test_files"));
//        if (notExists) {
//            Files.createDirectory(Paths.get("target").resolve("created_test_files"));
//            LOGGER.info("subfolder \"created_test_files\" created in target folder.");
//        }
//        out = Files.newOutputStream(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME_SINGLE));
//        zipOut = new ZipOutputStream(new BufferedOutputStream(out), StandardCharsets.UTF_8);
//        for (ControllerObject jsObjectToExport : jsObjectsToExport) {
//            Workflow workflow = (Workflow) jsObjectToExport.getContent();
//            String signature = jsObjectToExport.getSignedContent();
//            String zipEntryNameWorkflow = jsObjectToExport.getPath().substring(1) + ".workflow.json";
//            String zipEntryNameWorkflowSignature = jsObjectToExport.getPath().substring(1) + ".workflow.json.asc";
//            ZipEntry entryWorkflow = new ZipEntry(zipEntryNameWorkflow);
//            zipOut.putNextEntry(entryWorkflow);
//            String workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
//            zipOut.write(workflowJson.getBytes());
//            zipOut.closeEntry();
//            if (signature != null) {
//                ZipEntry entrySignature = new ZipEntry(zipEntryNameWorkflowSignature);
//                zipOut.putNextEntry(entrySignature);
//                zipOut.write(signature.getBytes());
//                zipOut.closeEntry();
//            }
//        }
//        zipOut.flush();
//        if (zipOut != null) {
//            zipOut.close();
//        }
//    }
//
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
            if (("/" + entryName).endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                workflows.add(Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), Workflow.class));
            }
        }
        return workflows;
    }

    private Set<Workflow> importWorkflows(String filepath) throws IOException {
        Set<Workflow> workflows = new HashSet<Workflow>();
        
        InputStream fileStream = getClass().getResourceAsStream(filepath);
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
            if (("/" + entryName).endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                workflows.add(Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), Workflow.class));
            }
        }
        return workflows;
    }

    private Set<Workflow> importWorkflows2() throws IOException {
        Set<Workflow> workflows = new HashSet<Workflow>();
        LOGGER.info("archive to read from exists: " + Files.exists(Paths.get("src/test/resources/import_deploy").resolve("export_high_from_gui.zip")));
        InputStream fileStream = getClass().getResourceAsStream("/import_deploy/export_high_from_gui.zip");
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
            if (("/" + entryName).endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                workflows.add(Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), Workflow.class));
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
            if (("/" + entryName).endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                workflows.add(Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), Workflow.class));
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
            if (("/" + entryName).endsWith(ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value())) {
                SignaturePath signaturePath = new SignaturePath();
                signaturePath.setObjectPath("/" + entryName.substring(0, entryName.indexOf(ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION
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
        workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson, StandardCharsets.UTF_8);
        signature.setSignatureString(SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    private Signature signWorkflowECDSA(Workflow workflow) 
            throws IOException, PGPException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Signature signature = new Signature();
        String privateKeyString = new String(Files.readAllBytes(Paths.get("src/test/resources/sos.private-ec-key.pem")), StandardCharsets.UTF_8);
        PrivateKey privateKey = KeyUtil.getPrivateECDSAKeyFromString(privateKeyString);
        String workflowJson = null;
        workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        signature.setSignatureString(SignObject.signX509("SHA512WithECDSA", privateKey, workflowJson));
        return signature;
    }

    @SuppressWarnings("unused")
    private Signature signSingleWorkflowPGP(Workflow workflow) throws IOException, PGPException {
        Signature signature = new Signature();
        String passphrase = null;
        InputStream privateKeyInputStream = getClass().getResourceAsStream("/sos.private-pgp-key.asc");
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson, StandardCharsets.UTF_8);
        signature.setSignatureString(SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    private Signature signWorkflowRSA(Workflow workflow)
            throws DataLengthException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException, IOException {
        Signature signature = new Signature();
        String privateKey = new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_KEY_PATH)));
        String workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        signature.setSignatureString(SignObject.signX509(privateKey, workflowJson));
        return signature;
    }

    private Signature signWorkflowRSAPKCS8(Workflow workflow)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Signature signature = new Signature();
        PrivateKey privateKey = KeyUtil.getPrivateKeyFromString(new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_PKCS8_KEY_PATH))));
        String workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
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
        agentJson = Globals.prettyPrintObjectMapper.writeValueAsString(agent);
        originalInputStream = IOUtils.toInputStream(agentJson, StandardCharsets.UTF_8);
        signature.setSignatureString(SignObject.signPGP(privateKeyInputStream, originalInputStream, passphrase));
        return signature;
    }

    @SuppressWarnings("unused")
    private Signature signAgentRefRSA(AgentRef agent)
            throws IOException, DataLengthException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException  {
        Signature signature = new Signature();
        String privateKey = new String (Files.readAllBytes(Paths.get(PRIVATE_RSA_KEY_PATH)));
        String agentJson = Globals.prettyPrintObjectMapper.writeValueAsString(agent);
        signature.setSignatureString(SignObject.signX509(privateKey, agentJson));
        return signature;
    }

    private Boolean verifySignaturePGP(Workflow workflow, String signatureString) throws IOException, PGPException {
        InputStream publicKeyInputStream = getClass().getResourceAsStream(PUBLICKEY_RESOURCE_PATH);
        InputStream signatureInputStream = IOUtils.toInputStream(signatureString, StandardCharsets.UTF_8);
        InputStream originalInputStream = null;
        String workflowJson = null;
        workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        originalInputStream = IOUtils.toInputStream(workflowJson, StandardCharsets.UTF_8);
        Boolean isVerified = null;
        isVerified = VerifySignature.verifyPGP(publicKeyInputStream, originalInputStream, signatureInputStream);
        return isVerified;
    }

    private Boolean verifySignatureWithRSAKey(PublicKey publicKey, Workflow workflow, String signatureString)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        String workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        return VerifySignature.verifyX509(publicKey, workflowJson, signatureString);
    }

    private Boolean verifySignatureWithX509Certificate(Certificate certificate, Workflow workflow, String signatureString)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException {
        String workflowJson = null;
        workflowJson = Globals.prettyPrintObjectMapper.writeValueAsString(workflow);
        return VerifySignature.verifyX509(certificate, workflowJson, signatureString);
    }

}
