package com.sos.joc.classes.inventory.validators;

import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.board.BoardType;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;

public class BoardValidator {

    public static void validate(IConfigurationObject config) throws Exception {
        validateBoard((Board) config);
    }

    public static void revalidate(IConfigurationObject config) throws Exception {
        validateBoard((Board) config);
    }

    private static void validateBoard(Board board) {
        if (board.getPostOrderToNoticeId() != null) {
            Validator.validateExpression("$.postOrderToNoticeId: ", board.getPostOrderToNoticeId());
        }
        if (board.getExpectOrderToNoticeId() != null) {
            Validator.validateExpression("$.expectOrderToNoticeId: ", board.getExpectOrderToNoticeId());
        }

        if (board.getBoardType() == null || board.getBoardType().equals(BoardType.GLOBAL)) {
            if (board.getPostOrderToNoticeId() == null && board.getExpectOrderToNoticeId() == null) {
                throw new JocConfigurationException("$.expectOrderToNoticeId: is required");
            }
            if (board.getEndOfLife() != null) {
                Validator.validateExpression("$.endOfLife: ", board.getEndOfLife());
            } else {
                throw new JocConfigurationException("$.endOfLife: is required");
            }
        } else {
            if (board.getPostOrderToNoticeId() != null && board.getExpectOrderToNoticeId() != null && !board.getPostOrderToNoticeId().equals(board
                    .getExpectOrderToNoticeId())) {
                throw new JocConfigurationException("use only $.postOrderToNoticeId");
            }
        }
    }

}
