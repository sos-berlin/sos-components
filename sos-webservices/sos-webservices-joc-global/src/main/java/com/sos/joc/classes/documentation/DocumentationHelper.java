package com.sos.joc.classes.documentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.digest.DigestUtils;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.docu.DocumentationImport;

public class DocumentationHelper {
    
    public static final List<String> SUPPORTED_SUBTYPES = Arrays.asList("html", "xml", "pdf", "xsl", "xsd", "javascript",
            "json", "css", "markdown", "gif", "jpeg", "png", "icon");
    public static final List<String> SUPPORTED_IMAGETYPES = Arrays.asList("pdf", "gif", "jpeg", "png", "icon");
    public static final List<String> ASSIGN_TYPES = Arrays.asList("html", "xml", "pdf", "markdown");

    private static void saveOrUpdate(DocumentationDBLayer dbLayer, DBItemDocumentation doc) throws DBConnectionRefusedException,
            DBInvalidDataException, SOSHibernateException {
        DBItemDocumentation docFromDB = dbLayer.getDocumentation(doc.getPath());
        if (docFromDB != null) {
            if (doc.hasImage()) {
                DBItemDocumentationImage imageFromDB = dbLayer.getDocumentationImage(docFromDB.getImageId());
                if (imageFromDB == null) {
                    // insert image if not exist
                    docFromDB.setImageId(saveImage(dbLayer, doc));
                } else {
                    // update image if hash unequal
                    String md5Hash = DigestUtils.md5Hex(doc.getImage());
                    if (!imageFromDB.getMd5Hash().equals(md5Hash)) {
                        imageFromDB.setMd5Hash(md5Hash);
                        imageFromDB.setImage(doc.getImage());
                        dbLayer.getSession().update(imageFromDB);
                    }
                }
            }
            docFromDB.setContent(doc.getContent());
            docFromDB.setType(doc.getType());
            docFromDB.setModified(Date.from(Instant.now()));
            dbLayer.getSession().update(docFromDB);
        } else {
            if (doc.hasImage()) {
                // insert image
                doc.setImageId(saveImage(dbLayer, doc));
            }
            doc.setIsRef(ASSIGN_TYPES.contains(doc.getType()));
            if (doc.getIsRef()) {
                String docRef = doc.getName().replaceFirst("^(.*)\\.[^\\.]+$", "$1"); // default = name without extension
                doc.setDocRef(dbLayer.getUniqueDocRef(docRef));
            }
            dbLayer.getSession().save(doc);
        }
    }

    public static void saveOrUpdate(DBItemDocumentation doc, DocumentationDBLayer dbLayer, DBItemJocAuditLog dbAudit)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, JocConfigurationException, DBOpenSessionException {
        JocAuditLog.storeAuditLogDetail(new AuditLogDetail(doc.getPath(), ObjectType.DOCUMENTATION.intValue()), dbLayer.getSession(), dbAudit);
        saveOrUpdate(dbLayer, doc);
    }

    private static Long saveImage(DocumentationDBLayer dbLayer, DBItemDocumentation doc) throws SOSHibernateException {
        DBItemDocumentationImage image = new DBItemDocumentationImage();
        image.setImage(doc.getImage());
        image.setMd5Hash(DigestUtils.md5Hex(doc.getImage()));
        dbLayer.getSession().save(image);
        return image.getId();
    }

    public static String getExtensionFromFilename(String filename) {
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

    private static Set<DBItemDocumentation> readZipFileContent(InputStream inputStream, String folder) throws IOException,
            JocUnsupportedFileTypeException {
        ZipInputStream zipStream = null;
        Set<DBItemDocumentation> documentations = new HashSet<DBItemDocumentation>();
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

                DBItemDocumentation documentation = new DBItemDocumentation();
                java.nio.file.Path targetFolder = Paths.get(folder);
                java.nio.file.Path complete = targetFolder.resolve(entryName.replaceFirst("^/", ""));
                documentation.setPath(complete.toString().replace('\\', '/'));
                documentation.setFolder(complete.getParent().toString().replace('\\', '/'));
                documentation.setName(complete.getFileName().toString());
                String fileExtension = getExtensionFromFilename(documentation.getName());
                boolean isPlainText = isPlainText(bytes);
                final String guessedMediaType = guessContentTypeFromBytes(bytes, fileExtension, isPlainText);
                if (guessedMediaType != null) {
                    Optional<String> supportedSubType = SUPPORTED_SUBTYPES.stream().filter(s -> guessedMediaType.contains(s)).findFirst();
                    Optional<String> supportedImageType = SUPPORTED_IMAGETYPES.stream().filter(s -> guessedMediaType.contains(s)).findFirst();
                    if (supportedImageType.isPresent()) {
                        documentation.setType(supportedImageType.get());
                        documentation.setImage(bytes);
                        documentation.setHasImage(true);
                    } else if (supportedSubType.isPresent()) {
                        documentation.setType(supportedSubType.get());
                        documentation.setContent(new String(bytes, StandardCharsets.UTF_8));
                        documentation.setHasImage(false);
                    } else {
                        throw new JocUnsupportedFileTypeException(String.format("%1$s unsupported, supported types are %2$s", complete.toString()
                                .replace('\\', '/'), SUPPORTED_SUBTYPES.toString()));
                    }
                } else {
                    throw new JocUnsupportedFileTypeException(String.format("%1$s unsupported, supported types are %2$s", complete.toString().replace(
                            '\\', '/'), SUPPORTED_SUBTYPES.toString()));
                }
                documentation.setCreated(Date.from(Instant.now()));
                documentation.setModified(documentation.getCreated());

                documentations.add(documentation);
            }
            return documentations;
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    // used during JOC start-up to insert JOC-JITL-Docs
    public static Set<String> readZipFileContent(InputStream inputStream, String folder, DocumentationDBLayer dbLayer)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        return saveOrUpdate(readZipFileContent(inputStream, folder), dbLayer);
    }

