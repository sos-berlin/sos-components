
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * java executable
 * <p>
 * executable with fixed property 'TYPE':'Java'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "javaClass"
})
public class ExecutableJava
    extends Executable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("javaClass")
    private String javaClass;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExecutableJava() {
    }

    /**
     * 
     * @param javaClass
     * @param tYPE
     */
    public ExecutableJava(String javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("javaClass")
    public String getJavaClass() {
        return javaClass;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("javaClass")
    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("javaClass", javaClass).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(javaClass).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExecutableJava) == false) {
            return false;
        }
        ExecutableJava rhs = ((ExecutableJava) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(javaClass, rhs.javaClass).isEquals();
    }

}
