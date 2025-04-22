package com.sos.yade.engine.addons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sos.commons.util.SOSCLIArgumentsParser;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon.JumpHostConfig.ConfigFile;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLJumpHostSettingsWriter;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.exceptions.YADEEngineJumpHostException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;

/** SOURCE_TO_JUMP_HOST_...: Source(SSHProvider) -> Jump(SSHProvider) -> Target(Any Provider) - old name - FROM INTERNET<br/>
 * - 1) Current YADE Client:<br/>
 * - 1.1) Changes the configuration on the fly:<br/>
 * - 1.1.1) Source - Jump Host<br/>
 * - 1.1.2) Target - the same as before - Any Provider<br/>
 * - 1.1.3) Note: so, the Current YADE Client will transfer files between the Jump Host and Target(Any Provider)<br/>
 * - 1.2) Connects to the Source(Jump Host)<br/>
 * - 1.3) Uploads generated settings.xml to the Jump Host<br/>
 * - 1.3.1) Note: the settings.xml contains configuration for transfer between "old" Source(SSHProvider) -> Jump Host(Local)<br/>
 * - 1.4) Optional uploads a FileList file to the Jump Host for the files selection on the "old" Source(SSHProvider) based on this file<br/>
 * - 2) Jump Host YADE Client (the JumpHost connection is already established - see 1.2))<br/>
 * - 2.1) CommandBeforeOperation - uses 1.3) to transfer files from Source(SSHProvider) -> Jump Host(Local)<br/>
 * - 2.2) Optional - writes a ResultSetFile of the file selection on the "old" Source(SSHProvider) in a temporary config dir on the Jump Host<br/>
 * - 2.3) Jump Host YADE Client - completed - "old" Source(SSHProvider) is disconnected<br/>
 * - 3) Current YADE Client:<br/>
 * - 3.1)<br/>
 * - 3.1.1) if 2.3) completed successfully<br/>
 * - 3.1.1.1) transfers files from Source(Jump Host) -> Target(Any Provider)<br/>
 * - 3.1.1.1.1) transfer error occurs and transactional is defined:<br/>
 * - 3.1.1.1.1.1) Source(Jump Host) - nothing should be extra rolled back, due to entire Jump Host temporary directory will be deleted anyway<br/>
 * - 3.1.1.1.1.2) Target(Any Provider) - the already transferred files should be rolled back<br/>
 * 
 * - Extra case MOVE operation and transactional (Remove files on the "old" Source(SSHProvider))<br/>
 * - 1) if all previous steps completed successfully<br/>
 * - 1.1) Note: the "old" Source(SSHProvider) is already disconnected<br/>
 * - 1.2) Note: the current Source(Jump Host) stays connected<br/>
 * - 1.3) Jump Host YADE Client is called again with a new generated settings XML<br/>
 * - 1.3.1) REMOVE Operation based on a ResultSetFile created with 2.2)<br/>
 * - 1.3.2) Note: if the ResultSetFile argument was not declared<br/>
 * - 1.3.2.1) 2.2) will creates in MOVE case a temporary ResultSetFile but not transfers it later to 1)<br/>
 * - 1.3.3) Note: if the ResultSetFile argument was declared<br/>
 * - 1.3.3.1) 2.2) this ResultSetFile is used and will be transferred to 1)<br/>
 * 
 * JUMP_HOST_TO_TARGET...: Source(Any Provider) -> Jump(SSHProvider) -> Target(SSHProvider) - old name - TO INTERNET<br/>
 * - Note: Jump(DMZ) operations: COPY/MOVE.<br/>
 * -- REMOVE/GETLIST not needs Jump functionality because executed on the Source(Any Provider)
 * --------------------------------------------------------------------------------------------------------------<br/>
 * JumpFragment/JumpCommand: <br/>
 * - if the JumpCommand includes --transactional and/or --paralelism options,<br/>
 * -- these options are used to perform operation between the Jump Host and Source|Target.<br/>
 * - Otherwise, the the current Settings (TransferOptions.Transactional or paralelism) are used */
public class YADEEngineJumpHostAddon {

    private static final String SOURCE_TO_JUMP_HOST_COPY_PROFILE_ID = "SOURCE_TO_JUMP_HOST_COPY";
    private static final String SOURCE_TO_JUMP_HOST_GETLIST_PROFILE_ID = "SOURCE_TO_JUMP_HOST_GETLIST";
    private static final String SOURCE_TO_JUMP_HOST_REMOVE_PROFILE_ID = "SOURCE_TO_JUMP_HOST_REMOVE";
    private static final String SOURCE_TO_JUMP_HOST_MOVE_LABEL_REMOVE_SOURCE = "REMOVE_SOURCE";

