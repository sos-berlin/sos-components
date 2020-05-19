package com.sos.joc.cluster.instances;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSShell;
import com.sos.jobscheduler.db.cluster.DBItemJocInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.db.DBLayerCluster;

public class JocInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInstance.class);

    private final SOSHibernateFactory dbFactory;
    private final JocConfiguration config;
    private final Date startTime;

    public JocInstance(SOSHibernateFactory factory, JocConfiguration jocConfig, Date jocStartTime) {
        dbFactory = factory;
        config = jocConfig;
        startTime = jocStartTime;
    }

    public DBItemJocInstance onStart() throws Exception {
        DBLayerCluster dbLayer = null;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());

            dbLayer.getSession().beginTransaction();
            DBItemOperatingSystem osItem = getOS(dbLayer);
            DBItemJocInstance item = dbLayer.getInstance(config.getMemberId());
            if (item == null) {
                item = new DBItemJocInstance();
                item.setMemberId(config.getMemberId());
                item.setOsId(osItem.getId());
                item.setDataDirectory(config.getDataDirectory().toString());
                item.setSecurityLevel(config.getSecurityLevel());
                item.setStartedAt(startTime);
                item.setTimezone(config.getTimezone());
                item.setTitle(config.getTitle());
                item.setHeartBeat(new Date());
                dbLayer.getSession().save(item);
            } else {
                item.setSecurityLevel(config.getSecurityLevel());
                item.setStartedAt(startTime);
                item.setTimezone(config.getTimezone());
                item.setTitle(config.getTitle());
                item.setHeartBeat(new Date());
                dbLayer.getSession().update(item);
            }
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.getSession().close();
            }
        }
    }

    private DBItemOperatingSystem getOS(DBLayerCluster dbLayer) throws Exception {
        try {
            DBItemOperatingSystem item = dbLayer.getOS(config.getHostname());
            if (item == null) {
                item = new DBItemOperatingSystem();
                item.setHostname(config.getHostname());
                item.setName(SOSShell.OS_NAME);
                item.setArchitecture(SOSShell.OS_ARCHITECTURE);
                item.setDistribution(SOSShell.OS_VERSION);
                item.setModified(new Date());
                dbLayer.getSession().save(item);
            }
            return item;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw e;
        }
    }

}
