
package com.sos.inventory.model.descriptor;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "descriptorId",
    "title",
    "account",
    "scheduled",
    "created"
})
public class Descriptor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("descriptorId")
    private String descriptorId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("account")
    private String account;
    @JsonProperty("scheduled")
    private Date scheduled;
    @JsonProperty("created")
    private Date created;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Descriptor() {
    }

    /**
     * 
     * @param scheduled
     * @param created
     * @param descriptorId
     * @param title
     * @param account
     */
    public Descriptor(String descriptorId, String title, String account, Date scheduled, Date created) {
        super();
        this.descriptorId = descriptorId;
        this.title = title;
        this.account = account;
        this.scheduled = scheduled;
        this.created = created;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("descriptorId")
    public String getDescriptorId() {
        return descriptorId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("descriptorId")
    public void setDescriptorId(String descriptorId) {
        this.descriptorId = descriptorId;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    @JsonProperty("scheduled")
    public Date getScheduled() {
        return scheduled;
    }

    @JsonProperty("scheduled")
    public void setScheduled(Date scheduled) {
        this.scheduled = scheduled;
    }

    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("descriptorId", descriptorId).append("title", title).append("account", account).append("scheduled", scheduled).append("created", created).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(title).append(account).append(scheduled).append(created).append(descriptorId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Descriptor) == false) {
            return false;
        }
        Descriptor rhs = ((Descriptor) other);
        return new EqualsBuilder().append(title, rhs.title).append(account, rhs.account).append(scheduled, rhs.scheduled).append(created, rhs.created).append(descriptorId, rhs.descriptorId).isEquals();
    }

}
