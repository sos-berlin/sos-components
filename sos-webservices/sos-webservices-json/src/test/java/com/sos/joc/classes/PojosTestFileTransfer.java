package com.sos.joc.classes;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.inventory.model.filetransfer.AFileTransfer;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.inventory.model.filetransfer.common.PostProcessing;
import com.sos.inventory.model.filetransfer.common.Processing;
import com.sos.inventory.model.filetransfer.fragments.cs.CredentialStoreAuthKeyFile;
import com.sos.inventory.model.filetransfer.fragments.cs.CredentialStoreAuthentication;
import com.sos.inventory.model.filetransfer.fragments.cs.CredentialStoreFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.FtpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.UrlConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl.Ssl;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl.Trusted;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpOptions;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpsOptions;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthPublicKey;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthentication;
import com.sos.inventory.model.filetransfer.profile.Profile;
import com.sos.inventory.model.filetransfer.profile.ProfileBase.Operation;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.inventory.model.filetransfer.profile.fragments.Local;
import com.sos.inventory.model.filetransfer.profile.fragments.SftpFragmentRef;
import com.sos.inventory.model.filetransfer.profile.source.FileSpecSelection;
import com.sos.inventory.model.filetransfer.profile.source.Selection;
import com.sos.inventory.model.filetransfer.profile.source.SourceOptions;
import com.sos.inventory.model.filetransfer.profile.target.TargetOptions;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.schema.JsonValidator;

