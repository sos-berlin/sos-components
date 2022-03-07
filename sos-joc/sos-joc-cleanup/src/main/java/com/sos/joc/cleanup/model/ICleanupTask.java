package com.sos.joc.cleanup.model;

import java.util.List;

import com.sos.joc.cleanup.CleanupServiceTask.TaskDateTime;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public interface ICleanupTask {

    public void start(List<TaskDateTime> datetimes);

    public void start(int counter);

    public JocServiceTaskAnswer stop(int maxTimeoutSeconds);

    public JocServiceTaskAnswerState getState();

    public boolean isStopped();

    public boolean isCompleted();

    public String getIdentifier();

    public String getTypeName();

}
