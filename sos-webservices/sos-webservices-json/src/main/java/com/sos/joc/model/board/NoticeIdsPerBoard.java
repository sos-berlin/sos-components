
package com.sos.joc.model.board;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * post notices
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "noticeBoardPath",
    "noticeIds"
})
public class NoticeIdsPerBoard {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    private String noticeBoardPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> noticeIds = new LinkedHashSet<String>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    public String getNoticeBoardPath() {
        return noticeBoardPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardPath")
    public void setNoticeBoardPath(String noticeBoardPath) {
        this.noticeBoardPath = noticeBoardPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeIds")
    public Set<String> getNoticeIds() {
        return noticeIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeIds")
    public void setNoticeIds(Set<String> noticeIds) {
        this.noticeIds = noticeIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("noticeBoardPath", noticeBoardPath).append("noticeIds", noticeIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(noticeBoardPath).append(noticeIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NoticeIdsPerBoard) == false) {
            return false;
        }
        NoticeIdsPerBoard rhs = ((NoticeIdsPerBoard) other);
        return new EqualsBuilder().append(noticeBoardPath, rhs.noticeBoardPath).append(noticeIds, rhs.noticeIds).isEquals();
    }

}
