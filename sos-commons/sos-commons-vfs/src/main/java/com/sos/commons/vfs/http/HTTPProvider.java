package com.sos.commons.vfs.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;

public class HTTPProvider extends AProvider<HTTPProviderArguments> {

    public HTTPProvider(ISOSLogger logger, HTTPProviderArguments arguments) throws SOSProviderInitializationException {
        super(logger, arguments);
    }

    @Override
    public String getPathSeparator() {
        return SOSPathUtil.PATH_SEPARATOR_UNIX;
    }

    @Override
    public boolean isAbsolutePath(String path) {
        return SOSPathUtil.isAbsoluteURIPath(path);
    }

    @Override
    public String normalizePath(String path) {
        try {
            return new URI(path).normalize().toString();
        } catch (URISyntaxException e) {
            return path;
        }
    }

    @Override
    public void connect() throws SOSProviderConnectException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists(String path) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createDirectoriesIfNotExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteIfExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProviderFile getFileIfExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProviderFile rereadFileIfExists(ProviderFile file) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFileContentIfExists(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeFile(String path, String content) throws SOSProviderException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws SOSProviderException {
        // TODO Auto-generated method stub

    }

    @Override
    public InputStream getInputStream(String path) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream(String path, boolean append) throws SOSProviderException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SOSCommandResult cancelCommands() {
        // TODO Auto-generated method stub
        return null;
    }

}