public class PojosTestFileTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojosTestFileTransfer.class);

    private static final String PATH_RAML = "raml/inventory/schemas/filetransfer";
    private static final String PATH_INVENTORY_SCHEMA_RESOURCES = "../../../../schemas/js7-inventory-schemas/src/main/resources";

    private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    @Ignore
    @Test
    public void validateCredentialStoreFragmentTest() throws Exception {
        try {
            CredentialStoreFragment f = new CredentialStoreFragment();
            f.setFile("my_file");
            CredentialStoreAuthentication a = new CredentialStoreAuthentication();

            CredentialStoreAuthKeyFile kf = new CredentialStoreAuthKeyFile();
            kf.setFile("key_file");
            a.setKeyFile(kf);
            f.setAuthentication(a);

            LOGGER.info(objectMapper.writeValueAsString(f));

            URI uri = createFromClasspath("fragments/cs/credentialStoreFragment-schema.json");
            JsonValidator.validate(objectMapper.writeValueAsBytes(f), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void validateFtpFragmentTest() throws Exception {
        try {
            FtpFragment f = new FtpFragment();
            BasicConnection bc = new BasicConnection();
            bc.setHostname("ftp_host");
            f.setConnection(bc);
            BasicAuthentication ba = new BasicAuthentication();
            ba.setAccount("my_user");
            f.setAuthentication(ba);

            FtpOptions ftp = new FtpOptions();
            f.setFtp(ftp);

            FtpsOptions ftps = new FtpsOptions();
            Ssl ssl = new Ssl();
            Trusted t = new Trusted();
            ssl.setTrusted(t);
            ftps.setSsl(ssl);
            // f.setFtps(ftps);

            f.setAlternativeFragmentName("my alternative fragment");

            f.setSocksProxyFragmentName("my socks proxy ref");

            LOGGER.info(objectMapper.writeValueAsString(f));

            LOGGER.info(Paths.get("").toAbsolutePath().toString());

            URI uri = createFromClasspath("fragments/protocol/ftpFragment-schema.json");
            // uri = createFromInventorySchemaResources("fragments/protocol/ftpFragment-schema.json");

            JsonValidator.validate(objectMapper.writeValueAsBytes(f), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void validateHttpFragmentTest() throws Exception {
        try {
            HttpFragment f = new HttpFragment();
            UrlConnection uc = new UrlConnection();
            uc.setUrl("http://test.de");
            f.setConnection(uc);

            BasicAuthentication ba = new BasicAuthentication();
            ba.setAccount("my_user");
            f.setAuthentication(ba);

            Ssl ssl = new Ssl();
            Trusted t = new Trusted();
            ssl.setTrusted(t);
            f.setSsl(ssl);

            f.setAlternativeFragmentName("my alternative fragment");

            LOGGER.info(objectMapper.writeValueAsString(f));

            URI uri = createFromClasspath("fragments/protocol/httpFragment-schema.json");
            // uri = createFromInventorySchemaResources("fragments/protocol/httpFragment-schema.json");

            JsonValidator.validate(objectMapper.writeValueAsBytes(f), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void validateSftpFragmentTest() throws Exception {
        try {
            SftpFragment f = new SftpFragment();
            BasicConnection bc = new BasicConnection();
            bc.setHostname("sftp_host");
            f.setConnection(bc);
            SftpAuthentication sa = new SftpAuthentication();
            sa.setAccount("my_user");
            SftpAuthPublicKey pk = new SftpAuthPublicKey("pk.file", null);
            sa.setPublicKey(pk);
            f.setAuthentication(sa);

            f.setAlternativeFragmentName("my alternative fragment");

            f.setCredentialStoreFragmentName("cs name");
            f.setDecryptionFragmentName("decryption name");
            f.setJumpFragmentName("jump name");
            f.setSocksProxyFragmentName("my proxy socks"); // only one is allowed: socks or http
            // f.setHttpProxyFragmentName("my proxy http");

            LOGGER.info(objectMapper.writeValueAsString(f));

            URI uri = createFromClasspath("fragments/protocol/sftpFragment-schema.json");
            uri = createFromInventorySchemaResources("fragments/protocol/sftpFragment-schema.json");
            JsonValidator.validate(objectMapper.writeValueAsBytes(f), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void validateProfileTestGetlist() throws Exception {
        try {
            Profile p = new Profile();
            p.setOperation(Operation.GETLIST);

            p.setAlternativeProfileName("alternative ref");

            Source s = new Source();
            Fragment ref = new Fragment();

            Local l = new Local();
            Processing pg = new Processing();
            PostProcessing pp = new PostProcessing();
            pp.setCommandAfterFile("command after file");
            pg.setPostProcessing(pp);
            l.setProcessing(pg);
            ref.setLocal(l);

            s.setFragment(ref);

            Selection sel = new Selection();
            FileSpecSelection sfs = new FileSpecSelection();
            sfs.setFileSpec(".*");
            sel.setFileSpec(sfs);
            s.setSelection(sel);

            SourceOptions so = new SourceOptions();
            s.setOptions(so);
            p.setSource(s);

            // csf.setEntryPath("my_entry path");
            // csf.setSubType(FileTransferSubType.CREDENTIAL_STORE);

            LOGGER.info(objectMapper.writeValueAsString(p));

            URI uri = createFromClasspath("profile/profile-schema.json");
            uri = createFromInventorySchemaResources("profile/profile-schema.json");

            JsonValidator.validate(objectMapper.writeValueAsBytes(p), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void validateProfileTestCopy() throws Exception {
        try {
            Profile p = new Profile();
            p.setOperation(Operation.COPY);

            p.setAlternativeProfileName("alternative ref");

            Source s = new Source();
            Fragment ref = new Fragment();

            Local l = new Local();
            Processing pg = new Processing();
            PostProcessing pp = new PostProcessing();
            pp.setCommandAfterFile("command after file");
            pg.setPostProcessing(pp);
            l.setProcessing(pg);
            ref.setLocal(l);

            s.setFragment(ref);

            Selection sel = new Selection();
            FileSpecSelection sfs = new FileSpecSelection();
            sfs.setFileSpec(".*");
            sel.setFileSpec(sfs);
            s.setSelection(sel);

            SourceOptions so = new SourceOptions();
            s.setOptions(so);
            p.setSource(s);

            Target t = new Target();
            t.setDirectory("my target dir");
            TargetOptions to = new TargetOptions();
            to.setAppendFiles("true");
            t.setOptions(to);

            SftpFragmentRef sftp = new SftpFragmentRef();
            sftp.setName("my sftp");
            ref = new Fragment();
            ref.setSftp(sftp);
            t.setFragment(ref);

            p.setTarget(t);

            FileTransferJobResource jr = new FileTransferJobResource();
            jr.setProfile(p);

            LOGGER.info(objectMapper.writeValueAsString(jr));

            URI uri = createFromClasspath("profile/profile-schema.json");
            uri = createFromInventorySchemaResources("filetransferJobResource-schema.json");

            JsonValidator.validate(objectMapper.writeValueAsBytes(jr), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void readInventoryRequestTest() {
        try {
            executeReadInventoryRequest("cs-fragment.json", CredentialStoreFragment.class);

            LOGGER.info("");
            executeReadInventoryRequest("sftp-fragment.json", SftpFragment.class);

            LOGGER.info("");
            executeReadInventoryRequest("profile.json", Profile.class);

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void executeReadInventoryRequest(String fileName, T clazz) {
        LOGGER.info(fileName + " ---------------------");
        try {
            String json = Files.readString(Paths.get("src/test/resources/filetransfer/" + fileName).toAbsolutePath());
            JsonValidator.validate(json.getBytes(StandardCharsets.UTF_8), ConfigurationObject.class);
            ConfigurationObject request = objectMapper.readValue(json, ConfigurationObject.class);

            AFileTransfer ft = (AFileTransfer) request.getConfiguration();
            LOGGER.info("  [FILETRANSFER]" + objectMapper.writeValueAsString(ft));
            LOGGER.info("  [FILETRANSFER] " + request.getObjectType());

            T o = (T) request.getConfiguration();
            LOGGER.info("  [" + o.getClass().getSimpleName() + "] " + objectMapper.writeValueAsString(o));

        } catch (Exception e) {
            LOGGER.error("[" + fileName + "]" + e.toString(), e);
        }
    }

    private URI createFromClasspath(String jsonPath) {
        return URI.create("classpath:/" + PATH_RAML + "/" + jsonPath);
    }

    private URI createFromInventorySchemaResources(String jsonPath) throws Exception {
        Path p = Paths.get(PATH_INVENTORY_SCHEMA_RESOURCES + "/" + PATH_RAML + "/" + jsonPath).toAbsolutePath().normalize();
        if (!Files.exists(p)) {
            throw new Exception("[createFromInventorySchemaResources][" + p + "]not found");
        }
        return URI.create("file:/" + p.toString().replace('\\', '/'));
    }

}