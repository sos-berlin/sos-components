package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.controller.ControllerAnswer;
import com.sos.joc.classes.controller.ControllerCallable;
import com.sos.joc.classes.controller.States;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerResource;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.JobScheduler200;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("controller")
public class ControllerResourceImpl extends JOCResourceImpl implements IControllerResource {

    private static final String API_CALL = "./controller";
    private static final String isUrlPattern = "^https?://[^\\s]+$";
    private static final Predicate<String> isUrl = Pattern.compile(isUrlPattern).asPredicate();

    @Override
    public JOCDefaultResponse postJobschedulerP(String accessToken, byte[] filterBytes) {
        return postJobscheduler(accessToken, filterBytes, true);
    }

    @Override
    public JOCDefaultResponse postJobscheduler(String accessToken, byte[] filterBytes) {
        return postJobscheduler(accessToken, filterBytes, false);
    }

    public JOCDefaultResponse postJobscheduler(String accessToken, byte[] filterBytes, boolean onlyDb) {
        SOSHibernateSession connection = null;
        try {
            String apiCall = API_CALL;
            if (onlyDb) {
                apiCall += "/p";
            }
            filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            String controllerId = jobSchedulerBody.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicControllerPermissions(controllerId, accessToken).getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            
            InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
            DBItemInventoryJSInstance schedulerInstance = null;
            
            
            if (jobSchedulerBody.getUrl() == null) {
                List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
                if (controllerInstances == null || controllerInstances.isEmpty()) {
                    // read db again?
                    throw new DBMissingDataException(String.format("Couldn't find Controller with id %s for security level %s", controllerId, Globals
                            .getJocSecurityLevel()));
                }
                schedulerInstance = States.getActiveControllerNode(controllerInstances, Proxy.of(controllerId).currentState().clusterState());

            } else {
                if (!isUrl.test(jobSchedulerBody.getUrl())) {
                    throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
                }
                schedulerInstance = instanceLayer.getInventoryInstanceByURL(jobSchedulerBody.getUrl());
                if (schedulerInstance == null) {
                    throw new DBMissingDataException(String.format("Couldn't find Controller with url %s for security level %s", jobSchedulerBody
                            .getUrl(), Globals.getJocSecurityLevel()));
                }
            }

            ControllerAnswer master = new ControllerCallable(schedulerInstance, osDBLayer.getInventoryOperatingSystem(schedulerInstance.getOsId()),
                    accessToken, onlyDb).call();

            if (!onlyDb && master != null) {
                Long osId = osDBLayer.saveOrUpdateOSItem(master.getDbOs());
                master.setOsId(osId);

                if (master.dbInstanceIsChanged()) {
                    instanceLayer.updateInstance(master.getDbInstance());
                }
            }

            JobScheduler200 entity = new JobScheduler200();
            entity.setController(master);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (ControllerConnectionRefusedException e) {
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
}
