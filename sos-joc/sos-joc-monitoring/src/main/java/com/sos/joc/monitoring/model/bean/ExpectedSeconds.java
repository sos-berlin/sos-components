package com.sos.joc.monitoring.model.bean;

import java.io.Serializable;

public class ExpectedSeconds implements Serializable {

    private static final long serialVersionUID = 1L;

    final Long seconds;
    final Long avg;

    public ExpectedSeconds(Long seconds, Long avg) {
        this.seconds = seconds;
        this.avg = avg;
    }

    public Long getSeconds() {
        return seconds;
    }

    public Long getAvg() {
        return avg;
    }

}
