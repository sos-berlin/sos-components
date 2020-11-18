package com.sos.joc.locks.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.locks.resource.ILocksResource;
import com.sos.joc.model.lock.LocksFilter;
import com.sos.joc.model.lock.LocksV;

@Path("locks")
public class LocksResourceImpl extends JOCResourceImpl implements ILocksResource {

    private static final String API_CALL = "./locks";

    @Override
    public JOCDefaultResponse postLocks(String xAccessToken, String accessToken, LocksFilter locksFilter) throws Exception {
        return postLocks(getAccessToken(xAccessToken, accessToken), locksFilter);
    }

    public JOCDefaultResponse postLocks(String accessToken, LocksFilter locksFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, locksFilter, accessToken, locksFilter.getControllerId(), getPermissonsJocCockpit(
                    locksFilter.getControllerId(), accessToken).getLock().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            LocksV entity = new LocksV();
            //entity.setLocks(null);
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