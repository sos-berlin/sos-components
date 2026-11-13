package com.sos.joc.inventory.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.filetransfer.common.FragmentRefTypeForProtocolFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.SftpFragment;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.SftpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthPublicKey;
import com.sos.inventory.model.filetransfer.fragments.protocol.sftp.SftpAuthentication;
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
    public void testPostStoreFileTransfer() throws Exception {
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

        SftpAlternativeFragmentRef af = new SftpAlternativeFragmentRef();
        af.setType(FragmentRefTypeForProtocolFragment.SFTP);
        af.setRef("my alternative fragment");
        f.setAlternativeRef(af);

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
}