    // -- Step 1) Target will be replaced by Jump (Source will "transfer" files to Jump)
    // -- Step 2) Configuration(Settings XML) will be generated for "transfers" files from Jump to Target
    // -- Step 3) Source creates/adds a Post-Processing command to call the Jump YADE Client with the generated Configuration
    private static final String JUMP_HOST_TO_TARGET_COPY_PROFILE_ID = "JUMP_HOST_TO_TARGET_COPY";

    /*** Name of the main configuration file uploaded to the Jump Host temporary config directory */
    private static final String SETTINGS_XML = "settings.xml";

    private final ISOSLogger logger;
    private final AYADEArgumentsLoader argsLoader;
    private final JumpHostConfig config;

    // PROFILE_JUMP_HOST_TO_TARGET: The selection of Source files went smoothly and some files were selected. The Target Provider is already connected
    private boolean isReady;

    // YADE1:
    // - 1) Source|Target -> Jump
    // -- always transactional, ignores TransferOptions.Transactional
    // - 2) From Jump -> Source|Target
    // -- can be controlled by the Jump YADE Client command line option: -transactional=true|false
    // JS7:
    // - 1) Source|Target -> Jump
    // -- transferToJumpHostAlwaysTransactional=true - as with YADE1
    // -- transferToJumpHostAlwaysTransactional=false - use TransferOptions.Transactional
    // - 2) From Jump -> Source|Target
    // -- same as with YADE1 (see YADE1 2) above)
    private boolean transferToJumpHostAlwaysTransactional = false;

    public static YADEEngineJumpHostAddon initialize(ISOSLogger logger, AYADEArgumentsLoader argsLoader) throws YADEEngineInitializationException {
        if (argsLoader == null || argsLoader.getJumpHostArgs() == null) {
            return null;
        }
        return new YADEEngineJumpHostAddon(logger, argsLoader);
    }

    private YADEEngineJumpHostAddon(ISOSLogger logger, AYADEArgumentsLoader argsLoader) throws YADEEngineInitializationException {
        this.logger = logger;
        this.argsLoader = argsLoader;
        this.config = new JumpHostConfig();
        init();
    }

    private void init() throws YADEEngineInitializationException {
        if (argsLoader.getJumpHostArgs().isConfiguredOnSource()) {
            String profileId;
            switch (argsLoader.getArgs().getOperation().getValue()) {
            case GETLIST:
                profileId = SOURCE_TO_JUMP_HOST_GETLIST_PROFILE_ID;
                break;
            case REMOVE:
                profileId = SOURCE_TO_JUMP_HOST_REMOVE_PROFILE_ID;
                break;
            default:
                profileId = SOURCE_TO_JUMP_HOST_COPY_PROFILE_ID;
                break;
            }
            config.setSourceToJumpHost(profileId, argsLoader.getArgs().getOperation().getValue());

            YADESourceArguments newSourceArgs = new YADESourceArguments();
            newSourceArgs.applyDefaultIfNullQuietly();
            newSourceArgs.getLabel().setValue(YADEJumpHostArguments.LABEL);

            newSourceArgs.getDirectory().setValue(config.dataDirectory);
            newSourceArgs.getRecursive().setValue(Boolean.valueOf(true));
            newSourceArgs.setProvider(argsLoader.getJumpHostArgs().getProvider());
            newSourceArgs.setCommands(argsLoader.getJumpHostArgs().getCommands());
            newSourceArgs.getCommands().addCommandBeforeOperation(config.getYADEClientCommand(config.settingsXML, config.profileId));
            // Source (Target Host)
            // Target (Any Provider) - the same as before
            argsLoader.setSourceArgs(newSourceArgs);
        } else {
            config.setJumpHostToTarget(JUMP_HOST_TO_TARGET_COPY_PROFILE_ID);

            YADETargetArguments newTargetArgs = new YADETargetArguments();
            newTargetArgs.applyDefaultIfNullQuietly();
            newTargetArgs.getLabel().setValue(YADEJumpHostArguments.LABEL);

            newTargetArgs.getDirectory().setValue(config.dataDirectory);
            newTargetArgs.getCreateDirectories().setValue(Boolean.valueOf(true));
            newTargetArgs.setProvider(argsLoader.getJumpHostArgs().getProvider());
            newTargetArgs.setCommands(argsLoader.getJumpHostArgs().getCommands());
            newTargetArgs.getCommands().addCommandAfterOperationOnSuccess(config.getYADEClientCommand(config.settingsXML, config.profileId));

            // Source (Any Provider) - the same as before
            // Target (Jump Host)
            argsLoader.setTargetArgs(newTargetArgs);
        }
        // logger.info(jumpHostSettingsXMLContent);

        if (logger.isDebugEnabled()) {
            logger.debug("[%s][init][%s][profileId]%s", YADEJumpHostArguments.LABEL, config.dataDirectory, config.profileId);
        }
    }

