package com.sos.commons.vfs.smb.smbj;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.SmbConfig.Builder;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.utils.SmbFiles;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.files.DeleteFilesResult;
import com.sos.commons.vfs.commons.file.files.RenameFilesResult;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.smb.SMBProvider;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;

public class ProviderImpl extends SMBProvider {

    private SMBClient client = null;
    private Session session = null;

    private boolean accessMaskMaximumAllowed = false;

    public ProviderImpl(ISOSLogger logger, SMBProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        boolean connected = false;
        try {
            getLogger().info(getConnectMsg());

            client = createClient();
            Connection connection = client.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
            connected = true;
            try {
                session = connection.authenticate(SMBAuthenticationContextFactory.create(getArguments()));
            } catch (Exception e) {
                throw new ProviderAuthenticationException(e);
            }
            getLogger().info(getConnectedMsg());
        } catch (Throwable e) {
            if (connected) {
                disconnect();
            }
            throw new ProviderConnectException(String.format("[%s]", getAccessInfo()), e);
        }
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        return session != null;
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        if (session == null && client == null) {
            return;
        }

        if (session != null) { // check due to session.getConnection()
            SOSClassUtil.closeQuietly(session); // session.close -> logout
            SOSClassUtil.closeQuietly(session.getConnection());
        }
        SOSClassUtil.closeQuietly(client);// closes connections ...

        session = null;
        client = null;

        getLogger().info(getDisconnectedMsg());
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        validatePrerequisites("selectFiles");

        selection = ProviderFileSelection.createIfNull(selection);
        selection.setFileTypeChecker(fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            FileIdBothDirectoryInformation r = (FileIdBothDirectoryInformation) fileRepresentator;
            return (!ProviderUtils.isDirectory(r) && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR));
        });

        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        List<ProviderFile> result = new ArrayList<>();
        try (DiskShare share = connectShare(directory)) {
            ProviderUtils.list(this, selection, getSMBPath(directory), result, share, 0);
        } catch (Throwable e) {
            throw new ProviderException(e);
        }
        return result;
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validatePrerequisites("exists", path, "path");

        try (DiskShare share = connectShare(path)) {
            // share.getFileInformation(getSMBPath(path));
            return share.fileExists(getSMBPath(path)) || share.folderExists(getSMBPath(path));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validatePrerequisites("createDirectoriesIfNotExists", path, "path");

        try (DiskShare share = connectShare(path)) {
            new SmbFiles().mkdirs(share, getSMBPath(path));
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteIfExists", path, "path");

        try (DiskShare share = connectShare(path)) {
            FileAllInformation info = null;
            try {
                info = share.getFileInformation(getSMBPath(path));
            } catch (SMBApiException e) {
                return false;// not exists
            }
            if (ProviderUtils.isDirectory(info)) {
                share.rmdir(getSMBPath(path), true);
            } else {
                share.rm(getSMBPath(path));
            }
            return true;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFilesIfExists(Collection, boolean)} */
    @Override
    public DeleteFilesResult deleteFilesIfExists(Collection<String> files, boolean stopOnSingleFileError) throws ProviderException {
        if (files == null) {
            return null;
        }
        validatePrerequisites("deleteFilesIfExists");

        DeleteFilesResult r = new DeleteFilesResult(files.size());
        try (DiskShare share = connectShare(files.iterator().next())) {// use the first element
            l: for (String file : files) {
                try {
                    String smbPath = getSMBPath(file);
                    if (share.fileExists(smbPath)) {
                        share.rm(smbPath);
                        r.addSuccess();
                    } else {
                        r.addNotFound(file);
                    }
                } catch (Throwable e) {
                    r.addError(file, e);
                    if (stopOnSingleFileError) {
                        break l;
                    }
                }
            }
        } catch (Throwable e) {
            new ProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#renameFilesIfSourceExists(Map, boolean)} */
    @Override
    public RenameFilesResult renameFilesIfSourceExists(Map<String, String> files, boolean stopOnSingleFileError) throws ProviderException {
        if (files == null) {
            return null;
        }
        validatePrerequisites("renameFilesIfSourceExists");

        RenameFilesResult r = new RenameFilesResult(files.size());
        try (DiskShare share = connectShare(files.keySet().iterator().next())) {// use the first element
            l: for (Map.Entry<String, String> entry : files.entrySet()) {
                String source = entry.getKey();
                String target = entry.getValue();
                try {
                    String sourceSMBPath = getSMBPath(source);
                    if (share.fileExists(sourceSMBPath)) {
                        try (File sourceFile = ProviderUtils.openExistingFileWithRenameAccess(accessMaskMaximumAllowed, share, sourceSMBPath)) {
                            sourceFile.rename(getSMBPath(target), true);
                            r.addSuccess();
                        }
                    } else {
                        r.addNotFound(source);
                    }
                } catch (Throwable e) {
                    r.addError(source, e);
                    if (stopOnSingleFileError) {
                        break l;
                    }
                }
            }
        } catch (Throwable e) {
            new ProviderException(e);
        }
        return r;
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileIfExists", path, "path");

        try (DiskShare share = connectShare(path)) {
            try {
                String smbPath = getSMBPath(path);
                return createProviderFile(smbPath, share.getFileInformation(smbPath));
            } catch (SMBApiException e) {// not exists
                return null;
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileContentIfExists", path, "path");

        StringBuilder content = new StringBuilder();
        try (InputStream is = new SMBInputStream(accessMaskMaximumAllowed, connectShare(path), getSMBPath(path)); Reader r = new InputStreamReader(is,
                StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(r)) {
            br.lines().forEach(content::append);
            return content.toString();
        } catch (SOSNoSuchFileException e) {
            return null;
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String,String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        try (OutputStream os = new SMBOutputStream(accessMaskMaximumAllowed, connectShare(path), getSMBPath(path), false)) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String,long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validatePrerequisites("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try (DiskShare share = connectShare(path)) {
            try (File file = ProviderUtils.openExistingFileWithChangeAttributeAccess(accessMaskMaximumAllowed, share, getSMBPath(path))) {
                FileBasicInformation info = file.getFileInformation().getBasicInformation();
                FileTime lastModified = FileTime.ofEpochMillis(milliseconds);
                // sets lastWriteTime,changeTime to lastModified
                file.setFileInformation(new FileBasicInformation(info.getCreationTime(), info.getLastAccessTime(), lastModified, lastModified, info
                        .getFileAttributes()));
            }
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        validatePrerequisites("getInputStream", path, "path");

        try {
            return new SMBInputStream(accessMaskMaximumAllowed, connectShare(path), getSMBPath(path));
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String,boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            return new SMBOutputStream(accessMaskMaximumAllowed, connectShare(path), getSMBPath(path), append);
        } catch (Throwable e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        if (client == null || session == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix() + method + "SMBClient/SMBSession");
        }
    }

    protected ProviderFile createProviderFile(String fullPath, FileIdBothDirectoryInformation info) {
        return createProviderFile(fullPath, info.getEndOfFile(), info.getLastWriteTime().toEpochMillis());
    }

    private SMBClient createClient() {
        Builder builder = SmbConfig.builder();
        applyConfiguratedProperties(getConfigurationPropertiesFromFiles(), builder);

        SmbConfig config = builder.build();
        if (getLogger().isDebugEnabled()) {
            List<String> excluded = Arrays.asList(
                    "authenticators;socketFactory;random;securityProvider;transportLayerFactory;clientGSSContextConfig;ntlmConfig".split(";"));
            getLogger().debug(String.format("%s[createClient][config]%s", getLogPrefix(), SOSString.toString(config, excluded)));
        }

        return new SMBClient(config);
    }

    private void applyConfiguratedProperties(Properties configuration, Builder builder) {
        if (configuration == null) {
            return;
        }
        configuration.entrySet().forEach(e -> {
            String key = e.getKey().toString().trim();
            String val = e.getValue().toString().trim();
            try {
                long t;
                switch (key) {
                case "workStationName": // Default: null
                    builder.withNtlmConfig().withWorkstationName(val);
                    break;

                case "soTimeout":// Default: 0
                    t = SOSDate.resolveAge("ms", val).longValue();
                    builder.withSoTimeout(t, TimeUnit.MILLISECONDS);
                    break;

                case "timeout":// Default: 60s, sets readTimeout, transactTimeout, writeTimeout
                    t = SOSDate.resolveAge("ms", val).longValue();
                    builder.withTimeout(t, TimeUnit.MILLISECONDS);
                    break;
                case "readTimeout":// Default: see timeout
                    t = SOSDate.resolveAge("ms", val).longValue();
                    builder.withReadTimeout(t, TimeUnit.MILLISECONDS);
                    break;
                case "transactTimeout":// Default: see timeout
                    t = SOSDate.resolveAge("ms", val).longValue();
                    builder.withTransactTimeout(t, TimeUnit.MILLISECONDS);
                    break;
                case "writeTimeout":// Default: see timeout
                    t = SOSDate.resolveAge("ms", val).longValue();
                    builder.withWriteTimeout(t, TimeUnit.MILLISECONDS);
                    break;

                case "bufferSize":// Default: 1048576(1024 * 1024),sets readBufferSize, transactBufferSize, writeBufferSize
                    builder.withBufferSize(Integer.parseInt(val));
                    break;
                case "readBufferSize":// Default: see bufferSize
                    builder.withReadBufferSize(Integer.parseInt(val));
                    break;
                case "transactBufferSize":// Default: see bufferSize
                    builder.withTransactBufferSize(Integer.parseInt(val));
                    break;
                case "writeBufferSize":// Default: see bufferSize
                    builder.withWriteBufferSize(Integer.parseInt(val));
                    break;

                case "dialects": // All: UNKNOWN; SMB_2_0_2; SMB_2_1; SMB_2XX; SMB_3_0; SMB_3_0_2; SMB_3_1_1
                    // Default: SMB_3_1_1; SMB_3_0_2; SMB_3_0; SMB_2_1; SMB_2_0_2
                    List<SMB2Dialect> l = Arrays.stream(val.split(";")).map(d -> SMB2Dialect.valueOf(d.trim())).collect(Collectors.toList());
                    builder.withDialects(l);
                    break;

                case "signingRequired": // Default: false
                    builder.withSigningRequired(Boolean.parseBoolean(val));
                    break;
                case "dfsEnabled":// Default: false
                    builder.withDfsEnabled(Boolean.parseBoolean(val));
                    break;
                case "multiProtocolNegotiate":// Default: false
                    builder.withMultiProtocolNegotiate(Boolean.parseBoolean(val));
                    break;
                case "encryptData":// Default: false
                    builder.withEncryptData(Boolean.parseBoolean(val));
                    break;
                case "sossmbj.accessMaskMaximumAllowed":
                    accessMaskMaximumAllowed = Boolean.parseBoolean(val);
                    break;
                case "sossmbj.pathSeparator":
                    getLogger().info(String.format(
                            "%s[applyConfiguratedProperties][%s=%s]The setting is no longer supported. The path separator %s is used.",
                            getLogPrefix(), key, val, getPathSeparator()));
                    break;
                }
            } catch (Throwable te) {
                getLogger().warn(String.format("%s[applyConfiguratedProperties][%s=%s]%s", getLogPrefix(), key, val, te.toString()), te);
            }
        });
    }

    /** Returns normalized path without shareName
     * 
     * @param path
     * @return */
    private String getSMBPath(String path) {
        String shareName = getShareName(path);
        String smbPath = normalizePath(path);

        smbPath = SOSString.trimStart(smbPath, getPathSeparator());
        if (!SOSString.isEmpty(shareName)) {
            // finds the share name in the path and removes it.
            int shareIndex = smbPath.indexOf(shareName + getPathSeparator());
            if (shareIndex != -1) {
                smbPath = smbPath.substring(shareIndex + shareName.length() + 1); // +1 for pathSeparator
            }
        }
        return smbPath;
    }

    /** Can throw a SMBRuntimeException
     * 
     * @param path
     * @return connected DiskShare */
    private DiskShare connectShare(String path) {
        return (DiskShare) session.connectShare(getShareName(path));
    }

    private ProviderFile createProviderFile(String fullPath, FileAllInformation info) {
        return createProviderFile(fullPath, info.getStandardInformation().getEndOfFile(), info.getBasicInformation().getLastWriteTime()
                .toEpochMillis());
    }

    private void validatePrerequisites(String method, String paramValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, paramValue, msg);
    }

}
