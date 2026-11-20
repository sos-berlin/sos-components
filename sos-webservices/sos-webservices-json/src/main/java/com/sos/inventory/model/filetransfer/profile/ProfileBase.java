
package com.sos.inventory.model.filetransfer.profile;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Profile
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "alternativeProfileName",
    "operation"
})
public class ProfileBase {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeProfileName")
    private String alternativeProfileName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operation")
    private ProfileBase.Operation operation;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProfileBase() {
    }

    /**
     * 
     * @param alternativeProfileName
     * @param operation
     */
    public ProfileBase(String alternativeProfileName, ProfileBase.Operation operation) {
        super();
        this.alternativeProfileName = alternativeProfileName;
        this.operation = operation;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeProfileName")
    public String getAlternativeProfileName() {
        return alternativeProfileName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeProfileName")
    public void setAlternativeProfileName(String alternativeProfileName) {
        this.alternativeProfileName = alternativeProfileName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operation")
    public ProfileBase.Operation getOperation() {
        return operation;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operation")
    public void setOperation(ProfileBase.Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("alternativeProfileName", alternativeProfileName).append("operation", operation).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(alternativeProfileName).append(operation).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProfileBase) == false) {
            return false;
        }
        ProfileBase rhs = ((ProfileBase) other);
        return new EqualsBuilder().append(alternativeProfileName, rhs.alternativeProfileName).append(operation, rhs.operation).isEquals();
    }

    public enum Operation {

        COPY("COPY"),
        MOVE("MOVE"),
        REMOVE("REMOVE"),
        GETLIST("GETLIST");
        private final String value;
        private final static Map<String, ProfileBase.Operation> CONSTANTS = new HashMap<String, ProfileBase.Operation>();

        static {
            for (ProfileBase.Operation c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Operation(String value) {
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
        public static ProfileBase.Operation fromValue(String value) {
            ProfileBase.Operation constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
