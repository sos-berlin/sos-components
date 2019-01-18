package com.sos.joc.processClasses.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.processClass.ProcessClassesFilter;
import com.sos.joc.model.processClass.ProcessClassesV;
import com.sos.joc.processClasses.resource.IProcessClassesResource;

@Path("process_classes")
public class ProcessClassesResourceImpl extends JOCResourceImpl implements IProcessClassesResource {

    private static final String API_CALL = "./process_classes";

    @Override
    public JOCDefaultResponse postProcessClasses(String xAccessToken, String accessToken, ProcessClassesFilter processClassFilter) throws Exception {
        return postProcessClasses(getAccessToken(xAccessToken, accessToken), processClassFilter);
    }

    public JOCDefaultResponse postProcessClasses(String accessToken, ProcessClassesFilter processClassFilter) throws Exception {
        try {
            SOSPermissionJocCockpit perms = getPermissonsJocCockpit(processClassFilter.getJobschedulerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, processClassFilter, accessToken, processClassFilter.getJobschedulerId(), perms
                    .getProcessClass().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ProcessClassesV entity = new ProcessClassesV();
            //entity.setProcessClasses(null);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JobSchedulerConnectionResetException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}