package com.sos.joc.cluster.handler;

public interface IClusterHandler {

    public void start() throws Exception;

    public String getIdentifier();

    public void stop();
}
