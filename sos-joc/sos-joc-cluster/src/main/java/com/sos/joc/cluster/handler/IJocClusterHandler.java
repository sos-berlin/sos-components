package com.sos.joc.cluster.handler;

import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;

public interface IJocClusterHandler {

    public JocClusterAnswer start();

    public String getIdentifier();

    public JocClusterAnswer stop();
}
