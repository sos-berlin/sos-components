package com.sos.joc.inventory.impl;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.inventory.model.filetransfer.common.PostProcessing;
import com.sos.inventory.model.filetransfer.common.Processing;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
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
import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FileTransferRelatedResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferRelatedResourceImplTest.class);

    @Ignore
    @Test
    public void testStoreSftpFragment1() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        SftpFragment f = new SftpFragment();
        BasicConnection bc = new BasicConnection();
        bc.setHostname("sftp_host");
        f.setConnection(bc);
        SftpAuthentication sa = new SftpAuthentication();
        sa.setAccount("my_user");
        SftpAuthPublicKey pk = new SftpAuthPublicKey("pk.file", null);
        sa.setPublicKey(pk);
        f.setAuthentication(sa);

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/filetransfer/my_sftp_fragment_1");
        in.setConfiguration(f);
        in.setObjectType(ConfigurationType.FT_FRAGMENT_SFTP);

        try {
            h.init();

            h.post("store", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testStoreSftpFragment2UsingAlternative() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        SftpFragment f = new SftpFragment();
        BasicConnection bc = new BasicConnection();
        bc.setHostname("sftp_host");
        f.setConnection(bc);
        SftpAuthentication sa = new SftpAuthentication();
        sa.setAccount("my_user");
        SftpAuthPublicKey pk = new SftpAuthPublicKey("pk.file", null);
        sa.setPublicKey(pk);
        f.setAuthentication(sa);

        f.setAlternativeSftpFragmentName("my_sftp_fragment_1");

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/filetransfer/my_sftp_fragment_2");
        in.setConfiguration(f);
        in.setObjectType(ConfigurationType.FT_FRAGMENT_SFTP);

        try {
            h.init();

            h.post("store", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testReleaseSftpFragment() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new ReleaseResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("release", Paths.get("src/test/resources/ws/inventory/impl/filetransfer/request-ReleaseResourceImpl-ft-fragment.json"));
            TimeUnit.SECONDS.sleep(10);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testStoreWorkflow() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("store", Paths.get("src/test/resources/ws/inventory/impl/filetransfer/request-StoreResourceImpl-workflow.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testStoreFileOrderSource() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("store", Paths.get("src/test/resources/ws/inventory/impl/filetransfer/request-StoreResourceImpl-fos.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testRenameWorkflow() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new RenameConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        com.sos.joc.model.inventory.rename.RequestFilter f = new com.sos.joc.model.inventory.rename.RequestFilter();
        f.setNewPath("/filetransfer/ft_w");
        f.setObjectType(ConfigurationType.WORKFLOW);
        f.setPath("/filetransfer/ft_wR");

        try {
            h.init();

            h.post("rename", f);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testReadFolder() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new FolderResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        try {
            h.init();

            h.post("readFolder", Paths.get("src/test/resources/ws/inventory/impl/filetransfer/request-FolderResourceImpl.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testRenameSftpFragment() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new RenameConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        com.sos.joc.model.inventory.rename.RequestFilter f = new com.sos.joc.model.inventory.rename.RequestFilter();
        f.setNewPath("/filetransfer/mySftp");
        f.setObjectType(ConfigurationType.FT_FRAGMENT_SFTP);
        f.setPath("/filetransfer/mySftpR");

        try {
            h.init();

            h.post("rename", f);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testRenameProfile() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new RenameConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        com.sos.joc.model.inventory.rename.RequestFilter f = new com.sos.joc.model.inventory.rename.RequestFilter();
        f.setNewPath("/filetransfer/ft_jr_profile");
        f.setObjectType(ConfigurationType.FT_PROFILE_JOBRESOURCE);
        f.setPath("/b");

        try {
            h.init();

            h.post("rename", f);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testStoreProfile() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        Profile p = new Profile();
        p.setOperation(Operation.COPY);

        // p.setAlternativeProfileName("alternative ref");

        Source s = new Source();
        Fragment ref = new Fragment();

        Local l = new Local();
        // l.setCredentialStoreFragmentName("cs ref");
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
        sftp.setName("mySftp");
        ref = new Fragment();
        ref.setSftp(sftp);
        t.setFragment(ref);

        p.setTarget(t);

        FileTransferJobResource jr = new FileTransferJobResource();
        jr.setProfile(p);

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/filetransfer/ft_jr_profile");
        in.setConfiguration(jr);
        in.setObjectType(ConfigurationType.FT_PROFILE_JOBRESOURCE);

        try {
            h.init();

            h.post("store", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
