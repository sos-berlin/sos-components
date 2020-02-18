package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.JobSchedulerAnswer;
import com.sos.joc.classes.jobscheduler.JobSchedulerCallable;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceClusterMembers;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.JobScheduler;
import com.sos.joc.model.jobscheduler.Masters;
import com.sos.schema.JsonValidator;

@Path("jobscheduler")
public class JobSchedulerResourceClusterMembersImpl extends JOCResourceImpl implements IJobSchedulerResourceClusterMembers {

    private static final String API_CALL = "./jobscheduler/cluster/members";

    @Override
    public JOCDefaultResponse postJobschedulerClusterMembersP(String accessToken, byte[] filterBytes) {
        return postJobschedulerClusterMembers(accessToken, filterBytes, true);
    }

    @Override
    public JOCDefaultResponse postJobschedulerClusterMembers(String accessToken, byte[] filterBytes) {
        return postJobschedulerClusterMembers(accessToken, filterBytes, false);
    }

    public JOCDefaultResponse postJobschedulerClusterMembers(String accessToken, byte[] filterBytes, boolean onlyDb) {
        SOSHibernateSession connection = null;

        try {
            JsonValidator.validateFailFast(filterBytes, JobSchedulerId.class);
            JobSchedulerId jobSchedulerFilter = Globals.objectMapper.readValue(filterBytes, JobSchedulerId.class);
            
            if (jobSchedulerFilter.getJobschedulerId() == null) {
                jobSchedulerFilter.setJobschedulerId("");
            }

            boolean isPermitted = true;
            String curJobSchedulerId = jobSchedulerFilter.getJobschedulerId();

            if (!curJobSchedulerId.isEmpty()) {
                SOSPermissionJocCockpit sosPermissionJocCockpit = getPermissonsJocCockpit(curJobSchedulerId, accessToken);
                isPermitted = sosPermissionJocCockpit.getJobschedulerMasterCluster().getView().isStatus() || sosPermissionJocCockpit
                        .getJobschedulerMaster().getView().isStatus();
            }

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerFilter, accessToken, curJobSchedulerId, isPermitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            List<JobSchedulerAnswer> masters = new ArrayList<JobSchedulerAnswer>();

            InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<DBItemInventoryInstance> schedulersFromDb = instanceLayer.getInventoryInstancesBySchedulerId(curJobSchedulerId);
            if (schedulersFromDb != null && !schedulersFromDb.isEmpty()) {

                List<JobSchedulerCallable> tasks = new ArrayList<JobSchedulerCallable>();
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
                    tasks.add(new JobSchedulerCallable(instance, osDBLayer.getInventoryOperatingSystem(instance.getOsId()), accessToken, onlyDb));
                }
                if (!tasks.isEmpty()) {
                    if (tasks.size() == 1) {
                        masters.add(tasks.get(0).call());
                    } else {
                        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(10, tasks.size()));
                        try {
                            for (Future<JobSchedulerAnswer> result : executorService.invokeAll(tasks)) {
                                try {
                                    masters.add(result.get());
                                } catch (ExecutionException e) {
                                    if (e.getCause() instanceof JocException) {
                                        throw (JocException) e.getCause();
                                    } else {
                                        throw (Exception) e.getCause();
                                    }
                                }
                            }
                        } finally {
                            executorService.shutdown();
                        }
                    }
                } else {
                    if (curJobSchedulerId.isEmpty()) {
                        return accessDeniedResponse();
                    }
                }
            }

            Masters entity = new Masters();
            if (!onlyDb) {
                if (!masters.isEmpty()) {

                    for (JobSchedulerAnswer master : masters) {
                        Long osId = osDBLayer.saveOrUpdateOSItem(master.getDbOs());
                        master.setOsId(osId);

                        if (master.dbInstanceIsChanged()) {
                            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
                            instanceDBLayer.updateInstance(master.getDbInstance());
                        }
                    }
                }
                entity.setMasters(masters.stream().map(JobScheduler.class::cast).collect(Collectors.toList()));
            } else {
                entity.setMasters(masters.stream().map(i -> {
                    i.setState(null);
                    return (JobScheduler) i;
                }).collect(Collectors.toList()));
            }

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
