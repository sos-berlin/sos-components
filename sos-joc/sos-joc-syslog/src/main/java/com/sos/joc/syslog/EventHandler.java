package com.sos.joc.syslog;

import java.nio.charset.StandardCharsets;

public class EventHandler {

    private byte[] raw = null;

    public EventHandler(byte[] message, int length) {
        this.raw = new byte[length];
        System.arraycopy(message, 0, this.raw, 0, length);
        parse();
    }

    private void parse() {
        System.out.println("Received message: " + newString());
    }
    
    private final String newString() {
        return new String(this.raw, StandardCharsets.UTF_8);
    }
}
