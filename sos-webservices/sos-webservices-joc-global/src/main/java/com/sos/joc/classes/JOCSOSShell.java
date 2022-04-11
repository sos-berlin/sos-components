package com.sos.joc.classes;

import java.nio.charset.Charset;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.joc.Globals;

public class JOCSOSShell {

    public static SOSCommandResult executeCommand(String script) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), null, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset charset) {
        return SOSShell.executeCommand(script, getCharset(charset), null, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), timeout, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset charset, SOSTimeout timeout) {
        return SOSShell.executeCommand(script, getCharset(charset), timeout, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSEnv env) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), null, env);
    }

    public static SOSCommandResult executeCommand(String script, Charset charset, SOSEnv env) {
        return SOSShell.executeCommand(script, getCharset(charset), null, env);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout, SOSEnv env) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), timeout, env);
    }

    private static Charset getCharset(Charset charset) {
        return charset == null ? getGlobalsJocCharset() : charset;
    }

    private static Charset getGlobalsJocCharset() {
        return Globals.getConfigurationGlobalsJoc().getEncodingCharset();
    }
}
