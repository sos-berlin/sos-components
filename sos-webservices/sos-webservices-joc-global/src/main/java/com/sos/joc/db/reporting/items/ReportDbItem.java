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

        try {
            super.setFrequency(Frequency.fromValue(frequency.intValue()));
        } catch (Exception e) {
            super.setFrequency((Frequency) null);
        }
    }

    public void setSort(Number sort) {

        try {
            super.setSort(ReportOrder.fromValue(sort.intValue()));
        } catch (Exception e) {
            super.setSort((ReportOrder) null);
        }
    }

    public void setPeriodLength(Number periodLength) {
        if (periodLength != null) {
            super.setPeriodLength(periodLength.intValue());
        }
    }

    public void setPeriodStep(Number periodStep) {
        if (periodStep != null) {
            super.setPeriodStep(periodStep.intValue());
        }
    }

    public void setTemplateName(Number templateId) {

        try {
            super.setTemplateName(TemplateId.fromValue(templateId.intValue()));
        } catch (Exception e) {
            super.setTemplateName((TemplateId) null);
        }
    }

    public void setDateFrom(Date dateFrom) {
        if (dateFrom != null) {
            try {
                super.setDateFrom(SOSDate.getDateAsString(dateFrom));
            } catch (SOSInvalidDataException e) {
                //
            }
        }
    }

    public void setDateTo(Date dateTo) {
        if (dateTo != null) {
            try {
                super.setDateTo(SOSDate.getDateAsString(dateTo));
            } catch (SOSInvalidDataException e) {
                //
            }
        }
    }

}
