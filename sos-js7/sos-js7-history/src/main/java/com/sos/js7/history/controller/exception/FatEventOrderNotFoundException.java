package com.sos.js7.history.controller.exception;

import com.sos.commons.exception.SOSException;

public class FatEventOrderNotFoundException extends SOSException {

    private static final long serialVersionUID = 1L;
    private final String orderId;

    public FatEventOrderNotFoundException(String message, String orderId) {
        super(message);
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

}
