package com.sos.joc.db.reporting.items;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.util.SOSDate;
import com.sos.inventory.model.report.Frequency;
import com.sos.inventory.model.report.ReportOrder;
import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.model.reporting.ReportItem;

public class ReportDbItem extends ReportItem {
    
    @JsonIgnore
    private String content;
    
    @JsonIgnore
    public String getContent() {
        return content;
    }
    
    @JsonIgnore
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setFrequency(Number frequency) {
        super.setFrequency(Frequency.fromValue(frequency.intValue()));
    }

    public void setSort(Number sort) {
        super.setSort(ReportOrder.fromValue(sort.intValue()));
    }
    
    public void setPeriodLength(Number periodLength) {
        super.setPeriodLength(Long.valueOf(periodLength.intValue()));
    }
    
    public void setPeriodStep(Number periodStep) {
        super.setPeriodStep(Long.valueOf(periodStep.intValue()));
    }
    
    public void setTemplateName(Number templateId) {
        try {
            super.setTemplateName(TemplateId.fromValue(templateId.intValue()));
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
