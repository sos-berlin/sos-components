package com.sos.joc.cluster.configuration;

import java.util.Date;

public class ClusterModeResult {
    
    private boolean use = false;
    private Date validFrom;
    private Date validUntil;
    
    public boolean getUse() {
        return use;
    }
    public void setUse(boolean use) {
        this.use = use;
    }
    
    public Date getValidFrom() {
        return validFrom;
    }
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }
    
    public Date getValidUntil() {
        return validUntil;
    }
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }
    
}
