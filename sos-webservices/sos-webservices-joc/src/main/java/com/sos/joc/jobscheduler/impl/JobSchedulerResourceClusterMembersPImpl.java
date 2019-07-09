package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceClusterMembersP;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.ClusterMemberType;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.JobSchedulerP;
import com.sos.joc.model.jobscheduler.MastersP;
import com.sos.joc.model.jobscheduler.OperatingSystem;

@Path("jobscheduler")
public class JobSchedulerResourceClusterMembersPImpl extends JOCResourceImpl
		implements IJobSchedulerResourceClusterMembersP {

	private static final String API_CALL = "./jobscheduler/cluster/members/p";

	@Override
	public JOCDefaultResponse postJobschedulerClusterMembers(String xAccessToken, String accessToken,
			JobSchedulerId jobSchedulerFilter) {
		return postJobschedulerClusterMembers(getAccessToken(xAccessToken, accessToken), jobSchedulerFilter);
	}

	public JOCDefaultResponse postJobschedulerClusterMembers(String accessToken, JobSchedulerId jobSchedulerFilter) {
		SOSHibernateSession connection = null;
		try {
			if (jobSchedulerFilter.getJobschedulerId() == null) {
				jobSchedulerFilter.setJobschedulerId("");
			}

			boolean isPermitted = true;
			String curJobSchedulerId = jobSchedulerFilter.getJobschedulerId();

            if (!curJobSchedulerId.isEmpty()) {
                isPermitted = getPermissonsJocCockpit(curJobSchedulerId, accessToken).getJobschedulerMasterCluster().getView().isStatus()
                        || getPermissonsJocCockpit(curJobSchedulerId, accessToken).getJobschedulerMaster().getView().isStatus();
            }

			JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerFilter, accessToken, curJobSchedulerId,
					isPermitted);
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			connection = Globals.createSosHibernateStatelessConnection("getClusterMembers");
			MastersP entity = new MastersP();
			List<JobSchedulerP> masters = new ArrayList<JobSchedulerP>();
			InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
			List<DBItemInventoryInstance> schedulersFromDb = instanceLayer
					.getInventoryInstancesBySchedulerId(curJobSchedulerId, true);
			if (schedulersFromDb != null && !schedulersFromDb.isEmpty()) {
				String masterId = "";
				for (DBItemInventoryInstance instance : schedulersFromDb) {
					if (curJobSchedulerId.isEmpty()) {
						if (instance.getSchedulerId() == null || instance.getSchedulerId().isEmpty()) {
							continue;
						}
                        if (!masterId.equals(instance.getSchedulerId())) {
                            masterId = instance.getSchedulerId();
                            isPermitted = getPermissonsJocCockpit(masterId, accessToken).getJobschedulerMasterCluster().getView().isStatus()
                                    || getPermissonsJocCockpit(masterId, accessToken).getJobschedulerMaster().getView().isStatus();
                        }
						if (!isPermitted) {
							continue;
						}
					}
					JobSchedulerP jobscheduler = new JobSchedulerP();
					jobscheduler.setJobschedulerId(instance.getSchedulerId());
					jobscheduler.setUrl(instance.getUri());
					jobscheduler.setStartedAt(instance.getStartedAt());
					ClusterMemberType clusterMemberType = new ClusterMemberType();
					if (instance.getCluster()) {
						clusterMemberType.set_type(ClusterType.PASSIVE);
					} else {
						clusterMemberType.set_type(ClusterType.STANDALONE);
					}
					jobscheduler.setClusterType(clusterMemberType);
					jobscheduler.setTimeZone(instance.getTimezone());
					jobscheduler.setVersion(instance.getVersion());
					jobscheduler.setSurveyDate(instance.getModified());
					InventoryOperatingSystemsDBLayer osLayer = new InventoryOperatingSystemsDBLayer(connection);
					DBItemOperatingSystem osFromDb = osLayer.getInventoryOperatingSystem(instance.getOsId());
					if (osFromDb != null) {
						OperatingSystem os = new OperatingSystem();
						os.setArchitecture(osFromDb.getArchitecture());
						os.setDistribution(osFromDb.getDistribution());
						os.setName(osFromDb.getName());
						jobscheduler.setOs(os);
					}
					masters.add(jobscheduler);
				}
				if (masters.isEmpty() && curJobSchedulerId.isEmpty()) {
					return accessDeniedResponse();
				}
			}
			entity.setMasters(masters);
			entity.setDeliveryDate(Date.from(Instant.now()));
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		} finally {
			Globals.disconnect(connection);
		}
	}

}