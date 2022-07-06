package com.sos.joc.deploy.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
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

import javax.naming.InvalidNameException;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.controller.model.agent.AgentRef;
import com.sos.joc.Globals;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.SigningCertificateUtil;
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
        LOGGER.trace("**************************  Deployment Tests started  *******************************");
        LOGGER.trace("");
        LOGGER.trace("**************************  Using PGP Keys  *****************************************");
        LOGGER.trace("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.trace("**************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test01ExportWorkflowsToArchiveFile() throws IOException {
        LOGGER.trace("*************************  export workflows to zip file Test ************************");
        Set<Workflow> workflows = DeploymentTestUtils.createWorkflowsforDeployment();
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        for (Workflow workflow : workflows) {
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            jsObjectsToExport.add(jsObject);
        }
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.trace("Archive bundle_js_single_workflow.zip succefully created in ./target/created_test_files!");
        LOGGER.trace("************************ End Test export workflows to zip file  *********************");
        LOGGER.trace("");
    }

//    @Test
//    public void test01aExportWorkflowToArchiveFile() throws IOException {
//        LOGGER.trace("*************************  export single workflow to zip file Test ******************");
//        Set<Workflow> workflows = DeploymentTestUtils.createSingleWorkflowsforDeployment();
//        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
//        for (Workflow workflow : workflows) {
//            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
//            jsObjectsToExport.add(jsObject);
//        }
//        exportSingleWorkflow(jsObjectsToExport);
//        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME_SINGLE)));
//        LOGGER.trace("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
//        LOGGER.trace("************************ End Test export workflows to zip file  *********************");
//        LOGGER.trace("");
//    }

    @Test
    public void test02ImportWorkflowsfromArchiveFile() throws IOException {
        LOGGER.trace("*************************  import workflows from zip file Test **********************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("************************* End Test import workflows from zip file  ******************");
        LOGGER.trace("");
    }

    @Test
    public void test03ImportWorkflowsFromSignAndUpdateArchiveFile() throws IOException, PGPException {
        LOGGER.trace("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       sign Workflows **************************************");
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
        LOGGER.trace(String.format("%1$d workflows signed", counterSigned));
        LOGGER.trace("*************************       export signature to zip file ************************");
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.trace("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
        LOGGER.trace("Archive contains the workflows and their signatures.");
        LOGGER.trace("************************* End Test import workflows from zip file  ******************");
        LOGGER.trace("");
    }

    @Test
    public void test04ImportWorkflowsandSignaturesFromArchiveFile() throws IOException, PGPException {
        Set<ControllerObject> jsObjects = new HashSet<ControllerObject>();
        LOGGER.trace("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       import signatures ***********************************");
        Set<SignaturePath> signatures = importSignaturePaths();
        assertEquals(100, signatures.size());
        LOGGER.trace(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    jsObject.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.trace("mapping signatures to workflows was successful!");
        LOGGER.trace("*************************  verify signatures ****************************************");
        int countVerified = 0;
        int countNotVerified = 0;
        for (ControllerObject jsObject : jsObjects) {
            if (verifySignaturePGP((Workflow) jsObject.getContent(), jsObject.getSignedContent())) {
                countVerified++;
            } else {
                countNotVerified++;
            }
        }
        LOGGER.trace(String.format("%1$d workflow signatures verified successfully", countVerified));
        LOGGER.trace(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.trace("************************* End Test import and verify ********************************");
    }

    @Test
    public void test05ImportWorkflowsFromSignAndUpdateArchiveFile()
            throws IOException, DataLengthException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
        LOGGER.trace("");
        LOGGER.trace("**************************  Using RSA Keys and a generate X.509 Certificate  ********");
        LOGGER.trace("********************************  PKCS12  *******************************************");
        LOGGER.trace("");
        LOGGER.trace("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       sign Workflows **************************************");
        Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
        int counterSigned = 0;
        for (Workflow workflow : workflows) {
            Signature signature = signWorkflowRSA(workflow);
//            LOGGER.trace("Base64 MIME encoded: " + signature.getSignatureString());
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
            assertNotNull(jsObject.getSignedContent());
            counterSigned++;
            jsObjectsToExport.add(jsObject);
        }
        assertEquals(100, counterSigned);
        LOGGER.trace(String.format("%1$d workflows signed", counterSigned));
        LOGGER.trace("*************************       export signature to zip file ************************");
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.trace("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
        LOGGER.trace("Archive contains the workflows and their signatures.");
        LOGGER.trace("************************* End Test import workflows from zip file  ******************");
        LOGGER.trace("");
    }

    @Test
    public void test06ImportWorkflowsandSignaturesFromArchiveFile()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Set<ControllerObject> jsObjects = new HashSet<ControllerObject>();
        LOGGER.trace("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       import signatures ***********************************");
        Set<SignaturePath> signatures = importSignaturePaths();
        assertEquals(100, signatures.size());
        LOGGER.trace(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    jsObject.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.trace("mapping signatures to workflows was successful!");
        LOGGER.trace("*************************  verify signatures ****************************************");
        LOGGER.trace("*************************  verify signatures with extracted Public Key **************");
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
        LOGGER.trace(String.format("%1$d workflow signatures verified with extracted PublicKey successfully", countVerified));
        LOGGER.trace(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.trace("*************************  verify signatures with generated Certificate  ************");
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
        LOGGER.trace(String.format("%1$d workflow signatures verified with generated Certificate successfully", countVerified));
        LOGGER.trace(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.trace("************************* End Test import and verify ********************************");
        LOGGER.trace("");
    }

    @Test
    public void test07ImportWorkflowsFromSignAndUpdateArchiveFile()
            throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
        LOGGER.trace("");
        LOGGER.trace("**************************  Using RSA Keys and a generate X.509 Certificate  ********");
        LOGGER.trace("********************************  PKCS8  ********************************************");
        LOGGER.trace("");
        LOGGER.trace("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       sign Workflows **************************************");
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
        LOGGER.trace(String.format("%1$d workflows signed", counterSigned));
        LOGGER.trace("*************************       export signature to zip file ************************");
        exportWorkflows(jsObjectsToExport);
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve(TARGET_FILENAME)));
        LOGGER.trace("Archive bundle_js_workflows.zip succefully created in ./target/created_test_files!");
        LOGGER.trace("Archive contains the workflows and their signatures.");
        LOGGER.trace("************************* End Test import workflows from zip file  ******************");
        LOGGER.trace("");
    }

    @Test
    public void test08ImportWorkflowsandSignaturesFromArchiveFile()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Set<ControllerObject> jsObjects = new HashSet<ControllerObject>();
        LOGGER.trace("************************* import workflows/signatures from zip file and verify Test *");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows();
        assertEquals(100, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       import signatures ***********************************");
        Set<SignaturePath> signatures = importSignaturePaths();
        assertEquals(100, signatures.size());
        LOGGER.trace(String.format("%1$d signatures successfully imported from archive!", workflows.size()));
        for (Workflow workflow : workflows) {
            ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow);
            SignaturePath signaturePath = signatures.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath().equals(
                    jsObject.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
            jsObject.setSignedContent(signaturePath.getSignature().getSignatureString());
            jsObjects.add(jsObject);
        }
        assertEquals(100, jsObjects.size());
        LOGGER.trace("mapping signatures to workflows was successful!");
        LOGGER.trace("*************************  verify signatures ****************************************");
        LOGGER.trace("*************************  verify signatures with extracted Public Key **************");
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
        LOGGER.trace(String.format("%1$d workflow signatures verified with extracted PublicKey successfully", countVerified));
        LOGGER.trace(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.trace("*************************  verify signatures with generated Certificate  ************");
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
        LOGGER.trace(String.format("%1$d workflow signatures verified with generated Certificate successfully", countVerified));
        LOGGER.trace(String.format("%1$d workflow signature verifications failed", countNotVerified));
        LOGGER.trace("************************* End Test import and verify ********************************");
        LOGGER.trace("");
    }

    @Test
    public void test11ImportWorkflowsFromSignAndUpdateArchiveFile() 
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, PGPException {
        LOGGER.trace("*************************  import sign and update workflows from zip file Test ******");
        LOGGER.trace("*************************       import workflows ************************************");
        Set<Workflow> workflows = importWorkflows2();
        assertEquals(2, workflows.size());
        LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
        LOGGER.trace("*************************       sign Workflows **************************************");
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
        LOGGER.trace(String.format("%1$d workflows signed", counterSigned));
        LOGGER.trace("*************************       export signature to zip file ************************");
        exportWorkflows(jsObjectsToExport, "export_high_from_gui.zip", ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value());
        assertTrue(Files.exists(Paths.get("target").resolve("created_test_files").resolve("export_high_from_gui.zip")));
        LOGGER.trace("Archive export_high_from_gui.zip succefully created in ./target/created_test_files!");
        LOGGER.trace("Archive contains the workflows and their signatures.");
        LOGGER.trace("************************* End Test import workflows from zip file  ******************");
        LOGGER.trace("");
    }

    @Test
    public void test12ImportWorkflowsFromFolderSignAndUpdateArchiveFiles() 
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, PGPException {
        LOGGER.trace("*************************  import sign and update workflows from zip files Test *****");
        LOGGER.trace("*************************       import workflows ************************************");
        Path archivesPath = Paths.get("src/test/resources/import_deploy");
        Set<Path> filePaths = Files.list(archivesPath).collect(Collectors.toSet());
        Integer archivesToSignCount = filePaths.size();
        filePaths.stream().forEach(item -> {
            try {
                Set<Workflow> workflows = importWorkflows(item.toString().replace("\\", "/").replace("src/test/resources", ""));
                LOGGER.trace(String.format("%1$d workflows successfully imported from archive!", workflows.size()));
                LOGGER.trace("*************************       sign Workflows **************************************");
                Set<ControllerObject> jsObjectsToExport = new HashSet<ControllerObject>();
                int counterSigned = 0;
                for (Workflow workflow : workflows) {
                    Signature signature = signWorkflowECDSA(workflow);
                    ControllerObject jsObject = DeploymentTestUtils.createJsObjectForDeployment(workflow, signature);
                    assertNotNull(jsObject.getSignedContent());
                    counterSigned++;
                    jsObjectsToExport.add(jsObject);
                }
                LOGGER.trace(String.format("%1$d workflows signed", counterSigned));
                LOGGER.trace("*************************       export signature to zip file ************************");
                exportWorkflows(jsObjectsToExport, item.getFileName().toString(),
                        item.getParent().toString().replace("\\", "/").replace("src/test/resources/",""), 
                        ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value());
                LOGGER.trace(String.format("Archive %1$s succefully created in ./target/created_test_files/%2$s!",
                        item.getFileName().toString(), item.getParent().toString().replace("\\", "/").replace("src/test/resources/","")));
                LOGGER.trace("Archive contains the workflows and their signatures.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Set<Path> signedArchivesPaths = Files.list(Paths.get("target/created_test_files/import_deploy")).collect(Collectors.toSet());
        assertTrue(archivesToSignCount == signedArchivesPaths.size());
        LOGGER.trace(String.format("%1$d archives signed.", signedArchivesPaths.size()));
        LOGGER.trace("************************* End Test import workflows from zip file  ******************");
        LOGGER.trace("");
    }

    @Test
    @Ignore
    /* This is NO Unit test!
     * This is an integration Test with hibernate and a DB!
     * to run this test, adjust path to your hibernate configuration file
     * uncomment the Ignore annotation
     **/
    public void test13DetermineItemsForCleanup() throws SOSHibernateException {
        LOGGER.trace("******************************  Determine Items For Cleanup Test  *******************");
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
            LOGGER.trace("Object withName: " + entry.getKey());
            LOGGER.trace("has deployment count of: " + entry.getValue().size());
            if(entry.getValue().size() <= 5) {
                continue;
            } else {
                // Third: map by deployment date 
                Map<Date, List<DBItemDeploymentHistory>> mapPerDate = 
                        entry.getValue().stream().collect(Collectors.groupingBy(DBItemDeploymentHistory::getDeploymentDate));
                
                Set<Date> sortedDates = mapPerDate.keySet().stream().sorted(Comparator.comparing(Date::getTime)).collect(Collectors.toSet());
                sortedDates.stream().sorted(Comparator.naturalOrder()).forEach(item -> LOGGER.trace("Nat. Order" + item.toString()));
                sortedDates.stream().sorted(Comparator.reverseOrder()).forEach(item -> LOGGER.trace("Rev. Order" + item.toString()));
            }
        }
        LOGGER.trace("**************************** Determine Items For Cleanup Test finished **************");
    }
    
    @Ignore
    @Test
    public void test14CheckCertificateAgainstCa() {
        LOGGER.trace("******************************  Check against CA Test  ******************************");
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = new SOSHibernateFactory(Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/jetty_base/resources/joc/hibernate.cfg.xml"));
            factory.setAutoCommit(true);
            factory.addClassMapping(DBLayer.getJocClassMapping());
            factory.addClassMapping(DBLayer.getHistoryClassMapping());
            factory.build();
            session = factory.openStatelessSession();
            DBLayerKeys dbLayer = new DBLayerKeys(session);
            JocKeyPair signingKeyPair = dbLayer.getKeyPair("sp", JocSecurityLevel.MEDIUM);
            DBLayerDeploy deployDbLayer = new DBLayerDeploy(session);
            List<DBItemInventoryCertificate> caCerts = deployDbLayer.getCaCertificates();
            X509Certificate cert = KeyUtil.getX509Certificate(signingKeyPair.getCertificate());
            boolean valid = PublishUtils.verifyCertificateAgainstCAs(cert, caCerts);
            assertTrue(valid);
        } catch (SOSHibernateException | CertificateException | UnsupportedEncodingException e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            if(session != null) {
                session.close();
            }
            if(factory != null) {
                factory.close();
            }
        }
        LOGGER.trace("**************************** Check against CA Test finished *************************");
    }
    
//    @Ignore
    @Test
    public void test15CreateAndCheckCertificateAgainstCa() {
        LOGGER.trace("******************************  Check against CA Test  ******************************");
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = new SOSHibernateFactory(Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/jetty_base/resources/joc/hibernate.cfg.xml"));
            factory.setAutoCommit(true);
            factory.addClassMapping(DBLayer.getJocClassMapping());
            factory.addClassMapping(DBLayer.getHistoryClassMapping());
            factory.build();
            session = factory.openStatelessSession();
            DBLayerKeys dbLayer = new DBLayerKeys(session);
            
            JocKeyPair signingKeyPair = null;
            JocKeyPair clientServerKeyPair = null;
            RolloutResponse rolloutResponse = null;
            boolean rootCaAvailable = false;
            JocKeyPair rootKeyPair = dbLayer.getAuthRootCaKeyPair();
            X509Certificate rootCert = null;
            if (rootKeyPair != null) {
                rootCaAvailable = true;
                rootCert = KeyUtil.getX509Certificate(rootKeyPair.getCertificate());
            }
            String accountName = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());;
            
            if (rootCaAvailable && rootCert != null) {
                // first: get new PK and X509 certificate from stored CA
                String newDN = CAUtils.createUserSubjectDN("CN=" + accountName, rootCert);
//                String san = accountName;
                CreateCSRFilter csrFilter = new CreateCSRFilter();
                csrFilter.setDn(newDN);
//                csrFilter.setSan(san);
                Calendar calendar = Calendar.getInstance();
                calendar.set(2099, 0, 31);
                Date validUntil = calendar.getTime();
                signingKeyPair = SigningCertificateUtil.createSigningKeyPair(session, csrFilter, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, validUntil);
                rolloutResponse = ClientServerCertificateUtil.createClientServerAuthKeyPair(session, csrFilter);
                clientServerKeyPair = rolloutResponse.getJocKeyPair();
                X509Certificate rootCertFromResponse = KeyUtil.getX509Certificate(rolloutResponse.getCaCert());
                boolean rootCrtsMatch = false;
                if(rootCert.equals(rootCertFromResponse)) {
                    rootCrtsMatch = true;
                }
                boolean signingCertValid = false;
                X509Certificate cert = KeyUtil.getX509Certificate(signingKeyPair.getCertificate());
                try {
                    cert.verify(rootCert.getPublicKey());
                    signingCertValid = true;
                    LOGGER.trace("created signing cert valid!");
                } catch (Exception e) {
                    LOGGER.trace("created signing cert not valid!");
                    LOGGER.trace(e.getMessage());
                    // Do nothing if verification fails, as an exception here only indicates that the verification failed
                }
                cert = KeyUtil.getX509Certificate(clientServerKeyPair.getCertificate());
                try {
                    cert.verify(rootCert.getPublicKey());
                    signingCertValid = true;
                    LOGGER.trace("created auth cert valid!");
                } catch (Exception e) {
                    LOGGER.trace("created auth cert not valid!");
                    LOGGER.trace(e.getMessage());
                    // Do nothing if verification fails, as an exception here only indicates that the verification failed
                }
            }
        } catch (SOSHibernateException | CertificateException | IOException | InvalidNameException | NoSuchAlgorithmException
                | NoSuchProviderException | InvalidAlgorithmParameterException | OperatorCreationException | InvalidKeySpecException
                | SOSMissingDataException | CertException e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            if(session != null) {
                session.close();
            }
            if(factory != null) {
                factory.close();
            }
        }
        LOGGER.trace("**************************** Check against CA Test finished *************************");
    }
    
    @Ignore
    @Test
    public void test16ECDSACreateCSRAndUserCertificate() 
            throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException, IOException, CertException, 
            InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidAlgorithmParameterException {
        LOGGER.trace("****************************  Test ECDSA: create rootCertificate, CSR and userCertificate  ****");
        // read root CA KeyPair from database
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        KeyPair rootKeyPair = null;
        X509Certificate rootCertificate = null;
        PrivateKey privKey = null;
        PublicKey pubKey = null;
        try {
            factory = new SOSHibernateFactory(Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/jetty_base/resources/joc/hibernate.cfg.xml"));
            factory.setAutoCommit(true);
            factory.addClassMapping(DBLayer.getJocClassMapping());
            factory.addClassMapping(DBLayer.getHistoryClassMapping());
            factory.build();
            session = factory.openStatelessSession();
            DBLayerKeys dbLayer = new DBLayerKeys(session);
            JocKeyPair jocRootKeyPair = dbLayer.getAuthRootCaKeyPair();
            if (jocRootKeyPair != null) {
                if(jocRootKeyPair.getKeyAlgorithm().equals(SOSKeyConstants.ECDSA_ALGORITHM_NAME)) {
                    privKey = KeyUtil.getPrivateECDSAKeyFromString(jocRootKeyPair.getPrivateKey());
                    if (jocRootKeyPair.getPublicKey() != null) {
                        pubKey = KeyUtil.getECDSAPublicKeyFromString(jocRootKeyPair.getPublicKey());
                    }
                    rootKeyPair = KeyUtil.getKeyPairFromECDSAPrivatKeyString(jocRootKeyPair.getPrivateKey());
                } else {
                    privKey = KeyUtil.getPrivateRSAKeyFromString(jocRootKeyPair.getPrivateKey());
                    if (jocRootKeyPair.getPublicKey() != null) {
                        pubKey = KeyUtil.getRSAPublicKeyFromString(jocRootKeyPair.getPublicKey());
                    }
                    rootKeyPair = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocRootKeyPair.getPrivateKey());
                }
                rootCertificate = KeyUtil.getX509Certificate(jocRootKeyPair.getCertificate());
            }
        } catch (SOSHibernateException | CertificateException | IOException  e) {
            LOGGER.debug(e.getMessage(), e);
        } finally {
            if(session != null) {
                session.close();
            }
            if(factory != null) {
                factory.close();
            }
        }
        assertNotNull(rootKeyPair);
        assertNotNull(rootCertificate);
        
        String rootPrivateKeyString = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootKeyPair.getPrivate().getEncoded()),
                SOSKeyConstants.PRIVATE_EC_KEY_HEADER, SOSKeyConstants.PRIVATE_EC_KEY_FOOTER);
        LOGGER.trace("root private key - algorithm: " + rootKeyPair.getPrivate().getAlgorithm());
        LOGGER.trace("root private key - format: " + rootKeyPair.getPrivate().getFormat());
        LOGGER.trace("\n" + rootPrivateKeyString);
        String rootCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(rootCertificate.getEncoded()), 
                SOSKeyConstants.CERTIFICATE_HEADER, SOSKeyConstants.CERTIFICATE_FOOTER);
        try {
            rootCertificate.verify(rootKeyPair.getPublic());
            LOGGER.trace("root certificate was successfully verified.");
            LOGGER.trace("\nCertificate cerdentials :\n" + ((X509Certificate)rootCertificate).toString());
            List<String> usages = ((X509Certificate)rootCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.trace("Usage: " + usage);
                } 
            }
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            // CertificateException on encoding errors
            // NoSuchAlgorithmException on unsupported signature algorithms
            // InvalidKeyException on incorrect key
            // NoSuchProviderException if there's no default provider
            // SignatureException on signature errors
            LOGGER.trace("root certificate verification failed against CA keyPairs public key.");
            LOGGER.trace(e.getMessage());
        }
        // create a user KeyPair
        KeyPair userKeyPair = KeyUtil.createECDSAKeyPair();
        String userPrivateKeyString = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userKeyPair.getPrivate().getEncoded()),
                SOSKeyConstants.PRIVATE_EC_KEY_HEADER, SOSKeyConstants.PRIVATE_EC_KEY_FOOTER);
        String userSubjectDN = CAUtils.createUserSubjectDN("SOS root CA", "SP", "www.sos-berlin.com", "SOS GmbH", "Berlin", "Berlin", "DE"); 
        LOGGER.trace("user subjectDN: " + userSubjectDN);
        // create a CSR based on the users KeyPair
        PKCS10CertificationRequest csr = CAUtils.createCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, userKeyPair, rootKeyPair, userSubjectDN);
        assertNotNull(csr);
        String csrAsString= KeyUtil.insertLineFeedsInEncodedString(DatatypeConverter.printBase64Binary(csr.getEncoded()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2099, 0, 31);
        Date validUntil = calendar.getTime();

        X509Certificate userCertificate = 
                CAUtils.signCSR(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, rootKeyPair.getPrivate(), userKeyPair, csr, 
                        (X509Certificate)rootCertificate, null, validUntil);
        assertNotNull(userCertificate);
        String userCert = KeyUtil.formatEncodedDataString(DatatypeConverter.printBase64Binary(userCertificate.getEncoded()), 
                SOSKeyConstants.CERTIFICATE_HEADER, SOSKeyConstants.CERTIFICATE_FOOTER);
        try {
            LOGGER.trace("****************************  Verify user Certificate:  ***************************************");
            userCertificate.verify(rootCertificate.getPublicKey());
            LOGGER.trace("user certificate was successfully verified against CA certificates public key.");
            LOGGER.trace("\nUser certificate credentials:\n" + userCertificate.toString());
            List<String> usages = ((X509Certificate)userCertificate).getExtendedKeyUsage();
            if (usages != null) {
                for (String usage : usages) {
                    LOGGER.trace("Usage: " + usage);
                } 
            }
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            // CertificateException on encoding errors
            // NoSuchAlgorithmException on unsupported signature algorithms
            // InvalidKeyException on incorrect key
            // NoSuchProviderException if there's no default provider
            // SignatureException on signature errors
            LOGGER.trace("user certificate verification failed against CA certificates public key.");
            LOGGER.trace(e.getMessage());
        }
        LOGGER.trace("**************  check if PublicKey from KeyPair and Public Key from user certificate are the same  ****");
        if (userKeyPair.getPublic().equals(userCertificate.getPublicKey())) {
            LOGGER.trace("Users PublicKey from Key Pair and Public Key from user certificate are the same!");
        } else {
            LOGGER.trace("Users PublicKey from Key Pair and Public Key from user certificate are not the same!");
        }
        String testStringToSign = "Test String to Sign";
        LOGGER.trace("************************************  Sign String with users Private Key:******************************");
        String signature = SignObject.signX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, userKeyPair.getPrivate(), testStringToSign);
        LOGGER.trace("************************************  Signature:  *****************************************************");
        LOGGER.trace("\n" + signature);
        LOGGER.trace("****************************  Signature verification with user certificate:  ******************");
        boolean verify = VerifySignature.verifyX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, userCertificate, testStringToSign, signature);
        LOGGER.trace("Signature verification with method \"VerifySignature.verifyX509BC\" successful: " + verify);
        assertTrue(verify);
        LOGGER.trace("***************************  Test create rootCertificate, CSR and userCertificate finished ***");
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
            LOGGER.trace(String.format("folder \"%1$s\" created.", targetFolderPath.toString()));
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
//            LOGGER.trace("subfolder \"created_test_files\" created in target folder.");
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
        LOGGER.trace("archive to read from exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME)));
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
        LOGGER.trace("archive to read from exists: " + Files.exists(Paths.get("src/test/resources/import_deploy").resolve("export_high_from_gui.zip")));
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
        LOGGER.trace("archive to read from exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME_SINGLE)));
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
        LOGGER.trace("archive to read exists: " + Files.exists(Paths.get("target/created_test_files").resolve(TARGET_FILENAME)));
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
