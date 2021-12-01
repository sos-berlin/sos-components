package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.dailyplan.common.DailyPlanHelper;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanSubmissions;
import com.sos.joc.dailyplan.db.FilterDailyPlanSubmissions;
import com.sos.joc.dailyplan.resource.IDailyPlanSubmissionsResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissions;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsItem;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanSubmissionsImpl extends JOCOrderResourceImpl implements IDailyPlanSubmissionsResource {

    @Override
    public JOCDefaultResponse postDailyPlanSubmissions(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {

            initLogging(IMPL_PATH_MAIN, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanSubmissionsFilter in = Globals.objectMapper.readValue(filterBytes, DailyPlanSubmissionsFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getDailyPlan().getView());
            if (response != null) {
                return response;
            }

            this.checkRequiredParameter("filter", in.getFilter());
            this.checkRequiredParameter("dateTo", in.getFilter().getDateTo());

            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(in.getControllerId());
            if (in.getFilter().getDateFrom() != null) {
                Date fromDate = DailyPlanHelper.stringAsDate(in.getFilter().getDateFrom());
                filter.setDateFrom(fromDate);
            }
            if (in.getFilter().getDateTo() != null) {
                Date toDate = DailyPlanHelper.stringAsDate(in.getFilter().getDateTo());
                filter.setDateTo(toDate);
            }
            filter.setSortMode("desc");
            filter.setOrderCriteria("id");

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_MAIN);
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            List<DBItemDailyPlanSubmission> items = dbLayer.getSubmissions(filter, 0);
            session.close();
            session = null;

            List<DailyPlanSubmissionsItem> result = new ArrayList<DailyPlanSubmissionsItem>();
            for (DBItemDailyPlanSubmission item : items) {
                DailyPlanSubmissionsItem p = new DailyPlanSubmissionsItem();
                p.setSubmissionHistoryId(item.getId());
                p.setControllerId(item.getControllerId());
                p.setDailyPlanDate(item.getSubmissionForDate());
                p.setSubmissionTime(item.getCreated());
                result.add(p);
            }

            DailyPlanSubmissions answer = new DailyPlanSubmissions();
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
    public JOCDefaultResponse postDeleteDailyPlanSubmissions(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_DELETE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            DailyPlanSubmissionsFilter in = Globals.objectMapper.readValue(filterBytes, DailyPlanSubmissionsFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getDailyPlan().getManage());
            if (response != null) {
                return response;
            }

            this.checkRequiredParameter("filter", in.getFilter());

            setSettings();
            FilterDailyPlanSubmissions filter = new FilterDailyPlanSubmissions();
            filter.setControllerId(in.getControllerId());
            if (in.getFilter().getDateFor() != null) {
                Date date = DailyPlanHelper.stringAsDate(in.getFilter().getDateFor());
                filter.setDateFor(date);
            } else {
                if (in.getFilter().getDateFrom() != null) {
                    Date fromDate = DailyPlanHelper.stringAsDate(in.getFilter().getDateFrom());
                    filter.setDateFrom(fromDate);
                }
                if (in.getFilter().getDateTo() != null) {
                    Date toDate = DailyPlanHelper.stringAsDate(in.getFilter().getDateTo());
                    filter.setDateTo(toDate);
                }
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH_DELETE);
            DBLayerDailyPlanSubmissions dbLayer = new DBLayerDailyPlanSubmissions(session);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            int result = dbLayer.delete(filter);
            Globals.commit(session);
            session.close();
            session = null;

            if (result > 0) {
                if (in.getFilter().getDateFor() != null) {
                    EventBus.getInstance().post(new DailyPlanEvent(in.getFilter().getDateFor()));
                }
                if (in.getFilter().getDateFrom() != null) {
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

}
