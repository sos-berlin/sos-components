package com.sos.commons.vfs.smb.smbj;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.SmbConfig.Builder;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.utils.SmbFiles;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.AProviderArguments.FileType;
import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.commons.ProviderReusableResourcePool;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.smb.SMBProvider;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;

public class SMBJProvider extends SMBProvider<SMBJProviderReusableResource, DiskShare> {

    private final Object clientLock = new Object();
    private volatile SMBClient client = null;
    private volatile Session session = null;

    private boolean accessMaskMaximumAllowed = false;

    public SMBJProvider(ISOSLogger logger, SMBProviderArguments args) throws ProviderInitializationException {
        super(logger, args);
        getArguments().tryRedefineHostPort();
    }

    /** Overrides {@link IProvider#connect()} */
    @Override
    public void connect() throws ProviderConnectException {
        if (SOSString.isEmpty(getArguments().getHost().getValue())) {
            throw new ProviderConnectException(new SOSRequiredArgumentMissingException("host"));
        }

        synchronized (clientLock) {
            try {
                if (client == null) {
                    client = createClient();
                }
                getLogger().info(getConnectMsg());
                Connection connection = client.connect(getArguments().getHost().getValue(), getArguments().getPort().getValue());
                try {
                    session = connection.authenticate(SMBAuthenticationContextFactory.create(getArguments()));
                } catch (Exception e) {
                    throw new ProviderAuthenticationException(e);
                }

                getLogger().info(getConnectedMsg());
            } catch (Exception e) {
                // Do not call disconnect() here. it sets the client to null and may cause a ProviderClientNotInitializedException instead of a real connection
                // error in methods executed after connect() - e.g. if retry, roll back...
                // Call disconnect() in the application's finally block.
                // disconnect();
                throw new ProviderConnectException(String.format("[%s]", getAccessInfo()), e);
            }
        }
    }

    /** Overrides {@link IProvider#isConnected()} */
    @Override
    public boolean isConnected() {
        synchronized (clientLock) {
            return session != null && session.getConnection() != null && session.getConnection().isConnected();
        }
    }

    /** Overrides {@link IProvider#disconnect()} */
    @Override
    public void disconnect() {
        synchronized (clientLock) {
            if (session == null && client == null) {
                return;
            }

            closeResources();
            session = null;
            client = null;

            getLogger().info(getDisconnectedMsg());
        }
    }

