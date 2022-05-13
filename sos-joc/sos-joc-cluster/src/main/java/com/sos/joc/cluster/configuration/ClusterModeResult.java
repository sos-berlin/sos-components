package com.sos.joc.cluster.configuration;

import java.util.Date;

public class ClusterModeResult {

    private final boolean jarFound;

    private boolean use = false;
    private Date validFrom;
    private Date validUntil;

    private boolean useSet = false;
    private boolean validFromSet = false;
    private boolean validUntilSet = false;

    public ClusterModeResult(boolean jarFound) {
        this.jarFound = jarFound;
    }

    public boolean isComplete() {
        return jarFound && useSet && validFromSet && validUntilSet;
    }

    public boolean getUse() {
        return use;
    }

    public void setUse(boolean val) {
        use = val;
        useSet = true;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date val) {
        validFrom = val;
        validFromSet = true;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date val) {
        validUntil = val;
        validUntilSet = true;
    }

    public boolean isJarFound() {
        return jarFound;
    }

}
