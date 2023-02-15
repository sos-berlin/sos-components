
package com.sos.inventory.model.descriptor.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor Installation Schema
 * <p>
 * JS7 Deployment Descriptor Installation Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "home",
    "data",
    "homeOwner",
    "dataOwner",
    "runUser",
    "httpPort",
    "httpsPort",
    "javaHome",
    "javaOptions"
})
public class Installation {

    @JsonProperty("home")
    private String home;
    @JsonProperty("data")
    private String data;
    @JsonProperty("homeOwner")
    private String homeOwner;
    @JsonProperty("dataOwner")
    private String dataOwner;
    @JsonProperty("runUser")
    private String runUser;
    @JsonProperty("httpPort")
    private String httpPort;
    @JsonProperty("httpsPort")
    private String httpsPort;
    @JsonProperty("javaHome")
    private String javaHome;
    @JsonProperty("javaOptions")
    private String javaOptions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Installation() {
    }

    /**
     * 
     * @param data
     * @param homeOwner
     * @param runUser
     * @param httpPort
     * @param httpsPort
     * @param dataOwner
     * @param javaOptions
     * @param home
     * @param javaHome
     */
    public Installation(String home, String data, String homeOwner, String dataOwner, String runUser, String httpPort, String httpsPort, String javaHome, String javaOptions) {
        super();
        this.home = home;
        this.data = data;
        this.homeOwner = homeOwner;
        this.dataOwner = dataOwner;
        this.runUser = runUser;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.javaHome = javaHome;
        this.javaOptions = javaOptions;
    }

    @JsonProperty("home")
    public String getHome() {
        return home;
    }

    @JsonProperty("home")
    public void setHome(String home) {
        this.home = home;
    }

    @JsonProperty("data")
    public String getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(String data) {
        this.data = data;
    }

    @JsonProperty("homeOwner")
    public String getHomeOwner() {
        return homeOwner;
    }

    @JsonProperty("homeOwner")
    public void setHomeOwner(String homeOwner) {
        this.homeOwner = homeOwner;
    }

    @JsonProperty("dataOwner")
    public String getDataOwner() {
        return dataOwner;
    }

    @JsonProperty("dataOwner")
    public void setDataOwner(String dataOwner) {
        this.dataOwner = dataOwner;
    }

    @JsonProperty("runUser")
    public String getRunUser() {
        return runUser;
    }

    @JsonProperty("runUser")
    public void setRunUser(String runUser) {
        this.runUser = runUser;
    }

    @JsonProperty("httpPort")
    public String getHttpPort() {
        return httpPort;
    }

    @JsonProperty("httpPort")
    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    @JsonProperty("httpsPort")
    public String getHttpsPort() {
        return httpsPort;
    }

    @JsonProperty("httpsPort")
    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    @JsonProperty("javaHome")
    public String getJavaHome() {
        return javaHome;
    }

    @JsonProperty("javaHome")
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    @JsonProperty("javaOptions")
    public String getJavaOptions() {
        return javaOptions;
    }

    @JsonProperty("javaOptions")
    public void setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("home", home).append("data", data).append("homeOwner", homeOwner).append("dataOwner", dataOwner).append("runUser", runUser).append("httpPort", httpPort).append("httpsPort", httpsPort).append("javaHome", javaHome).append("javaOptions", javaOptions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).append(homeOwner).append(runUser).append(httpPort).append(httpsPort).append(dataOwner).append(javaOptions).append(home).append(javaHome).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Installation) == false) {
            return false;
        }
        Installation rhs = ((Installation) other);
        return new EqualsBuilder().append(data, rhs.data).append(homeOwner, rhs.homeOwner).append(runUser, rhs.runUser).append(httpPort, rhs.httpPort).append(httpsPort, rhs.httpsPort).append(dataOwner, rhs.dataOwner).append(javaOptions, rhs.javaOptions).append(home, rhs.home).append(javaHome, rhs.javaHome).isEquals();
    }

}
