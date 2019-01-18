package com.sos.joc.processClasses.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.processClass.ProcessClassesFilter;
import com.sos.joc.model.processClass.ProcessClassesP;
import com.sos.joc.processClasses.resource.IProcessClassesResourceP;

@Path("process_classes")
public class ProcessClassesResourcePImpl extends JOCResourceImpl implements IProcessClassesResourceP {

    private static final String API_CALL = "./process_classes/p";

    @Override
    public JOCDefaultResponse postProcessClassesP(String xAccessToken, String accessToken, ProcessClassesFilter processClassFilter) throws Exception {
        return postProcessClassesP(getAccessToken(xAccessToken, accessToken), processClassFilter);
    }

    public JOCDefaultResponse postProcessClassesP(String accessToken, ProcessClassesFilter processClassFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, processClassFilter, accessToken, processClassFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(processClassFilter.getJobschedulerId(), accessToken).getLock().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            ProcessClassesP entity = new ProcessClassesP();
            //entity.setProcessClasses(null);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}