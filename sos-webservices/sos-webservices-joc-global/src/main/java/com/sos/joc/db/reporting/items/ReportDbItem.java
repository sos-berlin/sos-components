package com.sos.joc.db.reporting.items;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.report.Frequency;
import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.model.reporting.ReportItem;

public class ReportDbItem extends ReportItem {
    
    @JsonIgnore
    private byte[] content;
    
    @JsonIgnore
    public byte[] getContent() {
        return content;
    }
    
    @JsonIgnore
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public void setFrequency(Integer frequency) {
        super.setFrequency(Frequency.fromValue(frequency));
    }
    
    @Override
    public void setTemplateName(Integer templateId) {
        try {
            super.setTemplateName(TemplateId.fromValue(templateId));
        } catch (IllegalArgumentException e) {
            super.setTemplateName((TemplateId) null);
        }
    }
    
    public void setDateFrom(Date dateFrom) {
        try {
            super.setDateFrom(SOSDate.getDateAsString(dateFrom));
        } catch (SOSInvalidDataException e) {
            //
        }
    }
    
    public void setDateTo(Date dateTo) {
        try {
            super.setDateTo(SOSDate.getDateAsString(dateTo));
        } catch (SOSInvalidDataException e) {
            //
        }
    }
    
}
