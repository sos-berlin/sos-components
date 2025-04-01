package com.sos.yade.engine.addons.jump;

import java.util.UUID;

import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.addons.IYADEEngineExecutionAddon;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

public class YADEEngineJumpHostAddon implements IYADEEngineExecutionAddon {

    public static final String LOG_PREFIX = "[Jump]";

    private static final String PROFILE_ID_JUMP_HOST_TO_INTERNET = "jump_host_to_internet";

    private final ISOSLogger logger;
    private final AYADEArgumentsLoader argsLoader;

    private final String jumpConfigDirectory;
    private final String jumpDataDirectory;

    private String uuid;
    private String profileId;

    private YADEEngineJumpHostAddon(ISOSLogger logger, AYADEArgumentsLoader argsLoader) {
        this.logger = logger;
        this.argsLoader = argsLoader;

        String jumpBaseDirectory = getJumpDirectory(argsLoader);
        jumpConfigDirectory = jumpBaseDirectory + "/config";
        jumpDataDirectory = jumpBaseDirectory + "/data";
    }

    public static YADEEngineJumpHostAddon initialize(ISOSLogger logger, AYADEArgumentsLoader argsLoader) {
        if (argsLoader == null || argsLoader.getJumpArguments() == null) {
            return null;
        }
        return new YADEEngineJumpHostAddon(logger, argsLoader);
    }

    @Override
    public void onBeforeDelegatorInitialized(ISOSLogger logger, AYADEArgumentsLoader argsLoader) {

    }

    @Override
    public void onAfterSourceDelegatorConnected(YADESourceProviderDelegator sourceDelegator) {

    }

    @Override
    public void onAfterTargetDelegatorConnected(YADETargetProviderDelegator targetDelegator) {

    }

    /** when polling - isSourceDisconnectingEnabled can be false - only jump data directory should be removed */
    @Override
    public void onBeforeDelegatorDisconnected(YADESourceProviderDelegator sourceDelegator, boolean isSourceDisconnectingEnabled,
            YADETargetProviderDelegator targetDelegator) {

    }

    private void modifyArguments(AYADEArgumentsLoader argsLoader, String jumpDirectory) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();
        YADETargetArguments targetArgs = argsLoader.getTargetArgs();

        // Source -> DMZ(jumpDirectory) -> Internet
        if (argsLoader.getJumpArguments().getIsSource().isTrue()) {
            // Source - defined Source
            // Target - DMZ (Jump Host instead of defined Target)
            // PostTransfer commands -

            StringBuilder jumpCmd = new StringBuilder();
            jumpCmd.append(argsLoader.getJumpArguments().getYADEClientCommand().getValue());
            jumpCmd.append(" ").append("-operation=").append(argsLoader.getArgs().getOperation().getValue());
            jumpCmd.append(" ").append("-source_dir=\"").append(argsLoader.getSourceArgs().getDirectory().getValue()).append("\"");
            jumpCmd.append(" ").append("-target_dir=\"").append(jumpDirectory).append("\"");
            if (!argsLoader.getJumpArguments().getPlatform().isEmpty()) {
                jumpCmd.append(" ").append("-platform=\"").append(argsLoader.getJumpArguments().getPlatform().getValue()).append("\"");
            }
            if (argsLoader.getArgs().getTransactional().isTrue()) {
                jumpCmd.append(" ").append("-transactional=true");
            }
        }
    }

    private void createSourceToDMZ(AYADEArgumentsLoader argsLoader, String jumpDirectory) {
        YADESourceArguments sourceArgsX = argsLoader.getSourceArgs();

        YADEArguments args = argsLoader.getArgs().clone();
        args.getProfile().setValue(null);
        args.getSettings().setValue(null);

    }

    private String getJumpDirectory(AYADEArgumentsLoader argsLoader) {
        return SOSPathUtils.getUnixStyleDirectoryWithTrailingSeparator(argsLoader.getJumpArguments().getDirectory().getValue()) + "jade-dmz-"
                + getUUID();
    }

    private String getUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

}
