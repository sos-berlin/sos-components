package com.sos.commons.vfs.ssh.commons;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;

public enum SSHAuthMethod {

    PASSWORD, PUBLICKEY, KEYBOARD_INTERACTIVE;

    /** See XML schema for Preferred/Required Authentications
     * 
     * @param methods Examples: password,publickey
     * @return */
    public static List<SSHAuthMethod> fromString(String methods) {
        if (SOSString.isEmpty(methods)) {
            return null;
        }
        return Stream.of(methods.split(",")).map(String::trim).map(String::toUpperCase).map(SSHAuthMethod::valueOf).collect(Collectors.toList());
    }

    /** @param methods
     * @return Examples: password,publickey */
    public static String toString(List<SSHAuthMethod> methods) {
        if (methods == null) {
            return null;
        }
        return SOSString.join(methods, ",", n -> n.name().toLowerCase());
    }
}
