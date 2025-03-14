package com.sos.commons.vfs.webdav.jackrabbit;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
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

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.http.commons.HTTPClient;

public class ProviderUtils {

    // possible recursion
    public static List<ProviderFile> selectFiles(ProviderImpl provider, ProviderFileSelection selection, String directoryPath,
            List<ProviderFile> result) throws SOSProviderException {
        int counterAdded = 0;
        try {
            list(provider, selection, directoryPath, result, counterAdded);
        } catch (Throwable e) {
            throw new SOSProviderException(e);
        }
        return result;
    }

    public static HttpPropfind createResourcePropertiesRequest(URI uri) throws IOException {
        DavPropertyNameSet names = new DavPropertyNameSet();
        names.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));
        names.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTLENGTH));
        names.add(DavPropertyName.create(DavConstants.PROPERTY_GETLASTMODIFIED));
        return new HttpPropfind(uri, names, DavConstants.DEPTH_1);
    }

    public static boolean exists(HTTPClient client, URI uri) throws Exception {
        try (CloseableHttpResponse response = client.execute(new HttpPropfind(uri, null, DavConstants.DEPTH_0))) {
            StatusLine sl = response.getStatusLine();
            if (!HTTPClient.isSuccessful(sl)) {
                throw new IOException(HTTPClient.getResponseStatus(uri, sl));
            }
            return true;
        }
    }

    public static void createDirectory(HTTPClient client, URI uri) throws Exception {
        try (CloseableHttpResponse response = client.execute(new HttpMkcol(uri))) {
            StatusLine sl = response.getStatusLine();
            if (!HTTPClient.isSuccessful(sl)) {
                throw new IOException(HTTPClient.getResponseStatus(uri, sl));
            }
        }
    }

    public static boolean directoryExists(HTTPClient client, URI uri) throws Exception {
        DavPropertyNameSet names = new DavPropertyNameSet();
        names.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));

        HttpPropfind request = new HttpPropfind(uri, names, DavConstants.DEPTH_0);
        try (CloseableHttpResponse response = client.execute(request)) {
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
            throws SOSProviderException {
        try {
            HttpPropfind request = createResourcePropertiesRequest(new URI(provider.normalizePath(directoryPath)));

            Set<String> subDirectories = new HashSet<>();
            try (CloseableHttpResponse response = provider.getClient().execute(request)) {
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
                            if (selection.checkFileName(SOSPathUtil.getName(resource.getHref())) && selection.isValidFileType(resource)) {
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
            throw new SOSProviderException(e);
        }
        return counterAdded;
    }

    private static MultiStatus getMuiltiStatus(HttpPropfind request, CloseableHttpResponse response) throws DavException {
        request.checkSuccess(response);

        MultiStatus result = null;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MULTI_STATUS) {// 207
            result = request.getResponseBodyAsMultiStatus(response);
        }
        return result;
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

}
