package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.report.Frequency;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.reporting.DBItemReportRun;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.reporting.ReportRunState;
import com.sos.joc.model.reporting.ReportRunStateText;
import com.sos.joc.model.reporting.RunItem;
import com.sos.joc.model.reporting.RunItems;
import com.sos.joc.reporting.resource.IRunHistoryResource;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class RunHistoryImpl extends JOCResourceImpl implements IRunHistoryResource {
    
    private static final Map<ReportRunStateText, Integer> states = Collections.unmodifiableMap(new HashMap<ReportRunStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(ReportRunStateText.SUCCESSFUL, 0);
            put(ReportRunStateText.IN_PROGRESS, 1);
            put(ReportRunStateText.FAILED, 2);
            put(ReportRunStateText.UNKNOWN, 2);
        }
    });
    
    @Override
    public JOCDefaultResponse show(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            //JsonValidator.validateFailFast(filterBytes, RunFilter.class);
            //RunFilter in = Globals.objectMapper.readValue(filterBytes, RunFilter.class);
            
            // TODO: filter request
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }
            
            Function<DBItemReportRun, RunItem> mapToRunItem = dbItem -> {
                try {
                    RunItem item = new RunItem();
                    item.setRunId(dbItem.getId());
                    item.setPath(dbItem.getPath());
                    item.setTitle(dbItem.getTitle());
                    item.setMonthFrom(SOSDate.format(dbItem.getDateFrom(), "yyyy-MM"));
                    item.setMonthTo(SOSDate.format(dbItem.getDateFrom(), "yyyy-MM"));
                    item.setFrequencies(Arrays.asList(dbItem.getFrequencies().split(",")).stream().map(Integer::valueOf).sorted().map(
                            Frequency::fromValue).filter(Objects::nonNull).collect(Collectors.toSet()));
                    item.setHits(dbItem.getHits());
                    item.setTemplateName(dbItem.getTemplateId());
                    item.setModified(dbItem.getModified());
                    item.setErrorText(dbItem.getErrorText());
                    item.setState(getState(dbItem.getStateAsEnum()));
                    item.setVersion(null);
                    return item;
                } catch (Exception e) {
                    // TODO: error handling
                    return null;
                }
            };
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            RunItems entity = new RunItems();
            entity.setRuns(dbLayer.getAllRuns().stream().map(mapToRunItem).filter(Objects::nonNull).collect(Collectors.toList()));
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
    
    private static ReportRunState getState(ReportRunStateText dbState) {
        ReportRunState state = new ReportRunState();
        state.set_text(dbState);
        state.setSeverity(states.get(dbState));
        return state;
    }

}
