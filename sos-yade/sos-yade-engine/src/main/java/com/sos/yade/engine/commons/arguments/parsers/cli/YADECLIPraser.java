package com.sos.yade.engine.commons.arguments.parsers.cli;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.yade.engine.commons.arguments.parsers.AYADEParser;

public class YADECLIPraser extends AYADEParser {

    @Override
    public void parse(Object... args) throws Exception {
        if (args == null || args.length != 1) {
            throw new SOSMissingDataException("cliArgs");
        }
        if (args[0] == null || !(args[0] instanceof String[])) {
            throw new SOSMissingDataException("cliArgs");
        }

        String[] cliArgs = (String[]) args[0];
    }

}
