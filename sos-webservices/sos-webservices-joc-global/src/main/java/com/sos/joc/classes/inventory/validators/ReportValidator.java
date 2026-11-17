package com.sos.joc.classes.inventory.validators;

import java.time.LocalDate;

import com.sos.inventory.model.report.Report;
import com.sos.joc.classes.reporting.RunReport;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;

public class ReportValidator {

    public static void validate(IConfigurationObject config) throws Exception {
        validateReport((Report) config);
    }

    public static void revalidate(IConfigurationObject config) throws Exception {
        validateReport((Report) config);
    }

    private static void validateReport(Report report) {
        if (!report.getTemplateName().isSupported()) {
            throw new JocConfigurationException(String.format("Template '%s' is not longer supported", report.getTemplateName().value()));
        }
        if (report.getMonthTo() != null) {
            try {
                LocalDate mTo = RunReport.relativeDateToSpecificLocalDate(report.getMonthTo(), true);
                LocalDate mFrom = RunReport.relativeDateToSpecificLocalDate(report.getMonthFrom(), false);
                if (mTo != null && mFrom.isAfter(mTo)) {
                    throw new JocConfigurationException("$.monthFrom has to be older than $.monthTo");
                }
            } catch (IllegalArgumentException e) {
                throw new JocConfigurationException("$." + e.getMessage());
            }
        }
    }
}
