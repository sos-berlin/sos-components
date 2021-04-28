package com.sos.joc.jobs.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResourceOverviewSummary;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.JobsHistoricSummary;
import com.sos.joc.model.job.JobsOverView;
import com.sos.schema.JsonValidator;

@Path("jobs")
public class JobsResourceOverviewSummaryImpl extends JOCResourceImpl implements IJobsResourceOverviewSummary {

    private static final String API_CALL = "./jobs/overview/summary";

    @Override
    public JOCDefaultResponse postJobsOverviewSummary(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobsFilter.class);
            JobsFilter jobsFilter = Globals.objectMapper.readValue(filterBytes, JobsFilter.class);
            
            String controllerId = jobsFilter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                                Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet(); 
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JobsHistoricSummary jobsHistoricSummary = new JobsHistoricSummary();
            
            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setControllerIds(allowedControllers);
            
            if (jobsFilter.getDateFrom() != null) {
                historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(jobsFilter.getDateFrom(), jobsFilter.getTimeZone()));
            }
            if (jobsFilter.getDateTo() != null) {
                historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(jobsFilter.getDateTo(), jobsFilter.getTimeZone()));
            }
            
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            JobsOverView entity = new JobsOverView();
            entity.setSurveyDate(Date.from(Instant.now()));
            entity.setJobs(jobsHistoricSummary);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JobHistoryDBLayer jobHistoryDBLayer = new JobHistoryDBLayer(connection, historyFilter);
            jobsHistoricSummary.setFailed(jobHistoryDBLayer.getCountJobs(HistoryStateText.FAILED, permittedFolders));
            jobsHistoricSummary.setSuccessful(jobHistoryDBLayer.getCountJobs(HistoryStateText.SUCCESSFUL, permittedFolders));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
