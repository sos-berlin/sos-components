
package com.sos.joc.model.jobscheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dbms",
    "surveyDate",
    "version",
    "state"
})
public class Database {

    /**
     * Possible values are MySQL,Oracle,Postgres,Sybase,DB2,MS SQL Server
     * (Required)
     * 
     */
    @JsonProperty("dbms")
    @JsonPropertyDescription("Possible values are MySQL,Oracle,Postgres,Sybase,DB2,MS SQL Server")
    @JacksonXmlProperty(localName = "dbms")
    private String dbms;
    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "surveyDate")
    private Date surveyDate;
    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private DBState state;

    /**
     * Possible values are MySQL,Oracle,Postgres,Sybase,DB2,MS SQL Server
     * (Required)
     * 
     */
    @JsonProperty("dbms")
    @JacksonXmlProperty(localName = "dbms")
    public String getDbms() {
        return dbms;
    }

    /**
     * Possible values are MySQL,Oracle,Postgres,Sybase,DB2,MS SQL Server
     * (Required)
     * 
     */
    @JsonProperty("dbms")
    @JacksonXmlProperty(localName = "dbms")
    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * survey date of the inventory data; last time the inventory job has checked the live folder
     * <p>
     * Date of the inventory data. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JacksonXmlProperty(localName = "surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    @JacksonXmlProperty(localName = "version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public DBState getState() {
        return state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(DBState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dbms", dbms).append("surveyDate", surveyDate).append("version", version).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dbms).append(state).append(surveyDate).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Database) == false) {
            return false;
        }
        Database rhs = ((Database) other);
        return new EqualsBuilder().append(dbms, rhs.dbms).append(state, rhs.state).append(surveyDate, rhs.surveyDate).append(version, rhs.version).isEquals();
    }

}
