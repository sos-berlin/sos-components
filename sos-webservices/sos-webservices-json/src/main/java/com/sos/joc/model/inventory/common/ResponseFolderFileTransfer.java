
package com.sos.joc.model.inventory.common;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "credentialStoreFragments",
    "decryptionFragments",
    "mailFragments",
    "httpProxyFragments",
    "socksProxyFragments",
    "azureBlobStorageFragments",
    "ftpFragments",
    "httpFragments",
    "jumpFragments",
    "sftpFragments",
    "smbFragments",
    "webdavFragments",
    "profileJobResources"
})
public class ResponseFolderFileTransfer {

    @JsonProperty("credentialStoreFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> credentialStoreFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("decryptionFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> decryptionFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("mailFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> mailFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("httpProxyFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> httpProxyFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("socksProxyFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> socksProxyFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("azureBlobStorageFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> azureBlobStorageFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("ftpFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> ftpFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("httpFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> httpFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("jumpFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> jumpFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("sftpFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> sftpFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("smbFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> smbFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("webdavFragments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> webdavFragments = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("profileJobResources")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> profileJobResources = new LinkedHashSet<ResponseFolderItem>();

    @JsonProperty("credentialStoreFragments")
    public Set<ResponseFolderItem> getCredentialStoreFragments() {
        return credentialStoreFragments;
    }

    @JsonProperty("credentialStoreFragments")
    public void setCredentialStoreFragments(Set<ResponseFolderItem> credentialStoreFragments) {
        this.credentialStoreFragments = credentialStoreFragments;
    }

    @JsonProperty("decryptionFragments")
    public Set<ResponseFolderItem> getDecryptionFragments() {
        return decryptionFragments;
    }

    @JsonProperty("decryptionFragments")
    public void setDecryptionFragments(Set<ResponseFolderItem> decryptionFragments) {
        this.decryptionFragments = decryptionFragments;
    }

    @JsonProperty("mailFragments")
    public Set<ResponseFolderItem> getMailFragments() {
        return mailFragments;
    }

    @JsonProperty("mailFragments")
    public void setMailFragments(Set<ResponseFolderItem> mailFragments) {
        this.mailFragments = mailFragments;
    }

    @JsonProperty("httpProxyFragments")
    public Set<ResponseFolderItem> getHttpProxyFragments() {
        return httpProxyFragments;
    }

    @JsonProperty("httpProxyFragments")
    public void setHttpProxyFragments(Set<ResponseFolderItem> httpProxyFragments) {
        this.httpProxyFragments = httpProxyFragments;
    }

    @JsonProperty("socksProxyFragments")
    public Set<ResponseFolderItem> getSocksProxyFragments() {
        return socksProxyFragments;
    }

    @JsonProperty("socksProxyFragments")
    public void setSocksProxyFragments(Set<ResponseFolderItem> socksProxyFragments) {
        this.socksProxyFragments = socksProxyFragments;
    }

    @JsonProperty("azureBlobStorageFragments")
    public Set<ResponseFolderItem> getAzureBlobStorageFragments() {
        return azureBlobStorageFragments;
    }

    @JsonProperty("azureBlobStorageFragments")
    public void setAzureBlobStorageFragments(Set<ResponseFolderItem> azureBlobStorageFragments) {
        this.azureBlobStorageFragments = azureBlobStorageFragments;
    }

    @JsonProperty("ftpFragments")
    public Set<ResponseFolderItem> getFtpFragments() {
        return ftpFragments;
    }

    @JsonProperty("ftpFragments")
    public void setFtpFragments(Set<ResponseFolderItem> ftpFragments) {
        this.ftpFragments = ftpFragments;
    }

    @JsonProperty("httpFragments")
    public Set<ResponseFolderItem> getHttpFragments() {
        return httpFragments;
    }

    @JsonProperty("httpFragments")
    public void setHttpFragments(Set<ResponseFolderItem> httpFragments) {
        this.httpFragments = httpFragments;
    }

    @JsonProperty("jumpFragments")
    public Set<ResponseFolderItem> getJumpFragments() {
        return jumpFragments;
    }

    @JsonProperty("jumpFragments")
    public void setJumpFragments(Set<ResponseFolderItem> jumpFragments) {
        this.jumpFragments = jumpFragments;
    }

    @JsonProperty("sftpFragments")
    public Set<ResponseFolderItem> getSftpFragments() {
        return sftpFragments;
    }

    @JsonProperty("sftpFragments")
    public void setSftpFragments(Set<ResponseFolderItem> sftpFragments) {
        this.sftpFragments = sftpFragments;
    }

    @JsonProperty("smbFragments")
    public Set<ResponseFolderItem> getSmbFragments() {
        return smbFragments;
    }

    @JsonProperty("smbFragments")
    public void setSmbFragments(Set<ResponseFolderItem> smbFragments) {
        this.smbFragments = smbFragments;
    }

    @JsonProperty("webdavFragments")
    public Set<ResponseFolderItem> getWebdavFragments() {
        return webdavFragments;
    }

    @JsonProperty("webdavFragments")
    public void setWebdavFragments(Set<ResponseFolderItem> webdavFragments) {
        this.webdavFragments = webdavFragments;
    }

    @JsonProperty("profileJobResources")
    public Set<ResponseFolderItem> getProfileJobResources() {
        return profileJobResources;
    }

    @JsonProperty("profileJobResources")
    public void setProfileJobResources(Set<ResponseFolderItem> profileJobResources) {
        this.profileJobResources = profileJobResources;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentialStoreFragments", credentialStoreFragments).append("decryptionFragments", decryptionFragments).append("mailFragments", mailFragments).append("httpProxyFragments", httpProxyFragments).append("socksProxyFragments", socksProxyFragments).append("azureBlobStorageFragments", azureBlobStorageFragments).append("ftpFragments", ftpFragments).append("httpFragments", httpFragments).append("jumpFragments", jumpFragments).append("sftpFragments", sftpFragments).append("smbFragments", smbFragments).append("webdavFragments", webdavFragments).append("profileJobResources", profileJobResources).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mailFragments).append(azureBlobStorageFragments).append(sftpFragments).append(webdavFragments).append(httpProxyFragments).append(jumpFragments).append(socksProxyFragments).append(smbFragments).append(credentialStoreFragments).append(ftpFragments).append(decryptionFragments).append(profileJobResources).append(httpFragments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseFolderFileTransfer) == false) {
            return false;
        }
        ResponseFolderFileTransfer rhs = ((ResponseFolderFileTransfer) other);
        return new EqualsBuilder().append(mailFragments, rhs.mailFragments).append(azureBlobStorageFragments, rhs.azureBlobStorageFragments).append(sftpFragments, rhs.sftpFragments).append(webdavFragments, rhs.webdavFragments).append(httpProxyFragments, rhs.httpProxyFragments).append(jumpFragments, rhs.jumpFragments).append(socksProxyFragments, rhs.socksProxyFragments).append(smbFragments, rhs.smbFragments).append(credentialStoreFragments, rhs.credentialStoreFragments).append(ftpFragments, rhs.ftpFragments).append(decryptionFragments, rhs.decryptionFragments).append(profileJobResources, rhs.profileJobResources).append(httpFragments, rhs.httpFragments).isEquals();
    }

}
