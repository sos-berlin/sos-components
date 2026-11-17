package com.sos.joc.inventory.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.filetransfer.common.PostProcessing;
import com.sos.inventory.model.filetransfer.common.Processing;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthPublicKey;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthentication;
import com.sos.inventory.model.filetransfer.profile.Profile;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.ProfileBase.Operation;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.inventory.model.filetransfer.profile.fragments.Local;
import com.sos.inventory.model.filetransfer.profile.fragments.SftpFragmentRef;
import com.sos.inventory.model.filetransfer.profile.source.FileSpecSelection;
import com.sos.inventory.model.filetransfer.profile.source.Selection;
import com.sos.inventory.model.filetransfer.profile.source.SourceFileOptions;
import com.sos.inventory.model.filetransfer.profile.target.TargetFileOptions;
import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class StoreConfigurationResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreConfigurationResourceImplTest.class);

    @Ignore
    @Test
    public void testPostStoreFolder() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/A");
        in.setObjectType(ConfigurationType.FOLDER);

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
    public void testPostStoreWorkflow() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new StoreConfigurationResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/a");
        in.setObjectType(ConfigurationType.WORKFLOW);

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
    public void testPostStoreFileTransferSftpFragment() throws Exception {
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

        f.setAlternativeSftpFragmentName("my alternative fragment");

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/b");
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
    public void testPostStoreFileTransferProfile() throws Exception {
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

        SourceFileOptions so = new SourceFileOptions();
        Selection sel = new Selection();
        FileSpecSelection sfs = new FileSpecSelection();
        sfs.setFileSpec(".*");
        sel.setFileSpec(sfs);
        so.setSelection(sel);
        s.setSourceFileOptions(so);
        p.setSource(s);

        Target t = new Target();
        t.setDirectory("my target dir");
        TargetFileOptions tfo = new TargetFileOptions();
        tfo.setAppendFiles("true");
        t.setTargetFileOptions(tfo);

        SftpFragmentRef sftp = new SftpFragmentRef();
        sftp.setName("my sftp");
        ref = new Fragment();
        ref.setSftp(sftp);
        t.setFragment(ref);

        p.setTarget(t);

        ConfigurationObject in = new ConfigurationObject();
        in.setPath("/b");
        in.setConfiguration(p);
        in.setObjectType(ConfigurationType.FT_PROFILE);

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
