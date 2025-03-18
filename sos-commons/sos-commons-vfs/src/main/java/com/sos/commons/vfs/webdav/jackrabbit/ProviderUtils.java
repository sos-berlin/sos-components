package com.sos.commons.vfs.webdav.jackrabbit;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.http.apache.HTTPClient;
import com.sos.commons.vfs.http.apache.HTTPClient.ExecuteResult;
import com.sos.commons.vfs.http.commons.HTTPUtils;

public class ProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(ProviderImpl provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws ProviderException {
        int counterAdded = 0;
        try {
            list(provider, selection, directoryPath, result, counterAdded);
        } catch (Throwable e) {
            throw new ProviderException(e);
        }
        return result;
    }

    public static HttpPropfind createDirectoryPropertiesRequest(URI uri) throws IOException {
        return new HttpPropfind(uri, getResourceMetadataProperties(), DavConstants.DEPTH_1);
    }

    public static HttpPropfind createFilePropertiesRequest(URI uri) throws IOException {
        return new HttpPropfind(uri, getResourceMetadataProperties(), DavConstants.DEPTH_0);
    }

    public static MultiStatus getMuiltiStatus(HttpPropfind request, CloseableHttpResponse response) throws DavException {
        request.checkSuccess(response);

        MultiStatus result = null;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MULTI_STATUS) {// 207
            result = request.getResponseBodyAsMultiStatus(response);
        }
        return result;
    }

    public static boolean exists(HTTPClient client, URI uri) throws Exception {
        HttpPropfind request = new HttpPropfind(uri, null, DavConstants.DEPTH_0);
        try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
            int code = response.getStatusLine().getStatusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                if (HTTPUtils.isNotFound(code)) {
                    return false;
                }
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
            return true;
        }
    }

    public static void createDirectory(HTTPClient client, URI uri) throws Exception {
        HttpMkcol request = new HttpMkcol(uri);
        try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
            int code = response.getStatusLine().getStatusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
        }
    }

    public static boolean directoryExists(HTTPClient client, URI uri) throws Exception {
        DavPropertyNameSet names = new DavPropertyNameSet();
        names.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));

        HttpPropfind request = new HttpPropfind(uri, names, DavConstants.DEPTH_0);
        try (ExecuteResult result = client.execute(request); CloseableHttpResponse response = result.getResponse()) {
            int code = response.getStatusLine().getStatusCode();
            if (!HTTPUtils.isSuccessful(code)) {
                throw new IOException(HTTPClient.getResponseStatus(result));
            }
            MultiStatus status = getMuiltiStatus(request, response);
            if (status == null) {
                return false;
            }
            for (MultiStatusResponse resource : status.getResponses()) {
                DavPropertySet prop = resource.getProperties(HttpStatus.SC_OK);// 200
                if (prop == null) {
                    return false;
                }
                return isDirectory(prop);
            }
        }
        return false;
    }

    private static int list(ProviderImpl provider, ProviderFileSelection selection, String directoryPath, List<ProviderFile> result, int counterAdded)
            throws ProviderException {
        try {
            URI uri = new URI(provider.normalizePath(directoryPath));
            HttpPropfind request = createDirectoryPropertiesRequest(uri);

            Set<String> subDirectories = new HashSet<>();
            try (ExecuteResult executeResult = provider.getClient().execute(request); CloseableHttpResponse response = executeResult.getResponse()) {
                int code = response.getStatusLine().getStatusCode();
                if (!HTTPUtils.isSuccessful(code)) {
                    throw new IOException(HTTPClient.getResponseStatus(executeResult));
                }
                MultiStatus status = getMuiltiStatus(request, response);
                if (status != null) {
                    for (MultiStatusResponse resource : status.getResponses()) {
                        // if(!r.getHref().equals(uri.toString()))
                        if (selection.maxFilesExceeded(counterAdded)) {
                            return counterAdded;
                        }

                        DavPropertySet prop = resource.getProperties(HttpStatus.SC_OK);// 200
                        if (prop == null) {
                            continue;
                        }
                        if (isDirectory(prop)) {
                            if (selection.getConfig().isRecursive()) {
                                if (selection.checkDirectory(resource.getHref())) {
                                    subDirectories.add(resource.getHref());
                                }
                            }
                        } else {
                            if (selection.checkFileName(SOSPathUtils.getName(resource.getHref())) && selection.isValidFileType(resource)) {
                                ProviderFile file = provider.createProviderFile(resource.getHref(), response, prop);
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

    /** @param set
     * @return */
    private static boolean isDirectory(DavPropertySet set) {
        // <D:prop><D:resourcetype><D:collection/></D:resourcetype></D:prop>
        DavProperty<?> prop = set.get(DavConstants.PROPERTY_RESOURCETYPE);

        // resourcetype node exists with a collection child node
        // return prop != null && prop.getValue() instanceof Node && DavConstants.XML_COLLECTION.equals(((Node) prop.getValue()).getLocalName());
        return prop != null && prop.getValue() != null && prop.getValue().toString().contains(DavConstants.XML_COLLECTION);
    }

    private static DavPropertyNameSet getResourceMetadataProperties() {
        DavPropertyNameSet names = new DavPropertyNameSet();
        names.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));
        names.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTLENGTH));
        names.add(DavPropertyName.create(DavConstants.PROPERTY_GETLASTMODIFIED));
        return names;
    }

}
