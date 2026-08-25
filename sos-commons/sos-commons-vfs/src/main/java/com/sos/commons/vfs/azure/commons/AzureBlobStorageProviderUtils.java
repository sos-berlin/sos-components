package com.sos.commons.vfs.azure.commons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sos.commons.httpclient.azure.AzureBlobStorageClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.vfs.azure.AzureBlobStorageProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderDirectoryException;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.transform.SOSXmlTransformer;

/** @implNote AzureBlobStorageProviderUtils class must avoid throwing custom or new IOException instances, since IOException is reserved for signaling
 *           underlying connection or transport errors */
public class AzureBlobStorageProviderUtils {

    private static final String ROOT_FOLDER = "/";

    // possible recursion
    public static List<ProviderFile> selectFiles(AzureBlobStorageProvider provider, ProviderFileSelection selection, String containerName,
            String directoryPath, List<ProviderFile> result) throws Exception {
        int counterAdded = 0;

        list(provider, provider.requireAzureClient(), selection, containerName, directoryPath, result, 0, counterAdded);
        return result;
    }

    public static AzureBlobStorageResource getResource(AzureBlobStorageProvider provider, String containerName, String blobPath, boolean directory,
            boolean recursive, int level) throws Exception {

        AzureBlobStorageClient client = provider.requireAzureClient();
        if (directory) {
            HttpExecutionResult<String> result = client.executeGETBlobList(containerName, blobPath, false);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (provider.getLogger().isDebugEnabled()) {
                provider.getLogger().debug("%s[getResource][directory]%s", provider.getLogPrefix(), client.formatExecutionResultForException(result));
            }
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return null;
                } else if (HttpUtils.isUnauthorized(code)) {
                    throw new ProviderAuthenticationException(client.formatExecutionResultForException(result));
                } else {
                    throw new Exception(client.formatExecutionResultForException(result));
                }
            }
            List<AzureBlobStorageResource> resources = parseAzureBlobResources(provider, containerName, blobPath, result, recursive, level);
            return resources.isEmpty() ? null : resources.get(0);
        } else {
            HttpExecutionResult<Void> result = provider.requireAzureClient().executeHEADBlob(containerName, blobPath);
            result.formatWithResponseBody(true);
            int code = result.response().statusCode();
            if (provider.getLogger().isDebugEnabled()) {
                provider.getLogger().debug("%s[getResource][file]%s", provider.getLogPrefix(), client.formatExecutionResultForException(result));
            }
            if (!HttpUtils.isSuccessful(code)) {
                if (HttpUtils.isNotFound(code)) {
                    return null;
                } else if (HttpUtils.isUnauthorized(code)) {
                    throw new ProviderAuthenticationException(client.formatExecutionResultForException(result));
                } else {
                    throw new Exception(client.formatExecutionResultForException(result));
                }
            }
            return new AzureBlobStorageResource(containerName, blobPath, false, provider.requireAzureClient().getFileSize(result.response()), client
                    .getLastModifiedInMillis(result.response()));
        }
    }

    public static boolean directoryExistsIfHnsDisabled(AzureBlobStorageProvider provider, AzureBlobStorageClient client, String containerName,
            String directoryPath) throws Exception {

        directoryPath = SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(directoryPath);
        HttpExecutionResult<String> executeResult = client.executeGETBlobList(containerName, directoryPath, false);
        executeResult.formatWithResponseBody(true);
        int code = executeResult.response().statusCode();
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[directoryExistsIfHnsDisabled][directoryPath=%s]%s", provider.getLogPrefix(), directoryPath,
                    AzureBlobStorageClient.formatExecutionResult(executeResult));
        }
        if (!HttpUtils.isSuccessful(code)) {
            // HttpUtils.isNotFound:
            // 1) HNS=false - does not work because Azure Blob Storage uses virtual directories and does not return HTTP 404 when a directory does not exist.
            // Instead, it returns code=200 and an empty <Blobs /> response
            // see parseAzureBlobResources for HNS=false handling
            // 2) HNS=true - TODO check it
            if (HttpUtils.isNotFound(code)) {
                provider.throwDirectoryNotFoundException(directoryPath, client.formatExecutionResultForException(executeResult));
            }

            if (HttpUtils.isUnauthorized(code)) {
                throw new ProviderAuthenticationException(client.formatExecutionResultForException(executeResult));
            } else {
                throw new ProviderDirectoryException(client.formatExecutionResultForException(executeResult));
            }
        }

        String body = SOSXML.removeBOMIfExists(executeResult.response().body().trim());
        NodeList directoryNodes = SOSXML.parse(body, false).getElementsByTagName("BlobPrefix");
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[directoryExistsIfHnsDisabled][directoryPath=%s][directoryNodes(BlobPrefix)]size=%s", provider
                    .getLogPrefix(), directoryPath, directoryNodes.getLength());
        }
        if (directoryNodes.getLength() > 0) {
            return true;
        }

        NodeList fileNodes = SOSXML.parse(body, false).getElementsByTagName("Blob");
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[directoryExistsIfHnsDisabled][directoryPath=%s][fileNodes(Blob)]size=%s", provider.getLogPrefix(),
                    directoryPath, fileNodes.getLength());
        }
        return fileNodes.getLength() > 0;
    }

    private static int list(AzureBlobStorageProvider provider, AzureBlobStorageClient client, ProviderFileSelection selection, String containerName,
            String directoryPath, List<ProviderFile> result, int level, int counterAdded) throws Exception {

        directoryPath = SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(directoryPath);
        HttpExecutionResult<String> executeResult = client.executeGETBlobList(containerName, directoryPath, false);
        executeResult.formatWithResponseBody(true);
        int code = executeResult.response().statusCode();
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[list][directoryPath=%s]%s", provider.getLogPrefix(), directoryPath, AzureBlobStorageClient
                    .formatExecutionResult(executeResult));
        }
        if (!HttpUtils.isSuccessful(code)) {
            // HttpUtils.isNotFound:
            // 1) HNS=false - does not work because Azure Blob Storage uses virtual directories and does not return HTTP 404 when a directory does not exist.
            // Instead, it returns code=200 and an empty <Blobs /> response
            // see parseAzureBlobResources for HNS=false handling
            // 2) HNS=true - TODO check it
            if (HttpUtils.isNotFound(code)) {
                if (level == 0) {
                    provider.throwDirectoryNotFoundException(directoryPath, client.formatExecutionResultForException(executeResult));
                }
                // return 0;
            }

            if (HttpUtils.isUnauthorized(code)) {
                throw new ProviderAuthenticationException(client.formatExecutionResultForException(executeResult));
            } else {
                throw new ProviderDirectoryException(client.formatExecutionResultForException(executeResult));
            }
        }

        Set<String> subDirectories = new HashSet<>();
        int i = 0;
        for (AzureBlobStorageResource resource : parseAzureBlobResources(provider, containerName, directoryPath, executeResult, selection.getConfig()
                .isRecursive(), level)) {
            if (selection.maxFilesExceeded(counterAdded)) {
                return counterAdded;
            }

            i++;
            if (provider.getLogger().isDebugEnabled()) {
                provider.getLogger().debug("%s[list][%s]%s", provider.getLogPrefix(), i, resource);
            }
            if (resource.isDirectory()) {
                if (selection.getConfig().isRecursive()) {
                    // root folder already processed
                    if (!isRootFolder(resource.getBlobPath())) {
                        if (selection.checkDirectory(resource.getBlobPath())) {
                            subDirectories.add(resource.getBlobPath());
                        }
                    }
                }
            } else {
                if (selection.checkFileName(SOSPathUtils.getName(resource.getBlobPath())) && selection.isValidFileType(resource)) {
                    ProviderFile file = provider.createProviderFile(resource);
                    if (file == null) {
                        if (provider.getLogger().isDebugEnabled()) {
                            provider.getLogger().debug("%s[list][%s][skip][fullPath=%s]ProviderFile is null", provider.getLogPrefix(), i, resource
                                    .getFullPath());
                        }
                    } else {
                        if (selection.checkProviderFile(provider, file)) {
                            counterAdded++;

                            file.setIndex(counterAdded);
                            result.add(file);

                            if (provider.getLogger().isDebugEnabled()) {
                                provider.getLogger().debug("%s[list][%s][added][fullPath]%s", provider.getLogPrefix(), i, resource.getFullPath());
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
            counterAdded = list(provider, provider.requireAzureClient(), selection, containerName, subDirectory, result, level++, counterAdded);
        }
        return counterAdded;
    }

    private static List<AzureBlobStorageResource> parseAzureBlobResources(AzureBlobStorageProvider provider, String containerName, String blobPath,
            HttpExecutionResult<String> result, boolean recursive, int level) throws Exception {
        boolean isDebugEnabled = provider.getLogger().isDebugEnabled();

        String body = SOSXML.removeBOMIfExists(result.response().body().trim());
        NodeList fileNodes = SOSXML.parse(body, false).getElementsByTagName("Blob");
        if (isDebugEnabled) {
            provider.getLogger().debug("%s[parseAzureBlobResources][blobPath=%s][fileNodes(Blob)]size=%s", provider.getLogPrefix(), blobPath,
                    fileNodes.getLength());
        }

        // HNS=false: virtual directories
        // - if no blobs exist under the prefix, the directory does not exist
        if (level == 0 && !provider.isHnsEnabled() && fileNodes.getLength() == 0) {
            NodeList directoryNodes = SOSXML.parse(body, false).getElementsByTagName("BlobPrefix");
            if (isDebugEnabled) {
                provider.getLogger().debug("%s[parseAzureBlobResources][blobPath=%s][directoryNodes(BlobPrefix))]size=%s", provider.getLogPrefix(),
                        blobPath, directoryNodes.getLength());
            }
            if (directoryNodes.getLength() == 0) {
                provider.throwDirectoryNotFoundException(containerName + "/" + blobPath);
            }
        }

        List<AzureBlobStorageResource> resources = new ArrayList<>();
        for (int i = 0; i < fileNodes.getLength(); i++) {
            Element blob = (Element) fileNodes.item(i);
            if (isDebugEnabled) {
                provider.getLogger().debug("%s[parseAzureBlobResources][%s][file]%s", provider.getLogPrefix(), i, SOSXmlTransformer.nodeToString(
                        blob));
            }
            String resourcePath = SOSXML.getChildNodeValue(blob, "Name");
            if (SOSString.isEmpty(resourcePath)) {
                if (isDebugEnabled) {
                    provider.getLogger().debug("[parseAzureBlobResources][%s][file][skip]missing Name", i);
                }
                continue;
            }
            resources.add(new AzureBlobStorageResource(containerName, resourcePath, false, extractSize(blob), extractLastModified(blob)));
        }

        if (recursive) {
            NodeList subDirectoryNodes = SOSXML.parse(body, false).getElementsByTagName("BlobPrefix");
            for (int i = 0; i < subDirectoryNodes.getLength(); i++) {
                Element blob = (Element) subDirectoryNodes.item(i);
                if (isDebugEnabled) {
                    provider.getLogger().debug("%s[parseAzureBlobResources][%s][directory]%s", provider.getLogPrefix(), i, SOSXmlTransformer
                            .nodeToString(blob));
                }
                String resourcePath = SOSXML.getChildNodeValue(blob, "Name");
                if (SOSString.isEmpty(resourcePath)) {
                    if (isDebugEnabled) {
                        provider.getLogger().debug("[parseAzureBlobResources][%s][directory][skip]missing Name", i);
                    }
                    continue;
                }
                // root folder already processed
                if (isRootFolder(resourcePath)) {
                    continue;
                }
                resources.add(new AzureBlobStorageResource(containerName, resourcePath, true, -1L, HttpUtils.DEFAULT_LAST_MODIFIED));
            }
        }
        return resources;
    }

    private static long extractSize(Element blob) {
        String contentLength = SOSXML.getChildNodeValue(blob, "Content-Length");
        if (!SOSString.isEmpty(contentLength)) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1L;
    }

    private static long extractLastModified(Element blob) {
        String lastModified = SOSXML.getChildNodeValue(blob, "Last-Modified");
        if (SOSString.isEmpty(lastModified)) {
            return HttpUtils.DEFAULT_LAST_MODIFIED;
        }

        return HttpUtils.httpDateToMillis(lastModified);
    }

    private static boolean isRootFolder(String directoryPath) {
        if (directoryPath == null) {
            return true;
        }
        return ROOT_FOLDER.equals(directoryPath);
    }

}
