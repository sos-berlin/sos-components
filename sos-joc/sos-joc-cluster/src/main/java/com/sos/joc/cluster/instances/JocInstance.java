package com.sos.joc.cluster.instances;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSShell;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.db.DBLayerJocCluster;

public class JocInstance {

    private final SOSHibernateFactory dbFactory;
    private final JocConfiguration config;

    public JocInstance(SOSHibernateFactory factory, JocConfiguration jocConfig) {
        dbFactory = factory;
        config = jocConfig;
    }

    public DBItemJocInstance getInstance(Date startTime) throws Exception {
        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());

            dbLayer.getSession().beginTransaction();
            DBItemInventoryOperatingSystem osItem = getOS(dbLayer);
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
                item.setOrdering(config.getOrdering());
                item.setUri(null);// TODO
                item.setHeartBeat(new Date());
                dbLayer.getSession().save(item);
            } else {
                item.setSecurityLevel(config.getSecurityLevel());
                item.setStartedAt(startTime);
                item.setTimezone(config.getTimezone());
                item.setTitle(config.getTitle());
                item.setOrdering(config.getOrdering());
                // item.setUri(null);
                item.setHeartBeat(new Date());
                dbLayer.getSession().update(item);
            }
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
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

    private DBItemInventoryOperatingSystem getOS(DBLayerJocCluster dbLayer) throws Exception {
        DBItemInventoryOperatingSystem item = dbLayer.getOS(config.getHostname());
        if (item == null) {
            item = new DBItemInventoryOperatingSystem();
            item.setHostname(config.getHostname());
            item.setName(SOSShell.OS_NAME);
            item.setArchitecture(SOSShell.OS_ARCHITECTURE);
            item.setDistribution(SOSShell.OS_VERSION);
            item.setModified(new Date());
            dbLayer.getSession().save(item);
        }
        return item;
    }

}
