package com.sos.commons.vfs.ftp.commons;

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

    /** Check if the reply code is in the successful range (2xx)<br/>
     *
     *
     * Others:<br/>
     * - Check for intermediate replies (3xx), which are not failures, but may require further action<br/>
     * -- FTPReply.isPositiveIntermediate(replyCode))<br />
     * - Check for permanent failures (5xx)<br/>
     * -- FTPReply.isNegativePermanent(replyCode)<br/>
     * - Check for transient(that may be recoverable by another attempt) failures (4xx)<br/>
     * -- FTPReply.isNegativeTransient(replyCode)<br/>
     * 
     * @return boolean */
    public boolean isPositiveReply() {
        return FTPReply.isPositiveCompletion(code);
    }

    /** Checks whether the reply indicates that a command or its parameters are not supported.
     *
     * <p>
     * Matches the following FTP reply codes:
     * </p>
     * <ul>
     * <li>{@code 500} - Syntax error, command unrecognized</li>
     * <li>{@code 501} - Syntax error in parameters or arguments</li>
     * <li>{@code 502} - Command not implemented</li>
     * <li>{@code 504} - Command not implemented for that parameter</li>
     * </ul>
     *
     * <p>
     * {@code 503} is intentionally not included because it indicates a bad sequence of commands rather than an unsupported command.<br />
     * Falling back to another command would not resolve the sequence error.
     * </p>
     *
     * @return {@code true} if the reply indicates an unsupported command or parameter<br />
     *         {@code false} otherwise */
    public boolean isCommandNotSupportedReply() {
        return code == 500 || code == 501 || code == 502 || code == 504;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
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
