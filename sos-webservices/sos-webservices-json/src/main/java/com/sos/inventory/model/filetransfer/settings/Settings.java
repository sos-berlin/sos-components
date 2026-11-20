
package com.sos.inventory.model.filetransfer.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * File Transfer Job Resource additional settings
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "profileName",
    "useJumpInitialSourceTargetConnectionErrorCode",
    "credentialStoreFragments",
    "decryptionFragments",
    "mailFragments",
    "httpProxyFragments",
    "socksProxyFragments",
    "jumpFragments",
    "azureBlobStorageFragments",
    "ftpFragments",
    "httpFragments",
    "sftpFragments",
    "smbFragments",
    "webdavFragments",
    "alternativeProfiles"
})
public class Settings {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("profileName")
    private String profileName;
    @JsonProperty("useJumpInitialSourceTargetConnectionErrorCode")
    private Boolean useJumpInitialSourceTargetConnectionErrorCode;
    /**
     * Map of credential store fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragments")
    private CredentialStoreFragments credentialStoreFragments;
    /**
     * Map of decryption fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragments")
    private DecryptionFragments decryptionFragments;
    /**
     * Map of mail fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("mailFragments")
    private MailFragments mailFragments;
    /**
     * Map of HTTP proxy fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragments")
    private HttpProxyFragments httpProxyFragments;
    /**
     * Map of SOCKS proxy fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragments")
    private SocksProxyFragments socksProxyFragments;
    /**
     * Map of Jump fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragments")
    private JumpFragments jumpFragments;
    /**
     * Map of Azure Blob Stora fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("azureBlobStorageFragments")
    private AzureBlobStorageFragments azureBlobStorageFragments;
    /**
     * Map of FTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("ftpFragments")
    private FtpFragments ftpFragments;
    /**
     * Map of HTTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("httpFragments")
    private HttpFragments httpFragments;
    /**
     * Map of SFTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("sftpFragments")
    private SftpFragments sftpFragments;
    /**
     * Map of SMB fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("smbFragments")
    private SmbFragments smbFragments;
    /**
     * Map of WebDAV fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("webdavFragments")
    private WebdavFragments webdavFragments;
    /**
     * Map of alternative.
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeProfiles")
    private AlternativeProfiles alternativeProfiles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Settings() {
    }

    /**
     * 
     * @param profileName
     * @param mailFragments
     * @param azureBlobStorageFragments
     * @param sftpFragments
     * @param webdavFragments
     * @param useJumpInitialSourceTargetConnectionErrorCode
     * @param httpProxyFragments
     * @param jumpFragments
     * @param socksProxyFragments
     * @param smbFragments
     * @param credentialStoreFragments
     * @param ftpFragments
     * @param decryptionFragments
     * @param httpFragments
     * @param alternativeProfiles
     */
    public Settings(String profileName, Boolean useJumpInitialSourceTargetConnectionErrorCode, CredentialStoreFragments credentialStoreFragments, DecryptionFragments decryptionFragments, MailFragments mailFragments, HttpProxyFragments httpProxyFragments, SocksProxyFragments socksProxyFragments, JumpFragments jumpFragments, AzureBlobStorageFragments azureBlobStorageFragments, FtpFragments ftpFragments, HttpFragments httpFragments, SftpFragments sftpFragments, SmbFragments smbFragments, WebdavFragments webdavFragments, AlternativeProfiles alternativeProfiles) {
        super();
        this.profileName = profileName;
        this.useJumpInitialSourceTargetConnectionErrorCode = useJumpInitialSourceTargetConnectionErrorCode;
        this.credentialStoreFragments = credentialStoreFragments;
        this.decryptionFragments = decryptionFragments;
        this.mailFragments = mailFragments;
        this.httpProxyFragments = httpProxyFragments;
        this.socksProxyFragments = socksProxyFragments;
        this.jumpFragments = jumpFragments;
        this.azureBlobStorageFragments = azureBlobStorageFragments;
        this.ftpFragments = ftpFragments;
        this.httpFragments = httpFragments;
        this.sftpFragments = sftpFragments;
        this.smbFragments = smbFragments;
        this.webdavFragments = webdavFragments;
        this.alternativeProfiles = alternativeProfiles;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("profileName")
    public String getProfileName() {
        return profileName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("profileName")
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @JsonProperty("useJumpInitialSourceTargetConnectionErrorCode")
    public Boolean getUseJumpInitialSourceTargetConnectionErrorCode() {
        return useJumpInitialSourceTargetConnectionErrorCode;
    }

    @JsonProperty("useJumpInitialSourceTargetConnectionErrorCode")
    public void setUseJumpInitialSourceTargetConnectionErrorCode(Boolean useJumpInitialSourceTargetConnectionErrorCode) {
        this.useJumpInitialSourceTargetConnectionErrorCode = useJumpInitialSourceTargetConnectionErrorCode;
    }

    /**
     * Map of credential store fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragments")
    public CredentialStoreFragments getCredentialStoreFragments() {
        return credentialStoreFragments;
    }

    /**
     * Map of credential store fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragments")
    public void setCredentialStoreFragments(CredentialStoreFragments credentialStoreFragments) {
        this.credentialStoreFragments = credentialStoreFragments;
    }

    /**
     * Map of decryption fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragments")
    public DecryptionFragments getDecryptionFragments() {
        return decryptionFragments;
    }

    /**
     * Map of decryption fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragments")
    public void setDecryptionFragments(DecryptionFragments decryptionFragments) {
        this.decryptionFragments = decryptionFragments;
    }

    /**
     * Map of mail fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("mailFragments")
    public MailFragments getMailFragments() {
        return mailFragments;
    }

    /**
     * Map of mail fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("mailFragments")
    public void setMailFragments(MailFragments mailFragments) {
        this.mailFragments = mailFragments;
    }

    /**
     * Map of HTTP proxy fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragments")
    public HttpProxyFragments getHttpProxyFragments() {
        return httpProxyFragments;
    }

    /**
     * Map of HTTP proxy fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragments")
    public void setHttpProxyFragments(HttpProxyFragments httpProxyFragments) {
        this.httpProxyFragments = httpProxyFragments;
    }

    /**
     * Map of SOCKS proxy fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragments")
    public SocksProxyFragments getSocksProxyFragments() {
        return socksProxyFragments;
    }

    /**
     * Map of SOCKS proxy fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragments")
    public void setSocksProxyFragments(SocksProxyFragments socksProxyFragments) {
        this.socksProxyFragments = socksProxyFragments;
    }

    /**
     * Map of Jump fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragments")
    public JumpFragments getJumpFragments() {
        return jumpFragments;
    }

    /**
     * Map of Jump fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragments")
    public void setJumpFragments(JumpFragments jumpFragments) {
        this.jumpFragments = jumpFragments;
    }

    /**
     * Map of Azure Blob Stora fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("azureBlobStorageFragments")
    public AzureBlobStorageFragments getAzureBlobStorageFragments() {
        return azureBlobStorageFragments;
    }

    /**
     * Map of Azure Blob Stora fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("azureBlobStorageFragments")
    public void setAzureBlobStorageFragments(AzureBlobStorageFragments azureBlobStorageFragments) {
        this.azureBlobStorageFragments = azureBlobStorageFragments;
    }

    /**
     * Map of FTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("ftpFragments")
    public FtpFragments getFtpFragments() {
        return ftpFragments;
    }

    /**
     * Map of FTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("ftpFragments")
    public void setFtpFragments(FtpFragments ftpFragments) {
        this.ftpFragments = ftpFragments;
    }

    /**
     * Map of HTTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("httpFragments")
    public HttpFragments getHttpFragments() {
        return httpFragments;
    }

    /**
     * Map of HTTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("httpFragments")
    public void setHttpFragments(HttpFragments httpFragments) {
        this.httpFragments = httpFragments;
    }

    /**
     * Map of SFTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("sftpFragments")
    public SftpFragments getSftpFragments() {
        return sftpFragments;
    }

    /**
     * Map of SFTP fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("sftpFragments")
    public void setSftpFragments(SftpFragments sftpFragments) {
        this.sftpFragments = sftpFragments;
    }

    /**
     * Map of SMB fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("smbFragments")
    public SmbFragments getSmbFragments() {
        return smbFragments;
    }

    /**
     * Map of SMB fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("smbFragments")
    public void setSmbFragments(SmbFragments smbFragments) {
        this.smbFragments = smbFragments;
    }

    /**
     * Map of WebDAV fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("webdavFragments")
    public WebdavFragments getWebdavFragments() {
        return webdavFragments;
    }

    /**
     * Map of WebDAV fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("webdavFragments")
    public void setWebdavFragments(WebdavFragments webdavFragments) {
        this.webdavFragments = webdavFragments;
    }

    /**
     * Map of alternative.
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeProfiles")
    public AlternativeProfiles getAlternativeProfiles() {
        return alternativeProfiles;
    }

    /**
     * Map of alternative.
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeProfiles")
    public void setAlternativeProfiles(AlternativeProfiles alternativeProfiles) {
        this.alternativeProfiles = alternativeProfiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("profileName", profileName).append("useJumpInitialSourceTargetConnectionErrorCode", useJumpInitialSourceTargetConnectionErrorCode).append("credentialStoreFragments", credentialStoreFragments).append("decryptionFragments", decryptionFragments).append("mailFragments", mailFragments).append("httpProxyFragments", httpProxyFragments).append("socksProxyFragments", socksProxyFragments).append("jumpFragments", jumpFragments).append("azureBlobStorageFragments", azureBlobStorageFragments).append("ftpFragments", ftpFragments).append("httpFragments", httpFragments).append("sftpFragments", sftpFragments).append("smbFragments", smbFragments).append("webdavFragments", webdavFragments).append("alternativeProfiles", alternativeProfiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(profileName).append(mailFragments).append(azureBlobStorageFragments).append(sftpFragments).append(webdavFragments).append(useJumpInitialSourceTargetConnectionErrorCode).append(httpProxyFragments).append(jumpFragments).append(socksProxyFragments).append(smbFragments).append(credentialStoreFragments).append(ftpFragments).append(decryptionFragments).append(httpFragments).append(alternativeProfiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Settings) == false) {
            return false;
        }
        Settings rhs = ((Settings) other);
        return new EqualsBuilder().append(profileName, rhs.profileName).append(mailFragments, rhs.mailFragments).append(azureBlobStorageFragments, rhs.azureBlobStorageFragments).append(sftpFragments, rhs.sftpFragments).append(webdavFragments, rhs.webdavFragments).append(useJumpInitialSourceTargetConnectionErrorCode, rhs.useJumpInitialSourceTargetConnectionErrorCode).append(httpProxyFragments, rhs.httpProxyFragments).append(jumpFragments, rhs.jumpFragments).append(socksProxyFragments, rhs.socksProxyFragments).append(smbFragments, rhs.smbFragments).append(credentialStoreFragments, rhs.credentialStoreFragments).append(ftpFragments, rhs.ftpFragments).append(decryptionFragments, rhs.decryptionFragments).append(httpFragments, rhs.httpFragments).append(alternativeProfiles, rhs.alternativeProfiles).isEquals();
    }

}
