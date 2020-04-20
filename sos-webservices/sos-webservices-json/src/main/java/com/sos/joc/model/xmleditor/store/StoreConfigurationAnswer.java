
package com.sos.joc.model.xmleditor.store;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.AnswerMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor store configuration answer
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "modified",
    "message"
})
public class StoreConfigurationAnswer {

    @JsonProperty("id")
    private Integer id;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;
    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private AnswerMessage message;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public AnswerMessage getMessage() {
        return message;
    }

    /**
     * xmleditor answer message
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(AnswerMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("modified", modified).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modified).append(id).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreConfigurationAnswer) == false) {
            return false;
        }
        StoreConfigurationAnswer rhs = ((StoreConfigurationAnswer) other);
        return new EqualsBuilder().append(modified, rhs.modified).append(id, rhs.id).append(message, rhs.message).isEquals();
    }

}
