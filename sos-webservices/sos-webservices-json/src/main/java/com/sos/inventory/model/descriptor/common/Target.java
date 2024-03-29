
package com.sos.inventory.model.descriptor.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Deployment Descriptor Target Schema
 * <p>
 * JS7 Deployment Descriptor Target Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "connection",
    "authentication",
    "packageLocation",
    "execPre",
    "execPost",
    "makeService",
    "forceSudo",
    "serviceName",
    "serviceDir"
})
public class Target {

    /**
     * Deployment Descriptor Connection Schema
     * <p>
     * JS7 Deployment Descriptor Connection Schema
     * (Required)
     * 
     */
    @JsonProperty("connection")
    @JsonPropertyDescription("JS7 Deployment Descriptor Connection Schema")
    private Connection connection;
    /**
     * Deployment Descriptor Authentication Schema
     * <p>
     * JS7 Deployment Descriptor Authentication Schema
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    @JsonPropertyDescription("JS7 Deployment Descriptor Authentication Schema")
    private Authentication authentication;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("packageLocation")
    private String packageLocation;
    @JsonProperty("execPre")
    private String execPre;
    @JsonProperty("execPost")
    private String execPost;
    @JsonProperty("makeService")
    private Boolean makeService;
    @JsonProperty("forceSudo")
    private Boolean forceSudo;
    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("serviceDir")
    private String serviceDir;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Target() {
    }

    /**
     * 
     * @param execPost
     * @param makeService
     * @param serviceDir
     * @param connection
     * @param packageLocation
     * @param execPre
     * @param forceSudo
     * @param serviceName
     * @param authentication
     */
    public Target(Connection connection, Authentication authentication, String packageLocation, String execPre, String execPost, Boolean makeService, Boolean forceSudo, String serviceName, String serviceDir) {
        super();
        this.connection = connection;
        this.authentication = authentication;
        this.packageLocation = packageLocation;
        this.execPre = execPre;
        this.execPost = execPost;
        this.makeService = makeService;
        this.forceSudo = forceSudo;
        this.serviceName = serviceName;
        this.serviceDir = serviceDir;
    }

    /**
     * Deployment Descriptor Connection Schema
     * <p>
     * JS7 Deployment Descriptor Connection Schema
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public Connection getConnection() {
        return connection;
    }

    /**
     * Deployment Descriptor Connection Schema
     * <p>
     * JS7 Deployment Descriptor Connection Schema
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Deployment Descriptor Authentication Schema
     * <p>
     * JS7 Deployment Descriptor Authentication Schema
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Deployment Descriptor Authentication Schema
     * <p>
     * JS7 Deployment Descriptor Authentication Schema
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("packageLocation")
    public String getPackageLocation() {
        return packageLocation;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("packageLocation")
    public void setPackageLocation(String packageLocation) {
        this.packageLocation = packageLocation;
    }

    @JsonProperty("execPre")
    public String getExecPre() {
        return execPre;
    }

    @JsonProperty("execPre")
    public void setExecPre(String execPre) {
        this.execPre = execPre;
    }

    @JsonProperty("execPost")
    public String getExecPost() {
        return execPost;
    }

    @JsonProperty("execPost")
    public void setExecPost(String execPost) {
        this.execPost = execPost;
    }

    @JsonProperty("makeService")
    public Boolean getMakeService() {
        return makeService;
    }

    @JsonProperty("makeService")
    public void setMakeService(Boolean makeService) {
        this.makeService = makeService;
    }

    @JsonProperty("forceSudo")
    public Boolean getForceSudo() {
        return forceSudo;
    }

    @JsonProperty("forceSudo")
    public void setForceSudo(Boolean forceSudo) {
        this.forceSudo = forceSudo;
    }

    @JsonProperty("serviceName")
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty("serviceName")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty("serviceDir")
    public String getServiceDir() {
        return serviceDir;
    }

    @JsonProperty("serviceDir")
    public void setServiceDir(String serviceDir) {
        this.serviceDir = serviceDir;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("connection", connection).append("authentication", authentication).append("packageLocation", packageLocation).append("execPre", execPre).append("execPost", execPost).append("makeService", makeService).append("forceSudo", forceSudo).append("serviceName", serviceName).append("serviceDir", serviceDir).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(execPost).append(makeService).append(serviceDir).append(connection).append(packageLocation).append(execPre).append(forceSudo).append(serviceName).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Target) == false) {
            return false;
        }
        Target rhs = ((Target) other);
        return new EqualsBuilder().append(execPost, rhs.execPost).append(makeService, rhs.makeService).append(serviceDir, rhs.serviceDir).append(connection, rhs.connection).append(packageLocation, rhs.packageLocation).append(execPre, rhs.execPre).append(forceSudo, rhs.forceSudo).append(serviceName, rhs.serviceName).append(authentication, rhs.authentication).isEquals();
    }

}
