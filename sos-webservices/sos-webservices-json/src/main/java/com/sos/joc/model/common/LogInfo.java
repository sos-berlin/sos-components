
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * log info
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filename",
    "size",
    "download"
})
public class LogInfo {

    /**
     * name of temporary file. Can be used as parameter for ./jobscheduler/log, ./order/log or ./task/log
     * 
     */
    @JsonProperty("filename")
    @JsonPropertyDescription("name of temporary file. Can be used as parameter for ./jobscheduler/log, ./order/log or ./task/log")
    private String filename;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    private Long size;
    /**
     * if true then ./jobscheduler/log, ./order/log or ./task/log sends log as download (with Content-Disposition 'attachment').
     * 
     */
    @JsonProperty("download")
    @JsonPropertyDescription("if true then ./jobscheduler/log, ./order/log or ./task/log sends log as download (with Content-Disposition 'attachment').")
    private Boolean download = true;

    /**
     * name of temporary file. Can be used as parameter for ./jobscheduler/log, ./order/log or ./task/log
     * 
     */
    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    /**
     * name of temporary file. Can be used as parameter for ./jobscheduler/log, ./order/log or ./task/log
     * 
     */
    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    public Long getSize() {
        return size;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("size")
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * if true then ./jobscheduler/log, ./order/log or ./task/log sends log as download (with Content-Disposition 'attachment').
     * 
     */
    @JsonProperty("download")
    public Boolean getDownload() {
        return download;
    }

    /**
     * if true then ./jobscheduler/log, ./order/log or ./task/log sends log as download (with Content-Disposition 'attachment').
     * 
     */
    @JsonProperty("download")
    public void setDownload(Boolean download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("filename", filename).append("size", size).append("download", download).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(download).append(filename).append(size).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogInfo) == false) {
            return false;
        }
        LogInfo rhs = ((LogInfo) other);
        return new EqualsBuilder().append(download, rhs.download).append(filename, rhs.filename).append(size, rhs.size).isEquals();
    }

}
