package com.sos.commons.vfs.smb;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hierynomus.ntlm.messages.NtlmChallenge;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.buffer.Endian;
import com.hierynomus.spnego.NegTokenTarg;

public class SMBJTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMBJTest.class);

    @Ignore
    @Test
    public void decodePacketHexOutput() throws Exception {
        // Encoder/Decoder outputs
        String hex = "48 61 6c 6c 6f";
        hex = "a1 81 fc 30 81 f9 a0 03 0a 01 01 a1 0c 06 0a 2b 06 01 04 01 82 37 02 02 0a a2 81 e3 04 81 e0 4e 54 4c 4d 53 53 50 00 02 00 00 00 1e 00 1e 00 38 00 00 00 15 82 8a 62 f5 f8 6f b4 b0 9a 25 6e 00 00 00 00 00 00 00 00 8a 00 8a 00 56 00 00 00 06 01 00 00 00 00 00 0f 43 00 45 00 4e 00 54 00 4f 00 53 00 37 00 2d 00 54 00 45 00 53 00 54 00 4a 00 53 00 32 00 02 00 1e 00 43 00 45 00 4e 00 54 00 4f 00 53 00 37 00 2d 00 54 00 45 00 53 00 54 00 4a 00 53 00 32 00 01 00 1e 00 43 00 45 00 4e 00 54 00 4f 00 53 00 37 00 2d 00 54 00 45 00 53 00 54 00 4a 00 53 00 32 00 04 00 06 00 73 00 6f 00 73 00 03 00 28 00 63 00 65 00 6e 00 74 00 6f 00 73 00 37 00 2d 00 74 00 65 00 73 00 74 00 2d 00 6a 00 73 00 32 00 2e 00 73 00 6f 00 73 00 07 00 08 00 2a e7 82 7c f3 62 dd 01 00 00 00 00";

        byte[] token = java.util.HexFormat.of().parseHex(hex.replaceAll("\\s+", ""));

        byte[] signature = { 0x4e, 0x54, 0x4c, 0x4d, 0x53, 0x53, 0x50, 0x00 };
        int offset = -1;
        for (int i = 0; i <= token.length - signature.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(token, i, i + signature.length), signature)) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            throw new IllegalArgumentException("NTLMSSP signature not found");
        }

        LOGGER.info("- Method 1 ------------------------------");
        byte[] ntlmToken = java.util.Arrays.copyOfRange(token, offset, token.length);
        LOGGER.info("NTLM message type: " + (ntlmToken[8] & 0xff));
        LOGGER.info("NTLM message hex: " + java.util.HexFormat.ofDelimiter(" ").formatHex(ntlmToken));
        LOGGER.info("NTLMSSP offset: " + offset);
        LOGGER.info("NTLMSSP message type: " + ntlmToken[8]);

        LOGGER.info("- Method 2 ------------------------------");
        NtlmChallenge challenge = new NtlmChallenge();
        challenge.read(new Buffer.PlainBuffer(ntlmToken, Endian.LE));
        LOGGER.info("Target name: " + challenge.getTargetName());
        LOGGER.info("Server challenge: " + java.util.HexFormat.of().formatHex(challenge.getServerChallenge()));
        LOGGER.info("Target info: " + challenge.getTargetInfo());
        LOGGER.info("Negotiate flags: " + challenge.getNegotiateFlags());

        LOGGER.info("- Method 3 ------------------------------");
        NegTokenTarg negTokenTarg = new NegTokenTarg().read(token);
        NtlmChallenge serverNtlmChallenge = new NtlmChallenge();
        try {
            serverNtlmChallenge.read(new Buffer.PlainBuffer(negTokenTarg.getResponseToken(), Endian.LE));
        } catch (Buffer.BufferException e) {
            throw new IOException(e);
        }
        LOGGER.info("Received NTLM challenge: {}", serverNtlmChallenge);
    }

}
