package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.commons.util.SOSString;

import js7.data.board.BoardPathExpression;
import js7.data.workflow.instructions.ExpectNotices;

public class FatExpectNotices {

    private String boardPaths;

    public FatExpectNotices(ExpectNotices en) {
        if (en != null) {
            BoardPathExpression bpe = en.boardPaths();
            boardPaths = bpe == null ? null : bpe.toString();
        }
    }

    public String getBoardPaths() {
        return boardPaths;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
