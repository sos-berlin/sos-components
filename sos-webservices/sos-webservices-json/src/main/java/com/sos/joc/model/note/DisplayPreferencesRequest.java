
package com.sos.joc.model.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.note.common.DisplayPreferences;
import com.sos.joc.model.note.common.NoteIdentifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * set display preferences
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "displayPreferences"
})
public class DisplayPreferencesRequest
    extends NoteIdentifier
{

    /**
     * DisplayPreferences
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("displayPreferences")
    private DisplayPreferences displayPreferences;

    /**
     * DisplayPreferences
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("displayPreferences")
    public DisplayPreferences getDisplayPreferences() {
        return displayPreferences;
    }

    /**
     * DisplayPreferences
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("displayPreferences")
    public void setDisplayPreferences(DisplayPreferences displayPreferences) {
        this.displayPreferences = displayPreferences;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("displayPreferences", displayPreferences).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(displayPreferences).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DisplayPreferencesRequest) == false) {
            return false;
        }
        DisplayPreferencesRequest rhs = ((DisplayPreferencesRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(displayPreferences, rhs.displayPreferences).isEquals();
    }

}
