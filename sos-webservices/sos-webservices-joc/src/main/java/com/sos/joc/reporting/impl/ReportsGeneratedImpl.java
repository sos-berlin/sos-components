package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.db.reporting.items.ReportDbItem;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.reporting.ReportHistoryFilter;
import com.sos.joc.model.reporting.ReportItem;
import com.sos.joc.model.reporting.ReportItems;
import com.sos.joc.reporting.resource.IReportsGeneratedResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class ReportsGeneratedImpl extends JOCResourceImpl implements IReportsGeneratedResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsGeneratedImpl.class);
    
    @Override
    public JOCDefaultResponse showHistory(String accessToken, byte[] filterBytes) { //TODO deprecated
        return show(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse show(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, ReportHistoryFilter.class);
            ReportHistoryFilter in = Globals.objectMapper.readValue(filterBytes, ReportHistoryFilter.class);
            
            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }
            
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            Function<ReportDbItem, ReportItem> mapToReportItem = dbItem -> {
                try {
                    if (!canAdd(dbItem.getPath(), permittedFolders)) {
                       return null; 
                    }
                    if (in.getCompact() != Boolean.TRUE) {
                        dbItem.setData(Globals.objectMapper.readValue(dbItem.getContent(), ReportItem.class).getData());
                    } else {
                        dbItem.setData(null);
                    }
                    dbItem.setContent(null);
                    return dbItem;
                } catch (Exception e) {
                    if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                        LOGGER.info(getJocError().printMetaInfo());
                        getJocError().clearMetaInfo();
                    }
                    LOGGER.error(String.format("[%s] %s", dbItem.getPath(), e.toString()));
                    return null;
                }
            };
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            ReportItems entity = new ReportItems();
            // TODO more request filter
            Date dateFrom = getLocalDateTimeToDate(getLocalDateFrom(in.getDateFrom()));
            Date dateTo = getLocalDateTimeToDate(getLocalDateTo(in.getDateTo()));
            entity.setReports(dbLayer.getGeneratedReports(in.getRunIds(), in.getCompact() == Boolean.TRUE, in.getReportPaths(), in.getTemplateNames(),
                    dateFrom, dateTo).stream().map(mapToReportItem).filter(Objects::nonNull).collect(Collectors.toList()));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static LocalDateTime getLocalDateFrom(final String dateFrom) { // yyyy-MM-dd
        if (dateFrom == null) {
            return null;
        }
        String[] yearMonthDayFrom = dateFrom.split("-");
        return LocalDate.of(Integer.valueOf(yearMonthDayFrom[0]).intValue(), Integer.valueOf(yearMonthDayFrom[1]).intValue(), Integer.valueOf(
                yearMonthDayFrom[2])).atStartOfDay();
    }
    
    private static LocalDateTime getLocalDateTo(final String dateTo) { //yyyy-MM-dd
        LocalDateTime ld = getLocalDateFrom(dateTo);
        return ld == null ? null : ld.plusDays(1);
    }
    
    private static Date getLocalDateTimeToDate(final LocalDateTime ld) { //yyyy-MM-dd
        if (ld == null) {
            return null;
        }
        return Date.from(ld.atZone(ZoneId.systemDefault()).toInstant());
    }

}
