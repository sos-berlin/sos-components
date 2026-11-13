
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Profile
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "source",
    "target",
    "transferOptions"
})
public class Profile
    extends ProfileBase
{

    /**
     * Source
     * <p>
     * Source of a file transfer operation.
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("Source of a file transfer operation.")
    private Source source;
    /**
     * Target
     * <p>
     * Target of a file transfer operation.
     * 
     */
    @JsonProperty("target")
    @JsonPropertyDescription("Target of a file transfer operation.")
    private Target target;
    /**
     * Transfer Options
     * <p>
     * 
     * 
     */
    @JsonProperty("transferOptions")
    private TransferOptions transferOptions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Profile() {
    }

    /**
     * 
     * @param path
     * @param alternativeRef
     * @param source
     * @param documentationName
     * @param title
     * @param transferOptions
     * @param operation
     * @param version
     * @param target
     */
    public Profile(Source source, Target target, TransferOptions transferOptions, String alternativeRef, ProfileBase.Operation operation, String version, String path, String title, String documentationName) {
        super(alternativeRef, operation, version, path, title, documentationName);
        this.source = source;
        this.target = target;
        this.transferOptions = transferOptions;
    }

    /**
     * Source
     * <p>
     * Source of a file transfer operation.
     * 
     */
    @JsonProperty("source")
    public Source getSource() {
        return source;
    }

    /**
     * Source
     * <p>
     * Source of a file transfer operation.
     * 
     */
    @JsonProperty("source")
    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * Target
     * <p>
     * Target of a file transfer operation.
     * 
     */
    @JsonProperty("target")
    public Target getTarget() {
        return target;
    }

    /**
     * Target
     * <p>
     * Target of a file transfer operation.
     * 
     */
    @JsonProperty("target")
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * Transfer Options
     * <p>
     * 
     * 
     */
    @JsonProperty("transferOptions")
    public TransferOptions getTransferOptions() {
        return transferOptions;
    }

    /**
     * Transfer Options
     * <p>
     * 
     * 
     */
    @JsonProperty("transferOptions")
    public void setTransferOptions(TransferOptions transferOptions) {
        this.transferOptions = transferOptions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("source", source).append("target", target).append("transferOptions", transferOptions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(source).append(transferOptions).append(target).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Profile) == false) {
            return false;
        }
        Profile rhs = ((Profile) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(source, rhs.source).append(transferOptions, rhs.transferOptions).append(target, rhs.target).isEquals();
    }

}
