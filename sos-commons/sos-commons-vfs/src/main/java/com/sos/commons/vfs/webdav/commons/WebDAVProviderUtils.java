package com.sos.commons.vfs.webdav.commons;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.BaseHttpClient.ExecuteResult;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.webdav.WebDAVProvider;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.transform.SOSXmlTransformer;

public class WebDAVProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(WebDAVProvider provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws Exception {
        int counterAdded = 0;
        list(provider, selection, directoryPath, result, counterAdded);
        return result;
    }

    public static boolean exists(BaseHttpClient client, URI uri) throws Exception {
        ExecuteResult<String> result = client.executeWithResponseBody(createPROPFINDRequest(client, uri, "0"));
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return false;
            }
            throw new IOException(BaseHttpClient.getResponseStatus(result));
        }
        return true;
    }

    public static void createDirectory(WebDAVProvider provider, URI uri) throws Exception {
        HttpRequest.Builder builder = provider.getClient().createRequestBuilder(uri);
        ExecuteResult<Void> result = provider.getClient().executeNoResponseBody(builder.method("MKCOL", BodyPublishers.noBody()).build());
        if (!HttpUtils.isSuccessful(result.response().statusCode())) {
            throw new IOException(BaseHttpClient.getResponseStatus(result));
        }
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[createDirectory][%s]created", provider.getLogPrefix(), uri);
        }
    }

    public static boolean directoryExists(WebDAVProvider provider, URI uri) throws Exception {
        WebDAVResource resource = getResource(provider, uri);
        return resource == null ? false : resource.isDirectory();
    }

    public static WebDAVResource getResource(WebDAVProvider provider, URI uri) throws Exception {
        String depth = "0";
        ExecuteResult<String> result = provider.getClient().executeWithResponseBody(createPROPFINDRequest(provider.getClient(), uri, depth));
        int code = result.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return null;
            }
            throw new IOException(BaseHttpClient.getResponseStatus(result));
        }
        List<WebDAVResource> resources = parseWebDAVResources(provider, uri, depth, result);
        return resources.isEmpty() ? null : resources.get(0);
    }

    private static int list(WebDAVProvider provider, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result,
            int counterAdded) throws Exception {

        URI directoryURI = HttpUtils.ensureDirectoryURI(new URI(provider.normalizePath(directoryPath)));

        // not use Depth infinity - maybe not supported by the server and possible timeouts to get all levels ...
        String depth = "1";
        ExecuteResult<String> executeResult = provider.getClient().executeWithResponseBody(createPROPFINDRequest(provider.getClient(), directoryURI,
                depth));
        int code = executeResult.response().statusCode();
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return 0;
            }
            throw new IOException(BaseHttpClient.getResponseStatus(executeResult));
        }

        Set<String> subDirectories = new HashSet<>();
        for (WebDAVResource resource : parseWebDAVResources(provider, directoryURI, depth, executeResult)) {
            if (selection.maxFilesExceeded(counterAdded)) {
                return counterAdded;
            }
            if (resource.isDirectory()) {
                if (selection.getConfig().isRecursive()) {
                    if (selection.checkDirectory(resource.getURI())) {
                        subDirectories.add(resource.getURI());
                    }
                }
            } else {
                if (selection.checkFileName(SOSPathUtils.getName(resource.getURI())) && selection.isValidFileType(resource)) {
                    ProviderFile file = provider.createProviderFile(resource);
                    if (file == null) {
                        if (provider.getLogger().isDebugEnabled()) {
                            provider.getLogger().debug(provider.getPathOperationPrefix(resource.getURI()) + "[skip]" + resource);
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
        return counterAdded;
    }

    private static HttpRequest createPROPFINDRequest(BaseHttpClient client, URI uri, String depth) {
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

    /** A Response of a PROPFIND request with depth means:<br/>
     * - Depth: 0 -> Only the requested resource (no children)<br/>
     * - Depth: 1 -> The requested resource + direct children<br/>
     * - Depth: infinity -> Recursive listing of all subdirectories<br/>
     * 
     * @param provider
     * @param uri
     * @param depth
     * @param result
     * @return
     * @throws Exception */
    private static List<WebDAVResource> parseWebDAVResources(WebDAVProvider provider, URI uri, String depth, ExecuteResult<String> result)
            throws Exception {
        boolean isDebugEnabled = provider.getLogger().isDebugEnabled();

        NodeList responseNodes = SOSXML.parse(result.response().body(), true).getElementsByTagNameNS("*", "response");
        if (isDebugEnabled) {
            provider.getLogger().debug("%s[parseWebDAVResources][%s][D:response]size=%s", provider.getLogPrefix(), uri, responseNodes.getLength());
        }

        List<WebDAVResource> resources = new ArrayList<>();
        // see method description - ignore information about the requested resource itself if it a directory and depth != 0
        boolean responseOfURISelfChecked = false;
        for (int i = 0; i < responseNodes.getLength(); i++) {
            Element response = (Element) responseNodes.item(i);
            if (isDebugEnabled) {
                provider.getLogger().debug("%s[parseWebDAVResources][%s]%s", provider.getLogPrefix(), i, SOSXmlTransformer.nodeToString(response));
            }
            String resourceHref = extractHref(response);
            if (resourceHref == null) {
                provider.getLogger().debug("[parseWebDAVResources][%s][skip]missing href", i);
                continue;
            }

            // without encoding
            URI resourceURI = URI.create(HttpUtils.normalizePath(uri, resourceHref));
            boolean resourceIsDirectory = extractIsDirectory(response);
            if (!responseOfURISelfChecked && !depth.equals("0") && resourceIsDirectory) {
                if (HttpUtils.ensureDirectoryURI(uri).equals(HttpUtils.ensureDirectoryURI(resourceURI))) {
                    responseOfURISelfChecked = true;
                    continue;
                }
            }
            resources.add(new WebDAVResource(resourceURI, resourceIsDirectory, extractSize(response, "getcontentlength"), extractLastModified(
                    response)));
        }
        return resources;
    }

    private static String extractHref(Element response) {
        NodeList nodes = response.getElementsByTagNameNS("*", "href");
        if (nodes.getLength() > 0) {
            // original - without extra encoding - getTextContent returns escaped e.g. XML &amp; converted as & etc
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private static boolean extractIsDirectory(Element response) {
        NodeList resourceTypeNodes = response.getElementsByTagNameNS("*", "resourcetype");
        if (resourceTypeNodes.getLength() > 0) {
            NodeList collectionNodes = ((Element) resourceTypeNodes.item(0)).getElementsByTagNameNS("*", "collection");
            return collectionNodes.getLength() > 0;
        }
        return false;
    }

    private static long extractSize(Element response, String tagName) {
        NodeList nodes = response.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() > 0) {
            try {
                return Long.parseLong(nodes.item(0).getTextContent());
            } catch (NumberFormatException ignored) {
            }
        }
        return -1L;
    }

    private static long extractLastModified(Element response) {
        NodeList nodes = response.getElementsByTagNameNS("*", "getlastmodified");
        if (nodes.getLength() > 0) {
            return HttpUtils.httpDateToMillis(nodes.item(0).getTextContent());
        }
        return HttpUtils.DEFAULT_LAST_MODIFIED;
    }

}
