package com.sos.joc.history.controller.proxy.fatevent;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.util.SOSString;

import js7.base.time.Timestamp;
import js7.data.board.BoardPath;
import js7.data.board.Notice;
import js7.data.board.NoticeId;

public class FatPostNotice {

    private String id;
    private String board;
    private Date endOfLife;

    public FatPostNotice(Notice notice) {
        if (notice != null) {
            setId(notice.id());
            setBoard(notice.boardPath());
            setEndOfLife(notice.endOfLife());
        }
    }

    private void setId(NoticeId nId) {
        if (nId != null) {
            id = nId.string();
        }
    }

    private void setBoard(BoardPath bp) {
        if (bp != null) {
            board = bp.string();
        }
    }

    private void setEndOfLife(Timestamp t) {
        if (t != null) {
            endOfLife = Date.from(Instant.ofEpochMilli(t.toEpochMilli()));
        }
    }

    public String getId() {
        return id;
    }

    public String getBoard() {
        return board;
    }

    public Date getEndOfLife() {
        return endOfLife;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }

}
