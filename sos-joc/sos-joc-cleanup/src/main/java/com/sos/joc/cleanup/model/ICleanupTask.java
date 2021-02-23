package com.sos.joc.cleanup.model;

import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public interface ICleanupTask {

    public void start();

    public JocServiceTaskAnswer stop();

    public void setState(JocServiceTaskAnswerState state);

    public JocServiceTaskAnswerState getState();

    public boolean isStopped();

    public String getIdentifier();
}