    public void onAfterSourceDelegatorConnected(YADESourceProviderDelegator sourceDelegator) throws YADEEngineJumpHostException {
        if (argsLoader.getJumpHostArgs().isConfiguredOnSource()) {
            upload(sourceDelegator); // upload settings.xml

            if (config.sourceToJumpHost.fileList != null) {
                try {
                    upload(sourceDelegator, config.sourceToJumpHost.fileList);
                } catch (Exception e) {
                    throw new YADEEngineJumpHostException(String.format("[%s][upload][%s=%s]%s", YADEClientArguments.LABEL,
                            config.sourceToJumpHost.fileList.configurationName, config.sourceToJumpHost.fileList.localFile, e.toString()), e);
                }
            }
            isReady = true;
        }
    }

    public void onAfterTargetDelegatorConnected(YADETargetProviderDelegator targetDelegator) throws YADEEngineJumpHostException {
        if (!argsLoader.getJumpHostArgs().isConfiguredOnSource()) {
            upload(targetDelegator);// upload settings.xml
        }
        isReady = true;
    }

    /** @param isTransferSucceeded - the previous Jump Host execution was successful
     * @param isSourceDisconnectingEnabled - when polling - an be false - only jump data directory should be removed */
    public void onBeforeDelegatorDisconnected(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            List<ProviderFile> files, boolean isTransferSucceeded, boolean isSourceDisconnectingEnabled) throws YADEEngineJumpHostException {
        if (!isReady) {
            return;
        }

        AYADEProviderDelegator jumpHostDelegator = null;
        try {
            if (argsLoader.getJumpHostArgs().isConfiguredOnSource()) {
                jumpHostDelegator = sourceDelegator;

                if (isTransferSucceeded) {
                    if (config.sourceToJumpHost.deleteSourceFiles) {
                        String settingsXML = config.configDirectory + "/" + SOURCE_TO_JUMP_HOST_MOVE_LABEL_REMOVE_SOURCE.toLowerCase()
                                + "_settings.xml";
                        String profileId = SOURCE_TO_JUMP_HOST_MOVE_LABEL_REMOVE_SOURCE;
                        try {
                            String settingsXMLContent = YADEXMLJumpHostSettingsWriter.sourceToJumpHostMOVERemove(argsLoader, config, profileId);
                            upload(sourceDelegator, settingsXMLContent, settingsXML);
                            logger.info("[%s][upload][Settings][%s=%s]uploaded", YADEClientArguments.LABEL, sourceDelegator.getLabel(), settingsXML);
                        } catch (Exception e) {
                            throw new YADEEngineJumpHostException(String.format("[%s][upload][Settings][%s=%s]%s", YADEClientArguments.LABEL,
                                    sourceDelegator.getLabel(), settingsXML, e), e);
                        }
                        YADECommandExecutor.executeJumpHostCommand(logger, sourceDelegator, config.getYADEClientCommand(settingsXML, profileId));
                    }
                    if (config.sourceToJumpHost.resultSetFile != null) {
                        try {
                            download(sourceDelegator, config.sourceToJumpHost.resultSetFile);
                        } catch (Exception e) {
                            throw new YADEEngineJumpHostException(String.format("[%s][download][%s=%s]%s", YADEClientArguments.LABEL,
                                    config.sourceToJumpHost.resultSetFile.configurationName, config.sourceToJumpHost.resultSetFile.localFile, e
                                            .toString()), e);
                        }
                    }
                }
            } else {
                jumpHostDelegator = targetDelegator;
                if (isTransferSucceeded) {
                    if (config.jumpHostToTarget.deleteSourceFiles) {
                        jumpHostToTargetDeleteSourceFiles(sourceDelegator, files);
                    }
                }
            }
        } catch (YADEEngineJumpHostException e) {
            throw e;
        } catch (Throwable e) {
            throw new YADEEngineJumpHostException(e.toString(), e);
        } finally {
            deleteJumpDirectory(jumpHostDelegator, isSourceDisconnectingEnabled);
        }
    }

