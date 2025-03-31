package com.sos.yade.engine.commons.arguments.parsers;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineSettingsParserException;

public abstract class AYADEArgumentsSetter {

    private final YADEArguments args;
    private final YADEClientArguments clientArgs;
    private final YADESourceArguments sourceArgs;

    private YADETargetArguments targetArgs;
    private YADEJumpArguments jumpArgs;

    public AYADEArgumentsSetter() {
        this.args = new YADEArguments();
        this.clientArgs = new YADEClientArguments();
        this.sourceArgs = new YADESourceArguments();

        this.args.applyDefaultIfNullQuietly();
        this.clientArgs.applyDefaultIfNullQuietly();
        this.sourceArgs.applyDefaultIfNullQuietly();
    }

    // currently only for UnitTests
    protected AYADEArgumentsSetter(YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs, YADETargetArguments targetArgs,
            YADEJumpArguments jumpArgs) {
        this.args = args;
        this.clientArgs = clientArgs;
        this.sourceArgs = sourceArgs;
        this.targetArgs = targetArgs;
        this.jumpArgs = jumpArgs;
    }

    public abstract AYADEArgumentsSetter set(ISOSLogger logger, Object... params) throws YADEEngineSettingsParserException;

    public YADEArguments getArgs() {
        return args;
    }

    public YADEClientArguments getClientArgs() {
        return clientArgs;
    }

    public YADESourceArguments getSourceArgs() {
        return sourceArgs;
    }

    public YADETargetArguments getTargetArgs() {
        return targetArgs;
    }

    public YADEJumpArguments getJumpArguments() {
        return jumpArgs;
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

    public void initializeJumpArgsIfNull() {
        if (jumpArgs == null) {
            jumpArgs = new YADEJumpArguments();
            jumpArgs.applyDefaultIfNullQuietly();
        }
    }
}
