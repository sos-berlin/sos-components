package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.report.Frequency;
import com.sos.inventory.model.report.ReportPeriod;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.reporting.DBItemReportRun;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.reporting.ReportRunState;
import com.sos.joc.model.reporting.ReportRunStateText;
import com.sos.joc.model.reporting.RunHistoryFilter;
import com.sos.joc.model.reporting.RunItem;
import com.sos.joc.model.reporting.RunItems;
import com.sos.joc.reporting.resource.IRunHistoryResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.REPORTING)
public class RunHistoryImpl extends JOCResourceImpl implements IRunHistoryResource {

    private static final Map<ReportRunStateText, Integer> states = Collections.unmodifiableMap(new HashMap<ReportRunStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(ReportRunStateText.SUCCESSFUL, 6); // darkblue
            put(ReportRunStateText.IN_PROGRESS, 3); // lightblue
            put(ReportRunStateText.FAILED, 2); // red
            put(ReportRunStateText.UNKNOWN, 2);
        }
    });

    private static final String monthFromToFormat = "yyyy-MM";
    private static final Logger LOGGER = LoggerFactory.getLogger(RunHistoryImpl.class);

    @Override
    public JOCDefaultResponse show(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, RunHistoryFilter.class);
            RunHistoryFilter in = Globals.objectMapper.readValue(filterBytes, RunHistoryFilter.class);

            JOCDefaultResponse response = initPermissions(null, getBasicJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            RunItems entity = new RunItems();
            final JocError jocError = getJocError();

            Function<DBItemReportRun, RunItem> mapToRunItem = dbItem -> {
                try {
                    if (!folderIsPermitted(dbItem.getFolder(), permittedFolders)) {
                        return null;
                    }
                    RunItem item = new RunItem();
                    item.setRunId(dbItem.getId());
                    item.setPath(dbItem.getPath());
                    item.setTitle(dbItem.getTitle());
                    item.setMonthFrom(getMonth(dbItem.getDateFrom()));
                    item.setMonthTo(getMonth(dbItem.getDateTo()));
                    item.setFrequencies(getSortedFrequencies(dbItem));
                    item.setHits(dbItem.getHits());
                    item.setNumOfReports(Long.valueOf(dbItem.getReportCount()));
                    item.setTemplateName(dbItem.getTemplateIdAsEnum());
                    item.setModified(dbItem.getModified());
                    item.setErrorText(dbItem.getErrorText());
                    item.setState(getState(dbItem.getStateAsEnum()));
                    item.setControllerId(dbItem.getControllerId());
                    item.setSort(dbItem.getSortAsEnum());
                    item.setPeriod(getPeriod(dbItem));
                    item.setVersion(null);
                    return item;
                } catch (Exception e) {
                    if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                        LOGGER.info(jocError.printMetaInfo());
                        jocError.clearMetaInfo();
                    }
                    LOGGER.error(String.format("[runId=%s] %s", dbItem.getId(), e.toString()));
                    return null;
                }
            };

            entity.setRuns(dbLayer.getRuns(in.getReportPaths(), in.getTemplateNames(), in.getStates()).stream().map(mapToRunItem).filter(
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

    private static ReportRunState getState(ReportRunStateText dbState) {
        ReportRunState state = new ReportRunState();
        state.set_text(dbState);
        state.setSeverity(states.get(dbState));
        return state;
    }

    private static ReportPeriod getPeriod(DBItemReportRun dbItem) {
        ReportPeriod rp = new ReportPeriod();
        rp.setLength(dbItem.getPeriodLength());
        rp.setStep(dbItem.getPeriodStep());
        return rp;
    }

    private static String getMonth(Date monthFromTo) throws SOSInvalidDataException {
        return SOSDate.format(monthFromTo, monthFromToFormat);
    }

    private static Set<Frequency> getSortedFrequencies(DBItemReportRun dbItem) {
        return Arrays.asList(dbItem.getFrequencies().split(",")).stream().map(Integer::valueOf).sorted().map(Frequency::fromValue).filter(
                Objects::nonNull).collect(Collectors.toSet());
    }

}
