package com.sos.joc.classes;

import java.nio.charset.Charset;
import java.nio.file.Path;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.joc.Globals;

public class JOCSOSShell {

    public static SOSCommandResult executeCommand(String script) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), null, null, null);
    }

    public static SOSCommandResult executeCommand(String script, Path workfingDirectory) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), null, null, workfingDirectory);
    }

    public static SOSCommandResult executeCommand(String script, Charset charset) {
        return SOSShell.executeCommand(script, getCharset(charset), null, null, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), timeout, null, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset charset, SOSTimeout timeout) {
        return SOSShell.executeCommand(script, getCharset(charset), timeout, null, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSEnv env) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), null, env, null);
    }

    public static SOSCommandResult executeCommand(String script, Charset charset, SOSEnv env) {
        return SOSShell.executeCommand(script, getCharset(charset), null, env, null);
    }

    public static SOSCommandResult executeCommand(String script, SOSTimeout timeout, SOSEnv env, Path workingDirectory) {
        return SOSShell.executeCommand(script, getGlobalsJocCharset(), timeout, env, workingDirectory);
    }

    private static Charset getCharset(Charset charset) {
        return charset == null ? getGlobalsJocCharset() : charset;
    }

    private static Charset getGlobalsJocCharset() {
        return Globals.getConfigurationGlobalsJoc().getEncodingCharset();
    }
}
