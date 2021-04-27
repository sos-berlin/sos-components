package com.sos.commons.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SOSShellTest {

    @Test
    void test() {
        SOSCommandResult sosCommandResult = SOSShell.executeCommand("dir", true);
        System.out.println(sosCommandResult.getStdOut());
    }

}
