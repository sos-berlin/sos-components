
package com.sos.joc.model.yade;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * yade file
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "file"
})
public class TransferFile200 {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * yade file
     * <p>
     * compact=true -> required fields + possibly targetPath
     * (Required)
     * 
     */
    @JsonProperty("file")
    @JsonPropertyDescription("compact=true -> required fields + possibly targetPath")
    private TransferFile file;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * yade file
     * <p>
     * compact=true -> required fields + possibly targetPath
     * (Required)
     * 
     */
    @JsonProperty("file")
    public TransferFile getFile() {
        return file;
    }

    /**
     * yade file
     * <p>
     * compact=true -> required fields + possibly targetPath
     * (Required)
     * 
     */
    @JsonProperty("file")
    public void setFile(TransferFile file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("file", file).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(file).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransferFile200) == false) {
            return false;
        }
        TransferFile200 rhs = ((TransferFile200) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(file, rhs.file).isEquals();
    }

}
