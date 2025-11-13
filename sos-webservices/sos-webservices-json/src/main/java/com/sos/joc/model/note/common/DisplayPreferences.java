
package com.sos.joc.model.note.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * DisplayPreferences
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "width",
    "height"
})
public class DisplayPreferences {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("width")
    private Integer width;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("height")
    private Integer height;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("width")
    public Integer getWidth() {
        return width;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("width")
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("height")
    public Integer getHeight() {
        return height;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("height")
    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("width", width).append("height", height).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(width).append(height).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DisplayPreferences) == false) {
            return false;
        }
        DisplayPreferences rhs = ((DisplayPreferences) other);
        return new EqualsBuilder().append(width, rhs.width).append(height, rhs.height).isEquals();
    }

}
