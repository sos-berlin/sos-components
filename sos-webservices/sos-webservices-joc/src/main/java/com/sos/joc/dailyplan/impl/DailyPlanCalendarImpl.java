package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.resource.IDailyPlanCalendarResource;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanCalendarImpl extends JOCOrderResourceImpl implements IDailyPlanCalendarResource {

    @Override
    public JOCDefaultResponse deploy(String accessToken) {

        try {
            initLogging(IMPL_PATH, null, accessToken, CategoryType.DAILYPLAN);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getAdministration().getSettings()
                    .getManage()));
            if (response != null) {
                return response;
            }
            DailyPlanCalendar.getInstance().updateDailyPlanCalendar(null, accessToken, getJocError());
            return responseStatusJSOk(Date.from(Instant.now()));
            
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