    /** Overrides {@link AProvider#createClientPool()} */
    @Override
    public ProviderReusableResourcePool<SMBJProviderReusableResource, DiskShare> createResourcePool(String path) throws ProviderException {
        try {
            return new ProviderReusableResourcePool<>(id -> {
                try {
                    return new SMBJProviderReusableResource(id, this, path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new ProviderException(e);
        }
    }

    /** Overrides {@link IProvider#injectConnectivityFault()} */
    @Override
    public void injectConnectivityFault() {
        synchronized (clientLock) {
            closeResources();
            getLogger().info(getInjectConnectivityFaultMsg());
        }
    }

    /** Overrides {@link IProvider#selectFiles(ProviderFileSelection)} */
    @Override
    public List<ProviderFile> selectFiles(ProviderFileSelection selection) throws ProviderException {
        selection = ProviderFileSelection.createIfNull(selection);
        selection.setFileTypeChecker(fileRepresentator -> {
            if (fileRepresentator == null) {
                return false;
            }
            FileIdBothDirectoryInformation r = (FileIdBothDirectoryInformation) fileRepresentator;
            return (!SMBJProviderUtils.isDirectory(r) && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR));
        });

        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        final ProviderFileSelection finalSelection = selection;
        try {
            return requireResourcePool().withResource(share -> {
                List<ProviderFile> result = new ArrayList<>();
                SMBJProviderUtils.selectFiles(this, share, finalSelection, getSMBPath(directory), result, 0);
                return result;
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validateArgument("exists", path, "path");

        try {
            return requireResourcePool(path).withResource(share -> {
                return share.fileExists(getSMBPath(path)) || share.folderExists(getSMBPath(path));
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validateArgument("createDirectoriesIfNotExists", path, "path");

        try {
            String smbPath = getSMBPath(path);
            return requireResourcePool(path).withResource(share -> {
                new SmbFiles().mkdirs(share, smbPath);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
                }
                return true;
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validateArgument("deleteIfExists", path, "path");

        try {
            String smbPath = getSMBPath(path);
            return requireResourcePool(path).withResource(share -> {
                return SMBJProviderUtils.deleteIfExists(share, smbPath);
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause() == null ? e : e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        validateArgument("deleteFileIfExists", path, "path");

        try {
            String smbPath = getSMBPath(path);
            return requireResourcePool(path).withResource(share -> {
                return SMBJProviderUtils.deleteFileIfExists(share, smbPath);
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause() == null ? e : e.getCause());
        }
    }

    /** Overrides {@link IProvider#moveFileIfExists(String, String)} */
    @Override
    public boolean moveFileIfExists(String source, String target) throws ProviderException {
        validateArgument("moveFileIfExists", source, "source");
        validateArgument("moveFileIfExists", target, "target");

        try {
            String smbSourcePath = getSMBPath(source);
            String smbTargetPath = getSMBPath(target);
            return requireResourcePool(source).withResource(share -> {
                return SMBJProviderUtils.renameFileIfSourceExists(share, smbSourcePath, smbTargetPath, accessMaskMaximumAllowed);
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validateArgument("getFileIfExists", path, "path");

        try {
            String smbPath = getSMBPath(path);
            return requireResourcePool(path).withResource(share -> {
                try {
                    return createProviderFile(smbPath, share.getFileInformation(smbPath));
                } catch (SMBApiException e) {// not exists
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validateArgument("getFileContentIfExists", path, "path");

        try {
            return requireResourcePool(path).withResource(share -> {
                StringBuilder content = new StringBuilder();
                try (InputStream is = new SMBJInputStream(accessMaskMaximumAllowed, share, getSMBPath(path), 0L); Reader r = new InputStreamReader(is,
                        StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(r)) {
                    br.lines().forEach(content::append);
                    return content.toString();
                } catch (SOSNoSuchFileException e) {
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }

    }

    /** Overrides {@link IProvider#writeFile(String,String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validateArgument("writeFile", path, "path");

        try {
            requireResourcePool(path).withResource(share -> {
                try (OutputStream os = new SMBJOutputStream(accessMaskMaximumAllowed, share, getSMBPath(path), false)) {
                    os.write(content.getBytes(StandardCharsets.UTF_8));
                }
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }

    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String,long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validateArgument("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try {
            String smbPath = getSMBPath(path);
            requireResourcePool(path).withResource(share -> {
                SMBJProviderUtils.setFileLastModifiedFromMillis(share, smbPath, milliseconds, accessMaskMaximumAllowed);
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#supportsReadOffset()} */
    public boolean supportsReadOffset() {
        return false;
    }

    /** Overrides {@link IProvider#getInputStream(String)} */
    @Override
    public InputStream getInputStream(String path) throws ProviderException {
        return getInputStream(path, 0L);
    }

    /** Overrides {@link IProvider#getInputStream(String, long)} */
    @Override
    public InputStream getInputStream(String path, long offset) throws ProviderException {
        validateArgument("getInputStream", path, "path");

        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getInputStream][supportsReadOffset=%s, offset=%s]%s", getLogPrefix(), supportsReadOffset(), offset, path);
            }
            return requireResourcePool(path).withResource(share -> {
                return new SMBJInputStream(accessMaskMaximumAllowed, share, getSMBPath(path), offset);
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String,boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validateArgument("getOutputStream", path, "path");

        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("%s[getOutputStream][append=%s]%s", getLogPrefix(), append, path);
            }
            return requireResourcePool(path).withResource(share -> {
                return new SMBJOutputStream(accessMaskMaximumAllowed, share, getSMBPath(path), append);
            });
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#executeCommand(String, SOSTimeout, SOSEnv)} */
    @Override
    public SOSCommandResult executeCommand(String command, SOSTimeout timeout, SOSEnv env) {
        return SOSShell.executeCommand(command, timeout, env);
    }

    protected ProviderFile createProviderFile(String fullPath, FileIdBothDirectoryInformation info) {
        if (!fullPath.startsWith(getPathSeparator())) {
            fullPath = getPathSeparator() + fullPath;
        }
        return createProviderFile(fullPath, info.getEndOfFile(), info.getLastWriteTime().toEpochMillis());
    }

    /** Can throw a SMBRuntimeException
     * 
     * @param path
     * @return connected DiskShare */
    protected DiskShare connectShare(String path) throws ProviderException {
        synchronized (clientLock) {
            if (session == null) {
                // 0 - getStackTrace
                // 1 - requireClient
                // 2 - caller
                throw new ProviderClientNotInitializedException(getLogPrefix(), Session.class, SOSClassUtil.getMethodName(2));
            }
            return (DiskShare) session.connectShare(getShareName(path));
        }
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
            } catch (Exception te) {
                getLogger().warn(String.format("%s[applyConfiguratedProperties][%s=%s]%s", getLogPrefix(), key, val, te.toString()), te);
            }
        });
    }

    private void closeResources() {
        if (session != null) { // check due to session.getConnection()
            SOSClassUtil.closeQuietly(session); // session.close -> logout
            SOSClassUtil.closeQuietly(session.getConnection());
        }
        // close DiskShare instances
        closeResourcePool();
        SOSClassUtil.closeQuietly(client);// closes connections ...
    }

    /** Returns normalized path without shareName
     * 
     * @param path
     * @return */
    private String getSMBPath(String path) {
        String smbPath = normalizePath(path);
        if (SOSString.isEmpty(smbPath)) {
            return "";
        }
        String shareName = getShareName(path);
        smbPath = SOSString.trimStart(smbPath, getPathSeparator());
        if (!SOSString.isEmpty(shareName)) {
            if (shareName.equalsIgnoreCase(smbPath)) {
                return "";
            }
            // finds the share name in the path and removes it
            int shareIndex = smbPath.indexOf(shareName + getPathSeparator());
            if (shareIndex != -1) {
                smbPath = smbPath.substring(shareIndex + shareName.length() + 1); // +1 for pathSeparator
            }
        }
        return smbPath;
    }

    private ProviderFile createProviderFile(String fullPath, FileAllInformation info) {
        return createProviderFile(fullPath, info.getStandardInformation().getEndOfFile(), info.getBasicInformation().getLastWriteTime()
                .toEpochMillis());
    }

    @SuppressWarnings("unused")
    /** @apiNote Not currently used because this method changes the original name.<br/>
     *          Can be enabled if file name sanitization is explicitly desired, e.g., controlled by a new property <br/>
     *          private boolean sanitizeSMBPathFilename=false|true<br/>
     *          and applying this method in:<br/>
     *          getSMBPath() .. return sanitizeSMBPathFilename(smbPath) */
    private static String sanitizeSMBPathFilename(String input) {
        Path path = Paths.get(input);
        String fileName = path.getFileName().toString();

        String illegalChars = "[<>:\"/\\\\|?*\\p{Cntrl}]";
        if (!fileName.matches(".*" + illegalChars + ".*")) {
            return input;
        }

        String sanitized = fileName.replaceAll(illegalChars, "_");
        sanitized = Normalizer.normalize(sanitized, Normalizer.Form.NFC);

        Path parent = path.getParent();
        String finalPath = (parent != null) ? parent.resolve(sanitized).toString() : sanitized;
        return SOSPathUtils.toWindowsStyle(finalPath);
    }

}
