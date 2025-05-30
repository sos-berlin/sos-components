
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * ExpectNotices
 * <p>
 * instruction with fixed property 'TYPE':'ExpectNotices'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "noticeBoardNames",
    "whenNotAnnounced"
})
public class ExpectNotices
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardNames")
    @JsonAlias({
        "boardPaths"
    })
    private String noticeBoardNames;
    /**
     * WhenNotAnnouced Enum
     * <p>
     * 
     * 
     */
    @JsonProperty("whenNotAnnounced")
    private WhenNotAnnouced whenNotAnnounced = WhenNotAnnouced.fromValue("Wait");

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExpectNotices() {
    }

    /**
     * 
     * @param noticeBoardNames
     */
    public ExpectNotices(String noticeBoardNames) {
        super();
        this.noticeBoardNames = noticeBoardNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardNames")
    public String getNoticeBoardNames() {
        return noticeBoardNames;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("noticeBoardNames")
    public void setNoticeBoardNames(String noticeBoardNames) {
        this.noticeBoardNames = noticeBoardNames;
    }

    /**
     * WhenNotAnnouced Enum
     * <p>
     * 
     * 
     */
    @JsonProperty("whenNotAnnounced")
    public WhenNotAnnouced getWhenNotAnnounced() {
        return whenNotAnnounced;
    }

    /**
     * WhenNotAnnouced Enum
     * <p>
     * 
     * 
     */
    @JsonProperty("whenNotAnnounced")
    public void setWhenNotAnnounced(WhenNotAnnouced whenNotAnnounced) {
        this.whenNotAnnounced = whenNotAnnounced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("noticeBoardNames", noticeBoardNames).append("whenNotAnnounced", whenNotAnnounced).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(noticeBoardNames).append(whenNotAnnounced).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExpectNotices) == false) {
            return false;
        }
        ExpectNotices rhs = ((ExpectNotices) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(noticeBoardNames, rhs.noticeBoardNames).append(whenNotAnnounced, rhs.whenNotAnnounced).isEquals();
    }

}
