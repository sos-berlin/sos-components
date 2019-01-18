package com.sos.joc.locks.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.locks.resource.ILocksResourceP;
import com.sos.joc.model.lock.LocksFilter;
import com.sos.joc.model.lock.LocksP;

@Path("locks")
public class LocksResourcePImpl extends JOCResourceImpl implements ILocksResourceP {

    private static final String API_CALL = "./locks/p";

    @Override
    public JOCDefaultResponse postLocksP(String xAccessToken, String accessToken, LocksFilter locksFilter) throws Exception {
        return postLocksP(getAccessToken(xAccessToken, accessToken), locksFilter);
    }

    public JOCDefaultResponse postLocksP(String accessToken, LocksFilter locksFilter) throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, locksFilter, accessToken,
					locksFilter.getJobschedulerId(),
					getPermissonsJocCockpit(locksFilter.getJobschedulerId(), accessToken).getLock().getView()
							.isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			LocksP entity = new LocksP();
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