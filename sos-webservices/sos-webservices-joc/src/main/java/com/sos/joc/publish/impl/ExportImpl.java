package com.sos.joc.publish.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemDeployedConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemInventoryConfiguration;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IExportResource;

@Path("publish")
public class ExportImpl extends JOCResourceImpl implements IExportResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImpl.class);
    private static final String API_CALL = "./publish/export";
    private static final String SIGNATURE_EXTENSION = ".asc";

    
	@Override
	public JOCDefaultResponse postExportConfiguration(String xAccessToken, ExportFilter filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "", 
            		/*getPermissonsJocCockpit("", xAccessToken).getDocumentation().isExport()*/
            		true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            final Set<JSObject> jsObjects = getObjectsFromDB(filter, hibernateSession);
            String targetFilename = "bundle_js_objects.zip";
            StreamingOutput streamingOutput = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    ZipOutputStream zipOut = null;
                    try {
                        zipOut = new ZipOutputStream(new BufferedOutputStream(output), Charsets.UTF_8);
                        String content = null;
                        for (JSObject jsObject : jsObjects) {
                        	String extension = null;
                        	switch(jsObject.getObjectType()) {
                        	case WORKFLOW : 
                        		extension = ".workflow.json";
                        		content = Globals.objectMapper.writeValueAsString((Workflow)jsObject.getContent());
                        		break;
                        	case AGENT_REF :
                        		extension = ".agentRef.json";
                                content = Globals.objectMapper.writeValueAsString((AgentRef)jsObject.getContent());
                        		break;
                    		default:
                    			extension = ".workflow.json";
                        	}
                        	String zipEntryName = jsObject.getPath().substring(1).concat(extension); 
                            ZipEntry entry = new ZipEntry(zipEntryName);
                            zipOut.putNextEntry(entry);
                            zipOut.write(content.getBytes());
                            zipOut.closeEntry();
                            
                            if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                                String signatureZipEntryName = zipEntryName.concat(SIGNATURE_EXTENSION);
                                ZipEntry signatureEntry = new ZipEntry(signatureZipEntryName);
                                zipOut.putNextEntry(signatureEntry);
                                zipOut.write(jsObject.getSignedContent().getBytes());
                                zipOut.closeEntry();
                            }
                        }
                        zipOut.flush();
                    } finally {
                        if (zipOut != null) {
                            try {
                                zipOut.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            };
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(streamingOutput, targetFilename);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
	}

    private Set<JSObject> getObjectsFromDB(ExportFilter filter, SOSHibernateSession connection)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException {
        DBLayerDeploy dbLayer = new DBLayerDeploy(connection);
        Set<JSObject> allObjects = new HashSet<JSObject>();
        
        if (filter.getJsObjectPaths() != null) {
            List<DBItemDeployedConfiguration> jsObjectDbItems = dbLayer.getFilteredDeployedConfigurations(filter);
            for (DBItemDeployedConfiguration jsObject : jsObjectDbItems) {
                allObjects.add(mapObjectDBItemToJSObject(jsObject));
            } 
            List<DBItemInventoryConfiguration> jsDraftObjectDbItems = dbLayer.getFilteredInventoryConfigurationsForExport(filter);
            for (DBItemInventoryConfiguration jsDraftObject : jsDraftObjectDbItems) {
                allObjects.add(mapInventoryCfgToDeployedCfg(jsDraftObject));
            } 
        }
        return allObjects;
    }
    
    private JSObject mapInventoryCfgToDeployedCfg (DBItemInventoryConfiguration item) throws JsonParseException, JsonMappingException, IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(DeployType.fromValue(item.getObjectType()));
        switch (jsObject.getObjectType()) {
            case WORKFLOW:
                jsObject.setContent(Globals.objectMapper.readValue(item.getContent().getBytes(), Workflow.class));
                break;
            case AGENT_REF:
                jsObject.setContent(Globals.objectMapper.readValue(item.getContent().getBytes(), AgentRef.class));
                break;
            case LOCK:
                // TODO: 
                break;
        }
        jsObject.setSignedContent(item.getSignedContent());
        jsObject.setEditAccount(item.getEditAccount());
//        jsObject.setPublishAccount(item.getPublishAccount());
        jsObject.setVersion(item.getVersion());
        jsObject.setParentVersion(item.getParentVersion());
        jsObject.setComment(item.getComment());
        jsObject.setModified(item.getModified());
        return jsObject;
    }

    private JSObject mapObjectDBItemToJSObject (DBItemDeployedConfiguration item) throws JsonParseException, JsonMappingException, IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(DeployType.fromValue(item.getObjectType()));
        switch (jsObject.getObjectType()) {
            case WORKFLOW:
                jsObject.setContent(Globals.objectMapper.readValue(item.getContent().getBytes(), Workflow.class));
                break;
            case AGENT_REF:
                jsObject.setContent(Globals.objectMapper.readValue(item.getContent().getBytes(), AgentRef.class));
                break;
            case LOCK:
                // TODO: 
                break;
        }
        jsObject.setSignedContent(item.getSignedContent());
        jsObject.setEditAccount(item.getEditAccount());
        jsObject.setPublishAccount(item.getPublishAccount());
        jsObject.setVersion(item.getVersion());
        jsObject.setParentVersion(item.getParentVersion());
        jsObject.setComment(item.getComment());
        jsObject.setModified(item.getModified());
        return jsObject;
    }
}
