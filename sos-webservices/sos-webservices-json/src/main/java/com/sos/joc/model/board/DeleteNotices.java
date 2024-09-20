
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
 * delete notices
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "noticeBoardPath",
    "noticeIds"
})
public class DeleteNotices
    extends ModifyNotices
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("noticeBoardPath")
    private String noticeBoardPath;
    @JsonProperty("noticeIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> noticeIds = new LinkedHashSet<String>();

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("noticeBoardPath")
    public void setNoticeBoardPath(String noticeBoardPath) {
        this.noticeBoardPath = noticeBoardPath;
    }

    @JsonProperty("noticeIds")
    public Set<String> getNoticeIds() {
        return noticeIds;
    }

    @JsonProperty("noticeIds")
    public void setNoticeIds(Set<String> noticeIds) {
        this.noticeIds = noticeIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardPath", noticeBoardPath).append("noticeIds", noticeIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeBoardPath).append(noticeIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteNotices) == false) {
            return false;
        }
        DeleteNotices rhs = ((DeleteNotices) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardPath, rhs.noticeBoardPath).append(noticeIds, rhs.noticeIds).isEquals();
    }

}
