package com.sos.joc.cleanup.model;

import java.util.Date;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public interface ICleanupTask {

    public void start(Date date);

    public JocServiceTaskAnswerState cleanup(Date date) throws SOSHibernateException;

    public JocServiceTaskAnswer stop();

    public void setState(JocServiceTaskAnswerState state);

    public JocServiceTaskAnswerState getState();

    public boolean isStopped();

    public String getIdentifier();
}
