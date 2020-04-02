package com.sos.joc.publish.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.PublishImportAudit;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.PublishImportFilter;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.resource.IUploadSignedResource;

@Path("deploy")
public class UploadSignedImpl extends JOCResourceImpl implements IUploadSignedResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSignedImpl.class);
    private static final String API_CALL = "./deploy/upload";
    private static final List<String> SUPPORTED_SUBTYPES = new ArrayList<String>(Arrays.asList(
    		JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(),
    		JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value(),
    		JSObjectFileExtension.LOCK_FILE_EXTENSION.value()));
    private SOSHibernateSession connection = null;

    @Override
	public JOCDefaultResponse postUploadSignedConfiguration(String xAccessToken, 
			String jobschedulerId, 
			FormDataBodyPart body, 
			String timeSpent,
			String ticketLink,
			boolean updateRepo,
			String comment) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
		return postUploadSignedConfiguration(xAccessToken, jobschedulerId, body, auditLog);
	}

	public JOCDefaultResponse postUploadSignedConfiguration(String xAccessToken, String jobschedulerId, FormDataBodyPart body,
			AuditParams auditLog) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        try {
            PublishImportFilter filter = new PublishImportFilter();
//            filter.setAuditLog(auditLog);
            
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            }
            // copy&paste Permission, has o be changed to the correct permission for upload 
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, jobschedulerId,
            		/*getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getDocumentation().isImport()*/
            		true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

//            checkRequiredParameter("jobschedulerId", filter.getJobschedulerId());
            if (body == null) {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }

            stream = body.getEntityAs(InputStream.class);
            String extention = getExtensionFromFilename(uploadFileName);

            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            Optional<String> supportedSubType = SUPPORTED_SUBTYPES.stream().filter(s -> mediaSubType.contains(s)).findFirst();

            PublishImportAudit importAudit = new PublishImportAudit(filter);
            logAuditMessage(importAudit);

            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                readZipFileContent(stream, filter);
            } else {
            	throw new JocUnsupportedFileTypeException(String.format("The file %1$s to be uploaded must have the format zip!", uploadFileName)); 
            }
            
//            deployDocumentations();

            storeAuditLogEntry(importAudit);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
	}

    private String getExtensionFromFilename(String filename) {
        String extension = filename;
        if (filename == null) {
            return "";
        }
        if (extension.contains(".")) {
            extension = extension.replaceFirst(".*\\.([^\\.]+)$", "$1");
        } else {
            extension = "";
        }
        return extension.toLowerCase();
    }

    private void readZipFileContent(InputStream inputStream, PublishImportFilter filter) throws DBConnectionRefusedException, DBInvalidDataException,
            SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException, DBOpenSessionException {
        ZipInputStream zipStream = null;
//        Set<DBItemDocumentation> documentations = new HashSet<DBItemDocumentation>();
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                byte[] bytes = outBuffer.toByteArray();
                // TODO: read files from zip file an do something with it
                if (("/"+entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                	//processWorkflows
//                    setDeployDocumentations(bytes);
                    continue;
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value())) {
                	
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                	
                }
//                DBItemDocumentation documentation = new DBItemDocumentation();
//                documentation.setSchedulerId(filter.getJobschedulerId());
//                java.nio.file.Path targetFolder = Paths.get(filter.getFolder());
//                java.nio.file.Path complete = targetFolder.resolve(entryName.replaceFirst("^/", ""));
//                documentation.setPath(complete.toString().replace('\\', '/'));
//                documentation.setDirectory(complete.getParent().toString().replace('\\', '/'));
//                documentation.setName(complete.getFileName().toString());
//                String fileExtension = getExtensionFromFilename(documentation.getName());
//                boolean isPlainText = isPlainText(bytes);
//                final String guessedMediaType = guessContentTypeFromBytes(bytes, fileExtension, isPlainText);
//                if (guessedMediaType != null) {
//                    Optional<String> supportedSubType = SUPPORTED_SUBTYPES.stream().filter(s -> guessedMediaType.contains(s)).findFirst();
//                    Optional<String> supportedImageType = SUPPORTED_IMAGETYPES.stream().filter(s -> guessedMediaType.contains(s)).findFirst();
//                    if (supportedImageType.isPresent()) {
//                        documentation.setType(supportedImageType.get());
//                        documentation.setImage(bytes);
//                        documentation.setHasImage(true);
//                    } else if (supportedSubType.isPresent()) {
//                        documentation.setType(supportedSubType.get());
//                        documentation.setContent(new String(bytes, Charsets.UTF_8));
//                        documentation.setHasImage(false);
//                    } else {
//                        throw new JocUnsupportedFileTypeException(String.format("%1$s unsupported, supported types are %2$s", complete.toString()
//                                .replace('\\', '/'), SUPPORTED_SUBTYPES.toString()));
//                    }
//                } else {
//                    throw new JocUnsupportedFileTypeException(String.format("%1$s unsupported, supported types are %2$s", complete.toString().replace(
//                            '\\', '/'), SUPPORTED_SUBTYPES.toString()));
//                }
//                documentation.setCreated(Date.from(Instant.now()));
//                documentation.setModified(documentation.getCreated());
//                documentations.add(documentation);
//            }
//            if (!documentations.isEmpty()) {
//                if (connection == null) {
//                    connection = Globals.createSosHibernateStatelessConnection(API_CALL);
//                }
//                DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
//                for (DBItemDocumentation itemDocumentation : documentations) {
//                    saveOrUpdate(dbLayer, itemDocumentation);
//                }
//            } else {
//                throw new JocUnsupportedFileTypeException("The zip file to upload doesn't contain any supported file, supported types are "
//                        + SUPPORTED_SUBTYPES.toString());
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
