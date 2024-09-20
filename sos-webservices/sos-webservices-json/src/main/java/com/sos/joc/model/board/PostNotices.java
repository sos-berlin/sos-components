
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
    "noticeBoardPaths",
    "noticeId"
})
public class PostNotices
    extends ModifyNotices
{

    @JsonProperty("noticeBoardPaths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> noticeBoardPaths = new LinkedHashSet<String>();
    @JsonProperty("noticeId")
    private String noticeId;

    @JsonProperty("noticeBoardPaths")
    public Set<String> getNoticeBoardPaths() {
        return noticeBoardPaths;
    }

    @JsonProperty("noticeBoardPaths")
    public void setNoticeBoardPaths(Set<String> noticeBoardPaths) {
        this.noticeBoardPaths = noticeBoardPaths;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardPaths", noticeBoardPaths).append("noticeId", noticeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeBoardPaths).append(noticeId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PostNotices) == false) {
            return false;
        }
        PostNotices rhs = ((PostNotices) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardPaths, rhs.noticeBoardPaths).append(noticeId, rhs.noticeId).isEquals();
    }

}
