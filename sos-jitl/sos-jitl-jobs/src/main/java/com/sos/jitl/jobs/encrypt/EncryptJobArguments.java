package com.sos.jitl.jobs.encrypt;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class EncryptJobArguments extends JobArguments {

    private JobArgument<String> enciphermentCertificate  = new JobArgument<>("encipherment_certificate", true);
    private JobArgument<String> in = new JobArgument<>("in", false);
    private JobArgument<String> inFile = new JobArgument<>("infile", false);
    private JobArgument<String> outFile = new JobArgument<>("outfile", false);

    public String getEnciphermentCertificate() {
        return enciphermentCertificate.getValue();
    }

    public void setEnciphermentCertificate(String enciphermentCertificate) {
        this.enciphermentCertificate.setValue(enciphermentCertificate);
    }

    public String getIn() {
        return in.getValue();
    }

    public void setIn(String in) {
        this.in.setValue(in);
    }

    public String getInFile() {
        return inFile.getValue();
    }

    public void setInFile(String inFile) {
        this.inFile.setValue(inFile);
    }

    public String getOutFile() {
        return outFile.getValue();
    }

    public void setOutFile(String outFile) {
        this.outFile.setValue(outFile);
    }

}
