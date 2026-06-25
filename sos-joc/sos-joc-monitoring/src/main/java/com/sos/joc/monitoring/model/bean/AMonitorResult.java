package com.sos.joc.monitoring.model.bean;

import java.io.Serializable;

public abstract class AMonitorResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean isErrorOrder;

    private boolean isStep;
    private boolean isWarnStep;

    private boolean errorCompleted;
    private boolean successCompleted;
    private boolean warnCompleted;
    private boolean recoveryCompleted;

    public void setIsErrorOrder() {
        isErrorOrder = true;
    }

    public boolean isErrorOrder() {
        return isErrorOrder;
    }

    public void setIsStep() {
        isStep = true;
    }

    public boolean isStep() {
        return isStep;
    }

    public void setIsWarnStep() {
        setIsStep();
        isWarnStep = true;
    }

    public boolean isWarnStep() {
        return isWarnStep;
    }

    public void setErrorCompleted() {
        errorCompleted = true;
    }

    public boolean isErrorCompleted() {
        return errorCompleted;
    }

    public void setSuccessCompleted() {
        successCompleted = true;
    }

    public boolean isSuccessCompleted() {
        return successCompleted;
    }

    public void setWarnCompleted() {
        warnCompleted = true;
    }

    public boolean isWarnCompleted() {
        return warnCompleted;
    }

    public void setRecoveryCompleted() {
        recoveryCompleted = true;
    }

    public boolean isRecoveryCompleted() {
        return recoveryCompleted;
    }

    public boolean isCompleted() {
        return errorCompleted && successCompleted && warnCompleted && recoveryCompleted;
    }

}
