package com.sos.joc.classes.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.jobscheduler.ClusterMemberType;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.HostPortParameter;
import com.sos.joc.model.jobscheduler.JobSchedulerP;
import com.sos.joc.model.jobscheduler.OperatingSystem;

public class JobSchedulerPermanent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerPermanent.class);

    public static JobSchedulerP getJobScheduler(DBItemInventoryInstance dbItemInventoryInstance, boolean isSupervisorCall) throws JocException {

        SOSHibernateSession connection = null;

        try {
            connection = Globals.createSosHibernateStatelessConnection("getJobScheduler");

            JobSchedulerP jobscheduler = new JobSchedulerP();
            jobscheduler.setHost(dbItemInventoryInstance.getHostname());
            jobscheduler.setJobschedulerId(dbItemInventoryInstance.getSchedulerId());
            jobscheduler.setPort(dbItemInventoryInstance.getPort());
            jobscheduler.setStartedAt(dbItemInventoryInstance.getStartedAt());
            jobscheduler.setTimeZone(dbItemInventoryInstance.getTimeZone());
            jobscheduler.setVersion(dbItemInventoryInstance.getVersion());
            jobscheduler.setSurveyDate(dbItemInventoryInstance.getModified());
            jobscheduler.setUrl(dbItemInventoryInstance.getUrl());

            ClusterMemberType clusterMemberTypeSchema = new ClusterMemberType();
            clusterMemberTypeSchema.setPrecedence(dbItemInventoryInstance.getPrecedence());
            clusterMemberTypeSchema.set_type(ClusterType.fromValue(dbItemInventoryInstance.getClusterType()));
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

            if (!isSupervisorCall) {
                Long supervisorId = dbItemInventoryInstance.getSupervisorId();
                if (supervisorId != 0L) {
                    InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
                    DBItemInventoryInstance schedulerSupervisorInstancesDBItem = dbLayer.getInventoryInstanceByKey(supervisorId);
                    if (schedulerSupervisorInstancesDBItem == null) {
                        throw new DBMissingDataException(String.format("supervisor with Id = %s not found in table INVENTORY_INSTANCES", dbItemInventoryInstance
                                .getSupervisorId()));
                    } else {
                        HostPortParameter supervisor = new HostPortParameter();
                        supervisor.setHost(schedulerSupervisorInstancesDBItem.getHostname());
                        supervisor.setPort(schedulerSupervisorInstancesDBItem.getPort());
                        supervisor.setJobschedulerId(schedulerSupervisorInstancesDBItem.getSchedulerId());
                        jobscheduler.setSupervisor(supervisor);
                    }
                }
            }
            return jobscheduler;
        } finally {
            Globals.disconnect(connection);
        }

    }
}
