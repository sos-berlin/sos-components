package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.dailyplan.DailyPlanRunner;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanProjections;
import com.sos.joc.dailyplan.resource.IDailyPlanProjectionsResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanProjection;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.projections.ProjectionsResponse;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.year.YearsItem;
import com.sos.joc.model.order.ScheduleDatesFilter;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanProjectionsImpl extends JOCResourceImpl implements IDailyPlanProjectionsResource {

    @Override
    public JOCDefaultResponse projections(String accessToken, byte[] filterBytes) {

        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, ScheduleDatesFilter.class);
            ScheduleDatesFilter in = Globals.objectMapper.readValue(filterBytes, ScheduleDatesFilter.class);

            JocPermissions perms = getJocPermissions(accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, perms.getCalendars().getView() || perms.getDailyPlan().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerDailyPlanProjections dbLayer = new DBLayerDailyPlanProjections(session);
            List<DBItemDailyPlanProjection> items = dbLayer.getProjections(null);
            dbLayer.close();
            session = null;

            MetaItem meta = null;
            YearsItem years = new YearsItem();
            for (DBItemDailyPlanProjection item : items) {
                if (item.isMeta()) {
                    meta = Globals.objectMapper.readValue(item.getContent(), MetaItem.class);
                } else {
                    String year = String.valueOf(item.getId());

                    YearsItem yi = Globals.objectMapper.readValue(item.getContent(), YearsItem.class);
                    years.setAdditionalProperty(year, yi.getAdditionalProperties().get(year));
                }
            }

            // TODO filter - year, month, date
            // TODO reduce response e.g. for - year - without meta and schedule informations - e.g. only months and dates
            // TODO check folder permissions for meta and year

            ProjectionsResponse entity = new ProjectionsResponse();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setMeta(meta);
            entity.setYears(years);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
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
    public JOCDefaultResponse recreate(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH_RECREATE, filterBytes, accessToken);

            DailyPlanRunner.recreateProjections(JOCOrderResourceImpl.getDailyPlanSettings());

            return JOCDefaultResponse.responseStatus200(Date.from(Instant.now()));
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
