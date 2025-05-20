package com.sos.joc.xmleditor.commons.other;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.joc.Globals;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.exceptions.SOSAssignSchemaException;

public class OtherSchemaHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherSchemaHandler.class);
    private static final String TEMP_EXTENSION = ".sostmp";
    private String source;
    private Path target;
    private Path targetTemp;
    private URI httpDownloadUri;

    private static Path realPath = null;

    public void assign(ObjectType type, String fileUri, String fileName, String fileContent) throws Exception {
        source = null;
        targetTemp = null;
        target = null;
        if (fileUri == null) {
            if (SOSString.isEmpty(fileName)) {
                throw new Exception("missing schema file name");
            }
            source = fileName.trim();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s]create from local file", source));
            }
            String targetPath = getRelativeSchemaLocation(source);
            target = Path.of(targetPath);
            targetTemp = Path.of(targetPath.concat(TEMP_EXTENSION));
            SOSPath.overwrite(targetTemp, fileContent);
        } else {
            source = fileUri.trim();
            if (isHttp(source)) {// http(s)://
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s]copy from http(s)", source));
                }

                httpDownloadUri = toURI(source);
                String name = SOSPathUtils.getName(httpDownloadUri.toString());
                target = getLocalHttpSchema(name);
                String tempName = SOSPathUtils.getName(toURI(source.concat(TEMP_EXTENSION)).toString());
                targetTemp = downloadSchema(toURI(source), tempName);
            } else {
                Path sourcePath = Paths.get(source);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s]copy from local file", sourcePath));
                }
                if (sourcePath.isAbsolute()) {// C://Temp/xyz.xsd
                    target = getSchemaAsPath(sourcePath.getFileName().toString(), false);
                    targetTemp = copySchema(sourcePath);
                } else {// xyz.xsd
                    target = getSchemaAsPath(source, false);
                    targetTemp = target;
                }
            }
        }
    }

    public void onError(boolean deleteTempFile) {
        if (deleteTempFile) {
            try {
                Files.deleteIfExists(targetTemp);
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s]error on delete file: %s", targetTemp, e.toString()), e);
            }
        }
    }

    public static String getHttpOrFileSchemaIdentifier(String path) {
        if (isHttp(path)) {
            return path;
        }
        return getSchemaIdentifier(path);
    }

    public static String getSchemaIdentifier(String path) {
        return SOSPathUtils.getName(path);
    }

    public static String getSchemaIdentifier(Path path) {
        return SOSPathUtils.getName(path.toString());
    }

    public static List<Path> getSchemaFiles() throws Exception {
        setRealPath();
        Path path = realPath == null ? Paths.get(System.getProperty("user.dir")) : realPath;
        return getFiles(path.resolve(getRelativeSchemaLocation().toString()), false, "xsd");
    }

    public static String getSchema(String path, boolean downloadIfHttp) throws Exception {
        Path p = getSchemaAsPath(path, downloadIfHttp);
        if (p != null) {
            return SOSPath.readFile(p);
        }
        return null;
    }

    public static InputStream getSchemaAsInputStream(String path, boolean downloadIfHttp) throws Exception {
        Path p = getSchemaAsPath(path, downloadIfHttp);
        if (p != null) {
            return Files.newInputStream(p);
        }
        return null;
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

    private static URI toURI(String uri) throws Exception {
        URL url;
        try {
            url = new URL(HttpUtils.decodeUriPath(uri));
        } catch (Throwable e) {
            return new URI(uri.replaceAll(" ", "%20"));
        }
        return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
    }

    private static Path copySchema(Path source) throws Exception {
        Path target = getSchemaAsPath(source.getFileName().toString(), false);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static Path getLocalHttpSchema(String name) throws Exception {
        setRealPath();
        Path path = realPath;
        if (path != null) {
            path = path.resolve(getRelativeHttpSchemaLocation(name));
        }
        return path;
    }

    private static Path downloadSchema(URI uri, String targetName) throws Exception {
        Path target = getLocalHttpSchema(targetName);

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

    private static List<Path> getFiles(Path dir, boolean recursiv, String extension) throws Exception {
        if (recursiv) {
            return Files.walk(dir).filter(s -> s.toString().toLowerCase().endsWith("." + extension.toLowerCase())).map(Path::getFileName).sorted()
                    .collect(Collectors.toList());
        } else {
            return Files.walk(dir, 1).filter(s -> s.toString().toLowerCase().endsWith("." + extension.toLowerCase())).map(Path::getFileName).sorted()
                    .collect(Collectors.toList());
        }
    }

    private static StringBuilder getRelativeSchemaLocation() {
        return new StringBuilder(JocXmlEditor.JOC_SCHEMA_LOCATION).append("/others");
    }

    private static StringBuilder getRelativeHttpSchemaLocation() {
        return getRelativeSchemaLocation().append("/http");
    }

    private static String getRelativeSchemaLocation(final String name) {
        return getRelativeSchemaLocation().append("/").append(name).toString();
    }

    private static String getRelativeHttpSchemaLocation(final String name) {
        return getRelativeHttpSchemaLocation().append("/").append(name).toString();
    }

    private static Path getSchemaAsPath(String path, boolean downloadIfHttp) throws Exception {
        if (isHttp(path)) {
            Path file = null;
            if (downloadIfHttp) {
                try {
                    URI uri = toURI(path);
                    file = downloadSchema(uri, SOSPathUtils.getName(uri.toString()));
                } catch (Throwable e) {
                    StringBuilder httpLocation = getRelativeHttpSchemaLocation();
                    LOGGER.error(String.format("[%s]can't download file, try to find in the %s location ..", path, httpLocation));
                }
            }
            if (file == null) {
                file = getLocalHttpSchema(SOSPathUtils.getName(toURI(path).toString()));
            }
            if (file != null && Files.exists(file)) {
                return file;
            }
        } else {
            setRealPath();
            if (realPath != null) {
                Path file = realPath.resolve(getRelativeSchemaLocation(SOSPathUtils.getName(path)));
                if (Files.exists(file)) {
                    return file;
                }
            }
        }
        return null;
    }

    public String getSource() {
        return source;
    }

    public Path getTarget() {
        return target;
    }

    public Path getTargetTemp() {
        return targetTemp;
    }

    public URI getHttpDownloadUri() {
        return httpDownloadUri;
    }
}