    public boolean isConfiguredOnSource() {
        return argsLoader.getJumpHostArgs().isConfiguredOnSource();
    }

    private void deleteJumpDirectory(AYADEProviderDelegator jumpHostDelegator, boolean isSourceDisconnectingEnabled) {
        if (jumpHostDelegator == null) {
            return;
        }
        String dir = isSourceDisconnectingEnabled ? config.directory : config.dataDirectory;
        try {
            SSHProvider jumpHostSSH = (SSHProvider) jumpHostDelegator.getProvider();
            boolean deleted;
            if (argsLoader.getJumpHostArgs().isPlatformEnabled()) {
                if (argsLoader.getJumpHostArgs().isWindowsPlatform()) {
                    deleted = jumpHostSSH.deleteWindowsDirectory(dir);
                } else {
                    deleted = jumpHostSSH.deleteUnixDirectory(dir);
                }
            } else {
                deleted = jumpHostSSH.deleteDirectory(dir);
            }

            if (deleted) {
                logger.info("[%s][%s]deleted", YADEJumpHostArguments.LABEL, dir);
            } else {
                logger.info("[%s][%s]not found", YADEJumpHostArguments.LABEL, dir);
            }
        } catch (ProviderException e) {
            logger.warn("[%s][%s][delete]%s", YADEJumpHostArguments.LABEL, dir, e.toString(), e);
        }
    }

    /** deletes files on the Source because the Source is accessible */
    private void jumpHostToTargetDeleteSourceFiles(AYADEProviderDelegator delegator, List<ProviderFile> files) throws YADEEngineJumpHostException {
        delegator.getProvider().enableReusableResource();
        for (ProviderFile f : files) {
            try {
                if (delegator.getProvider().deleteIfExists(f.getFullPath())) {
                    logger.info("[%s][deleteSourceFiles][%s]deleted", delegator.getLabel(), f.getFullPath());
                }
            } catch (Exception e) {
                throw new YADEEngineJumpHostException(String.format("[%s][deleteSourceFiles]%s", delegator.getLabel(), e.toString()), e);
            }
        }
    }

    private void upload(AYADEProviderDelegator delegator) throws YADEEngineJumpHostException {
        String label = YADEClientArguments.LABEL;
        try {
            delegator.getProvider().createDirectoriesIfNotExists(config.configDirectory);
            upload(delegator, config.settingsXMLContent, config.settingsXML);
            logger.info("[%s][upload][Settings][%s=%s]uploaded", label, delegator.getLabel(), config.settingsXML);
        } catch (Exception e) {
            throw new YADEEngineJumpHostException(String.format("[%s][upload][Settings][%s=%s]%s", label, delegator.getLabel(), config.settingsXML,
                    e), e);
        }
    }

    private void upload(AYADEProviderDelegator delegator, String content, String targetFileOnJumpHost) throws Exception {
        delegator.getProvider().writeFile(targetFileOnJumpHost, content);
    }

    private void upload(AYADEProviderDelegator delegator, ConfigFile configFile) throws Exception {
        if (configFile.localFile == null) {
            return;
        }
        delegator.getProvider().writeFile(configFile.jumpHostFile, SOSPath.readFile(configFile.localFile));
        logger.info("[%s][upload][%s][%s -> %s=%s]uploaded", YADEClientArguments.LABEL, configFile.configurationName, configFile.localFile, delegator
                .getLabel(), configFile.jumpHostFile);
    }

    private void download(AYADEProviderDelegator delegator, ConfigFile configFile) throws Exception {
        if (configFile.localFile == null) {
            return;
        }
        String label = YADEClientArguments.LABEL;
        // delegator.getLabel();// <- Jump

        String content = delegator.getProvider().getFileContentIfExists(configFile.jumpHostFile);
        if (content == null) {
            logger.info("[%s][download][%s][%s=%s][skip]not found", label, configFile.configurationName, delegator.getLabel(),
                    configFile.jumpHostFile);
        } else {
            SOSPath.overwrite(configFile.localFile, content);
            logger.info("[%s][download][%s][%s=%s -> %s]downloaded", label, configFile.configurationName, delegator.getLabel(),
                    configFile.jumpHostFile, configFile.localFile);
        }
    }

