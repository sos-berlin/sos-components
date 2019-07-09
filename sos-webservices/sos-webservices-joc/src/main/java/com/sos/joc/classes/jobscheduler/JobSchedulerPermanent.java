package com.sos.joc.classes.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.jobscheduler.ClusterMemberType;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.JobSchedulerP;
import com.sos.joc.model.jobscheduler.OperatingSystem;

public class JobSchedulerPermanent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerPermanent.class);

    public static JobSchedulerP getJobScheduler(DBItemInventoryInstance dbItemInventoryInstance, boolean isSupervisorCall) throws JocException {

        SOSHibernateSession connection = null;

        try {
            connection = Globals.createSosHibernateStatelessConnection("getJobScheduler");

            JobSchedulerP jobscheduler = new JobSchedulerP();
            jobscheduler.setJobschedulerId(dbItemInventoryInstance.getSchedulerId());
            jobscheduler.setStartedAt(dbItemInventoryInstance.getStartedAt());
            jobscheduler.setTimeZone(dbItemInventoryInstance.getTimezone());
            jobscheduler.setVersion(dbItemInventoryInstance.getVersion());
            jobscheduler.setSurveyDate(dbItemInventoryInstance.getModified());
            jobscheduler.setUrl(dbItemInventoryInstance.getUri());

            ClusterMemberType clusterMemberTypeSchema = new ClusterMemberType();
            if (dbItemInventoryInstance.getCluster()) {
            	clusterMemberTypeSchema.set_type(ClusterType.PASSIVE);
            } else {
            	clusterMemberTypeSchema.set_type(ClusterType.STANDALONE);
            }
            jobscheduler.setClusterType(clusterMemberTypeSchema);

            Long osId = dbItemInventoryInstance.getOsId();
            DBItemOperatingSystem osItem = null;
            OperatingSystem os = new OperatingSystem();
            if (osId != 0L) {
                InventoryOperatingSystemsDBLayer osLayer = new InventoryOperatingSystemsDBLayer(connection);
                osItem = osLayer.getInventoryOperatingSystem(dbItemInventoryInstance.getOsId());
            }
            if (osItem != null) {
                os.setArchitecture(osItem.getArchitecture());
                os.setDistribution(osItem.getDistribution());
                os.setName(osItem.getName());
            } else {
                os.setArchitecture("");
                os.setDistribution("");
                os.setName("");
                LOGGER.error("The operating system could not determine");
            }
            jobscheduler.setOs(os);

            return jobscheduler;
        } finally {
            Globals.disconnect(connection);
        }

    }
}
