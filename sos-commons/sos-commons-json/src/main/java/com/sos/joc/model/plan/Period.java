
package com.sos.joc.model.plan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * undefined for startMode=0
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "begin",
    "end",
    "repeat"
})
public class Period {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("begin")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "begin")
    private Date begin;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("end")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "end")
    private Date end;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    private Long repeat;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin")
    public Date getBegin() {
        return begin;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("begin")
    @JacksonXmlProperty(localName = "begin")
    public void setBegin(Date begin) {
        this.begin = begin;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end")
    public Date getEnd() {
        return end;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("end")
    @JacksonXmlProperty(localName = "end")
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    public Long getRepeat() {
        return repeat;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("repeat")
    @JacksonXmlProperty(localName = "repeat")
    public void setRepeat(Long repeat) {
        this.repeat = repeat;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("begin", begin).append("end", end).append("repeat", repeat).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(end).append(begin).append(repeat).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Period) == false) {
            return false;
        }
        Period rhs = ((Period) other);
        return new EqualsBuilder().append(end, rhs.end).append(begin, rhs.begin).append(repeat, rhs.repeat).isEquals();
    }

}
