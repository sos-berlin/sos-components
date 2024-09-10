package com.sos.joc.cleanup.model;

import java.util.List;

import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;

public interface ICleanupTask {

    public void start(List<TaskDateTime> datetimes);

    public void start(int counter);

    public JocClusterServiceTaskState stop(int maxTimeoutSeconds);

    public JocClusterServiceTaskState getState();

    public boolean isStopped();

    public boolean isCompleted();

    public String getIdentifier();

    public String getTypeName();

}
