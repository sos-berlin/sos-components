
package com.sos.joc.model.inventory.fileordersource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.sign.model.fileordersource.FileOrderSource;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FileOrderSource configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class FileOrderSourcePublish
    extends ControllerObject
{

    /**
     * FileOrderSource
     * <p>
     * deploy object with fixed property 'TYPE':'FileWatch'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'FileWatch'")
    private FileOrderSource content;

    /**
     * FileOrderSource
     * <p>
     * deploy object with fixed property 'TYPE':'FileWatch'
     * 
     */
    @JsonProperty("content")
    public FileOrderSource getContent() {
        return content;
    }

    /**
     * FileOrderSource
     * <p>
     * deploy object with fixed property 'TYPE':'FileWatch'
     * 
     */
    @JsonProperty("content")
    public void setContent(FileOrderSource content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("content", content).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(content).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileOrderSourcePublish) == false) {
            return false;
        }
        FileOrderSourcePublish rhs = ((FileOrderSourcePublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
