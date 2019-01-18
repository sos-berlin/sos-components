package com.sos.joc.task.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.Task;
import com.sos.joc.model.job.Task200;
import com.sos.joc.model.job.TaskCause;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.task.resource.ITaskResource;

@Path("task")
public class TaskResourceImpl extends JOCResourceImpl implements ITaskResource {

	private static final String API_CALL = "./task";

	@Override
	public JOCDefaultResponse postTask(String xAccessToken, String accessToken, TaskFilter taskFilter)
			throws Exception {
		return postTask(getAccessToken(xAccessToken, accessToken), taskFilter);
	}

	public JOCDefaultResponse postTask(String accessToken, TaskFilter taskFilter) throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, taskFilter, accessToken,
					taskFilter.getJobschedulerId(),
					getPermissonsJocCockpit(taskFilter.getJobschedulerId(), accessToken).getJob().getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			Task task = new Task();
			task.set_cause(TaskCause.NONE);
			Task200 entity = new Task200();
			entity.setDeliveryDate(Date.from(Instant.now()));
			entity.setSurveyDate(Date.from(Instant.now()));
			entity.setTask(task);

			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}
}
