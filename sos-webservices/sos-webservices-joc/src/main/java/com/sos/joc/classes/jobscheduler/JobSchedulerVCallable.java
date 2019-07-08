package com.sos.joc.classes.jobscheduler;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.jobscheduler.ClusterMemberType;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.JobSchedulerState;
import com.sos.joc.model.jobscheduler.JobSchedulerStateText;
import com.sos.joc.model.jobscheduler.JobSchedulerV;

public class JobSchedulerVCallable implements Callable<JobSchedulerV> {
    private final DBItemInventoryInstance dbItemInventoryInstance;
    private final String accessToken;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerVCallable.class);
    
    public JobSchedulerVCallable(DBItemInventoryInstance dbItemInventoryInstance, String accessToken) {
        this.dbItemInventoryInstance = dbItemInventoryInstance;
        this.accessToken = accessToken;
    }
    
    @Override
    public JobSchedulerV call() throws Exception {
    	JsonObject answer = null;
    	try {
			JOCJsonCommand jocJsonCommand = new JOCJsonCommand(dbItemInventoryInstance);
			jocJsonCommand.setUriBuilderForOverview();
			answer = jocJsonCommand.getJsonObjectFromGet(accessToken);
		} catch (JocException e) {
			LOGGER.info("",e);
		}
    	return getJobScheduler(answer);
    }
    
    private JobSchedulerV getJobScheduler(JsonObject answer) {
        JobSchedulerV js = new JobSchedulerV();
        if (answer != null) {
        	//Date surveyDate = JobSchedulerDate.getDateFromEventId(answer.getJsonNumber("eventId").longValue());
        	js.setSurveyDate(Date.from(Instant.now()));
        	js.setStartedAt(Date.from(Instant.ofEpochMilli(answer.getJsonNumber("startedAt").longValue())));
            //js.setStartedAt(JobSchedulerDate.getDateFromISO8601String(answer.getString("startedAt", null)));
            js.setState(getJobSchedulerState("running")); //TODO is not an answer
        } else {
        	js.setState(getJobSchedulerState("unreachable"));
        }
        js.setHost(dbItemInventoryInstance.getHostname());
        js.setJobschedulerId(dbItemInventoryInstance.getSchedulerId());
        js.setPort(dbItemInventoryInstance.getPort());
        ClusterMemberType clusterMemberTypeSchema = new ClusterMemberType();
        clusterMemberTypeSchema.setPrecedence(dbItemInventoryInstance.getPrecedence());
        clusterMemberTypeSchema.set_type(ClusterType.fromValue(dbItemInventoryInstance.getClusterType()));
        js.setClusterType(clusterMemberTypeSchema);
        js.setUrl(dbItemInventoryInstance.getUrl());
        return js;
    }
    
	private JobSchedulerState getJobSchedulerState(String state) {
		JobSchedulerState jobSchedulerState = new JobSchedulerState();
		switch (state) {
		case "starting":
			jobSchedulerState.set_text(JobSchedulerStateText.STARTING);
			jobSchedulerState.setSeverity(0);
			break;
		case "running":
			jobSchedulerState.set_text(JobSchedulerStateText.RUNNING);
			jobSchedulerState.setSeverity(0);
			break;
		case "paused":
			jobSchedulerState.set_text(JobSchedulerStateText.PAUSED);
			jobSchedulerState.setSeverity(1);
			break;
		case "stopping":
		case "stopping_let_run":
		case "stopped":
			jobSchedulerState.set_text(JobSchedulerStateText.TERMINATING);
			jobSchedulerState.setSeverity(3);
			break;
		case "waiting_for_activation":
			jobSchedulerState.set_text(JobSchedulerStateText.WAITING_FOR_ACTIVATION);
			jobSchedulerState.setSeverity(3);
			break;
		case "unreachable":
			jobSchedulerState.set_text(JobSchedulerStateText.UNREACHABLE);
			jobSchedulerState.setSeverity(2);
			break;
		}
		return jobSchedulerState;
	}
}
