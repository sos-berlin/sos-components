
package com.sos.controller.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Delete Notice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "boardPath",
    "noticeId"
})
public class DeleteNotice
    extends Command
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    private String boardPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeId")
    private String noticeId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DeleteNotice() {
    }

    /**
     * 
     * @param boardPath
     * @param noticeId
     */
    public DeleteNotice(String boardPath, String noticeId) {
        super();
        this.boardPath = boardPath;
        this.noticeId = noticeId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    public String getBoardPath() {
        return boardPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    public void setBoardPath(String boardPath) {
        this.boardPath = boardPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeId")
    public String getNoticeId() {
        return noticeId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeId")
    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("boardPath", boardPath).append("noticeId", noticeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeId).append(boardPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteNotice) == false) {
            return false;
        }
        DeleteNotice rhs = ((DeleteNotice) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeId, rhs.noticeId).append(boardPath, rhs.boardPath).isEquals();
    }

}
