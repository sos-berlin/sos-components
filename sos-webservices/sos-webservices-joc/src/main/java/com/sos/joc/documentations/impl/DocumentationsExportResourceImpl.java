package com.sos.joc.documentations.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.DeleteTempFile;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.documentation.DocumentationContent;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentations.resource.IDocumentationsExportResource;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.docu.DocumentationFilter;
import com.sos.joc.model.docu.DocumentationsFilter;
import com.sos.joc.model.docu.ExportInfo;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;

@Path("documentations")
public class DocumentationsExportResourceImpl extends JOCResourceImpl implements IDocumentationsExportResource {

    private static final String API_CALL = "./documentations/export";

    @Override
    public JOCDefaultResponse postExportDocumentations(String accessToken, byte[] filterBytes) {

        SOSHibernateSession sosHibernateSession = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.DOCUMENTATIONS);
            JsonValidator.validateFailFast(filterBytes, DocumentationsFilter.class);
            DocumentationsFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getDocumentations().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String targetFilename = "documentation.zip";

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            final List<DocumentationContent> contents = mapToDocumentationContents(documentationsFilter, sosHibernateSession);
            StreamingOutput streamingOutput = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    ZipOutputStream zipOut = null;
                    try {
                        zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                        for (DocumentationContent content : contents) {
                            ZipEntry entry = new ZipEntry(content.getPath().substring(1));
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
            return responseOctetStreamDownloadStatus200(streamingOutput, targetFilename);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse getExportDocumentations(String accessToken, String filename) {
        try {
            if (filename == null) {
                initLogging(API_CALL, null, accessToken, CategoryType.DOCUMENTATIONS); 
            } else {
                initLogging(String.format("%s?filename=%s", API_CALL, filename), null, accessToken, CategoryType.DOCUMENTATIONS);
            }
            checkRequiredParameter("filename", filename);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getDocumentations().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            java.nio.file.Path path = Paths.get(System.getProperty("java.io.tmpdir"), filename);
            if (!Files.isReadable(path)) {
                throw new ControllerObjectNotExistException("Couldn't find temp. file '" + filename + "'.");
            }

            final java.nio.file.Path downPath = Files.move(path, path.getParent().resolve(path.getFileName().toString() + ".zip"),
                    StandardCopyOption.ATOMIC_MOVE);

            StreamingOutput fileStream = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    InputStream in = null;
                    try {
                        in = Files.newInputStream(downPath);
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                        output.flush();
                    } finally {
                        try {
                            output.close();
                        } catch (Exception e) {
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception e) {
                            }
                        }
                        try {
                            Files.deleteIfExists(downPath);
                        } catch (Exception e) {
                        }
                    }
                }
            };

            return responseOctetStreamDownloadStatus200(fileStream, "sos-documentations" + ".zip");
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse postExportInfo(String accessToken, byte[] filterBytes) {

        SOSHibernateSession sosHibernateSession = null;
        ZipOutputStream zipOut = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.DOCUMENTATIONS);
            JsonValidator.validateFailFast(filterBytes, DocumentationFilter.class);
            DocumentationsFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getDocumentations().getManage()));

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL + "/info");
            List<DocumentationContent> contents = mapToDocumentationContents(documentationsFilter, sosHibernateSession);

            java.nio.file.Path path = Files.createTempFile("sos-download-", ".zip.tmp");
            zipOut = new ZipOutputStream(Files.newOutputStream(path));
            for (DocumentationContent content : contents) {
                ZipEntry entry = new ZipEntry(content.getPath().substring(1));
                zipOut.putNextEntry(entry);
                zipOut.write(content.getContent());
                zipOut.closeEntry();
            }
            zipOut.flush();

            ExportInfo entity = new ExportInfo();
            entity.setFilename(path.getFileName().toString());
            entity.setDeliveryDate(Date.from(Instant.now()));

            DeleteTempFile runnable = new DeleteTempFile(path);
            new Thread(runnable).start();

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private List<DocumentationContent> mapToDocumentationContents(DocumentationsFilter filter, SOSHibernateSession sosHibernateSession)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, JsonProcessingException,
            DBMissingDataException {
        DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
        List<DBItemDocumentation> docs = getDocsFromDb(dbLayer, filter);
        List<DocumentationContent> contents = new ArrayList<DocumentationContent>();
        for (DBItemDocumentation doc : docs) {
            DocumentationContent content = null;
            if (doc.getContent() != null) {
                content = new DocumentationContent(doc.getPath(), doc.getContent().getBytes(StandardCharsets.UTF_8));
            } else {
                DBItemDocumentationImage image = dbLayer.getDocumentationImage(doc.getImageId());
                if (image != null) {
                    content = new DocumentationContent(doc.getPath(), image.getImage());
                }
            }
            if (content != null) {
                contents.add(content);
            }
        }
        return contents;
    }

    private List<DBItemDocumentation> getDocsFromDb(DocumentationDBLayer dbLayer, DocumentationsFilter filter)
            throws JocMissingRequiredParameterException, DBConnectionRefusedException, DBInvalidDataException, DBMissingDataException {
        List<DBItemDocumentation> docs = new ArrayList<DBItemDocumentation>();
        if (filter.getDocumentations() != null && !filter.getDocumentations().isEmpty()) {
            docs = dbLayer.getDocumentations(filter.getDocumentations().stream().collect(Collectors.toList()));
        } else if (filter.getFolders() != null && !filter.getFolders().isEmpty()) {
            for (Folder folder : filter.getFolders()) {
                docs.addAll(dbLayer.getDocumentations(null, folder.getFolder(), folder.getRecursive(), false));
            }
        } else {
            throw new JocMissingRequiredParameterException("Neither 'documentations' nor 'folders' are specified!");
        }
        if (docs == null || docs.isEmpty()) {
            throw new DBMissingDataException("No 'documentations' found!");
        }
        return docs;
    }

}
