package com.sos.joc.classes;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.inventory.model.filetransfer.AFileTransfer;
import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProfile;
import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProtocolFragment;
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
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.FtpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.HttpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefAny;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefAny.Type;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.SftpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthPublicKey;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthentication;
import com.sos.inventory.model.filetransfer.profile.Profile;
import com.sos.inventory.model.filetransfer.profile.ProfileBase.Operation;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.refs.FragmentRef;
import com.sos.inventory.model.filetransfer.profile.refs.Local;
import com.sos.inventory.model.filetransfer.profile.source.FileSpecSelection;
import com.sos.inventory.model.filetransfer.profile.source.Selection;
import com.sos.inventory.model.filetransfer.profile.source.SourceFileOptions;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.schema.JsonValidator;

public class PojosTestFileTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojosTestFileTransfer.class);

    private static final String PATH_RAML = "raml/inventory/schemas/filetransfer";
    private static final String PATH_INVENTORY_SCHEMA_RESOURCES = "/dev/JS7/2.x/schemas/js7-inventory-schemas/src/main/resources";

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

            FtpAlternativeFragmentRef af = new FtpAlternativeFragmentRef();
            af.setType(FragmentRefTypeForProtocolFragment.FTP);
            af.setRef("my alternative fragment");
            f.setAlternativeRef(af);

            ProxyFragmentRefAny ph = new ProxyFragmentRefAny();
            ph.setType(Type.HTTP);
            ph.setRef("my proxy ref");
            f.setProxyRef(ph);

            LOGGER.info(objectMapper.writeValueAsString(f));

            LOGGER.info(Paths.get("").toAbsolutePath().toString());

            URI uri = createFromClasspath("fragments/protocol/ftpFragment-schema.json");
            uri = createFromInventorySchemaResources("fragments/protocol/ftpFragment-schema.json");

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

            HttpAlternativeFragmentRef af = new HttpAlternativeFragmentRef();
            af.setType(FragmentRefTypeForProtocolFragment.HTTP);
            af.setRef("my alternative fragment");
            f.setAlternativeRef(af);

            LOGGER.info(objectMapper.writeValueAsString(f));

            URI uri = createFromClasspath("fragments/protocol/httpFragment-schema.json");
            uri = createFromInventorySchemaResources("fragments/protocol/httpFragment-schema.json");

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

            SftpAlternativeFragmentRef af = new SftpAlternativeFragmentRef();
            af.setType(FragmentRefTypeForProtocolFragment.SFTP);
            af.setRef("my alternative fragment");
            f.setAlternativeRef(af);

            LOGGER.info(objectMapper.writeValueAsString(f));

            URI uri = createFromClasspath("fragments/protocol/sftpFragment-schema.json");
            JsonValidator.validate(objectMapper.writeValueAsBytes(f), uri, true);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void validateProfileTest() throws Exception {
        try {
            Profile p = new Profile();
            p.setOperation(Operation.GETLIST);

            p.setAlternativeRef("alternative ref");

            Source s = new Source();
            FragmentRef ref = new FragmentRef();

            Local l = new Local();
            l.setCredentialStoreRef("cs ref");
            // ref.setLocal(l);
            // ref.setType(FragmentRefTypeForProfile_schema.LOCAL);

            ref.setType(FragmentRefTypeForProfile.SFTP);
            ref.setRef("my sftp ref");

            s.setFragmentRef(ref);
            SourceFileOptions so = new SourceFileOptions();
            Selection sel = new Selection();
            FileSpecSelection sfs = new FileSpecSelection();
            sfs.setFileSpec(".*");
            sel.setFileSpec(sfs);
            so.setSelection(sel);
            s.setSourceFileOptions(so);
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

    private URI createFromInventorySchemaResources(String jsonPath) {
        return URI.create("file:/" + PATH_INVENTORY_SCHEMA_RESOURCES + "/" + PATH_RAML + "/" + jsonPath);
    }

}