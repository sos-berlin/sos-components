
package com.sos.controller.model.order;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExpectedNotice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "boardName",
    "noticeId"
})
public class ExpectedNotice {

    @JsonProperty("boardName")
    @JsonAlias({
        "boardPath"
    })
    private String boardName;
    @JsonProperty("noticeId")
    private String noticeId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExpectedNotice() {
    }

    /**
     * 
     * @param boardName
     * @param noticeId
     */
    public ExpectedNotice(String boardName, String noticeId) {
        super();
        this.boardName = boardName;
        this.noticeId = noticeId;
    }

    @JsonProperty("boardName")
    public String getBoardName() {
        return boardName;
    }

    @JsonProperty("boardName")
    public void setBoardName(String boardName) {
        this.boardName = boardName;
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
        return new ToStringBuilder(this).append("boardName", boardName).append("noticeId", noticeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(boardName).append(noticeId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectedNotice) == false) {
            return false;
        }
        ExpectedNotice rhs = ((ExpectedNotice) other);
        return new EqualsBuilder().append(boardName, rhs.boardName).append(noticeId, rhs.noticeId).isEquals();
    }

}
