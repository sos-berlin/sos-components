package com.sos.joc.classes.xmleditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.classes.xmleditor.exceptions.SOSAssignSchemaException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class JocXmlEditor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocXmlEditor.class);

    public static final String SCHEMA_URI_YADE = "https://www.sos-berlin.com/schema/yade/YADE_configuration_v1.12.xsd";
    public static final String SCHEMA_URI_NOTIFICATION = "https://www.sos-berlin.com/schema/jobscheduler/Notification_configuration_v1.0.xsd";
    public static final String SCHEMA_FILENAME_YADE = "YADE_configuration_v1.12.xsd";
    public static final String SCHEMA_FILENAME_NOTIFICATION = "Notification_configuration_v1.0.xsd";
    public static final String SCHEMA_ROOT_ELEMENT_NAME_YADE = "Configurations";
    public static final String SCHEMA_ROOT_ELEMENT_NAME__NOTIFICATION = "Configurations";

    public static final String CONFIGURATION_BASENAME_YADE = "yade";
    public static final String CONFIGURATION_BASENAME_NOTIFICATION = "notification";

    public static final String APPLICATION_PATH = "xmleditor";
    public static final String CHARSET = "UTF-8";
    public static final String JOC_SCHEMA_LOCATION = "xsd";
    public static final String NEW_LINE = "\r\n";

    public static final String CODE_NO_CONFIGURATION_EXIST = "XMLEDITOR-101";
    public static final String ERROR_CODE_VALIDATION_ERROR = "XMLEDITOR-401";
    public static final String ERROR_CODE_UNSUPPORTED_OBJECT_TYPE = "XMLEDITOR-402";

    private static Path realPath = null;

    public static String getRootElementName(ObjectType type) {
        if (type.equals(ObjectType.YADE)) {
            return SCHEMA_ROOT_ELEMENT_NAME_YADE;
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return SCHEMA_ROOT_ELEMENT_NAME__NOTIFICATION;
        }
        return null;
    }

    public static Document parseXml(String xml) throws Exception {
        if (SOSString.isEmpty(xml)) {
            return null;
        }
        return SOSXML.parse(xml);
    }

    public static Document parseXml(InputStream is) throws Exception {
        if (is == null) {
            return null;
        }
        return SOSXML.parse(is);
    }

    public static String getResourceImplPath(final String path) {
        return String.format("./%s/%s", APPLICATION_PATH, path);
    }

    public static Path getStandardAbsoluteSchemaLocation(ObjectType type) throws Exception {
        setRealPath();
        Path path = realPath;
        if (path != null) {
            path = path.resolve(getStandardRelativeSchemaLocation(type));
        }
        return path;
    }

    public static String getStandardSchemaIdentifier(ObjectType type) throws Exception {
        if (type.equals(ObjectType.YADE)) {
            return SCHEMA_FILENAME_YADE;
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return SCHEMA_FILENAME_NOTIFICATION;
        }
        return null;
    }

    public static List<Path> getSchemaFiles(ObjectType type) throws Exception {
        setRealPath();
        Path path = realPath == null ? Paths.get(System.getProperty("user.dir")) : realPath;
        StringBuilder sb = type.equals(ObjectType.YADE) ? getYadeRelativeSchemaLocation() : getOthersRelativeSchemaLocation();
        return getFiles(path.resolve(sb.toString()), false, "xsd");
    }

    public static Path getSchema(ObjectType type, String name) throws Exception {
        setRealPath();
        Path path = realPath;
        if (path != null) {
            path = path.resolve(getRelativeSchemaLocation(type, name));
        }
        return path;
    }

    public static Path getHttpSchema(ObjectType type, String name) throws Exception {
        setRealPath();
        Path path = realPath;
        if (path != null) {
            path = path.resolve(getRelativeHttpSchemaLocation(type, name));
        }
        return path;
    }

    public static Path getSchema(ObjectType type, String path, boolean downloadIfHttp) throws Exception {
        Path file = null;
        if (isHttp(path)) {
            if (downloadIfHttp) {
                try {
                    URI uri = toURI(path);
                    file = downloadSchema(type, uri, getFileName(uri));
                } catch (Throwable e) {
                    StringBuilder httpLocation = type.equals(ObjectType.YADE) ? getYadeRelativeHttpSchemaLocation()
                            : getOthersRelativeHttpSchemaLocation();
                    LOGGER.error(String.format("[%s]can't download file, try to find in the %s location ..", path, httpLocation));
                }
            }
            if (file == null) {
                file = JocXmlEditor.getHttpSchema(type, getFileName(toURI(path)));
            }

        } else {
            file = JocXmlEditor.getSchema(type, getFileName(Paths.get(path)));
        }
        return file;
    }

    public static String readSchema(ObjectType type, String path) throws Exception {
        Path file = getSchema(type, path, true);
        if (Files.exists(file)) {
            return getFileContent(file);
        } else {
            throw new Exception(String.format("[%s]file not found", path));
        }
    }

    public static Path downloadSchema(ObjectType type, URI uri, String targetName) throws Exception {
        Path target = getHttpSchema(type, targetName);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setReadTimeout(5_000);
            conn.setConnectTimeout(5_000);
            int responseCode = conn.getResponseCode();
            if (responseCode > 299) {
                String msg = null;
                String add = "";
                if (responseCode == 302) {
                    add = " URL redirection";
                }
                try {
                    msg = String.format("[%s][Response code %s] %s%s", uri, responseCode, conn.getResponseMessage(), add);
                } catch (Throwable e) {
                    msg = String.format("[%s] Response code %s%s", uri, responseCode, add);
                }
                throw new SOSAssignSchemaException(msg);
            }
            try (InputStream inputStream = conn.getInputStream()) {
                Path parent = target.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable ex) {
                throw ex;
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return target;
    }

    public static Path copySchema(ObjectType type, Path source) throws Exception {
        Path target = JocXmlEditor.getSchema(type, source.getFileName().toString());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    public static Path createSchema(ObjectType type, String fileName, String fileContent) throws Exception {
        Path target = JocXmlEditor.getSchema(type, fileName);
        Files.write(target, fileContent.getBytes(JocXmlEditor.CHARSET), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return target;
    }

    public static String getHttpOrFileSchemaIdentifier(String path) {
        if (isHttp(path)) {
            return path;
        }
        return getFileName(Paths.get(path));
    }

    public static String getSchemaIdentifier(Path path) {
        return getFileName(path);
    }

    private static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    public static String getFileName(URI uri) {
        String path = null;
        try {
            path = URLDecoder.decode(uri.toString(), CHARSET);
        } catch (Throwable e) {
            path = uri.toString().replaceAll("%20", " ");
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static URI toURI(String uri) throws Exception {
        URL url;
        try {
            url = new URL(URLDecoder.decode(uri, CHARSET));
        } catch (Throwable e) {
            return new URI(uri.replaceAll(" ", "%20"));
        }
        return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
    }

    public static String getStandardRelativeSchemaLocation(final ObjectType type) {
        if (type == null) {
            return null;
        }
        if (type.equals(ObjectType.YADE)) {
            return getYadeRelativeSchemaLocation().append("/").append(SCHEMA_FILENAME_YADE).toString();
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return getNotificationRelativeSchemaLocation().append("/").append(SCHEMA_FILENAME_NOTIFICATION).toString();
        }
        return null;
    }

    public static String getSchemaLocation4Db(ObjectType type, String schemaIdentifier) {
        switch (type) {
        case YADE:
            if (SOSString.isEmpty(schemaIdentifier)) {
                return SCHEMA_FILENAME_YADE;
            }
            return schemaIdentifier;
        case OTHER:
            return schemaIdentifier;
        default:
            if (SOSString.isEmpty(schemaIdentifier)) {
                return SCHEMA_FILENAME_NOTIFICATION;
            }
            return schemaIdentifier;
        }
    }

    private static StringBuilder getYadeRelativeSchemaLocation() {
        return new StringBuilder(JOC_SCHEMA_LOCATION).append("/yade");
    }

    private static StringBuilder getYadeRelativeHttpSchemaLocation() {
        return getYadeRelativeSchemaLocation().append("/http");
    }

    private static StringBuilder getNotificationRelativeSchemaLocation() {
        return new StringBuilder(JOC_SCHEMA_LOCATION).append("/notification");
    }

    private static StringBuilder getOthersRelativeSchemaLocation() {
        return new StringBuilder(JOC_SCHEMA_LOCATION).append("/others");
    }

    private static StringBuilder getOthersRelativeHttpSchemaLocation() {
        return getOthersRelativeSchemaLocation().append("/http");
    }

    private static String getRelativeSchemaLocation(ObjectType type, final String name) {
        switch (type) {
        case YADE:
            return getYadeRelativeSchemaLocation().append("/").append(name).toString();
        default:// OTHER
            return getOthersRelativeSchemaLocation().append("/").append(name).toString();
        }
    }

    private static String getRelativeHttpSchemaLocation(ObjectType type, final String name) {
        switch (type) {
        case YADE:
            return getYadeRelativeHttpSchemaLocation().append("/").append(name).toString();
        default:// OTHER
            return getOthersRelativeHttpSchemaLocation().append("/").append(name).toString();
        }
    }

    public static String getConfigurationName(final ObjectType type) {
        return getConfigurationName(type, null);
    }

    public static String getConfigurationName(final ObjectType type, final String name) {
        if (type.equals(ObjectType.OTHER)) {
            return name;
        }
        return getStandardBaseName(type) + ".xml";
    }

    public static boolean checkRequiredParameter(final String paramKey, final ObjectType paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.toString().isEmpty()) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public static boolean checkRequiredParameter(final String paramKey, final List<?> paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.size() == 0) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    private static String getStandardBaseName(ObjectType type) {
        if (type == null) {
            return null;
        }
        if (type.equals(ObjectType.YADE)) {
            return CONFIGURATION_BASENAME_YADE;
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return CONFIGURATION_BASENAME_NOTIFICATION;
        }
        return null;
    }

    private static List<Path> getFiles(Path dir, boolean recursiv, String extension) throws Exception {
        if (recursiv) {
            return Files.walk(dir).filter(s -> s.toString().toLowerCase().endsWith("." + extension.toLowerCase())).map(Path::getFileName).sorted()
                    .collect(Collectors.toList());
        } else {
            return Files.walk(dir, 1).filter(s -> s.toString().toLowerCase().endsWith("." + extension.toLowerCase())).map(Path::getFileName).sorted()
                    .collect(Collectors.toList());
        }
    }

    private static String bytes2string(byte[] bytes) {
        try {
            return new String(bytes, CHARSET);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    public static String getFileContent(Path path) throws IOException {
        return bytes2string(Files.readAllBytes(path));
    }

    // TODO
    public static boolean isHttp(String path) {
        path = path.toLowerCase();
        return path.startsWith("https://") || path.startsWith("http://");
    }

    public static void setRealPath() throws Exception {
        if (realPath != null) {
            return;
        }
        // realPath = Paths.get(System.getProperty("user.dir"), "resources/joc").normalize();
        realPath = Globals.sosCockpitProperties.getResourceDir();
    }

}
