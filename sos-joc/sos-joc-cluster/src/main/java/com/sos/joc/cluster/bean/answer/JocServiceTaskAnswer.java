package com.sos.joc.cluster.bean.answer;

import com.sos.joc.model.cluster.common.state.JocClusterServiceTaskState;

public class JocServiceTaskAnswer {

    private JocClusterServiceTaskState state;

    public JocServiceTaskAnswer(JocClusterServiceTaskState state) {
        this.state = state;
    }

    public JocClusterServiceTaskState getState() {
        return state;
    }

}
