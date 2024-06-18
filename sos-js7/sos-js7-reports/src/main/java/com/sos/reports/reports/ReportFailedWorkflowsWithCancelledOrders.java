package com.sos.reports.reports;

import com.sos.joc.model.order.OrderStateText;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportRecord;

public class ReportFailedWorkflowsWithCancelledOrders extends ReportStateWorkflows implements IReport {

    @Override
    public boolean getCondition(ReportRecord orderRecord) {
        return (orderRecord.getError() && orderRecord.getOrderState() != null && orderRecord.getOrderState().equals(OrderStateText.CANCELLED
                .intValue()));
    }

}
