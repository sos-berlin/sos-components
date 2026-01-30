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
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.exceptions.ProviderAuthenticationException;
import com.sos.commons.vfs.exceptions.ProviderClientNotInitializedException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;
import com.sos.commons.vfs.smb.SMBProvider;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;

public class SMBJProvider extends SMBProvider {

    private SMBClient client = null;
    private Session session = null;

    private boolean accessMaskMaximumAllowed = false;
    private boolean reusableResourceEnabled;

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
            // creates a shared DiskShare connection by default
            enableReusableResource();

            getLogger().info(getConnectedMsg());
        } catch (Exception e) {
            // Do not call disconnect() here. it sets the client to null and may cause a ProviderClientNotInitializedException instead of a real connection
            // error in methods executed after connect() - e.g. if retry, roll back...
            // Call disconnect() in the application's finally block.
            // disconnect();
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

        disableReusableResource();

        closeSessionAndClient();
        session = null;
        client = null;

        getLogger().info(getDisconnectedMsg());
    }

    /** Overrides {@link IProvider#injectConnectivityFault()} */
    @Override
    public void injectConnectivityFault() {
        closeSessionAndClient();
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
            return (!SMBJProviderUtils.isDirectory(r) && getArguments().getValidFileTypes().getValue().contains(FileType.REGULAR));
        });

        String directory = selection.getConfig().getDirectory() == null ? "" : selection.getConfig().getDirectory();
        try (DiskShare share = connectShare(directory)) {
            List<ProviderFile> result = new ArrayList<>();
            SMBJProviderUtils.selectFiles(this, selection, getSMBPath(directory), result, share, 0);
            return result;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(directory), e);
        }
    }

    /** Overrides {@link IProvider#exists(String)} */
    @Override
    public boolean exists(String path) throws ProviderException {
        validatePrerequisites("exists", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            if (reusable == null) {
                try (DiskShare share = connectShare(path)) {
                    // share.getFileInformation(getSMBPath(path));
                    return share.fileExists(getSMBPath(path)) || share.folderExists(getSMBPath(path));
                }
            } else {
                DiskShare share = reusable.getDiskShare(path);
                return share.fileExists(getSMBPath(path)) || share.folderExists(getSMBPath(path));
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#createDirectoriesIfNotExists(String)} */
    @Override
    public boolean createDirectoriesIfNotExists(String path) throws ProviderException {
        validatePrerequisites("createDirectoriesIfNotExists", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            String smbPath = getSMBPath(path);
            if (reusable == null) {
                try (DiskShare share = connectShare(path)) {
                    new SmbFiles().mkdirs(share, smbPath);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
                    }
                    return true;
                }
            } else {
                new SmbFiles().mkdirs(reusable.getDiskShare(path), smbPath);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("%s[createDirectoriesIfNotExists][%s]created", getLogPrefix(), path);
                }
                return true;
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#deleteIfExists(String)} */
    @Override
    public boolean deleteIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteIfExists", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            String smbPath = getSMBPath(path);
            if (reusable == null) {
                try (DiskShare share = connectShare(path)) {
                    return SMBJProviderUtils.deleteIfExists(share, smbPath);
                }
            } else {
                return SMBJProviderUtils.deleteIfExists(reusable.getDiskShare(path), smbPath);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause() == null ? e : e.getCause());
        }
    }

    /** Overrides {@link IProvider#deleteFileIfExists(String)} */
    @Override
    public boolean deleteFileIfExists(String path) throws ProviderException {
        validatePrerequisites("deleteFileIfExists", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            String smbPath = getSMBPath(path);
            if (reusable == null) {
                try (DiskShare share = connectShare(path)) {
                    return SMBJProviderUtils.deleteFileIfExists(share, smbPath);
                }
            } else {
                return SMBJProviderUtils.deleteFileIfExists(reusable.getDiskShare(path), smbPath);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e.getCause() == null ? e : e.getCause());
        }
    }

    /** Overrides {@link IProvider#moveFileIfExists(String, String)} */
    @Override
    public boolean moveFileIfExists(String source, String target) throws ProviderException {
        validatePrerequisites("moveFileIfExists", source, "source");
        validateArgument("moveFileIfExists", target, "target");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            String smbSourcePath = getSMBPath(source);
            String smbTargetPath = getSMBPath(target);
            if (reusable == null) {
                try (DiskShare share = connectShare(source)) {
                    return SMBJProviderUtils.renameFileIfSourceExists(share, smbSourcePath, smbTargetPath, accessMaskMaximumAllowed);
                }
            } else {
                return SMBJProviderUtils.renameFileIfSourceExists(reusable.getDiskShare(source), smbSourcePath, smbTargetPath,
                        accessMaskMaximumAllowed);
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(source + "->" + target), e);
        }
    }

    /** Overrides {@link IProvider#getFileIfExists(String)} */
    @Override
    public ProviderFile getFileIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileIfExists", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            String smbPath = getSMBPath(path);
            if (reusable == null) {
                try (DiskShare share = connectShare(path)) {
                    try {
                        return createProviderFile(smbPath, share.getFileInformation(smbPath));
                    } catch (SMBApiException e) {// not exists
                        return null;
                    }
                }
            } else {
                try {
                    return createProviderFile(smbPath, reusable.getDiskShare(path).getFileInformation(smbPath));
                } catch (SMBApiException e) {// not exists
                    return null;
                }
            }
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getFileContentIfExists(String)} */
    @Override
    public String getFileContentIfExists(String path) throws ProviderException {
        validatePrerequisites("getFileContentIfExists", path, "path");

        SMBJProviderReusableResource reusable = getReusableResource();
        DiskShare share = reusable == null ? connectShare(path) : reusable.getDiskShare(path);

        StringBuilder content = new StringBuilder();
        try (InputStream is = new SMBJInputStream(accessMaskMaximumAllowed, share, reusable == null, getSMBPath(path), 0L); Reader r =
                new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(r)) {
            br.lines().forEach(content::append);
            return content.toString();
        } catch (SOSNoSuchFileException e) {
            return null;
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#writeFile(String,String)} */
    @Override
    public void writeFile(String path, String content) throws ProviderException {
        validatePrerequisites("writeFile", path, "path");

        SMBJProviderReusableResource reusable = getReusableResource();
        DiskShare share = reusable == null ? connectShare(path) : reusable.getDiskShare(path);

        try (OutputStream os = new SMBJOutputStream(accessMaskMaximumAllowed, share, reusable == null, getSMBPath(path), false)) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#setFileLastModifiedFromMillis(String,long)} */
    @Override
    public void setFileLastModifiedFromMillis(String path, long milliseconds) throws ProviderException {
        validatePrerequisites("setFileLastModifiedFromMillis", path, path);
        validateModificationTime(path, milliseconds);

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            String smbPath = getSMBPath(path);
            if (reusable == null) {
                try (DiskShare share = connectShare(path)) {
                    SMBJProviderUtils.setFileLastModifiedFromMillis(share, smbPath, milliseconds, accessMaskMaximumAllowed);
                }
            } else {
                SMBJProviderUtils.setFileLastModifiedFromMillis(reusable.getDiskShare(path), smbPath, milliseconds, accessMaskMaximumAllowed);
            }
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
        validatePrerequisites("getInputStream", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            DiskShare share = reusable == null ? connectShare(path) : reusable.getDiskShare(path);

            return new SMBJInputStream(accessMaskMaximumAllowed, share, reusable == null, getSMBPath(path), offset);
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link IProvider#getOutputStream(String,boolean)} */
    @Override
    public OutputStream getOutputStream(String path, boolean append) throws ProviderException {
        validatePrerequisites("getOutputStream", path, "path");

        try {
            SMBJProviderReusableResource reusable = getReusableResource();
            DiskShare share = reusable == null ? connectShare(path) : reusable.getDiskShare(path);

            return new SMBJOutputStream(accessMaskMaximumAllowed, share, reusable == null, getSMBPath(path), append);
        } catch (Exception e) {
            throw new ProviderException(getPathOperationPrefix(path), e);
        }
    }

    /** Overrides {@link AProvider#validatePrerequisites(String)} */
    @Override
    public void validatePrerequisites(String method) throws ProviderException {
        if (client == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix(), SMBClient.class, method);
        }
        if (session == null) {
            throw new ProviderClientNotInitializedException(getLogPrefix(), Session.class, method);
        }
    }

    /** Overrides {@link AProvider#enableReusableResource()} */
    @Override
    public void enableReusableResource() {
        if (reusableResourceEnabled) {
            return;
        }
        try {
            super.enableReusableResource(new SMBJProviderReusableResource(this));
            reusableResourceEnabled = true;
        } catch (Exception e) {
            getLogger().warn(getLogPrefix() + "[enableReusableResource]" + e);
        }
    }

    /** Overrides {@link AProvider#getReusableResource()} */
    @Override
    public SMBJProviderReusableResource getReusableResource() {
        if (!reusableResourceEnabled) {
            return null;
        }
        return (SMBJProviderReusableResource) super.getReusableResource();
    }

    @Override
    public void disableReusableResource() {
        super.disableReusableResource();
        reusableResourceEnabled = false;
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
    protected DiskShare connectShare(String path) {
        return (DiskShare) session.connectShare(getShareName(path));
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

    private void closeSessionAndClient() {
        if (session != null) { // check due to session.getConnection()
            SOSClassUtil.closeQuietly(session); // session.close -> logout
            SOSClassUtil.closeQuietly(session.getConnection());
        }
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

    private void validatePrerequisites(String method, String paramValue, String msg) throws ProviderException {
        validatePrerequisites(method);
        validateArgument(method, paramValue, msg);
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
