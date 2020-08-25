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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.publish.ExportFilter;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.resource.IExportResource;
import com.sos.joc.publish.util.PublishUtils;

@Path("publish")
public class ExportImpl extends JOCResourceImpl implements IExportResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImpl.class);
    private static final String API_CALL = "./publish/export";
    private static final String SIGNATURE_EXTENSION = ".asc";
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();
    
	@Override
	public JOCDefaultResponse postExportConfiguration(String xAccessToken, ExportFilter filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "", 
            		/*getPermissonsJocCockpit("", xAccessToken).getPublish().isExport()*/
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
                        		extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                        		content = om.writeValueAsString((Workflow)jsObject.getContent());
                        		break;
                            case AGENT_REF :
                                extension = JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.toString();
                                content = om.writeValueAsString((AgentRef)jsObject.getContent());
                                break;
                            case LOCK :
                                extension = JSObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                // TODO:
//                                content = om.writeValueAsString((Lock)jsObject.getContent());
                                break;
                            case JUNCTION :
                                extension = JSObjectFileExtension.JUNCTION_FILE_EXTENSION.toString();
                                // TODO:
//                                content = om.writeValueAsString((Junction)jsObject.getContent());
                                break;
                    		default:
                    			extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
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
            List<DBItemDeploymentHistory> jsObjectDbItems = dbLayer.getFilteredDeployedConfigurations(filter);
            for (DBItemDeploymentHistory jsObject : jsObjectDbItems) {
                allObjects.add(mapDepHistoryToJSObject(jsObject));
            } 
            List<DBItemInventoryConfiguration> jsDraftObjectDbItems = dbLayer.getFilteredInventoryConfigurationsForExport(filter);
            for (DBItemInventoryConfiguration jsDraftObject : jsDraftObjectDbItems) {
                allObjects.add(mapInvConfigToDepHistory(jsDraftObject));
            } 
        }
        return allObjects;
    }
    
    private JSObject mapInvConfigToDepHistory (DBItemInventoryConfiguration item) throws JsonParseException, JsonMappingException, IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(PublishUtils.mapInventoryMetaConfigurationType(InventoryMeta.ConfigurationType.fromValue(item.getType())));
        switch (jsObject.getObjectType()) {
            case WORKFLOW:
                jsObject.setContent(om.readValue(item.getContent().getBytes(), Workflow.class));
                break;
            case AGENT_REF:
                jsObject.setContent(om.readValue(item.getContent().getBytes(), AgentRef.class));
                break;
            case LOCK:
                // TODO: 
                break;
            case JUNCTION:
                // TODO: 
                break;
            default:
                break;
        }
        // TODO: get Signature from different Table
//        jsObject.setSignedContent(item.getSignedContent());
        // TOTOD: getAccount from calling web service
//        jsObject.setPublishAccount(item.getAccount());
//        jsObject.setVersion(item.getVersion());
//        jsObject.setParentVersion(item.getParentVersion());
        jsObject.setModified(item.getModified());
        return jsObject;
    }

    private JSObject mapDepHistoryToJSObject (DBItemDeploymentHistory item) throws JsonParseException, JsonMappingException, IOException {
        JSObject jsObject = new JSObject();
        jsObject.setId(item.getId());
        jsObject.setPath(item.getPath());
        jsObject.setObjectType(PublishUtils.getDeployTypeFromOrdinal(item.getObjectType()));
        switch (jsObject.getObjectType()) {
            case WORKFLOW:
                jsObject.setContent(om.readValue(item.getContent().getBytes(), Workflow.class));
                break;
            case AGENT_REF:
                jsObject.setContent(om.readValue(item.getContent().getBytes(), AgentRef.class));
                break;
            case LOCK:
                // TODO: 
                break;
            case JUNCTION:
                // TODO: 
                break;
        }
        jsObject.setSignedContent(item.getSignedContent());
        jsObject.setVersion(item.getVersion());
        return jsObject;
    }
    
}
