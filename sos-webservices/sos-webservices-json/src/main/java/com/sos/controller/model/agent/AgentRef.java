
package com.sos.controller.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "director",
    "standbyDirector"
})
public class AgentRef {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    private String director;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("standbyDirector")
    private String standbyDirector;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentRef() {
    }

    /**
     * 
     * @param standbyDirector
     * @param director
     * @param id
     */
    public AgentRef(String id, String director, String standbyDirector) {
        super();
        this.id = id;
        this.director = director;
        this.standbyDirector = standbyDirector;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    public String getDirector() {
        return director;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    public void setDirector(String director) {
        this.director = director;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("standbyDirector")
    public String getStandbyDirector() {
        return standbyDirector;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("standbyDirector")
    public void setStandbyDirector(String standbyDirector) {
        this.standbyDirector = standbyDirector;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("director", director).append("standbyDirector", standbyDirector).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(standbyDirector).append(director).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentRef) == false) {
            return false;
        }
        AgentRef rhs = ((AgentRef) other);
        return new EqualsBuilder().append(id, rhs.id).append(standbyDirector, rhs.standbyDirector).append(director, rhs.director).isEquals();
    }

}
