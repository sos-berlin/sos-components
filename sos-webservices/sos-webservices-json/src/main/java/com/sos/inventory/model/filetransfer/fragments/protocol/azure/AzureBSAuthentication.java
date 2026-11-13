
package com.sos.inventory.model.filetransfer.fragments.protocol.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Azure Blob Storage authentication. Provide either 'sharedKey' or 'sasToken'.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "accountName",
    "containerName",
    "apiVersion",
    "sharedKey",
    "sasToken"
})
public class AzureBSAuthentication {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("containerName")
    private String containerName;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("apiVersion")
    private String apiVersion;
    /**
     * Shared key authentication.
     * 
     */
    @JsonProperty("sharedKey")
    @JsonPropertyDescription("Shared key authentication.")
    private AzureBSAuthSharedKey sharedKey;
    /**
     * SAS token authentication.
     * 
     */
    @JsonProperty("sasToken")
    @JsonPropertyDescription("SAS token authentication.")
    private AzureBSAuthSasToken sasToken;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AzureBSAuthentication() {
    }

    /**
     * 
     * @param sharedKey
     * @param sasToken
     * @param apiVersion
     * @param accountName
     * @param containerName
     */
    public AzureBSAuthentication(String accountName, String containerName, String apiVersion, AzureBSAuthSharedKey sharedKey, AzureBSAuthSasToken sasToken) {
        super();
        this.accountName = accountName;
        this.containerName = containerName;
        this.apiVersion = apiVersion;
        this.sharedKey = sharedKey;
        this.sasToken = sasToken;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("containerName")
    public String getContainerName() {
        return containerName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("containerName")
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Shared key authentication.
     * 
     */
    @JsonProperty("sharedKey")
    public AzureBSAuthSharedKey getSharedKey() {
        return sharedKey;
    }

    /**
     * Shared key authentication.
     * 
     */
    @JsonProperty("sharedKey")
    public void setSharedKey(AzureBSAuthSharedKey sharedKey) {
        this.sharedKey = sharedKey;
    }

    /**
     * SAS token authentication.
     * 
     */
    @JsonProperty("sasToken")
    public AzureBSAuthSasToken getSasToken() {
        return sasToken;
    }

    /**
     * SAS token authentication.
     * 
     */
    @JsonProperty("sasToken")
    public void setSasToken(AzureBSAuthSasToken sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountName", accountName).append("containerName", containerName).append("apiVersion", apiVersion).append("sharedKey", sharedKey).append("sasToken", sasToken).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sharedKey).append(sasToken).append(apiVersion).append(accountName).append(containerName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AzureBSAuthentication) == false) {
            return false;
        }
        AzureBSAuthentication rhs = ((AzureBSAuthentication) other);
        return new EqualsBuilder().append(sharedKey, rhs.sharedKey).append(sasToken, rhs.sasToken).append(apiVersion, rhs.apiVersion).append(accountName, rhs.accountName).append(containerName, rhs.containerName).isEquals();
    }

}
