package com.sos.joc.deploy.mapper;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.deploy.JSObject;

public class WorkflowToJSObjectMapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowToJSObjectMapper.class);
	
	public static JSObject mapToJSObject (Workflow workflow) {
		try {
			ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
			JSObject jsObject = new JSObject();
			jsObject.setId(0L);
			jsObject.setJobschedulerId("");
			jsObject.setEditAccount("");
			jsObject.setPublishAccount("");
			jsObject.setPath(workflow.getPath());
			jsObject.setObjectType(workflow.getTYPE().toString());
			jsObject.setContent(om.writeValueAsString(workflow));
			jsObject.setUri(null);
			jsObject.setState("");
			jsObject.setValid(true);
			jsObject.setVersion(Integer.getInteger(workflow.getVersionId()));
			jsObject.setParentVersion(Integer.getInteger(workflow.getVersionId()));
			jsObject.setComment("");
			jsObject.setModified(Date.from(Instant.now()));
			return jsObject;
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}
}
