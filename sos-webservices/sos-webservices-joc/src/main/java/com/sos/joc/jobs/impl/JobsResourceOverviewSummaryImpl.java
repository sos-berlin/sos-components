package com.sos.joc.jobs.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResourceOverviewSummary;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.JobsHistoricSummary;
import com.sos.joc.model.job.JobsOverView;

@Path("jobs")
public class JobsResourceOverviewSummaryImpl extends JOCResourceImpl implements IJobsResourceOverviewSummary {

    private static final String API_CALL = "./jobs/overview/summary";

    @Override
    public JOCDefaultResponse postJobsOverviewSummary(String accessToken, JobsFilter jobsFilter) throws Exception {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobsFilter, accessToken, jobsFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    jobsFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JobsHistoricSummary jobsHistoricSummary = new JobsHistoricSummary();
            
            HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setSchedulerId(jobsFilter.getJobschedulerId());
            
            boolean withFolderFilter = jobsFilter.getFolders() != null && !jobsFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            
            List<Folder> folders = addPermittedFolder(jobsFilter.getFolders());

            if (jobsFilter.getDateFrom() != null) {
                historyFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(jobsFilter.getDateFrom(), jobsFilter.getTimeZone()));
            }
            if (jobsFilter.getDateTo() != null) {
                historyFilter.setExecutedTo(JobSchedulerDate.getDateTo(jobsFilter.getDateTo(), jobsFilter.getTimeZone()));
            }

            if (!jobsFilter.getJobs().isEmpty()) {
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                for (JobPath jobPath : jobsFilter.getJobs()) {
                    if (jobPath != null && canAdd(jobPath.getJob(), permittedFolders)) {
                        historyFilter.addJob(jobPath.getJob());
                    }
                }
            } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                historyFilter.addFolders(jobsFilter.getFolders());
            }

            JobsOverView entity = new JobsOverView();
            entity.setSurveyDate(Date.from(Instant.now()));
            entity.setJobs(jobsHistoricSummary);
            JobHistoryDBLayer jobHistoryDBLayer = new JobHistoryDBLayer(connection, historyFilter);
            if (hasPermission) {
                jobsHistoricSummary.setFailed(jobHistoryDBLayer.getCountJobHistoryFromTo(HistoryStateText.FAILED));
                jobsHistoricSummary.setSuccessful(jobHistoryDBLayer.getCountJobHistoryFromTo(HistoryStateText.SUCCESSFUL));
            }
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
