package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanSubmissions;
import com.sos.joc.dailyplan.resource.IDailyPlanSubmissionsResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.submissions.SubmissionsDeleteRequest;
import com.sos.joc.model.dailyplan.submissions.SubmissionsRequest;
import com.sos.joc.model.dailyplan.submissions.SubmissionsResponse;
import com.sos.joc.model.dailyplan.submissions.items.SubmissionItem;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanSubmissionsImpl extends JOCOrderResourceImpl implements IDailyPlanSubmissionsResource {

    @Override
    public JOCDefaultResponse postDailyPlanSubmissions(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            // TODO redefine raml and remove filter class

            initLogging(IMPL_PATH_MAIN, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, SubmissionsRequest.class);
            SubmissionsRequest in = Globals.objectMapper.readValue(inBytes, SubmissionsRequest.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getDailyPlan().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_MAIN);
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            List<DBItemDailyPlanSubmission> items = dbLayer.getSubmissions(in.getControllerId(), SOSDate.getDate(in.getFilter().getDateFrom()),
                    SOSDate.getDate(in.getFilter().getDateTo()));
            session.close();
            session = null;

            List<SubmissionItem> result = null;
            if (items == null || items.size() == 0) {
                result = Collections.emptyList();
            } else {
                // sort descending by submission time
                result = items.stream().map(e -> {
                    return map(e);
                }).sorted(Comparator.comparing(SubmissionItem::getSubmissionTime).reversed()).collect(Collectors.toList());
            }

            SubmissionsResponse answer = new SubmissionsResponse();
            answer.setSubmissionHistoryItems(result);
            answer.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(answer);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    @Override
    public JOCDefaultResponse postDeleteDailyPlanSubmissions(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            // TODO redefine raml and remove filter class
            initLogging(IMPL_PATH_DELETE, inBytes, accessToken);
            // use validate instead of validateFailFast when anyOf/oneOf is defined
            JsonValidator.validate(inBytes, SubmissionsDeleteRequest.class);
            SubmissionsDeleteRequest in = Globals.objectMapper.readValue(inBytes, SubmissionsDeleteRequest.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getDailyPlan().getManage());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_DELETE);
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            int result = dbLayer.delete(StartupMode.manual, in.getControllerId(), in.getFilter().getDateFor(), in.getFilter().getDateFrom(), in
                    .getFilter().getDateTo());
            Globals.commit(session);
            session.close();
            session = null;

            if (result > 0) {
                if (in.getFilter().getDateFor() != null) {
                    EventBus.getInstance().post(new DailyPlanEvent(in.getFilter().getDateFor()));
                } else if (in.getFilter().getDateFrom() != null) {
                    EventBus.getInstance().post(new DailyPlanEvent(in.getFilter().getDateFrom()));
                }
            }
            return JOCDefaultResponse.responseStatusJSOk(new Date());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private SubmissionItem map(DBItemDailyPlanSubmission item) {
        SubmissionItem p = new SubmissionItem();
        p.setSubmissionHistoryId(item.getId());
        p.setControllerId(item.getControllerId());
        p.setDailyPlanDate(item.getSubmissionForDate());
        p.setSubmissionTime(item.getCreated());
        return p;
    }

}
