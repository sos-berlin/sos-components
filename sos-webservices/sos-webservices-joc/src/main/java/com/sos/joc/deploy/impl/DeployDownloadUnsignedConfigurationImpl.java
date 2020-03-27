package com.sos.joc.deploy.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemJSObject;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployContent;
import com.sos.joc.db.deploy.DeployDBLayer;
import com.sos.joc.deploy.resource.IDeployDownloadUnsignedConfigurationResource;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.publish.DeployFilter;

@Path("deploy")
public class DeployDownloadUnsignedConfigurationImpl extends JOCResourceImpl implements IDeployDownloadUnsignedConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployDownloadUnsignedConfigurationImpl.class);
    private static final String API_CALL = "./deploy/download";
    
	@Override
	public JOCDefaultResponse postDownloadUnsignedConfiguration(String xAccessToken, DeployFilter filter) throws Exception {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, filter.getJobschedulerId(), 
            		/*getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getDocumentation().isExport()*/
            		true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            final List<DeployContent> contents = mapToDeployContents(filter, connection);
            String targetFilename = "unsigned_" + filter.getJobschedulerId() + ".zip";

            StreamingOutput streamingOutput = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    ZipOutputStream zipOut = null;
                    try {
                        zipOut = new ZipOutputStream(new BufferedOutputStream(output), Charsets.UTF_8);
                        for (DeployContent content : contents) {
                        	String extension = null;
                        	switch(content.getType()) {
                        	case "WORKFLOW" : 
                        		extension = ".workflow.json";
                        		break;
                        	case "AGENT_REF" :
                        		extension = ".agentRef.json";
                        		break;
                    		default:
                    			extension = ".workflow.json";
                        	}
                        	String zipEntryName = content.getPath().substring(1) + extension; 
                            ZipEntry entry = new ZipEntry(zipEntryName);
                            zipOut.putNextEntry(entry);
                            zipOut.write(content.getContent());
                            zipOut.closeEntry();
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
            Globals.disconnect(connection);
        }
	}

    private List<DeployContent> mapToDeployContents(DeployFilter filter, SOSHibernateSession connection)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, JsonProcessingException,
            DBMissingDataException {
        DeployDBLayer dbLayer = new DeployDBLayer(connection);
        List<DBItemJSObject> jsObjects = dbLayer.getJobSchedulerObjectsByConfiguration(filter.getJobschedulerId());
        List<DeployContent> contents = new ArrayList<DeployContent>();
        for(DBItemJSObject jsObject : jsObjects) {
        	DeployContent content = new DeployContent(jsObject.getPath(), jsObject.getContent().getBytes(Charsets.UTF_8), jsObject.getObjectType());
            contents.add(content);
        }
        return contents;
    }
    
}
