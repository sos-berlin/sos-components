
package com.sos.inventory.model.filetransfer.profile.fragments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Fragment Reference
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "azureBlobStorage",
    "ftp",
    "http",
    "local",
    "sftp",
    "smb",
    "webdav"
})
public class Fragment {

    /**
     * Azure Protocol Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("azureBlobStorage")
    private AzureFragmentRef azureBlobStorage;
    /**
     * FTP Protocol Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("ftp")
    private FtpFragmentRef ftp;
    /**
     * HTTP Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("http")
    @JsonPropertyDescription("allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.")
    private HttpFragmentRef http;
    /**
     * Local Protocol Fragment Reference
     * <p>
     * Local source/target options
     * 
     */
    @JsonProperty("local")
    @JsonPropertyDescription("Local source/target options")
    private Local local;
    /**
     * SFTP Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("sftp")
    @JsonPropertyDescription("allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.")
    private SftpFragmentRef sftp;
    /**
     * SMB Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("smb")
    @JsonPropertyDescription("allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.")
    private SmbFragmentsRef smb;
    /**
     * WebDAV Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("webdav")
    @JsonPropertyDescription("allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.")
    private WebdavFragmentRef webdav;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fragment() {
    }

    /**
     * 
     * @param ftp
     * @param azureBlobStorage
     * @param http
     * @param smb
     * @param sftp
     * @param webdav
     * @param local
     */
    public Fragment(AzureFragmentRef azureBlobStorage, FtpFragmentRef ftp, HttpFragmentRef http, Local local, SftpFragmentRef sftp, SmbFragmentsRef smb, WebdavFragmentRef webdav) {
        super();
        this.azureBlobStorage = azureBlobStorage;
        this.ftp = ftp;
        this.http = http;
        this.local = local;
        this.sftp = sftp;
        this.smb = smb;
        this.webdav = webdav;
    }

    /**
     * Azure Protocol Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("azureBlobStorage")
    public AzureFragmentRef getAzureBlobStorage() {
        return azureBlobStorage;
    }

    /**
     * Azure Protocol Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("azureBlobStorage")
    public void setAzureBlobStorage(AzureFragmentRef azureBlobStorage) {
        this.azureBlobStorage = azureBlobStorage;
    }

    /**
     * FTP Protocol Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("ftp")
    public FtpFragmentRef getFtp() {
        return ftp;
    }

    /**
     * FTP Protocol Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("ftp")
    public void setFtp(FtpFragmentRef ftp) {
        this.ftp = ftp;
    }

    /**
     * HTTP Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("http")
    public HttpFragmentRef getHttp() {
        return http;
    }

    /**
     * HTTP Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("http")
    public void setHttp(HttpFragmentRef http) {
        this.http = http;
    }

    /**
     * Local Protocol Fragment Reference
     * <p>
     * Local source/target options
     * 
     */
    @JsonProperty("local")
    public Local getLocal() {
        return local;
    }

    /**
     * Local Protocol Fragment Reference
     * <p>
     * Local source/target options
     * 
     */
    @JsonProperty("local")
    public void setLocal(Local local) {
        this.local = local;
    }

    /**
     * SFTP Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("sftp")
    public SftpFragmentRef getSftp() {
        return sftp;
    }

    /**
     * SFTP Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("sftp")
    public void setSftp(SftpFragmentRef sftp) {
        this.sftp = sftp;
    }

    /**
     * SMB Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("smb")
    public SmbFragmentsRef getSmb() {
        return smb;
    }

    /**
     * SMB Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("smb")
    public void setSmb(SmbFragmentsRef smb) {
        this.smb = smb;
    }

    /**
     * WebDAV Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("webdav")
    public WebdavFragmentRef getWebdav() {
        return webdav;
    }

    /**
     * WebDAV Protocol Fragment Reference
     * <p>
     * allOf is present for and documentation validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("webdav")
    public void setWebdav(WebdavFragmentRef webdav) {
        this.webdav = webdav;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("azureBlobStorage", azureBlobStorage).append("ftp", ftp).append("http", http).append("local", local).append("sftp", sftp).append("smb", smb).append("webdav", webdav).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ftp).append(azureBlobStorage).append(http).append(smb).append(sftp).append(webdav).append(local).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fragment) == false) {
            return false;
        }
        Fragment rhs = ((Fragment) other);
        return new EqualsBuilder().append(ftp, rhs.ftp).append(azureBlobStorage, rhs.azureBlobStorage).append(http, rhs.http).append(smb, rhs.smb).append(sftp, rhs.sftp).append(webdav, rhs.webdav).append(local, rhs.local).isEquals();
    }

}
