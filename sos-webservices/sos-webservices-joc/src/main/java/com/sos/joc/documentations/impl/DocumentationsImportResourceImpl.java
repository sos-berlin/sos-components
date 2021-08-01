package com.sos.joc.documentations.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.documentation.DocumentationHelper;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.documentation.impl.DocumentationResourceImpl;
import com.sos.joc.documentations.resource.IDocumentationsImportResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.docu.DocumentationImport;

@Path("documentations")
public class DocumentationsImportResourceImpl extends JOCResourceImpl implements IDocumentationsImportResource {

    private static final String API_CALL = "./documentations/import";

    @Override
    public JOCDefaultResponse postImportDocumentations(String xAccessToken, String accessToken, String folder, FormDataBodyPart body,
            String timeSpent, String ticketLink, String comment) {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {
        }
        return postImportDocumentations(getAccessToken(xAccessToken, accessToken), folder, body, auditLog);
    }

    private JOCDefaultResponse postImportDocumentations(String xAccessToken, String folder, FormDataBodyPart body, AuditParams auditLog) {

        InputStream stream = null;
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, null, xAccessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getDocumentations().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            DocumentationImport filter = new DocumentationImport();
            if (body == null) {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            } else {
                filter.setFile(URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8"));
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbAudit = storeAuditLog(auditLog, null, CategoryType.DOCUMENTATIONS, connection);

            if (folder == null || folder.isEmpty()) {
                folder = "/";
            }
            if (!folderPermissions.isPermittedForFolder(folder)) {
                throw new JocFolderPermissionsException(folder);
            }

            filter.setFolder(normalizeFolder(folder.replace('\\', '/')));

            stream = body.getEntityAs(InputStream.class);
            String extention = DocumentationHelper.getExtensionFromFilename(filter.getFile());

            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            Optional<String> supportedSubType = DocumentationHelper.SUPPORTED_SUBTYPES.stream().filter(s -> mediaSubType.contains(s)).findFirst();
            Optional<String> supportedImageType = DocumentationHelper.SUPPORTED_IMAGETYPES.stream().filter(s -> mediaSubType.contains(s)).findFirst();
            Set<String> folders = new HashSet<>();
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);

            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                folders = DocumentationHelper.readZipFileContent(stream, filter.getFolder(), dbLayer, dbAudit);
            } else if (supportedImageType.isPresent()) {
                DocumentationHelper.saveOrUpdate(DocumentationHelper.setDBItemDocumentationImage(IOUtils.toByteArray(stream), filter,
                        supportedImageType.get()), dbLayer, dbAudit);
            } else if (supportedSubType.isPresent()) {
                if ("xml".equals(supportedSubType.get())) {
                    switch (extention) {
                    case "xsl":
                    case "xslt":
                        supportedSubType = Optional.of("xsl");
                        break;
                    case "xsd":
                        supportedSubType = Optional.of("xsd");
                        break;
                    }
                }
                DocumentationHelper.saveOrUpdate(DocumentationHelper.setDBItemDocumentation(IOUtils.toByteArray(stream), filter, supportedSubType
                        .get()), dbLayer, dbAudit);
            } else if ("md".equals(extention) || "markdown".equals(extention)) {
                byte[] b = IOUtils.toByteArray(stream);
                if (DocumentationHelper.isPlainText(b)) {
                    DocumentationHelper.saveOrUpdate(DocumentationHelper.setDBItemDocumentation(b, filter, "markdown"), dbLayer, dbAudit);
                } else {
                    throw new JocUnsupportedFileTypeException("Unsupported file type (" + mediaSubType + "), supported types are "
                            + DocumentationHelper.SUPPORTED_SUBTYPES.toString());
                }
            } else {
                throw new JocUnsupportedFileTypeException("Unsupported file type (" + mediaSubType + "), supported types are "
                        + DocumentationHelper.SUPPORTED_SUBTYPES.toString());
            }

            folders.add(filter.getFolder());
            folders.forEach(f -> DocumentationResourceImpl.postEvent(f));

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

}
