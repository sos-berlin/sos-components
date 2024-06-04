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
    
    public void setFrequency(Integer frequency) {
        super.setFrequency(Frequency.fromValue(frequency));
    }

    public void setSort(Integer sort) {
        super.setSort(ReportOrder.fromValue(sort));
    }
    
    public void setPeriodLength(Integer periodLength) {
        super.setPeriodLength(Long.valueOf(periodLength));
    }
    
    public void setPeriodStep(Integer periodStep) {
        super.setPeriodStep(Long.valueOf(periodStep));
    }
    
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
