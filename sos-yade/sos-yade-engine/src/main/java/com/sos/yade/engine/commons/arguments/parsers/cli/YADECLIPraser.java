package com.sos.yade.engine.commons.arguments.parsers.cli;

import com.sos.yade.engine.commons.arguments.parsers.AYADEParser;
import com.sos.yade.engine.exceptions.YADEEngineSettingsParserException;

public class YADECLIPraser extends AYADEParser {

    @Override
    public YADECLIPraser parse(Object... args) throws YADEEngineSettingsParserException {
        if (args == null || args.length != 1) {
            throw new YADEEngineSettingsParserException("missing cliArgs");
        }
        if (args[0] == null || !(args[0] instanceof String[])) {
            throw new YADEEngineSettingsParserException("missing cliArgs");
        }

        String[] cliArgs = (String[]) args[0];

        return this;
    }

}
