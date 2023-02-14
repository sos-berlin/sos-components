
package com.sos.inventory.model.descriptor.joc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sos.inventory.model.descriptor.common.Installation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor Installation Schema
 * <p>
 * JS7 JOC Descriptor Installation Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "setupDir",
    "title",
    "securityLevel",
    "dbmsConfig",
    "dbmsDriver",
    "dbmsInit",
    "isUser",
    "isPreserveEnv"
})
public class JocInstallation
    extends Installation
{

    @JsonProperty("setupDir")
    private String setupDir;
    @JsonProperty("title")
    private String title;
    @JsonProperty("securityLevel")
    private JocInstallation.SecurityLevel securityLevel;
    @JsonProperty("dbmsConfig")
    private String dbmsConfig;
    @JsonProperty("dbmsDriver")
    private String dbmsDriver;
    @JsonProperty("dbmsInit")
    private JocInstallation.DbmsInit dbmsInit;
    @JsonProperty("isUser")
    private Boolean isUser;
    @JsonProperty("isPreserveEnv")
    private Boolean isPreserveEnv;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JocInstallation() {
    }

    /**
     * 
     * @param setupDir
     * @param data
     * @param httpPort
     * @param title
     * @param httpsPort
     * @param home
     * @param javaHome
     * @param securityLevel
     * @param dbmsConfig
     * @param homeOwner
     * @param runUser
     * @param dbmsInit
     * @param dbmsDriver
     * @param isUser
     * @param isPreserveEnv
     * @param dataOwner
     * @param javaOptions
     */
    public JocInstallation(String setupDir, String title, JocInstallation.SecurityLevel securityLevel, String dbmsConfig, String dbmsDriver, JocInstallation.DbmsInit dbmsInit, Boolean isUser, Boolean isPreserveEnv, String home, String data, String homeOwner, String dataOwner, String runUser, String httpPort, String httpsPort, String javaHome, String javaOptions) {
        super(home, data, homeOwner, dataOwner, runUser, httpPort, httpsPort, javaHome, javaOptions);
        this.setupDir = setupDir;
        this.title = title;
        this.securityLevel = securityLevel;
        this.dbmsConfig = dbmsConfig;
        this.dbmsDriver = dbmsDriver;
        this.dbmsInit = dbmsInit;
        this.isUser = isUser;
        this.isPreserveEnv = isPreserveEnv;
    }

    @JsonProperty("setupDir")
    public String getSetupDir() {
        return setupDir;
    }

    @JsonProperty("setupDir")
    public void setSetupDir(String setupDir) {
        this.setupDir = setupDir;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("securityLevel")
    public JocInstallation.SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    @JsonProperty("securityLevel")
    public void setSecurityLevel(JocInstallation.SecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    @JsonProperty("dbmsConfig")
    public String getDbmsConfig() {
        return dbmsConfig;
    }

    @JsonProperty("dbmsConfig")
    public void setDbmsConfig(String dbmsConfig) {
        this.dbmsConfig = dbmsConfig;
    }

    @JsonProperty("dbmsDriver")
    public String getDbmsDriver() {
        return dbmsDriver;
    }

    @JsonProperty("dbmsDriver")
    public void setDbmsDriver(String dbmsDriver) {
        this.dbmsDriver = dbmsDriver;
    }

    @JsonProperty("dbmsInit")
    public JocInstallation.DbmsInit getDbmsInit() {
        return dbmsInit;
    }

    @JsonProperty("dbmsInit")
    public void setDbmsInit(JocInstallation.DbmsInit dbmsInit) {
        this.dbmsInit = dbmsInit;
    }

    @JsonProperty("isUser")
    public Boolean getIsUser() {
        return isUser;
    }

    @JsonProperty("isUser")
    public void setIsUser(Boolean isUser) {
        this.isUser = isUser;
    }

    @JsonProperty("isPreserveEnv")
    public Boolean getIsPreserveEnv() {
        return isPreserveEnv;
    }

    @JsonProperty("isPreserveEnv")
    public void setIsPreserveEnv(Boolean isPreserveEnv) {
        this.isPreserveEnv = isPreserveEnv;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("setupDir", setupDir).append("title", title).append("securityLevel", securityLevel).append("dbmsConfig", dbmsConfig).append("dbmsDriver", dbmsDriver).append("dbmsInit", dbmsInit).append("isUser", isUser).append("isPreserveEnv", isPreserveEnv).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(securityLevel).append(setupDir).append(dbmsConfig).append(dbmsInit).append(title).append(dbmsDriver).append(isUser).append(isPreserveEnv).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocInstallation) == false) {
            return false;
        }
        JocInstallation rhs = ((JocInstallation) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(securityLevel, rhs.securityLevel).append(setupDir, rhs.setupDir).append(dbmsConfig, rhs.dbmsConfig).append(dbmsInit, rhs.dbmsInit).append(title, rhs.title).append(dbmsDriver, rhs.dbmsDriver).append(isUser, rhs.isUser).append(isPreserveEnv, rhs.isPreserveEnv).isEquals();
    }

    public enum DbmsInit {

        BY_INSTALLER("byInstaller"),
        BY_JOC("byJoc"),
        OFF("off");
        private final String value;
        private final static Map<String, JocInstallation.DbmsInit> CONSTANTS = new HashMap<String, JocInstallation.DbmsInit>();

        static {
            for (JocInstallation.DbmsInit c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private DbmsInit(String value) {
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
        public static JocInstallation.DbmsInit fromValue(String value) {
            JocInstallation.DbmsInit constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum SecurityLevel {

        LOW("low"),
        MEDIUM("medium"),
        HIGH("high");
        private final String value;
        private final static Map<String, JocInstallation.SecurityLevel> CONSTANTS = new HashMap<String, JocInstallation.SecurityLevel>();

        static {
            for (JocInstallation.SecurityLevel c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private SecurityLevel(String value) {
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
        public static JocInstallation.SecurityLevel fromValue(String value) {
            JocInstallation.SecurityLevel constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
