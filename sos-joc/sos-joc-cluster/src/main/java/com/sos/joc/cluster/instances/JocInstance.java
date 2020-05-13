package com.sos.joc.cluster.instances;

import java.net.UnknownHostException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSShell;
import com.sos.jobscheduler.db.cluster.DBItemJocInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.cluster.db.DBLayerCluster;

public class JocInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInstance.class);

    private final SOSHibernateFactory dbFactory;
    private final String dataDirectory;
    private final String timezone;
    private final Date startTime;
    private String hostname;

    public JocInstance(SOSHibernateFactory factory, String jocDataDirectory, String jocTimezone, Date jocStartTime) {
        dbFactory = factory;
        dataDirectory = jocDataDirectory;
        timezone = jocTimezone;
        startTime = jocStartTime;

        try {
            hostname = SOSShell.getHostname();
        } catch (UnknownHostException e) {
            hostname = "unknown";
            LOGGER.error(e.toString(), e);
        }
    }

    public DBItemJocInstance onStart() throws Exception {
        DBLayerCluster dbLayer = null;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());

            dbLayer.getSession().beginTransaction();
            DBItemOperatingSystem osItem = getOS(dbLayer);
            DBItemJocInstance item = dbLayer.getInstance(getMemberId());
            if (item == null) {
                item = new DBItemJocInstance();
                item.setMemberId(getMemberId());
                item.setOsId(osItem.getId());
                item.setDataDirectory(dataDirectory);
                item.setStartedAt(startTime);
                item.setTimezone(timezone);
                item.setHeartBeat(new Date());
                dbLayer.getSession().save(item);
            } else {
                item.setStartedAt(startTime);
                item.setTimezone(timezone);
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
            DBItemOperatingSystem item = dbLayer.getOS(hostname);
            if (item == null) {
                item = new DBItemOperatingSystem();
                item.setHostname(hostname);
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

    public String getMemberId() {
        return hostname + ":" + dataDirectory;
    }

}
