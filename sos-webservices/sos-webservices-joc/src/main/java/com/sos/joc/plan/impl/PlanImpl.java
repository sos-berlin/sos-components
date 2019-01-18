package com.sos.joc.plan.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.Plan;
import com.sos.joc.model.plan.PlanCreated;
import com.sos.joc.model.plan.PlanFilter;
import com.sos.joc.plan.resource.IPlanResource;

@Path("plan")
public class PlanImpl extends JOCResourceImpl implements IPlanResource {

    private static final String API_CALL = "./plan";
    
    @Override
    public JOCDefaultResponse postPlan(String xAccessToken, String accessToken, PlanFilter planFilter) throws Exception {
        return postPlan(getAccessToken(xAccessToken, accessToken), planFilter);
    }

    public JOCDefaultResponse postPlan(String accessToken, PlanFilter planFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, planFilter, accessToken, planFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    planFilter.getJobschedulerId(), accessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Plan entity = new Plan();
            PlanCreated created = new PlanCreated();
            created.setDays(null);
            created.setUntil(null);
            entity.setCreated(created);
            //entity.setPlanItems(null);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.printStackTrace();
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
