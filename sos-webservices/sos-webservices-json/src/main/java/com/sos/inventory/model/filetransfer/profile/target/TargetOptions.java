
package com.sos.inventory.model.filetransfer.profile.target;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Target Options
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "appendFiles",
    "atomicity",
    "checkSize",
    "cumulateFiles",
    "compressFiles",
    "createIntegrityHashFile",
    "keepModificationDate",
    "disableMakeDirectories",
    "disableOverwriteFiles",
    "resumeFiles"
})
public class TargetOptions {

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("appendFiles")
    private String appendFiles;
    @JsonProperty("atomicity")
    private Atomicity atomicity;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSize")
    private String checkSize;
    @JsonProperty("cumulateFiles")
    private CumulateFiles cumulateFiles;
    @JsonProperty("compressFiles")
    private CompressFiles compressFiles;
    @JsonProperty("createIntegrityHashFile")
    private CreateIntegrityHash createIntegrityHashFile;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("keepModificationDate")
    private String keepModificationDate;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableMakeDirectories")
    private String disableMakeDirectories;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableOverwriteFiles")
    private String disableOverwriteFiles;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("resumeFiles")
    private String resumeFiles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public TargetOptions() {
    }

    /**
     * 
     * @param cumulateFiles
     * @param appendFiles
     * @param atomicity
     * @param disableOverwriteFiles
     * @param createIntegrityHashFile
     * @param keepModificationDate
     * @param checkSize
     * @param compressFiles
     * @param disableMakeDirectories
     * @param resumeFiles
     */
    public TargetOptions(String appendFiles, Atomicity atomicity, String checkSize, CumulateFiles cumulateFiles, CompressFiles compressFiles, CreateIntegrityHash createIntegrityHashFile, String keepModificationDate, String disableMakeDirectories, String disableOverwriteFiles, String resumeFiles) {
        super();
        this.appendFiles = appendFiles;
        this.atomicity = atomicity;
        this.checkSize = checkSize;
        this.cumulateFiles = cumulateFiles;
        this.compressFiles = compressFiles;
        this.createIntegrityHashFile = createIntegrityHashFile;
        this.keepModificationDate = keepModificationDate;
        this.disableMakeDirectories = disableMakeDirectories;
        this.disableOverwriteFiles = disableOverwriteFiles;
        this.resumeFiles = resumeFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("appendFiles")
    public String getAppendFiles() {
        return appendFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("appendFiles")
    public void setAppendFiles(String appendFiles) {
        this.appendFiles = appendFiles;
    }

    @JsonProperty("atomicity")
    public Atomicity getAtomicity() {
        return atomicity;
    }

    @JsonProperty("atomicity")
    public void setAtomicity(Atomicity atomicity) {
        this.atomicity = atomicity;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSize")
    public String getCheckSize() {
        return checkSize;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("checkSize")
    public void setCheckSize(String checkSize) {
        this.checkSize = checkSize;
    }

    @JsonProperty("cumulateFiles")
    public CumulateFiles getCumulateFiles() {
        return cumulateFiles;
    }

    @JsonProperty("cumulateFiles")
    public void setCumulateFiles(CumulateFiles cumulateFiles) {
        this.cumulateFiles = cumulateFiles;
    }

    @JsonProperty("compressFiles")
    public CompressFiles getCompressFiles() {
        return compressFiles;
    }

    @JsonProperty("compressFiles")
    public void setCompressFiles(CompressFiles compressFiles) {
        this.compressFiles = compressFiles;
    }

    @JsonProperty("createIntegrityHashFile")
    public CreateIntegrityHash getCreateIntegrityHashFile() {
        return createIntegrityHashFile;
    }

    @JsonProperty("createIntegrityHashFile")
    public void setCreateIntegrityHashFile(CreateIntegrityHash createIntegrityHashFile) {
        this.createIntegrityHashFile = createIntegrityHashFile;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("keepModificationDate")
    public String getKeepModificationDate() {
        return keepModificationDate;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("keepModificationDate")
    public void setKeepModificationDate(String keepModificationDate) {
        this.keepModificationDate = keepModificationDate;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableMakeDirectories")
    public String getDisableMakeDirectories() {
        return disableMakeDirectories;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableMakeDirectories")
    public void setDisableMakeDirectories(String disableMakeDirectories) {
        this.disableMakeDirectories = disableMakeDirectories;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableOverwriteFiles")
    public String getDisableOverwriteFiles() {
        return disableOverwriteFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableOverwriteFiles")
    public void setDisableOverwriteFiles(String disableOverwriteFiles) {
        this.disableOverwriteFiles = disableOverwriteFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("resumeFiles")
    public String getResumeFiles() {
        return resumeFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("resumeFiles")
    public void setResumeFiles(String resumeFiles) {
        this.resumeFiles = resumeFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("appendFiles", appendFiles).append("atomicity", atomicity).append("checkSize", checkSize).append("cumulateFiles", cumulateFiles).append("compressFiles", compressFiles).append("createIntegrityHashFile", createIntegrityHashFile).append("keepModificationDate", keepModificationDate).append("disableMakeDirectories", disableMakeDirectories).append("disableOverwriteFiles", disableOverwriteFiles).append("resumeFiles", resumeFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cumulateFiles).append(appendFiles).append(atomicity).append(disableOverwriteFiles).append(createIntegrityHashFile).append(keepModificationDate).append(checkSize).append(compressFiles).append(disableMakeDirectories).append(resumeFiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TargetOptions) == false) {
            return false;
        }
        TargetOptions rhs = ((TargetOptions) other);
        return new EqualsBuilder().append(cumulateFiles, rhs.cumulateFiles).append(appendFiles, rhs.appendFiles).append(atomicity, rhs.atomicity).append(disableOverwriteFiles, rhs.disableOverwriteFiles).append(createIntegrityHashFile, rhs.createIntegrityHashFile).append(keepModificationDate, rhs.keepModificationDate).append(checkSize, rhs.checkSize).append(compressFiles, rhs.compressFiles).append(disableMakeDirectories, rhs.disableMakeDirectories).append(resumeFiles, rhs.resumeFiles).isEquals();
    }

}
