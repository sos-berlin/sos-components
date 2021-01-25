
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.controller.ComponentState;
import com.sos.joc.model.controller.ConnectionState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dbms",
    "version",
    "componentState",
    "connectionState"
})
public class DB {

    /**
     * possible values 'SQL Server', 'MySQL', 'Oracle', 'PostgreSQL'
     * (Required)
     * 
     */
    @JsonProperty("dbms")
    @JsonPropertyDescription("possible values 'SQL Server', 'MySQL', 'Oracle', 'PostgreSQL'")
    private String dbms;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    private ComponentState componentState;
    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("connectionState")
    private ConnectionState connectionState;

    /**
     * possible values 'SQL Server', 'MySQL', 'Oracle', 'PostgreSQL'
     * (Required)
     * 
     */
    @JsonProperty("dbms")
    public String getDbms() {
        return dbms;
    }

    /**
     * possible values 'SQL Server', 'MySQL', 'Oracle', 'PostgreSQL'
     * (Required)
     * 
     */
    @JsonProperty("dbms")
    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    public ComponentState getComponentState() {
        return componentState;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("componentState")
    public void setComponentState(ComponentState componentState) {
        this.componentState = componentState;
    }

    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("connectionState")
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("connectionState")
    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dbms", dbms).append("version", version).append("componentState", componentState).append("connectionState", connectionState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(componentState).append(dbms).append(version).append(connectionState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DB) == false) {
            return false;
        }
        DB rhs = ((DB) other);
        return new EqualsBuilder().append(componentState, rhs.componentState).append(dbms, rhs.dbms).append(version, rhs.version).append(connectionState, rhs.connectionState).isEquals();
    }

}
