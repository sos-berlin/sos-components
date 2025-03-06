package com.sos.commons.vfs.ftp.common;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.sos.commons.util.SOSCollection;

public class FTPProtocolReply {

    private final int code;
    private final String text;

    public FTPProtocolReply(FTPClient client) {
        this.code = client.getReplyCode();
        this.text = getText(client);
    }

    /** Reply code 211 */
    public boolean isSystemStatusReply() {
        return code == FTPReply.SYSTEM_STATUS;
    }

    /** Reply code 213 - file exists */
    public boolean isFileStatusReply() {
        return code == FTPReply.FILE_STATUS;
    }

    /** Reply code 550 - not found/permissions/locked... */
    public boolean isFileUnavailableReply() {
        return code == FTPReply.FILE_UNAVAILABLE;
    }

    public boolean isPositiveReply() {
        return FTPReply.isPositiveCompletion(code);
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
