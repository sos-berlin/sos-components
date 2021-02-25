package com.sos.joc.cleanup.model;

import java.util.Date;

import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public interface ICleanupTask {

    public void start(Date date);

    public JocServiceTaskAnswer stop();
  
    public JocServiceTaskAnswerState getState();

    public boolean isStopped();

    public String getIdentifier();

    public String getTypeName();

}