    public class JumpHostConfig {

        private final String directory;
        private final String configDirectory;
        private final String dataDirectory;
        private final String settingsXML;

        private boolean transactional;
        private String atomicPrefix;
        private String atomicSuffix;

        private String profileId;
        private String settingsXMLContent;

        private SourceToJumpHost sourceToJumpHost;
        private JumpHostToTarget jumpHostToTarget;

        private JumpHostConfig() {
            directory = getJumpHostDirectory();
            configDirectory = directory + "/config";
            dataDirectory = directory + "/data";
            settingsXML = configDirectory + "/" + SETTINGS_XML;

            if (argsLoader.getTargetArgs() != null) {// COPY/MOVE
                if (transferToJumpHostAlwaysTransactional) {
                    argsLoader.getArgs().getTransactional().setValue(true);
                }
                transactional = getJumpHostTransactional();
                atomicPrefix = transactional ? argsLoader.getTargetArgs().getAtomicPrefix().getValue() : null;
                atomicSuffix = transactional ? argsLoader.getTargetArgs().getAtomicSuffix().getValue() : null;
            }
        }

        public String getDataDirectory() {
            return dataDirectory;
        }

        public boolean isTransactional() {
            return transactional;
        }

        public boolean isAtomicEnabled() {
            return atomicPrefix != null || atomicSuffix != null;
        }

        public String getAtomicPrefix() {
            return atomicPrefix;
        }

        public String getAtomicSuffix() {
            return atomicSuffix;
        }

        public String getProfileId() {
            return profileId;
        }

        public SourceToJumpHost getSourceToJumpHost() {
            return sourceToJumpHost;
        }

        private void setSourceToJumpHost(String profileId, TransferOperation operation) throws YADEEngineInitializationException {
            this.sourceToJumpHost = new SourceToJumpHost();
            this.profileId = profileId;

            // TODO move FileList to the ClientArguments? since the file should be present on the client system...
            if (argsLoader.getSourceArgs().isFileListEnabled()) {
                if (!Files.exists(argsLoader.getSourceArgs().getFileList().getValue())) {
                    throw new YADEEngineInitializationException(String.format("[%s][%s]not found", argsLoader.getSourceArgs().getFileList().getName(),
                            argsLoader.getSourceArgs().getFileList().getValue()));
                }
                this.sourceToJumpHost.setFileList(argsLoader.getSourceArgs().getFileList());
                argsLoader.getSourceArgs().getFileList().setValue(null);
            }
            if (argsLoader.getClientArgs().getResultSetFile().isDirty()) {
                this.sourceToJumpHost.setResultSetFile(argsLoader.getClientArgs().getResultSetFile());
                argsLoader.getClientArgs().getResultSetFile().setValue(null);
            }

            switch (operation) {
            case GETLIST:
                this.settingsXMLContent = YADEXMLJumpHostSettingsWriter.sourceToJumpHostGETLIST(argsLoader, this);
                break;
            case REMOVE:
                this.settingsXMLContent = YADEXMLJumpHostSettingsWriter.sourceToJumpHostREMOVE(argsLoader, this);
                break;
            case MOVE:
                argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
                this.sourceToJumpHost.deleteSourceFiles = true;
                if (this.sourceToJumpHost.resultSetFile == null) {
                    this.sourceToJumpHost.setResultSetFile(argsLoader.getClientArgs().getResultSetFile(), SOURCE_TO_JUMP_HOST_MOVE_LABEL_REMOVE_SOURCE
                            .toLowerCase() + "_" + new Date().getTime() + ".sos.rs");
                }
                this.settingsXMLContent = YADEXMLJumpHostSettingsWriter.sourceToJumpHostCOPY(argsLoader, this);
                break;
            case COPY:
            default:
                this.settingsXMLContent = YADEXMLJumpHostSettingsWriter.sourceToJumpHostCOPY(argsLoader, this);
                break;
            }
        }

        private void setJumpHostToTarget(String profileId) {
            this.jumpHostToTarget = new JumpHostToTarget();
            // delete source files "manually" (transaction-independent) only if the transfer from Jump to the Target was successful
            if (TransferOperation.MOVE.equals(argsLoader.getArgs().getOperation().getValue())) {
                argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
                this.jumpHostToTarget.deleteSourceFiles = true;
            }

            this.profileId = profileId;
            this.settingsXMLContent = YADEXMLJumpHostSettingsWriter.jumpHostToTargetCOPY(argsLoader, this);

        }

