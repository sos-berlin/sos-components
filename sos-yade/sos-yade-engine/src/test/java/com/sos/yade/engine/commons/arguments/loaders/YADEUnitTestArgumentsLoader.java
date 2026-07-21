package com.sos.yade.engine.commons.arguments.loaders;

import java.nio.file.Path;
import java.util.Map;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADEUnitTestArgumentsLoader extends AYADEArgumentsLoader {

    public YADEUnitTestArgumentsLoader(YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, YADEJumpHostArguments jumpHostArgs) {
        super(args, clientArgs, sourceArgs, targetArgs, jumpHostArgs);
    }

    @Override
    public YADEUnitTestArgumentsLoader load(ISOSLogger logger, Path settings, String profile, String alternativeProfile,
            Map<String, String> replacerMap, boolean replaceCaseSensitive, boolean replacerKeepUnresolvedVariables)
            throws YADEEngineSettingsLoadException {
        return this;
    }

}
