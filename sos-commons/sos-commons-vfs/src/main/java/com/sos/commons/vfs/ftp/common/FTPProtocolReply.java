package com.sos.commons.vfs.ftp.common;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;

import com.sos.commons.util.SOSCollection;

public class FTPProtocolReply {

    private final int code;
    private final String text;

    public FTPProtocolReply(FTPClient client) {
        this.code = client.getReplyCode();
        this.text = getText(client);
    }

    public boolean isSystemStatusReply() {
        return code == FTPReply.SYSTEM_STATUS;
    }

    public boolean isPositiveReply() {
        return FTPReply.isPositiveCompletion(code);
    }

    public boolean isNotFoundReply(FTPClient client, String path) {
        // 550 - not found/permissions/locked...
        if (code == FTPReply.FILE_UNAVAILABLE) {
            // Reply text is not analyzed due to different implementations/languages
            try {
                client.sendCommand(FTPCmd.SIZE, path);
                int replyCode = client.getReplyCode();
                if (replyCode == FTPReply.FILE_STATUS) {// 213 - file exists
                    return false;
                }
                if (replyCode == FTPReply.FILE_UNAVAILABLE) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + code + "]" + text;
    }

    /** { "200 OK" } -> "200 OK"<br/>
     * { "150 Opening", "226 Transfer complete" } -> "[150 Opening]226 Transfer complete"<br/>
     * 
     * @return */
    private String getText(FTPClient client) {
        String[] replies = client.getReplyStrings();
        if (SOSCollection.isEmpty(replies)) {
            return "";
        }
        return IntStream.range(0, replies.length)
                // all except the last in brackets
                .mapToObj(i -> (i < replies.length - 1) ? "[" + replies[i].trim() + "]" : replies[i].trim())
                // join
                .collect(Collectors.joining(""));
    }

}
