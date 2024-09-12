
package com.sos.joc.model.inventory.changes.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * time span to restrict the time period
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "from",
    "to"
})
public class Timespan {

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * (Required)
     * 
     */
    @JsonProperty("from")
    @JsonPropertyDescription("ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty")
    private String from;
    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty")
    private String to;

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * (Required)
     * 
     */
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * (Required)
     * 
     */
    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("to")
    public String getTo() {
        return to;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("to")
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("from", from).append("to", to).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(from).append(to).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Timespan) == false) {
            return false;
        }
        Timespan rhs = ((Timespan) other);
        return new EqualsBuilder().append(from, rhs.from).append(to, rhs.to).isEquals();
    }

}
