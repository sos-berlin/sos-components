package com.sos.yade.engine.commons.arguments.parsers;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.exceptions.YADEEngineSettingsParserException;

public class YADEUnitTestArgumentsSetter extends AYADEArgumentsSetter {

    public YADEUnitTestArgumentsSetter(YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, YADEJumpArguments jumpArgs) {
        super(args, clientArgs, sourceArgs, targetArgs, jumpArgs);
    }

    @Override
    public YADEUnitTestArgumentsSetter set(ISOSLogger logger, Object... params) throws YADEEngineSettingsParserException {

        return this;
    }

}
