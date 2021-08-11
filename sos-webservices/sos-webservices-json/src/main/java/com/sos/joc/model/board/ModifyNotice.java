
package com.sos.joc.model.board;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * delete/post notice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "boardPath",
    "noticeId",
    "endOfLife"
})
public class ModifyNotice {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("endOfLife")
    private Long endOfLife;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("boardPath")
    public String getBoardPath() {
        return boardPath;
    }

    /**
     * string without < and >
     * <p>
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("endOfLife")
    public Long getEndOfLife() {
        return endOfLife;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("endOfLife")
    public void setEndOfLife(Long endOfLife) {
        this.endOfLife = endOfLife;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("boardPath", boardPath).append("noticeId", noticeId).append("endOfLife", endOfLife).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(boardPath).append(controllerId).append(noticeId).append(endOfLife).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyNotice) == false) {
            return false;
        }
        ModifyNotice rhs = ((ModifyNotice) other);
        return new EqualsBuilder().append(boardPath, rhs.boardPath).append(controllerId, rhs.controllerId).append(noticeId, rhs.noticeId).append(endOfLife, rhs.endOfLife).isEquals();
    }

}
