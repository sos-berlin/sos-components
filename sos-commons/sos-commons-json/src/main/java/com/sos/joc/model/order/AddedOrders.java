
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Err419;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * add order response
 * <p>
 * if ok=true then orders collection is required
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "orders",
    "ok",
    "errors"
})
public class AddedOrders {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "deliveryDate")
    private Date deliveryDate;
    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "orders")
    private List<OrderPath200> orders = new ArrayList<OrderPath200>();
    @JsonProperty("ok")
    @JacksonXmlProperty(localName = "ok")
    private Boolean ok;
    @JsonProperty("errors")
    @JacksonXmlProperty(localName = "error")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "errors")
    private List<Err419> errors = new ArrayList<Err419>();

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
    public List<OrderPath200> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
    public void setOrders(List<OrderPath200> orders) {
        this.orders = orders;
    }

    @JsonProperty("ok")
    @JacksonXmlProperty(localName = "ok")
    public Boolean getOk() {
        return ok;
    }

    @JsonProperty("ok")
    @JacksonXmlProperty(localName = "ok")
    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    @JsonProperty("errors")
    @JacksonXmlProperty(localName = "error")
    public List<Err419> getErrors() {
        return errors;
    }

    @JsonProperty("errors")
    @JacksonXmlProperty(localName = "error")
    public void setErrors(List<Err419> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("orders", orders).append("ok", ok).append("errors", errors).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orders).append(deliveryDate).append(ok).append(errors).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddedOrders) == false) {
            return false;
        }
        AddedOrders rhs = ((AddedOrders) other);
        return new EqualsBuilder().append(orders, rhs.orders).append(deliveryDate, rhs.deliveryDate).append(ok, rhs.ok).append(errors, rhs.errors).isEquals();
    }

}