        private String getJumpHostDirectory() {
            String jumpDirectory = SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(argsLoader.getJumpHostArgs().getDirectory().getValue());
            if (SOSPathUtils.isAbsoluteWindowsOpenSSHPath(jumpDirectory)) {
                jumpDirectory = jumpDirectory.substring(1);
            }
            return jumpDirectory + "jade-dmz-" + UUID.randomUUID().toString();
        }

        private boolean getJumpHostTransactional() {
            Map<String, String> jumpHostClientArgs = SOSCLIArgumentsParser.parse(argsLoader.getJumpHostArgs().getYADEClientCommand().getValue()
                    .toLowerCase());
            String transactional = jumpHostClientArgs.get(argsLoader.getArgs().getTransactional().getName());
            if (transactional == null) {
                return argsLoader.getArgs().getTransactional().getValue();
            } else {
                if ("false".equals(transactional)) {
                    return false;

                } else {
                    return true;
                }
            }
        }

        private String getYADEClientCommand(String settingsXML, String profileId) {
            String parallelism = "";
            if (argsLoader.getArgs().isParallelismEnabled()) {
                SOSArgument<Integer> arg = argsLoader.getArgs().getParallelism();
                // Jump Host command not contains 'parallelism='
                if (!argsLoader.getJumpHostArgs().getYADEClientCommand().getValue().toLowerCase().contains(arg.getName() + "=")) {
                    parallelism = " --" + arg.getName() + "=" + arg.getValue();
                }
            }

            return String.format("%s -settings=\"%s\" -profile=\"%s\"%s", argsLoader.getJumpHostArgs().getYADEClientCommand().getValue(), settingsXML,
                    profileId, parallelism);
        }

        public class SourceToJumpHost {

            private ConfigFile fileList;
            private ConfigFile resultSetFile;
            // Source(SSHProvider) -> Jump(SSHProvider) -> Target(Any Provider)
            // The MOVE operation is internally converted to a COPY operation if "To Jump" is transactional.
            // Otherwise, the current YADE Client deletes the Source files
            // - after the Source(SSHProvider) -> Jump(SSHProvider) step due to MOVE
            // - and not after the Jump(SSHProvider) -> Target(Any Provider) step is completed
            // - JumpHost MOVE implementation: Source(SSHProvider) - COPY -> Jump(SSHProvider) -> Target(Any Provider) -> Source(SSHProvider) delete files
            private boolean deleteSourceFiles;

            public ConfigFile getFileList() {
                return fileList;
            }

            public ConfigFile getResultSetFile() {
                return resultSetFile;
            }

            private void setFileList(SOSArgument<Path> arg) {
                fileList = new ConfigFile(arg.getName(), arg.getValue());
            }

            private void setResultSetFile(SOSArgument<Path> arg) {
                resultSetFile = new ConfigFile(arg.getName(), arg.getValue());
            }

            private void setResultSetFile(SOSArgument<Path> arg, String jumpHostFileName) {
                resultSetFile = new ConfigFile(arg.getName(), jumpHostFileName);
            }
        }

        public class JumpHostToTarget {

            // Source(Any Provider) -> Jump(SSHProvider) -> Target(SSHProvider)
            // The MOVE operation is internally converted to a COPY operation if "To Jump" is transactional.
            // Otherwise, the current YADE Client deletes the Source files
            // - after the Source(Any Provider) -> Jump(SSHProvider) step due to MOVE
            // - and not after the Jump(SSHProvider) -> Target(SSHProvider) step is completed
            // - JumpHost MOVE implementation: Source(Any Provider) - COPY -> Jump(SSHProvider) -> Target(SSHProvider) -> Source(Any Provider) delete files
            private boolean deleteSourceFiles;
        }

        public class ConfigFile {

            private final String configurationName;
            private final Path localFile;
            private final String jumpHostFile;

            private ConfigFile(String configurationName, Path localFile) {
                this.configurationName = configurationName;
                this.localFile = localFile;
                this.jumpHostFile = configDirectory + "/" + localFile.getFileName();
            }

            private ConfigFile(String configurationName, String jumpHostFileName) {
                this.configurationName = configurationName;
                this.localFile = null;
                this.jumpHostFile = configDirectory + "/" + jumpHostFileName;
            }

            public String getJumpHostFile() {
                return jumpHostFile;
            }

        }

    }

}
