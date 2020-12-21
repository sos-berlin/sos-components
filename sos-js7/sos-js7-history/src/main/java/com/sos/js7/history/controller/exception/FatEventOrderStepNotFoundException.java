package com.sos.js7.history.controller.exception;

import com.sos.commons.exception.SOSException;

public class FatEventOrderStepNotFoundException extends SOSException {

    private static final long serialVersionUID = 1L;
    private final String orderId;
    private final Long currentEventId;

    public FatEventOrderStepNotFoundException(String message, String orderId, Long currentEventId) {
        super(message);
        this.orderId = orderId;
        this.currentEventId = currentEventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getCurrentEventId() {
        return currentEventId;
    }
}
