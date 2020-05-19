package com.sos.joc.cluster.handler;

import com.sos.joc.cluster.api.bean.ClusterAnswer;

public interface IClusterHandler {

    public ClusterAnswer start();

    public String getIdentifier();

    public ClusterAnswer stop();
}
