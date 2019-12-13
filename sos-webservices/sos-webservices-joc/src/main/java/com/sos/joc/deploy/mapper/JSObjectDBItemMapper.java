package com.sos.joc.deploy.mapper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.model.deploy.JSObject;

public class JSObjectDBItemMapper {

	public static DBItemJSObject mapJsObjectToDBitem (final JSObject jsObject) throws JsonProcessingException {
		ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
		DBItemJSObject dbItem = new DBItemJSObject();
		dbItem.setId(jsObject.getId());
		dbItem.setComment(jsObject.getComment());
		if (DeployType.WORKFLOW == jsObject.getObjectType()) {
			Workflow workflow = (Workflow)jsObject.getContent();
			dbItem.setContent(om.writeValueAsString(workflow));
			dbItem.setObjectType(workflow.getTYPE().value());
			dbItem.setPath(workflow.getPath());
			
		} else if (DeployType.AGENT_REF == jsObject.getObjectType()) {
			AgentRef agentRef = (AgentRef)jsObject.getContent();
			dbItem.setContent(om.writeValueAsString(agentRef));
			dbItem.setObjectType(agentRef.getTYPE().value());
			dbItem.setPath(agentRef.getPath());
		}
		dbItem.setEditAccount(jsObject.getEditAccount());
		dbItem.setModified(jsObject.getModified());
		dbItem.setParentVersion(jsObject.getParentVersion());
		dbItem.setPublishAccount(jsObject.getPublishAccount());
		dbItem.setSchedulerId(jsObject.getJobschedulerId());
		dbItem.setVersion(jsObject.getVersion());
		return dbItem;
	}

	public static JSObject mapDBitemToJsObject (final DBItemJSObject dbItem) throws JsonParseException, JsonMappingException,
		IOException {
		ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
		JSObject jsObject = new JSObject();
		jsObject.setComment(dbItem.getComment());
		jsObject.setEditAccount(dbItem.getEditAccount());
		jsObject.setModified(dbItem.getModified());
		jsObject.setObjectType(DeployType.fromValue(dbItem.getObjectType()));
		if(jsObject.getObjectType() == DeployType.WORKFLOW) {
			Workflow workflow = om.readValue(dbItem.getContent(), Workflow.class);
			jsObject.setContent(workflow);
			jsObject.setVersion(workflow.getVersionId());
		} else if (jsObject.getObjectType() == DeployType.AGENT_REF) {
			AgentRef agentRef = om.readValue(dbItem.getContent(), AgentRef.class);
			jsObject.setContent(agentRef);
			jsObject.setVersion(agentRef.getVersionId());
		}
		jsObject.setParentVersion(dbItem.getParentVersion());
		jsObject.setPublishAccount(dbItem.getPublishAccount());
		jsObject.setJobschedulerId(dbItem.getSchedulerId());
		jsObject.setId(dbItem.getId());
		return jsObject;
	}

}
