package com.sos.yade.engine.commons.arguments.loaders;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADENotificationArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public abstract class AYADEArgumentsLoader {

    private final YADEArguments args;
    private final YADEClientArguments clientArgs;

    private YADESourceArguments sourceArgs;
    private YADETargetArguments targetArgs;
    private YADEJumpHostArguments jumpHostArgs;
    private YADENotificationArguments notificationArgs;

    private SOSMapVariableReplacer varReplacer;

    private Set<String> visitedProfiles = new HashSet<>();

    public AYADEArgumentsLoader() {
        this.args = new YADEArguments();
        this.clientArgs = new YADEClientArguments();
        this.sourceArgs = new YADESourceArguments();

        this.args.applyDefaultIfNullQuietly();
        this.clientArgs.applyDefaultIfNullQuietly();
        this.sourceArgs.applyDefaultIfNullQuietly();
    }

    // currently only for UnitTests
    protected AYADEArgumentsLoader(YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs, YADETargetArguments targetArgs,
            YADEJumpHostArguments jumpHostArgs) {
        this.args = args;
        this.clientArgs = clientArgs;
        this.sourceArgs = sourceArgs;
        this.targetArgs = targetArgs;
        this.jumpHostArgs = jumpHostArgs;
    }

    public abstract AYADEArgumentsLoader load(ISOSLogger logger, Path settings, String profile, String alternativeProfile,
            Map<String, String> replacerMap, boolean replaceCaseSensitive, boolean replacerKeepUnresolvedVariables)
            throws YADEEngineSettingsLoadException;

    public YADEArguments getArgs() {
        return args;
    }

    public YADEClientArguments getClientArgs() {
        return clientArgs;
    }

    public YADESourceArguments getSourceArgs() {
        return sourceArgs;
    }

    public void setSourceArgs(YADESourceArguments val) {
        sourceArgs = val;
    }

    public YADETargetArguments getTargetArgs() {
        return targetArgs;
    }

    public void setTargetArgs(YADETargetArguments val) {
        targetArgs = val;
    }

    public YADEJumpHostArguments getJumpHostArgs() {
        return jumpHostArgs;
    }

    public YADENotificationArguments getNotificationArgs() {
        return notificationArgs;
    }

    public void nullifyTargetArgs() {
        targetArgs = null;
    }

    public void initializeTargetArgsIfNull() {
        if (targetArgs == null) {
            targetArgs = new YADETargetArguments();
            targetArgs.applyDefaultIfNullQuietly();
        }
    }

    public void initializeJumpHostArgsIfNull() {
        if (jumpHostArgs == null) {
            jumpHostArgs = new YADEJumpHostArguments();
            jumpHostArgs.applyDefaultIfNullQuietly();
        }
    }

    public void initializeNotificationArgsIfNull() {
        if (notificationArgs == null) {
            notificationArgs = new YADENotificationArguments();
        }
    }

    public SOSMapVariableReplacer getVarReplacer() {
        return varReplacer;
    }

    public void setVarReplacer(SOSMapVariableReplacer val) {
        varReplacer = val;
    }

    public boolean profileEqualsAlternativeProfile(String alternativeProfile) {
        try {
            return getArgs().getProfile().getValue().equals(alternativeProfile);
        } catch (Exception e) {
            // NPE
            return false;
        }
    }

    public boolean profileEqualsAlternativeProfile() {
        return profileEqualsAlternativeProfile(getArgs().getAlternativeProfile().getValue());
    }

    public boolean isNullOrVisitedProfile(String profile) {
        return profile == null ? true : visitedProfiles.contains(profile);
    }

    public void setVisitedProfile(AYADEArgumentsLoader argsLoader, String profile) {
        if (argsLoader != null) {
            visitedProfiles.addAll(argsLoader.visitedProfiles);
        }
        visitedProfiles.add(profile);
    }

    public void setVisitedProfile(String profile) {
        setVisitedProfile(null, profile);
    }

    public void clearVisitedProfiles() {
        visitedProfiles.clear();
    }
}
