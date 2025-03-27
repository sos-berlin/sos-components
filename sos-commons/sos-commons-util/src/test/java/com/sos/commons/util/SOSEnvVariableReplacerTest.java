package com.sos.commons.util;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSEnvVariableReplacerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSEnvVariableReplacerTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, String> env = System.getenv();
        String win = "Hi %USERNAME% - %COMPUTERNAME% - %NOT_EXISTS%";
        String unix = "Hi ${USERNAME} - $Computername123 - $NOT_EXISTS";
        String all = "[WINDOWS]" + win + "[UNIX]" + unix;

        SOSEnvVariableReplacer r = new SOSEnvVariableReplacer(env, true);
        LOGGER.info("[caseSensitive=true, keepUnresolvedVariables=true]");
        LOGGER.info(String.format("    [WINDOWS]%s", r.replaceWindowsVars(win)));
        LOGGER.info(String.format("    [UNIX]%s", r.replaceUnixVars(unix)));
        LOGGER.info(String.format("    [ALL]%s", r.replaceAllVars(all)));

        LOGGER.info("[caseSensitive=true, keepUnresolvedVariables=true]");
        win = "Hi %Username% - %COMPUTERname% - %NOT_EXISTS%";
        r = new SOSEnvVariableReplacer(env, false);
        LOGGER.info(String.format("    [WINDOWS]%s", r.replaceWindowsVars(win)));
        LOGGER.info(String.format("    [UNIX]%s", r.replaceUnixVars(unix)));
        LOGGER.info(String.format("    [ALL]%s", r.replaceAllVars(all)));

        LOGGER.info("[caseSensitive=false, keepUnresolvedVariables=false]");
        win = "Hi %Username% - %COMPUTERname% - %NOT_EXISTS%";
        r = new SOSEnvVariableReplacer(env, false, false);
        LOGGER.info(String.format("    [WINDOWS]%s", r.replaceWindowsVars(win)));
        LOGGER.info(String.format("    [UNIX]%s", r.replaceUnixVars(unix)));
        LOGGER.info(String.format("    [ALL]%s", r.replaceAllVars(all)));
    }
}
