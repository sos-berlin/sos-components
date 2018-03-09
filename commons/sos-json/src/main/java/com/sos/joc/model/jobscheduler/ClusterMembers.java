
package com.sos.joc.model.jobscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobScheduler cluster members
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "_type",
    "members"
})
public class ClusterMembers {

    /**
     * JobScheduler id of all cluster member
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JsonPropertyDescription("JobScheduler id of all cluster member")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)
     * (Required)
     * 
     */
    @JsonProperty("_type")
    @JsonPropertyDescription("Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)")
    @JacksonXmlProperty(localName = "_type")
    private ClusterMembers._type _type;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    @JacksonXmlProperty(localName = "member")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "members")
    private List<ClusterMember> members = new ArrayList<ClusterMember>();

    /**
     * JobScheduler id of all cluster member
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * JobScheduler id of all cluster member
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)
     * (Required)
     * 
     */
    @JsonProperty("_type")
    @JacksonXmlProperty(localName = "_type")
    public ClusterMembers._type get_type() {
        return _type;
    }

    /**
     * Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)
     * (Required)
     * 
     */
    @JsonProperty("_type")
    @JacksonXmlProperty(localName = "_type")
    public void set_type(ClusterMembers._type _type) {
        this._type = _type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    @JacksonXmlProperty(localName = "member")
    public List<ClusterMember> getMembers() {
        return members;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    @JacksonXmlProperty(localName = "member")
    public void setMembers(List<ClusterMember> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("_type", _type).append("members", members).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_type).append(jobschedulerId).append(members).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterMembers) == false) {
            return false;
        }
        ClusterMembers rhs = ((ClusterMembers) other);
        return new EqualsBuilder().append(_type, rhs._type).append(jobschedulerId, rhs.jobschedulerId).append(members, rhs.members).isEquals();
    }

    public enum _type {

        STANDALONE("standalone"),
        ACTIVE("active"),
        PASSIVE("passive");
        private final String value;
        private final static Map<String, ClusterMembers._type> CONSTANTS = new HashMap<String, ClusterMembers._type>();

        static {
            for (ClusterMembers._type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private _type(String value) {
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
        public static ClusterMembers._type fromValue(String value) {
            ClusterMembers._type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
