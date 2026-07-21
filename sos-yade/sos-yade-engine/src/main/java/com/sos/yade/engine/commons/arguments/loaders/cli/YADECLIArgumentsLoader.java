package com.sos.yade.engine.commons.arguments.loaders.cli;

import java.nio.file.Path;
import java.util.Map;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADECLIArgumentsLoader extends AYADEArgumentsLoader {

    @Override
    public YADECLIArgumentsLoader load(ISOSLogger logger, Path settings, String profile, String alternativeProfile, Map<String, String> replacerMap,
            boolean replaceCaseSensitive, boolean replacerKeepUnresolvedVariables) throws YADEEngineSettingsLoadException {

        // String[] cliArgs = (String[]) params[0];

        return this;
    }

}
