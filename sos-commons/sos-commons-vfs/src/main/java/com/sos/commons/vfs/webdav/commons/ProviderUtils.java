package com.sos.commons.vfs.webdav.commons;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.http.commons.HTTPClient;
import com.sos.commons.vfs.http.commons.HTTPClient.ExecuteResult;
import com.sos.commons.vfs.http.commons.HTTPUtils;
import com.sos.commons.vfs.webdav.WebDAVProvider;

public class ProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(WebDAVProvider provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws ProviderException {
        int counterAdded = 0;
        try {
            list(provider, selection, directoryPath, result, counterAdded);
        } catch (Throwable e) {
            throw new ProviderException(e);
        }
        return result;
    }

    public static boolean exists(HTTPClient client, URI uri) throws Exception {
        ExecuteResult<String> result = client.executeWithResponseBody(createPROPFINDRequest(client, uri, "0"));
        int code = result.response().statusCode();
        if (!HTTPUtils.isSuccessful(code)) {
            if (HTTPUtils.isNotFound(code)) {
                return false;
            }
            throw new IOException(HTTPClient.getResponseStatus(result));
        }
        return true;
    }

    public static void createDirectory(HTTPClient client, URI uri) throws Exception {
        HttpRequest.Builder builder = client.createRequestBuilder(uri);
        ExecuteResult<Void> result = client.executeWithoutResponseBody(builder.method("MKCOL", BodyPublishers.noBody()).build());
        if (!HTTPUtils.isSuccessful(result.response().statusCode())) {
            throw new IOException(HTTPClient.getResponseStatus(result));
        }
    }

    public static boolean directoryExists(HTTPClient client, URI uri) throws Exception {
        WebDAVResource resource = getResource(client, uri);
        return resource == null ? false : resource.isDirectory();
    }

    public static WebDAVResource getResource(HTTPClient client, URI uri) throws Exception {
        ExecuteResult<String> result = client.executeWithResponseBody(createPROPFINDRequest(client, uri, "0"));
        int code = result.response().statusCode();
        if (!HTTPUtils.isSuccessful(code)) {
            if (HTTPUtils.isNotFound(code)) {
                return null;
            }
            throw new IOException(HTTPClient.getResponseStatus(result));
        }
        List<WebDAVResource> resources = parseWebDAVResources(result.response().body(), uri.toString());
        return resources.isEmpty() ? null : resources.get(0);
    }

    // not use Depth infinity - maybe not supported by the server and possible timeouts to get all levels ...
    private static int list(WebDAVProvider provider, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result,
            int counterAdded) throws ProviderException {
        try {
            URI uri = new URI(provider.normalizePath(directoryPath));

            ExecuteResult<String> executeResult = provider.getClient().executeWithResponseBody(createPROPFINDRequest(provider.getClient(), uri, "0"));
            int code = executeResult.response().statusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return 0;
                }
                throw new IOException(HTTPClient.getResponseStatus(executeResult));
            }

            Set<String> subDirectories = new HashSet<>();
            for (WebDAVResource resource : parseWebDAVResources(directoryPath, directoryPath)) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    return counterAdded;
                }
                if (resource.isDirectory()) {
                    if (selection.getConfig().isRecursive()) {
                        if (selection.checkDirectory(resource.getHref())) {
                            subDirectories.add(resource.getHref());
                        }
                    }
                } else {
                    if (selection.checkFileName(SOSPathUtils.getName(resource.getHref())) && selection.isValidFileType(resource)) {
                        ProviderFile file = provider.createProviderFile(resource);
                        if (file == null) {
                            if (provider.getLogger().isDebugEnabled()) {
                                provider.getLogger().debug(provider.getPathOperationPrefix(resource.getHref()) + "[skip]" + resource);
                            }
                        } else {
                            if (selection.checkProviderFileMinMaxSize(file)) {
                                counterAdded++;

                                file.setIndex(counterAdded);
                                result.add(file);

                                if (provider.getLogger().isDebugEnabled()) {
                                    provider.getLogger().debug(provider.getPathOperationPrefix(file.getFullPath()) + "added");
                                }
                            }
                        }
                    }
                }
            }
            for (String subDirectory : subDirectories) {
                if (selection.maxFilesExceeded(counterAdded)) {
                    return counterAdded;
                }
                counterAdded = list(provider, selection, subDirectory, result, counterAdded);
            }
        } catch (Throwable e) {
            throw new ProviderException(e);
        }
        return counterAdded;
    }

    private static HttpRequest createPROPFINDRequest(HTTPClient client, URI uri, String depth) {
        HttpRequest.Builder builder = client.createRequestBuilder(uri)
                // Depth
                .header("Depth", depth)
                // XML
                .header("Content-Type", "application/xml")
                // Method
                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(getPROPFINDRequestBody()));
        return builder.build();
    }

    private static String getPROPFINDRequestBody() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <d:propfind xmlns:d="DAV:">
                    <d:prop>
                        <d:resourcetype/>
                        <d:getcontentlength/>
                        <d:getlastmodified/>
                    </d:prop>
                </d:propfind>
                """;
        return xml;
    }

    // XML-Antwort in WebDAVResource-Liste parsen
    private static List<WebDAVResource> parseWebDAVResources(String xml, String basePath) {
        List<WebDAVResource> resources = new ArrayList<>();

        Pattern itemPattern = Pattern.compile("<d:response>(.*?)</d:response>", Pattern.DOTALL);
        Matcher matcher = itemPattern.matcher(xml);
        while (matcher.find()) {
            String item = matcher.group(1);
            boolean isDirectory = item.contains("<d:collection/>");
            long size = extractLong(item, "<d:getcontentlength>(\\d+)</d:getcontentlength>");
            long lastModified = extractLastModified(item);
            String path = extractPath(item, basePath);
            resources.add(new WebDAVResource(path, isDirectory, size, lastModified));
        }
        return resources;
    }

    private static long extractLong(String item, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(item);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private static long extractLastModified(String item) {
        Matcher matcher = Pattern.compile("<d:getlastmodified>(.*?)</d:getlastmodified>").matcher(item);
        if (matcher.find()) {
            return HTTPUtils.toMillis(matcher.group(1));
        }
        return HTTPUtils.DEFAULT_LAST_MODIFIED;
    }

    private static String extractPath(String item, String basePath) {
        Matcher matcher = Pattern.compile("<d:href>(.*?)</d:href>").matcher(item);
        if (matcher.find()) {
            String path = matcher.group(1);
            if (path.startsWith(basePath)) {
                return path;
            }
        }
        return basePath;
    }

}
