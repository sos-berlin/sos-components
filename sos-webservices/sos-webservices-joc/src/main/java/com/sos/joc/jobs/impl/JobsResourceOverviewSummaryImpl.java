package com.sos.joc.jobs.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResourceOverviewSummary;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.JobsHistoricSummary;
import com.sos.joc.model.job.JobsOverView;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("jobs")
public class JobsResourceOverviewSummaryImpl extends JOCResourceImpl implements IJobsResourceOverviewSummary {

    private static final String API_CALL = "./jobs/overview/summary";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsResourceOverviewSummaryImpl.class);

    @Override
    public JOCDefaultResponse postJobsOverviewSummary(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, JobsFilter.class);
            JobsFilter jobsFilter = Globals.objectMapper.readValue(filterBytes, JobsFilter.class);
            
            String controllerId = jobsFilter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                if (Proxies.getControllerDbInstances().isEmpty()) {
                    permitted = getBasicControllerDefaultPermissions(accessToken).getOrders().getView();
                } else {
                    allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                            availableController -> getBasicControllerPermissions(availableController, accessToken).getOrders().getView()).collect(
                                    Collectors.toSet());
                    permitted = !allowedControllers.isEmpty();
                    if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                        allowedControllers = Collections.emptySet();
                    }
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getBasicControllerPermissions(controllerId, accessToken).getOrders().getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JobsHistoricSummary jobsHistoricSummary = new JobsHistoricSummary();
            JobsOverView entity = new JobsOverView();
            if (Proxies.getControllerDbInstances().isEmpty()) {
                entity.setSurveyDate(Date.from(Instant.now()));
                entity.setJobs(jobsHistoricSummary);
                jobsHistoricSummary.setFailed(0L);
                jobsHistoricSummary.setSuccessful(0L);
                JocError jocError = getJocError();
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.warn(InventoryInstancesDBLayer.noRegisteredControllers());
                return JOCDefaultResponse.responseStatus200(entity);
            }
            
            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setControllerIds(allowedControllers);
            
            if (jobsFilter.getDateFrom() != null) {
                historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(jobsFilter.getDateFrom(), jobsFilter.getTimeZone()));
            }
            if (jobsFilter.getDateTo() != null) {
                historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(jobsFilter.getDateTo(), jobsFilter.getTimeZone()));
            }
            
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            entity.setSurveyDate(Date.from(Instant.now()));
            entity.setJobs(jobsHistoricSummary);
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, historyFilter);
            long failed = dbLayer.getCountJobs(HistoryStateText.FAILED, permittedFolders);
            long successful = dbLayer.getCountJobs(HistoryStateText.SUCCESSFUL, permittedFolders);
            session.close();
            session = null;
            
            jobsHistoricSummary.setFailed(failed);
            jobsHistoricSummary.setSuccessful(successful);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
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
