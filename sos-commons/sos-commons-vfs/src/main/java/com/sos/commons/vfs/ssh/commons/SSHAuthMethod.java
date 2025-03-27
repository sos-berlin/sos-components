package com.sos.commons.vfs.ssh.commons;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;

public enum SSHAuthMethod {

    PASSWORD, PUBLICKEY, KEYBOARD_INTERACTIVE;

    // See XML schema for Preferred/Required Authentications
    // Examples: password,publickey
    public static List<SSHAuthMethod> fromString(String methods) {
        if (SOSString.isEmpty(methods)) {
            return null;
        }
        return Stream.of(methods.split(",")).map(String::trim).map(String::toUpperCase).map(SSHAuthMethod::valueOf).collect(Collectors.toList());
    }
}
