package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.resource.IDailyPlanCalendarResource;
import com.sos.joc.exceptions.JocException;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanCalendarImpl extends JOCOrderResourceImpl implements IDailyPlanCalendarResource {

    @Override
    public JOCDefaultResponse deploy(String accessToken) {

        try {
            initLogging(IMPL_PATH, null, accessToken);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getAdministration().getSettings().getManage());
            if (response != null) {
                return response;
            }
            DailyPlanCalendar.getInstance().updateDailyPlanCalendar(null, accessToken, getJocError());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
