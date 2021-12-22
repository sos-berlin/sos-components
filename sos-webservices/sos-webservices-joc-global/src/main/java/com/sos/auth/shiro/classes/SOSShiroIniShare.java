package com.sos.auth.shiro.classes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.exceptions.JocException;

public class SOSShiroIniShare {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroIniShare.class);
    private String iniFileName;
    private SOSHibernateSession sosHibernateSession;

    public SOSShiroIniShare(SOSHibernateSession sosHibernateSession) throws JocException {
        super();
        this.sosHibernateSession = sosHibernateSession;
    }

    public void provideIniFile() throws SOSHibernateException, JocException, IOException {
        iniFileName = Globals.getShiroIniInClassPath();
        if (!iniFileName.startsWith("file:")) {
            LOGGER.warn("can not provide shiro.ini file from filesystem");
        } else {
            iniFileName = iniFileName.replaceFirst("^file:", "");
        }

        String iniFileNameActive = Globals.getIniFileForShiro(iniFileName);
        File iniFileActive = new File(iniFileNameActive);

        checkForceFile();
        String inifileContent = getContentFromDatabase();
        if (inifileContent.isEmpty()) {
            File forceFile = new File(iniFileName);
            forceFile.delete();
            iniFileActive.renameTo(forceFile);
            checkForceFile();
            inifileContent = getContentFromDatabase();
        }

        createShiroIniFileFromDb(inifileContent, iniFileActive);

    }

    private void checkForceFile()
            throws SOSHibernateException, JocException, UnsupportedEncodingException, IOException {
        File forceFile = new File(iniFileName);

        double fileSize = forceFile.length() / 1024d;
        fileSize = fileSize / 1024d;

        if (forceFile.exists()) {
            if (fileSize < 1 && fileSize > 0) {
                LOGGER.debug(forceFile.toString() + " found. Will be moved to database");
                copyFileToDb(forceFile);
                forceFile.delete();
                File iniFile = new File(Globals.getIniFileForShiro(iniFileName));
                File destinationFile = new File(iniFileName + ".backup");
                destinationFile.delete();
                iniFile.renameTo(destinationFile);
            } else {
                LOGGER.info(forceFile.toString() + " found. Will be ignored and deleted due to an invalid filesize");
                forceFile.delete();
            }
        }
    }


    public void copyFileToDb(File iniFile) throws SOSHibernateException, JocException, UnsupportedEncodingException, IOException {
        try {
            Globals.beginTransaction(sosHibernateSession);

            DBItemJocConfiguration jocConfigurationDbItem;
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();

            filter.setAccount(".");
            filter.setConfigurationType("SHIRO");
            List<DBItemJocConfiguration> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurationList(filter,0);
            if (listOfConfigurtions.size() > 0) {
                jocConfigurationDbItem = listOfConfigurtions.get(0);
            } else {
                jocConfigurationDbItem = new DBItemJocConfiguration();
                jocConfigurationDbItem.setId(null);
                jocConfigurationDbItem.setAccount(".");
                jocConfigurationDbItem.setConfigurationType("SHIRO");
                jocConfigurationDbItem.setName("shiro.ini");
                jocConfigurationDbItem.setShared(true);
                jocConfigurationDbItem.setInstanceId(0L);
                jocConfigurationDbItem.setControllerId("");
            }

            String content = new String(Files.readAllBytes(Paths.get(iniFile.getAbsolutePath())), "UTF-8");

            jocConfigurationDbItem.setConfigurationItem(content);
            Long id = jocConfigurationDBLayer.saveOrUpdateConfiguration(jocConfigurationDbItem);
            if (jocConfigurationDbItem.getId() == null) {
                jocConfigurationDbItem.setId(id);
            }
            Globals.commit(sosHibernateSession);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            throw e;
        }
    }

    private void createShiroIniFileFromDb(String inifileContent, File iniFileActive) throws IOException {
        String contentIniFileActive = "*nothing";
        if (iniFileActive.exists()) {
            contentIniFileActive = new String(Files.readAllBytes(Paths.get(iniFileActive.getAbsolutePath())), "UTF-8");
        }

        if (!inifileContent.equals(contentIniFileActive)) {
            LOGGER.debug (iniFileActive.toString() + " content changed. Will be updated from database");
            byte[] bytes = inifileContent.getBytes(StandardCharsets.UTF_8);
            Files.write(Paths.get(Globals.getIniFileForShiro(iniFileName)), bytes, java.nio.file.StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }

    }

    private String getContentFromDatabase() throws SOSHibernateException {
        try {
            Globals.beginTransaction(sosHibernateSession);

            DBItemJocConfiguration jocConfigurationDbItem;
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();

            filter.setAccount(".");
            filter.setConfigurationType("SHIRO");
            List<DBItemJocConfiguration> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurationList(filter,0);
            Globals.commit(sosHibernateSession);

            if (listOfConfigurtions.size() > 0) {
                jocConfigurationDbItem = listOfConfigurtions.get(0);
                return jocConfigurationDbItem.getConfigurationItem();
            } else {
                return "";
            }
        } catch (SOSHibernateException e) {
            Globals.rollback(sosHibernateSession);
            throw e;
        }

    }
}
