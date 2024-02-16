package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.db.reporting.items.ReportDbItem;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.reporting.ReportHistoryFilter;
import com.sos.joc.model.reporting.ReportItem;
import com.sos.joc.model.reporting.ReportItems;
import com.sos.joc.reporting.resource.IReportHistoryResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class ReportHistoryImpl extends JOCResourceImpl implements IReportHistoryResource {
    
    @Override
    public JOCDefaultResponse show(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReportHistoryFilter.class);
            ReportHistoryFilter in = Globals.objectMapper.readValue(filterBytes, ReportHistoryFilter.class);
            
            boolean permitted = true;

            // TODO: PERMISSIONS, maybe a new permission

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }
            
//            Function<DBItemReportHistory, ReportItem> mapToReportItem = dbItem -> {
//                try {
//                    ReportItem item = Globals.objectMapper.readValue(dbItem.getContent(), ReportItem.class);
//                    item.setId(dbItem.getId());
//                    item.setDateFrom(SOSDate.getDateAsString(dbItem.getDateFrom()));
//                    item.setDateTo(SOSDate.getDateAsString(dbItem.getDateTo()));
//                    item.setFrequency(dbItem.getFrequencyAsEnum());
//                    item.setSize(dbItem.getSize());
//                    item.setTemplateId(dbItem.getTemplateId());
//                    item.setCreated(dbItem.getCreated());
//                    return item;
//                } catch (Exception e) {
//                    // TODO: error handling
//                    return null;
//                }
//            };
            
            Function<ReportDbItem, ReportItem> mapToReportItem = dbItem -> {
                try {
                    if (in.getCompact() != Boolean.TRUE) {
                        dbItem.setData(Globals.objectMapper.readValue(dbItem.getContent(), ReportItem.class).getData());
                    } else {
                        dbItem.setData(null);
                    }
                    dbItem.setContent(null);
                    return dbItem;
                } catch (Exception e) {
                    // TODO: error handling
                    return null;
                }
            };
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            ReportItems entity = new ReportItems();
            entity.setReports(dbLayer.getAllReports(in.getIds(), in.getCompact() == Boolean.TRUE).stream().map(mapToReportItem).filter(
                    Objects::nonNull).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
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

}
