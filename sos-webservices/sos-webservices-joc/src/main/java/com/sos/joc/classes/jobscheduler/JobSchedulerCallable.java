package com.sos.joc.classes.jobscheduler;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;

public class JobSchedulerCallable implements Callable<JobSchedulerAnswer> {
	private final DBItemInventoryInstance dbItemInventoryInstance;
	private final DBItemOperatingSystem dbOsSystem;
	private final String accessToken;
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCallable.class);

    public JobSchedulerCallable(DBItemInventoryInstance dbItemInventoryInstance, DBItemOperatingSystem dbOsSystem, String accessToken) {
		this.dbItemInventoryInstance = dbItemInventoryInstance;
		this.dbOsSystem = dbOsSystem;
		this.accessToken = accessToken;
	}

	@Override
	public JobSchedulerAnswer call() throws JobSchedulerInvalidResponseDataException {
		Overview answer = null;
		try {
			JOCJsonCommand jocJsonCommand = new JOCJsonCommand(dbItemInventoryInstance, accessToken);
			jocJsonCommand.setUriBuilderForOverview();
			answer = jocJsonCommand.getJsonObjectFromGet(Overview.class);
		} catch (JobSchedulerInvalidResponseDataException e) {
			throw e;
		} catch (JocException e) {
			LOGGER.info("", e);
		}
		JobSchedulerAnswer js = new JobSchedulerAnswer(answer, dbItemInventoryInstance, dbOsSystem);
		js.setFields();
		return js;
	}
}
