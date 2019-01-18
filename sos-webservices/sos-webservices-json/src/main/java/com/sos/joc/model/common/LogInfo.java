
package com.sos.joc.model.common;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Generated("org.jsonschema2pojo")
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
    private Boolean download = true;

    /**
     * name of temporary file. Can be used as parameter for ./jobscheduler/log, ./order/log or ./task/log
     * 
     * @return
     *     The filename
     */
    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    /**
     * name of temporary file. Can be used as parameter for ./jobscheduler/log, ./order/log or ./task/log
     * 
     * @param filename
     *     The filename
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
     * @return
     *     The size
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
     * @param size
     *     The size
     */
    @JsonProperty("size")
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * if true then ./jobscheduler/log, ./order/log or ./task/log sends log as download (with Content-Disposition 'attachment').
     * 
     * @return
     *     The download
     */
    @JsonProperty("download")
    public Boolean getDownload() {
        return download;
    }

    /**
     * if true then ./jobscheduler/log, ./order/log or ./task/log sends log as download (with Content-Disposition 'attachment').
     * 
     * @param download
     *     The download
     */
    @JsonProperty("download")
    public void setDownload(Boolean download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filename).append(size).append(download).toHashCode();
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
        return new EqualsBuilder().append(filename, rhs.filename).append(size, rhs.size).append(download, rhs.download).isEquals();
    }

}
