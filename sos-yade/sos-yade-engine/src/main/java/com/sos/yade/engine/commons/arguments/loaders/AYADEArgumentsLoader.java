package com.sos.yade.engine.commons.arguments.loaders;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public abstract class AYADEArgumentsLoader {

    private final YADEArguments args;
    private final YADEClientArguments clientArgs;
    
    private YADESourceArguments sourceArgs;
    private YADETargetArguments targetArgs;
    private YADEJumpHostArguments jumpHostArgs;

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

    public abstract AYADEArgumentsLoader load(ISOSLogger logger, Object... params) throws YADEEngineSettingsLoadException;

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
}