    public static Set<String> readZipFileContent(InputStream inputStream, String folder, DocumentationDBLayer dbLayer, DBItemJocAuditLog dbAudit)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        return saveOrUpdate(readZipFileContent(inputStream, folder), dbLayer, dbAudit);
    }

    private static Set<String> saveOrUpdate(Set<DBItemDocumentation> documentations, DocumentationDBLayer dbLayer)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException {
        return saveOrUpdate(documentations, dbLayer, null);
    }

    private static Set<String> saveOrUpdate(Set<DBItemDocumentation> documentations, DocumentationDBLayer dbLayer, DBItemJocAuditLog dbAudit)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException {
        Set<String> folders = new HashSet<>();
        if (!documentations.isEmpty()) {
            if (dbAudit != null) {
                JocAuditLog.storeAuditLogDetails(documentations.stream().map(doc -> new AuditLogDetail(doc.getPath(), ObjectType.DOCUMENTATION
                        .intValue())).collect(Collectors.toSet()), dbLayer.getSession(), dbAudit);
            }
            for (DBItemDocumentation itemDocumentation : documentations) {
                saveOrUpdate(dbLayer, itemDocumentation);
                folders.add(itemDocumentation.getFolder());
            }

        } else {
            throw new JocUnsupportedFileTypeException("The zip file to upload doesn't contain any supported file, supported types are "
                    + SUPPORTED_SUBTYPES.toString());
        }
        return folders;
    }

    public static DBItemDocumentation setDBItemDocumentation(byte[] b, DocumentationImport filter, String mediaSubType) throws IOException,
            JocUnsupportedFileTypeException {
        DBItemDocumentation documentation = new DBItemDocumentation();
        documentation.setFolder(filter.getFolder());
        documentation.setName(filter.getFile());
        documentation.setPath((filter.getFolder() + "/" + filter.getFile()).replaceAll("//+", "/"));
        documentation.setCreated(Date.from(Instant.now()));
        documentation.setModified(documentation.getCreated());
        documentation.setType(mediaSubType);
        documentation.setContent(new String(b, StandardCharsets.UTF_8));
        documentation.setHasImage(false);
        return documentation;
    }

    public static DBItemDocumentation setDBItemDocumentationImage(byte[] b, DocumentationImport filter, String mediaSubType) throws IOException,
            JocUnsupportedFileTypeException {
        DBItemDocumentation documentation = new DBItemDocumentation();
        documentation.setFolder(filter.getFolder());
        documentation.setName(filter.getFile());
        documentation.setPath((filter.getFolder() + "/" + filter.getFile()).replaceAll("//+", "/"));
        documentation.setCreated(Date.from(Instant.now()));
        documentation.setModified(documentation.getCreated());
        documentation.setType(mediaSubType);
        documentation.setImage(b);
        documentation.setHasImage(true);
        return documentation;
    }

    private static String guessContentTypeFromBytes(byte[] b, String extension, boolean isPlainText) throws IOException {
        InputStream is = null;
        String media = null;
        try {
            is = new ByteArrayInputStream(b);
            media = URLConnection.guessContentTypeFromStream(is);
            if (media != null) {
                media = media.replaceFirst("^.*\\/(x-)?", "");
            }
            if (media == null && isPlainText) {
                switch (extension) {
                case "js":
                    media = "javascript";
                    break;
                case "json":
                    media = "json";
                    break;
                case "css":
                    media = "css";
                    break;
                case "md":
                case "markdown":
                    media = "markdown";
                    break;
                case "txt":
                    media = "plain";
                    break;
                case "html":
                case "xhtml":
                case "htm":
                    media = "html";
                    break;
                }
            } else if (media != null && media.contains("xml")) {
                switch (extension) {
                case "xsl":
                case "xslt":
                    media = "xsl";
                    break;
                case "xsd":
                    media = "xsd";
                    break;
                default:
                    media = "xml";
                }
            } else if (media == null && !isPlainText) {
                if ("pdf".equals(extension)) {
                    media = "pdf";
                } else if ("ico".equals(extension)) {
                    media = "icon";
                }
            }
            return media;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static boolean isPlainText(byte[] b) {
        try {
            StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(b));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

}
