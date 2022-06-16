
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * PostNotice
 * <p>
 * instruction with fixed property 'TYPE':'PostNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardNames"
})
public class PostNotices
    extends Instruction
{

    @JsonProperty("noticeBoardNames")
    @JsonAlias({
        "boardPaths"
    })
    private List<String> noticeBoardNames = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PostNotices() {
    }

    /**
     * 
     * @param noticeBoardNames
     */
    public PostNotices(List<String> noticeBoardNames) {
        super();
        this.noticeBoardNames = noticeBoardNames;
    }

    @JsonProperty("noticeBoardNames")
    public List<String> getNoticeBoardNames() {
        return noticeBoardNames;
    }

    @JsonProperty("noticeBoardNames")
    public void setNoticeBoardNames(List<String> noticeBoardNames) {
        this.noticeBoardNames = noticeBoardNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardNames", noticeBoardNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeBoardNames).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardNames, rhs.noticeBoardNames).isEquals();
    }

}
