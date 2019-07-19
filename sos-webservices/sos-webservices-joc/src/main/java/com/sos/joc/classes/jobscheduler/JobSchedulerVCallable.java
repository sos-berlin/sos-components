package com.sos.joc.classes.jobscheduler;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
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
        	//TODO Sends unexpected timestamp
        	js.setStartedAt(Date.from(Instant.ofEpochMilli(answer.getJsonNumber("startedAt").longValue())));
            //js.setStartedAt(JobSchedulerDate.getDateFromISO8601String(answer.getString("startedAt", null)));
        	//TODO state is not in the answer
            js.setState(getJobSchedulerState("running"));
        } else {
        	js.setState(getJobSchedulerState("unreachable"));
        }
        js.setJobschedulerId(dbItemInventoryInstance.getSchedulerId());
        ClusterMemberType clusterMemberTypeSchema = new ClusterMemberType();
        if (dbItemInventoryInstance.getCluster()) {
        	clusterMemberTypeSchema.set_type(ClusterType.PASSIVE);
        	clusterMemberTypeSchema.setPrecedence(dbItemInventoryInstance.getPrimaryMaster() ? 0 : 1);
        } else {
        	clusterMemberTypeSchema.set_type(ClusterType.STANDALONE);
        	clusterMemberTypeSchema.setPrecedence(0);
        }
        js.setClusterType(clusterMemberTypeSchema);
        js.setUrl(dbItemInventoryInstance.getUri());
        //TODO
        js.setHost(JOCResourceImpl.toHost(dbItemInventoryInstance.getUri()));
        js.setPort(JOCResourceImpl.toPort(dbItemInventoryInstance.getUri()));
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
