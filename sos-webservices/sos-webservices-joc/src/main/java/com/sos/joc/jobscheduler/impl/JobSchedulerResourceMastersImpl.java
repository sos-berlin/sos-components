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

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.MasterAnswer;
import com.sos.joc.classes.jobscheduler.MasterCallable;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceMasters;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.Master;
import com.sos.joc.model.jobscheduler.Masters;
import com.sos.schema.JsonValidator;

@Path("jobscheduler")
public class JobSchedulerResourceMastersImpl extends JOCResourceImpl implements IJobSchedulerResourceMasters {

    private static final String API_CALL = "./jobscheduler/masters";

    @Override
    public JOCDefaultResponse postJobschedulerInstancesP(String accessToken, byte[] filterBytes) {
        return postJobschedulerInstances(accessToken, filterBytes, true);
    }

    @Override
    public JOCDefaultResponse postJobschedulerInstances(String accessToken, byte[] filterBytes) {
        return postJobschedulerInstances(accessToken, filterBytes, false);
    }

    public JOCDefaultResponse postJobschedulerInstances(String accessToken, byte[] filterBytes, boolean onlyDb) {
        SOSHibernateSession connection = null;

        try {
            JsonValidator.validateFailFast(filterBytes, JobSchedulerId.class);
            JobSchedulerId jobSchedulerFilter = Globals.objectMapper.readValue(filterBytes, JobSchedulerId.class);

            String jobSchedulerId = jobSchedulerFilter.getJobschedulerId();
            if (jobSchedulerId == null) {
                jobSchedulerId = "";
            }

            initGetPermissions(accessToken);
            SOSShiroCurrentUser user = jobschedulerUser.getSosShiroCurrentUser();
            boolean isPermitted = true;
            if (!jobSchedulerId.isEmpty()) {
                isPermitted = user.getSosPermissionJocCockpit(jobSchedulerId).getJobschedulerMaster().getView().isStatus();
            }

            String apiCall = API_CALL;
            if (onlyDb) {
                apiCall += "/p";
            }
            JOCDefaultResponse jocDefaultResponse = init(apiCall, null, accessToken, jobSchedulerId, isPermitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            Masters entity = new Masters();
            entity.setMasters(getMasters(jobSchedulerId, accessToken, connection, onlyDb, user));
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

    private static List<Master> getMasters(String jobSchedulerId, String accessToken, SOSHibernateSession connection, boolean onlyDb,
            SOSShiroCurrentUser user) throws InterruptedException, JocException, Exception {
        return getMasterAnswers(jobSchedulerId, accessToken, connection, onlyDb, user).stream().map(Master.class::cast).collect(Collectors.toList());
    }

    public static List<MasterAnswer> getMasterAnswers(String jobSchedulerId, String accessToken, SOSHibernateSession connection)
            throws InterruptedException, JocException, Exception {
        return getMasterAnswers(jobSchedulerId, accessToken, connection, false, null);
    }

    public static List<MasterAnswer> getMasterAnswers(String jobSchedulerId, String accessToken, SOSHibernateSession connection, boolean onlyDb,
            SOSShiroCurrentUser user) throws InterruptedException, JocException, Exception {
        InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
        List<DBItemInventoryInstance> schedulerInstances = instanceLayer.getInventoryInstancesBySchedulerId(jobSchedulerId);
        List<MasterAnswer> masters = new ArrayList<MasterAnswer>();
        if (schedulerInstances != null) {
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<MasterCallable> tasks = new ArrayList<MasterCallable>();
            for (DBItemInventoryInstance schedulerInstance : schedulerInstances) {
                // skip all masters where the user doesn't have the permission to see its status
                if (jobSchedulerId.isEmpty() && user != null && !user.getSosPermissionJocCockpit(schedulerInstance.getSchedulerId())
                        .getJobschedulerMaster().getView().isStatus()) {
                    continue;
                }
                tasks.add(new MasterCallable(schedulerInstance, osDBLayer.getInventoryOperatingSystem(schedulerInstance.getOsId()), accessToken,
                        onlyDb));
            }
            if (!tasks.isEmpty()) {
                if (tasks.size() == 1) {
                    masters.add(tasks.get(0).call());
                } else {
                    ExecutorService executorService = Executors.newFixedThreadPool(Math.min(10, tasks.size()));
                    try {
                        for (Future<MasterAnswer> result : executorService.invokeAll(tasks)) {
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
            }

            if (!onlyDb) {
                for (MasterAnswer master : masters) {
                    Long osId = osDBLayer.saveOrUpdateOSItem(master.getDbOs());
                    master.setOsId(osId);

                    if (master.dbInstanceIsChanged()) {
                        instanceLayer.updateInstance(master.getDbInstance());
                    }
                }
            }
        }
        return masters;
    }

}
