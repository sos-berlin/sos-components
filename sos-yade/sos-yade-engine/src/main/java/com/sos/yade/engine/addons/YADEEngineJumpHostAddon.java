package com.sos.yade.engine.addons;

import java.util.UUID;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLJumpHostSettingsWriter;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

public class YADEEngineJumpHostAddon {

    public static final String IDENTIFIER = "Jump";
    private static final String LOG_PREFIX = "[Jump]";

    /** Source(SSHProvider) -> Jump(SSHProvider) -> Target(Any Provider) - old name - FROM INTERNET */
    private static final String PROFILE_JUMP_HOST_TO_TARGET = "jump_host_to_target";
    /** Source(Any Provider) -> Jump(SSHProvider) -> Target(SSHProvider) - - old name - TO INTERNET */
    private static final String PROFILE_JUMP_HOST_TO_SOURCE = "jump_host_to_source";

    private final ISOSLogger logger;
    private final AYADEArgumentsLoader argsLoader;

    private final String jumpHostDirectory;
    private final String jumpHostConfigDirectory;
    private final String jumpHostDataDirectory;
    private final String jumpHostSettingsXML;

    private String uuid;
    private String jumpHostProfileId;
    private String jumpHostSettingsXMLContent;

    private YADEEngineJumpHostAddon(ISOSLogger logger, AYADEArgumentsLoader argsLoader) {
        this.logger = logger;
        this.argsLoader = argsLoader;

        jumpHostDirectory = getJumpHostDirectory(argsLoader);
        jumpHostConfigDirectory = jumpHostDirectory + "/config";
        jumpHostDataDirectory = jumpHostDirectory + "/data";
        jumpHostSettingsXML = jumpHostConfigDirectory + "/settings.xml";
    }

    public static YADEEngineJumpHostAddon initialize(ISOSLogger logger, AYADEArgumentsLoader argsLoader) {
        if (argsLoader == null || argsLoader.getJumpArgs() == null) {
            return null;
        }
        return new YADEEngineJumpHostAddon(logger, argsLoader);
    }

    public void onBeforeDelegatorInitialized() {

        // - Source(SSHProvider) -> Jump(SSHProvider) -> Target(Any Provider)
        // -- Step 1) Target will be replaced by Jump (Source will "transfer" files to Jump)
        // -- Step 2) Configuration(Settings XML) will be generated for "transfers" files from Jump to Target
        // -- Step 3) Source creates/adds a Pre-Processing command to call the Jump YADE Client with the generated Configuration
        if (argsLoader.getJumpArgs().isConfiguredOnSource()) {
            jumpHostProfileId = PROFILE_JUMP_HOST_TO_SOURCE;
        }
        // - Source(Any Provider) -> Jump(SSHProvider) -> Target(SSHProvider)
        // -- Step 1) Target will be replaced by Jump (Source will "transfer" files to Jump)
        // -- Step 2) Configuration(Settings XML) will be generated for "transfers" files from Jump to Target
        // -- Step 3) Source creates/adds a Post-Processing command to call the Jump YADE Client with the generated Configuration
        else {
            // TODO check YADE1 behaviour - Jump command can contains this flag...
            boolean transactional = argsLoader.getArgs().getTransactional().isTrue();

            jumpHostProfileId = PROFILE_JUMP_HOST_TO_TARGET;
            jumpHostSettingsXMLContent = YADEXMLJumpHostSettingsWriter.fromJumpHostToTarget(argsLoader, jumpHostProfileId, jumpHostDataDirectory,
                    transactional);

            YADETargetArguments newTargetArgs = new YADETargetArguments();
            newTargetArgs.applyDefaultIfNullQuietly();

            // TODO maybe change operation to COPY if MOVE ....
            newTargetArgs.getDirectory().setValue(jumpHostDataDirectory);
            newTargetArgs.getCreateDirectories().setValue(true);
            newTargetArgs.setProvider(argsLoader.getJumpArgs().getProvider());
            newTargetArgs.setCommands(argsLoader.getJumpArgs().getCommands());
            newTargetArgs.getCommands().addCommandAfterOperationOnSuccess(getJumpYADEClientCommand());

            argsLoader.setTargetArgs(newTargetArgs);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("%s[onBeforeDelegatorInitialized][%s][internal profile_id]", LOG_PREFIX, jumpHostDataDirectory, jumpHostProfileId);
        }
    }

    public void onAfterSourceDelegatorConnected(YADESourceProviderDelegator sourceDelegator) {
        logger.info("%s[onAfterSourceDelegatorConnected]...", LOG_PREFIX);
    }

    public void onAfterTargetDelegatorConnected(YADETargetProviderDelegator targetDelegator) throws Exception {
        if (!argsLoader.getJumpArgs().isConfiguredOnSource()) {
            targetDelegator.getProvider().createDirectoriesIfNotExists(jumpHostConfigDirectory);
            targetDelegator.getProvider().writeFile(jumpHostSettingsXML, jumpHostSettingsXMLContent);
            if (logger.isDebugEnabled()) {
                logger.debug("%s[onAfterTargetDelegatorConnected][%s]written", LOG_PREFIX, jumpHostSettingsXML);
            }
        }
    }

    public String getNewLabelForSourceDelegator() {
        return argsLoader.getJumpArgs().isConfiguredOnSource() ? IDENTIFIER : null;
    }

    public String getNewLabelForTargetDelegator() {
        return argsLoader.getJumpArgs().isConfiguredOnSource() ? null : IDENTIFIER;
    }

    /** when polling - isSourceDisconnectingEnabled can be false - only jump data directory should be removed */
    public void onBeforeDelegatorDisconnected(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            boolean isSourceDisconnectingEnabled) {
        if (argsLoader.getJumpArgs().isConfiguredOnSource()) {

        } else {
            String dir = isSourceDisconnectingEnabled ? jumpHostDirectory : jumpHostDataDirectory;
            try {
                if (targetDelegator.getProvider().deleteIfExists(dir)) {
                    logger.info("%s[%s]deleted", LOG_PREFIX, dir);
                } else {
                    logger.info("%s[%s]not found", LOG_PREFIX, dir);
                }
            } catch (ProviderException e) {
                logger.warn("%s[%s][delete]%s", LOG_PREFIX, dir, e.toString());
            }
        }
    }

    private String getJumpYADEClientCommand() {
        return String.format("%s -settings=\"%s\" -profile=\"%s\"", argsLoader.getJumpArgs().getYADEClientCommand().getValue(), jumpHostSettingsXML,
                jumpHostProfileId);
    }

    private String getJumpHostDirectory(AYADEArgumentsLoader argsLoader) {
        return SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(argsLoader.getJumpArgs().getDirectory().getValue()) + "jade-dmz-" + getUUID();
    }

    private String getUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

}
