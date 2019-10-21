package com.sos.joc.deploy.mapper;

import java.time.Instant;
import java.util.Date;

import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.joc.model.deploy.JSObject;

public abstract class AgentRefToJSObjectMapper {

	public static JSObject mapToJSObject (AgentRef agentRef) {
		JSObject jsObject = new JSObject();
		jsObject.setId(0L);
		jsObject.setJobschedulerId(null);
		jsObject.setEditAccount("");
		jsObject.setPublishAccount("");
		jsObject.setPath(agentRef.getPath());
		jsObject.setObjectType(agentRef.getTYPE().toString());
		jsObject.setContent(null);
		jsObject.setUri(agentRef.getUri());
		jsObject.setState(null);
		jsObject.setValid(true);
		jsObject.setVersion(Integer.getInteger(agentRef.getVersionId()));
		jsObject.setParentVersion(Integer.getInteger(agentRef.getVersionId()));
		jsObject.setComment("");
		jsObject.setModified(Date.from(Instant.now()));
		return jsObject;
	}
}
