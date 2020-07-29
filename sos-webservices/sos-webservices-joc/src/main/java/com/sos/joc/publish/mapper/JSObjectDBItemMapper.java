package com.sos.joc.publish.mapper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.model.publish.JSObject;

public class JSObjectDBItemMapper {

	public static DBItemDeploymentHistory mapJsObjectToDBitem (final JSObject jsObject) throws JsonProcessingException {
		ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
		DBItemDeploymentHistory dbItem = new DBItemDeploymentHistory();
		dbItem.setId(jsObject.getId());
		if (DeployType.WORKFLOW.equals(jsObject.getObjectType())) {
			Workflow workflow = (Workflow)jsObject.getContent();
			dbItem.setContent(om.writeValueAsString(workflow));
			dbItem.setObjectType(workflow.getTYPE().ordinal());
			dbItem.setPath(workflow.getPath());
			
		} else if (DeployType.AGENT_REF.equals(jsObject.getObjectType())) {
			AgentRef agentRef = (AgentRef)jsObject.getContent();
			dbItem.setContent(om.writeValueAsString(agentRef));
			dbItem.setObjectType(agentRef.getTYPE().ordinal());
			dbItem.setPath(agentRef.getPath());
		} else if (DeployType.LOCK.equals(jsObject.getObjectType())) {
            // TODO: 
        } else if (DeployType.JUNCTION.equals(jsObject.getObjectType())) {
            // TODO: 
        }
		dbItem.setVersion(jsObject.getVersion());
		return dbItem;
	}

	public static JSObject mapDBitemToJsObject (final DBItemDeploymentHistory dbItem) throws JsonParseException, JsonMappingException,
		IOException {
		ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
		JSObject jsObject = new JSObject();
		switch(dbItem.getObjectType()) {
		case 0:
	        jsObject.setObjectType(DeployType.WORKFLOW);		    
		    break;
		case 1:
            jsObject.setObjectType(DeployType.AGENT_REF);            
		    break;
        case 2:
            jsObject.setObjectType(DeployType.LOCK);            
            break;
        case 3:
            jsObject.setObjectType(DeployType.JUNCTION);            
            break;
        default:
            jsObject.setObjectType(DeployType.WORKFLOW);            
		}
		if(DeployType.WORKFLOW.equals(jsObject.getObjectType())) {
			Workflow workflow = om.readValue(dbItem.getContent(), Workflow.class);
			jsObject.setContent(workflow);
			jsObject.setVersion(workflow.getVersionId());
		} else if (DeployType.AGENT_REF.equals(jsObject.getObjectType())) {
			AgentRef agentRef = om.readValue(dbItem.getContent(), AgentRef.class);
			jsObject.setContent(agentRef);
			jsObject.setVersion(agentRef.getVersionId());
		} else if (DeployType.LOCK.equals(jsObject.getObjectType())) {
            // TODO:
        } else if (DeployType.JUNCTION.equals(jsObject.getObjectType())) {
            // TODO:
        }
		jsObject.setId(dbItem.getId());
		return jsObject;
	}

}
