package com.sos.commons.vfs.ssh.commons;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.vfs.ssh.SSHProvider;

public class SSHServerInfo {

    public enum OS {
        UNKNOWN, UNIX, WINDOWS
    };

    public enum Shell {
        UNKNOWN, UNIX, WINDOWS, CYGWIN
    };

    private static final String COMMANDO = "uname";

    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private final String serverVersion;
    private final SOSCommandResult commandResult;
    // String because of linux|darwin|aix|hp-ux|solaris|sunos|freebsd ...
    private String os = OS.UNKNOWN.name();
    private Shell shell = Shell.UNKNOWN;

    public SSHServerInfo(SSHProvider<?, ?> provider, String serverVersion) {
        this.serverVersion = serverVersion;
        if (provider.getArguments().getDisableAutoDetectShell().isTrue()) {
            this.commandResult = new SOSCommandResult(serverVersion);
            // exitCode=null
            if (provider.getLogger().isDebugEnabled()) {
                provider.getLogger().debug("[SSHServerInfo][%s=true][skip exec=%s][identifying OS from server version]%s", provider.getArguments()
                        .getDisableAutoDetectShell().getName(), COMMANDO, serverVersion);
            }
        } else {
            this.commandResult = provider.executeCommand(COMMANDO);
            if (provider.getLogger().isDebugEnabled()) {
                provider.getLogger().debug("[SSHServerInfo]%s", SOSString.replaceNewLines(commandResult.toString(), " "));
            }
        }
        analyze(provider);
    }

    private void analyze(SSHProvider<?, ?> provider) {
        if (commandResult == null) {
            return;
        }

        // getDisableAutoDetectShell is active or uname exitCode=null
        if (!commandResult.hasExitCode()) {
            if (commandResult.getStdErr().length() > 0) {
                os = OS.WINDOWS.name();
            } else {
                trySetOSFromServerVersion();
            }
            return;
        }

        // SFTP only
        // - Windows OpenSSH DE - [SSHServerInfo][uname][exitCode=0][std:out=This service allows sftp connections only.][std:err=]
        // - Unix OpenSSH - [SSHServerInfo][uname][exitCode=1][std:out=This service allows sftp connections only.][std:err=]
        String stdOut = commandResult.getStdOut().trim();
        String stdOutLowerCase = stdOut.toLowerCase();
        String stdErrLowerCase = commandResult.getStdErr().trim().toLowerCase();
        boolean isSFTPOnly = stdOutLowerCase.contains("sftp") && stdOutLowerCase.contains("only") && stdErrLowerCase.isEmpty();
        switch (commandResult.getExitCode()) {
        case 0:
            if (stdOutLowerCase.matches("(?i).*(linux|darwin|aix|hp-ux|solaris|sunos|freebsd).*")) {
                os = stdOut;
                setShell(isSFTPOnly, Shell.UNIX);
            } else if (stdOutLowerCase.matches("(?i).*cygwin.*")) {
                // OS is Windows but shell is Unix like
                // unix commands have to be used
                os = OS.WINDOWS.name();
                setShell(isSFTPOnly, Shell.CYGWIN);
            } else if (stdOutLowerCase.equals("win32nt")) {// CompleteFTP_25.0.6
                os = OS.WINDOWS.name();
                setShell(isSFTPOnly, Shell.UNIX);
            } else {
                trySetOSFromServerVersion();
                setShell(isSFTPOnly, Shell.UNIX);
            }
            break;
        case 9009:
            // Windows - command not found
            os = OS.WINDOWS.name();
            setShell(isSFTPOnly, Shell.WINDOWS);
            break;
        case 1:
            // call of uname under Windows OS delivers exit code 9009 or exit code 1 and target shell cmd.exe
            // the exit code depends on the remote SSH implementation
            trySetOSFromServerVersion(); // maybe OpenSSH_for_Windows_x.x
            if (isSFTPOnly) {
                shell = Shell.UNKNOWN;
            } else {
                // [uname][exitCode=1][std:out=][std:err=Der Befehl "uname" ist entweder falsch geschrieben oder konnte nicht gefunden werden.]
                if (stdOutLowerCase.isEmpty() && stdErrLowerCase.contains(COMMANDO.toLowerCase())) {
                    os = OS.WINDOWS.name();
                    shell = Shell.WINDOWS;
                } else {
                    SOSCommandResult ver = provider.executeCommand("ver");
                    if (provider.getLogger().isDebugEnabled()) {
                        provider.getLogger().debug("[SSHServerInfo]%s", commandResult);
                    }
                    if (ver.getStdOut().toUpperCase().contains(OS.WINDOWS.name())) {
                        os = OS.WINDOWS.name();
                        shell = Shell.WINDOWS;
                    }
                    // otherwise UNKNOWN
                }
            }
            break;
        case 127:
            // call of uname under Windows OS with CopSSH (cygwin) and target shell /bin/bash delivers exit code 127
            // command uname is not installed by default through CopSSH installation
            os = OS.WINDOWS.name();
            setShell(isSFTPOnly, Shell.CYGWIN);
            break;
        default:
            trySetOSFromServerVersion();
            shell = Shell.UNKNOWN;
            break;
        }
    }

    private void trySetOSFromServerVersion() {
        if (!SOSString.isEmpty(serverVersion)) {
            if (serverVersion.toUpperCase().contains(OS.WINDOWS.name())) {
                os = OS.WINDOWS.name();
            }
        }
    }

    private void setShell(boolean isSFTPOnly, Shell shell) {
        this.shell = isSFTPOnly ? Shell.UNKNOWN : shell;
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

    public boolean isWindowsShell() {
        return shell.equals(Shell.WINDOWS);
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

        if (commandResult.hasError(false)) {
            result.append(" (");
            result.append(commandResult.getCommand());
            if (commandResult.isNonZeroExitCode()) {
                result.append(" exitCode=").append(commandResult.getExitCode());
            }
            if (commandResult.hasException()) {
                result.append(" ").append(commandResult.getException().toString());
            }
            result.append(")");
        }
        return result.toString();
    }

}
