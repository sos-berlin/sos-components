package com.sos.js7.history.controller.exception;

import com.sos.commons.exception.SOSException;

public class FatEventOrderNotFoundException extends SOSException {

    private static final long serialVersionUID = 1L;
    private final String orderKey;
    private final Long currentEventId;

    public FatEventOrderNotFoundException(String message, String orderKey, Long currentEventId) {
        super(message);
        this.orderKey = orderKey;
        this.currentEventId = currentEventId;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public Long getCurrentEventId() {
        return currentEventId;
    }

}
