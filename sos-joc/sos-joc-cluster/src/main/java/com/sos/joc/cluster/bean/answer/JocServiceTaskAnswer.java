package com.sos.joc.cluster.bean.answer;

public class JocServiceTaskAnswer {

    public enum JocServiceTaskAnswerState {
        COMPLETED, UNCOMPLETED, UNKNOWN
    }

    private JocServiceTaskAnswerState state;

    public JocServiceTaskAnswer(JocServiceTaskAnswerState state) {
        this.state = state;
    }

    public JocServiceTaskAnswerState getState() {
        return state;
    }

}
