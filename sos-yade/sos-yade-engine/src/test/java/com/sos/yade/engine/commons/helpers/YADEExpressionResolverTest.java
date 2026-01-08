package com.sos.yade.engine.commons.helpers;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.arguments.base.SOSArgument;

public class YADEExpressionResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEExpressionResolverTest.class);

    @Ignore
    @Test
    public void testAsArg() {

        SOSArgument<String> arg = new SOSArgument<String>("Test", false);

        replaceArg(arg, "Test_[date:yyyyMM].txt");
        replaceArg(arg, "Test_[date:yyyyMMdd HH-mm timezone:Etc/UTC].txt");
        replaceArg(arg, "Test_[date:yyyyMMdd HH-mm timezone:Europe/Berlin].txt");
        replaceArg(arg, "Test_[date:yyyyMMdd HH-mm timezone:+02:00].txt");
        replaceArg(arg, "Test_[date:yyyyMMdd HH-mm timezone:+5:30].txt");
        replaceArg(arg, "Test_[date:yyyyMMdd HH-mm timezone:-5:30].txt");
        replaceArg(arg, "Test_[date:yyyy-MM-dd-!!].txt");
        replaceArg(arg, "Test_XXX.txt");
        replaceArg(arg, "Test_[date:yyyyMMdd HH-mm timezone:Europe/Berlin2].txt");
    }

    private static void replaceArg(SOSArgument<String> arg, String val) {
        arg.setValue(val);
        try {
            YADEExpressionResolver.replaceDateExpressions(arg);
            LOGGER.info("[" + arg.getName() + "][isDirty=" + arg.isDirty() + "][" + val + "]" + arg.getValue());
        } catch (Exception e) {
            LOGGER.info("[" + arg.getName() + "]" + val);
            LOGGER.error(e.toString(), e);
        }
    }

}
