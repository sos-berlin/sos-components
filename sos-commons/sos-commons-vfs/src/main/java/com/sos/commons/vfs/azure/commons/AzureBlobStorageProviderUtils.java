package com.sos.commons.vfs.azure.commons;

import java.io.IOException;
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
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.transform.SOSXmlTransformer;

public class AzureBlobStorageProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(AzureBlobStorageProvider provider, ProviderFileSelection selection, String containerName,
            String directoryPath, List<ProviderFile> result) throws Exception {
        int counterAdded = 0;

        list(provider, selection, containerName, directoryPath, result, counterAdded);
        return result;
    }

    public static AzureBlobStorageResource getResource(AzureBlobStorageProvider provider, String containerName, String blobPath, boolean directory,
            boolean recursive) throws Exception {
        HttpExecutionResult<String> result = provider.getClient().executeGETBlobInfo(containerName, blobPath, false);
        result.formatWithResponseBody(true);
        int code = result.response().statusCode();
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[getResource]%s", provider.getLogPrefix(), provider.getClient().formatExecutionResultForException(result));
        }
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return null;
            }
            throw new IOException(provider.getClient().formatExecutionResultForException(result));
        }
        List<AzureBlobStorageResource> resources = parseAzureBlobResources(provider, containerName, blobPath, result, recursive);
        return resources.isEmpty() ? null : resources.get(0);
    }

    private static int list(AzureBlobStorageProvider provider, ProviderFileSelection selection, String containerName, String directoryPath,
            List<ProviderFile> result, int counterAdded) throws Exception {

        directoryPath = SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(directoryPath);
        HttpExecutionResult<String> executeResult = provider.getClient().executeGETBlobInfo(containerName, directoryPath, false);
        executeResult.formatWithResponseBody(true);
        int code = executeResult.response().statusCode();
        if (provider.getLogger().isDebugEnabled()) {
            provider.getLogger().debug("%s[list][directoryPath=%s]%s", provider.getLogPrefix(), directoryPath, AzureBlobStorageClient
                    .formatExecutionResult(executeResult));
        }
        if (!HttpUtils.isSuccessful(code)) {
            if (HttpUtils.isNotFound(code)) {
                return 0;
            }
            throw new IOException(provider.getClient().formatExecutionResultForException(executeResult));
        }

        Set<String> subDirectories = new HashSet<>();
        for (AzureBlobStorageResource resource : parseAzureBlobResources(provider, containerName, directoryPath, executeResult, selection.getConfig()
                .isRecursive())) {
            if (selection.maxFilesExceeded(counterAdded)) {
                return counterAdded;
            }
            if (resource.isDirectory()) {
                if (selection.getConfig().isRecursive()) {
                    if (selection.checkDirectory(resource.getBlobPath())) {
                        subDirectories.add(resource.getBlobPath());
                    }
                }
            } else {
                if (selection.checkFileName(SOSPathUtils.getName(resource.getBlobPath())) && selection.isValidFileType(resource)) {
                    ProviderFile file = provider.createProviderFile(resource);
                    if (file == null) {
                        if (provider.getLogger().isDebugEnabled()) {
                            provider.getLogger().debug(provider.getPathOperationPrefix(resource.getFullPath()) + "[skip]" + resource);
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
            counterAdded = list(provider, selection, containerName, subDirectory, result, counterAdded);
        }
        return counterAdded;
    }

    private static List<AzureBlobStorageResource> parseAzureBlobResources(AzureBlobStorageProvider provider, String containerName, String blobPath,
            HttpExecutionResult<String> result, boolean recursive) throws Exception {
        boolean isDebugEnabled = provider.getLogger().isDebugEnabled();

        String body = SOSXML.removeBOMIfExists(result.response().body().trim());
        NodeList fileNodes = SOSXML.parse(body, false).getElementsByTagName("Blob");
        if (isDebugEnabled) {
            provider.getLogger().debug("%s[parseAzureBlobResources][blobPath=%s]size=%s", provider.getLogPrefix(), blobPath, fileNodes.getLength());
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
                provider.getLogger().debug("[parseAzureBlobResources][%s][skip]missing Name", i);
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

}
