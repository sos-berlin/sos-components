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
		dbItem.setComment(jsObject.getComment());
		dbItem.setContent(om.writeValueAsString(jsObject));
		dbItem.setEditAccount(jsObject.getEditAccount());
		dbItem.setModified(jsObject.getModified());
		dbItem.setObjectType(jsObject.getTYPE().value());
		dbItem.setParentVersion(jsObject.getParentVersion());
		dbItem.setPath(jsObject.getPath());
		dbItem.setPublishAccount(jsObject.getPublishAccount());
		dbItem.setSchedulerId(jsObject.getJobschedulerId());
		dbItem.setState(jsObject.getState());
		dbItem.setUri(jsObject.getUri());
		if (jsObject.getValid() == null) {
			dbItem.setValid(false);
		} else {
			dbItem.setValid(jsObject.getValid());
		}
		dbItem.setVersion(jsObject.getVersion());
		dbItem.setId(jsObject.getId());
		return dbItem;
	}

	public static JSObject mapDBitemToJsObject (final DBItemJSObject dbItem) throws JsonParseException, JsonMappingException,
		IOException {
		ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
		JSObject jsObject = new JSObject();
		jsObject.setComment(dbItem.getComment());
		jsObject.setEditAccount(dbItem.getEditAccount());
		jsObject.setModified(dbItem.getModified());
		jsObject.setTYPE(DeployType.fromValue(dbItem.getObjectType()));
		if(DeployType.WORKFLOW.equals(jsObject.getTYPE())) {
			jsObject.setContent(om.readValue(dbItem.getContent(), Workflow.class));
		} else if (DeployType.AGENT_REF.equals(jsObject.getTYPE())) {
			jsObject.setContent(om.readValue(dbItem.getContent(), AgentRef.class));
		}
		jsObject.setParentVersion(dbItem.getParentVersion());
		jsObject.setPath(dbItem.getPath());
		jsObject.setPublishAccount(dbItem.getPublishAccount());
		jsObject.setJobschedulerId(dbItem.getSchedulerId());
		jsObject.setState(dbItem.getState());
		jsObject.setUri(dbItem.getUri());
		jsObject.setValid(dbItem.isValid());
		jsObject.setVersion(dbItem.getVersion());
		jsObject.setId(dbItem.getId());
		return jsObject;
	}

}
