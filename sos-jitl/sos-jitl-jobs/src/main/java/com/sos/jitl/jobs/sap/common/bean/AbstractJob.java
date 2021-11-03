
package com.sos.jitl.jobs.sap.common.bean;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abstract job
 * <p>
 * e.g. POST /scheduler/jobs
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "description",
    "action",
    "active",
    "httpMethod"
})
public abstract class AbstractJob {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("action")
    private String action;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    private Boolean active = false;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("httpMethod")
    private AbstractJob.HttpMethod httpMethod = AbstractJob.HttpMethod.fromValue("POST");

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public AbstractJob withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public AbstractJob withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    public AbstractJob withAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
    }

    public AbstractJob withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("httpMethod")
    public AbstractJob.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("httpMethod")
    public void setHttpMethod(AbstractJob.HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public AbstractJob withHttpMethod(AbstractJob.HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("description", description).append("action", action).append("active", active).append("httpMethod", httpMethod).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(description).append(action).append(active).append(httpMethod).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AbstractJob) == false) {
            return false;
        }
        AbstractJob rhs = ((AbstractJob) other);
        return new EqualsBuilder().append(name, rhs.name).append(description, rhs.description).append(action, rhs.action).append(active, rhs.active).append(httpMethod, rhs.httpMethod).isEquals();
    }

    public enum HttpMethod {

        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");
        private final String value;
        private final static Map<String, AbstractJob.HttpMethod> CONSTANTS = new HashMap<String, AbstractJob.HttpMethod>();

        static {
            for (AbstractJob.HttpMethod c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private HttpMethod(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static AbstractJob.HttpMethod fromValue(String value) {
            AbstractJob.HttpMethod constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
