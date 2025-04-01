package com.sos.yade.engine.commons.arguments.loaders.cli;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADECLIArgumentsLoader extends AYADEArgumentsLoader {

    @Override
    public YADECLIArgumentsLoader load(ISOSLogger logger, Object... params) throws YADEEngineSettingsLoadException {
        if (params == null || params.length != 1) {
            throw new YADEEngineSettingsLoadException("missing cliArgs");
        }
        if (params[0] == null || !(params[0] instanceof String[])) {
            throw new YADEEngineSettingsLoadException("missing cliArgs");
        }

        String[] cliArgs = (String[]) params[0];

        return this;
    }

}
