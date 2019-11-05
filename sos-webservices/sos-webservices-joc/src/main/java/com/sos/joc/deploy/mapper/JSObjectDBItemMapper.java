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
import com.sos.joc.model.deploy.IJSObject;
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
			dbItem.setUri(agentRef.getUri());
			dbItem.setPath(agentRef.getPath());
		}
		dbItem.setEditAccount(jsObject.getEditAccount());
		dbItem.setModified(jsObject.getModified());
		dbItem.setParentVersion(jsObject.getParentVersion());
		dbItem.setPublishAccount(jsObject.getPublishAccount());
		dbItem.setSchedulerId(jsObject.getJobschedulerId());
		dbItem.setState(jsObject.getState());
		if (jsObject.getValid() == null) {
			dbItem.setValid(false);
		} else {
			dbItem.setValid(jsObject.getValid());
		}
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
//		jsObject.setTYPE(DeployType.fromValue(dbItem.getObjectType()));
		IJSObject iJsObject = om.readValue(dbItem.getContent(), IJSObject.class);
		if(iJsObject instanceof Workflow) {
			jsObject.setContent(om.readValue(dbItem.getContent(), Workflow.class));
		} else if (iJsObject instanceof AgentRef) {
			jsObject.setContent(om.readValue(dbItem.getContent(), AgentRef.class));
		}
		jsObject.setParentVersion(dbItem.getParentVersion());
		jsObject.setPublishAccount(dbItem.getPublishAccount());
		jsObject.setJobschedulerId(dbItem.getSchedulerId());
		jsObject.setState(dbItem.getState());
//		jsObject.setPath(jsObject.getContent());
//		jsObject.setUri(dbItem.getUri());
		jsObject.setValid(dbItem.isValid());
		jsObject.setVersion(dbItem.getVersion());
		jsObject.setId(dbItem.getId());
		return jsObject;
	}

}
