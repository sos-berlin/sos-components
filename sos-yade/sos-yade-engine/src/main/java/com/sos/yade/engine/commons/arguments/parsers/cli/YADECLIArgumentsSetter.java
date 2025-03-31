package com.sos.yade.engine.commons.arguments.parsers.cli;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.parsers.xml.YADEXMLArgumentsSetter;
import com.sos.yade.engine.exceptions.YADEEngineSettingsParserException;

public class YADECLIArgumentsSetter extends YADEXMLArgumentsSetter {

    @Override
    public YADECLIArgumentsSetter set(ISOSLogger logger, Object... params) throws YADEEngineSettingsParserException {
        if (params == null || params.length != 1) {
            throw new YADEEngineSettingsParserException("missing cliArgs");
        }
        if (params[0] == null || !(params[0] instanceof String[])) {
            throw new YADEEngineSettingsParserException("missing cliArgs");
        }

        String[] cliArgs = (String[]) params[0];

        return this;
    }

}
