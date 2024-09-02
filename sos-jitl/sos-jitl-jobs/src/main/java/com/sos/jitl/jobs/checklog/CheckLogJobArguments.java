package com.sos.jitl.jobs.checklog;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class CheckLogJobArguments extends JobArguments {

    private JobArgument<String> job = new JobArgument<>("job", true);
    private JobArgument<String> label = new JobArgument<>("label", false);
    private JobArgument<String> pattern = new JobArgument<>("pattern", true);
    private JobArgument<String> separator = new JobArgument<>("separator", false, "|");
    private JobArgument<Boolean> multiline = new JobArgument<>("multiline", false, false);
    private JobArgument<Boolean> caseInsensitive = new JobArgument<>("case_insensitive", false, false);
    private JobArgument<Boolean> unixLines = new JobArgument<>("unix_lines", false, false);

    public CheckLogJobArguments() {
        super(new CredentialStoreArguments());
    }

    public String getJob() {
        return job.getValue();
    }

    public void setJob(String job) {
        this.job.setValue(job);
    }

    public String getLabel() {
        return label.getValue();
    }

    public void setLabel(String label) {
        this.label.setValue(label);
    }

    public String getPattern() {
        return pattern.getValue();
    }

    public void setPattern(String pattern) {
        this.pattern.setValue(pattern);
    }

    public String getSeparator() {
        return separator.getValue();
    }

    public void setSeparator(String separator) {
        this.separator.setValue(separator);
    }

    public Boolean getMultiline() {
        return multiline.getValue();
    }

    public void setMultiline(Boolean multiline) {
        this.multiline.setValue(multiline);
    }

    public Boolean getCaseInsensitive() {
        return caseInsensitive.getValue();
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive.setValue(caseInsensitive);
    }

    public Boolean getUnixLines() {
        return unixLines.getValue();
    }

    public void setUnixLines(Boolean unixLines) {
        this.unixLines.setValue(unixLines);
    }

}
