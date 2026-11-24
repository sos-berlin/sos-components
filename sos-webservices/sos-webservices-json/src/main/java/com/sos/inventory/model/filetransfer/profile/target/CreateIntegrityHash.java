
package com.sos.inventory.model.filetransfer.profile.target;

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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "hashAlgorithm"
})
public class CreateIntegrityHash {

    @JsonProperty("hashAlgorithm")
    private CreateIntegrityHash.HashAlgorithm hashAlgorithm;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CreateIntegrityHash() {
    }

    /**
     * 
     * @param hashAlgorithm
     */
    public CreateIntegrityHash(CreateIntegrityHash.HashAlgorithm hashAlgorithm) {
        super();
        this.hashAlgorithm = hashAlgorithm;
    }

    @JsonProperty("hashAlgorithm")
    public CreateIntegrityHash.HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    @JsonProperty("hashAlgorithm")
    public void setHashAlgorithm(CreateIntegrityHash.HashAlgorithm hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("hashAlgorithm", hashAlgorithm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hashAlgorithm).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CreateIntegrityHash) == false) {
            return false;
        }
        CreateIntegrityHash rhs = ((CreateIntegrityHash) other);
        return new EqualsBuilder().append(hashAlgorithm, rhs.hashAlgorithm).isEquals();
    }

    public enum HashAlgorithm {

        MD_5("md5");
        private final String value;
        private final static Map<String, CreateIntegrityHash.HashAlgorithm> CONSTANTS = new HashMap<String, CreateIntegrityHash.HashAlgorithm>();

        static {
            for (CreateIntegrityHash.HashAlgorithm c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private HashAlgorithm(String value) {
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
        public static CreateIntegrityHash.HashAlgorithm fromValue(String value) {
            CreateIntegrityHash.HashAlgorithm constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
