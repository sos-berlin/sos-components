package com.sos.commons.vfs.ssh.common;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.common.SOSCommandResult;

public class SSHShellInfo {

    public enum OS {
        UNKNOWN, UNIX, WINDOWS
    };

    public enum Shell {
        UNKNOWN, UNIX, WINDOWS, CYGWIN
    };

    private final String serverVersion;
    private final SOSCommandResult commandResult;
    private String os = OS.UNKNOWN.name();
    private Shell shell = Shell.UNKNOWN;

    public SSHShellInfo(String serverVersion, SOSCommandResult commandResult) {
        this.serverVersion = serverVersion;
        this.commandResult = commandResult;
        analyze();
    }

    private void analyze() {
        if (commandResult == null || commandResult.getException() != null) {
            return;
        }
        // TODO analyze this.serverVersion and commandResult.getException()
        if (commandResult.getExitCode() == null) {
            if (commandResult.getStdErr().length() > 0) {
                os = OS.WINDOWS.name();
                // shell = ?
            }
            return;
        }

        switch (commandResult.getExitCode()) {
        case 0:
            String stdOut = commandResult.getStdOut().trim();
            if (stdOut.matches("(?i).*(linux|darwin|aix|hp-ux|solaris|sunos|freebsd).*")) {
                os = stdOut;
                shell = Shell.UNIX;
            } else if (stdOut.matches("(?i).*cygwin.*")) {
                // OS is Windows but shell is Unix like
                // unix commands have to be used
                os = OS.WINDOWS.name();
                shell = Shell.CYGWIN;
            } else {
                os = OS.UNKNOWN.name();
                shell = Shell.UNIX;
            }
            break;
        case 9009:
        case 1:
            // call of uname under Windows OS delivers exit code 9009 or exit code 1 and target shell cmd.exe
            // the exit code depends on the remote SSH implementation
            os = OS.WINDOWS.name();
            shell = Shell.WINDOWS;
            break;
        case 127:
            // call of uname under Windows OS with CopSSH (cygwin) and target shell /bin/bash delivers exit code 127
            // command uname is not installed by default through CopSSH installation
            os = OS.WINDOWS.name();
            shell = Shell.CYGWIN;
            break;
        default:
            os = OS.UNKNOWN.name();
            shell = Shell.UNKNOWN;
            break;
        }
    }

    public String getOS() {
        return os;
    }

    public Shell getShell() {
        return shell;
    }

    public SOSCommandResult getCommandResult() {
        return commandResult;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    @Override
    public String toString() {
        List<String> l = new ArrayList<>();
        if (serverVersion != null) {
            l.add("Identity=" + serverVersion);
        }
        l.add("OS=" + os);
        l.add("Shell=" + shell.name());

        StringBuilder result = new StringBuilder("Server ");
        result.append(String.join(", ", l));

        if (commandResult.hasError()) {
            result.append(" (");
            result.append(commandResult.getCommand());
            if (commandResult.getExitCode() != null && commandResult.getExitCode() > 0) {
                result.append(" exitCode=").append(commandResult.getExitCode());
            }
            if (commandResult.getException() != null) {
                result.append(" ").append(commandResult.getException().toString());
            }
            result.append(")");
        }
        return result.toString();
    }

}
