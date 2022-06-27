
package com.sos.controller.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set if state == ExpectingNotices
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "boardPath",
    "noticeId"
})
public class ExpectedNotices {

    @JsonProperty("boardPath")
    private String boardPath;
    @JsonProperty("noticeId")
    private String noticeId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExpectedNotices() {
    }

    /**
     * 
     * @param boardPath
     * @param noticeId
     */
    public ExpectedNotices(String boardPath, String noticeId) {
        super();
        this.boardPath = boardPath;
        this.noticeId = noticeId;
    }

    @JsonProperty("boardPath")
    public String getBoardPath() {
        return boardPath;
    }

    @JsonProperty("boardPath")
    public void setBoardPath(String boardPath) {
        this.boardPath = boardPath;
    }

    @JsonProperty("noticeId")
    public String getNoticeId() {
        return noticeId;
    }

    @JsonProperty("noticeId")
    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("boardPath", boardPath).append("noticeId", noticeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(noticeId).append(boardPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectedNotices) == false) {
            return false;
        }
        ExpectedNotices rhs = ((ExpectedNotices) other);
        return new EqualsBuilder().append(noticeId, rhs.noticeId).append(boardPath, rhs.boardPath).isEquals();
    }

}
