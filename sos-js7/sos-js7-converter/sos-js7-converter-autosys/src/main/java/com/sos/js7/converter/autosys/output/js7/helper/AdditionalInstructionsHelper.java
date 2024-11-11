package com.sos.js7.converter.autosys.output.js7.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.WorkflowResult;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;

public class AdditionalInstructionsHelper {

    // 1) Create/handle additional Notices when a workflow(currently implemented for BOX) uses IN condition for itself
    private static final boolean WORKFLOW_ITSELF_BOARDS_CREATE = true;
    // true - extra ExpectNotice, false - merged in the existing ExpectNotices
    public static final boolean WORKFLOW_ITSELF_BOARDS_CREATE_AS_SEPARATE_EXPECT_NOTICE = false;
    private static final String WORKFLOW_ITSELF_BOARD_NAME_PREFIX = "OK-";
    private static final boolean WORKFLOW_ITSELF_BOARDS_CREATE_ONE_BOARD = true; // OK-WORKFLOW
    private static final String WORKFLOW_ITSELF_BOARDS_CREATE_ONE_BOARD_ORDER_TO_NOTICE_ID = "$js7WorkflowPath";

    // 2) For construct to delete posted notices: 1) PostNotices(OK) 2) ConsumeNotices(OK || <used PostNotices>)
    private static final String POST_CONSUME_BOARD_NAME = "OK";
    private static final String POST_CONSUME_BOARD_LIFETIME = "$js7EpochMilli + 3 * 1000"; // 3 seconds
    private static final String POST_CONSUME_BOARD_ORDER_TO_NOTICE_ID = "$js7OrderId";

    // 1) and 2)
    private static final Map<String, Board> WORKFLOW_ITSELF_BOARDS = new HashMap<>();
    private static final Map<String, Board> POST_CONSUME_BOARDS = new HashMap<>();

    public static void clear() {
        WORKFLOW_ITSELF_BOARDS.clear();
        POST_CONSUME_BOARDS.clear();
    }

    public static boolean convertBoards(JS7ConverterResult result, Path p, String boardName) {
        if (POST_CONSUME_BOARDS.containsKey(boardName)) {
            JS7ConverterHelper.createNoticeBoardByParentPath(result, p, boardName, AdditionalInstructionsHelper.POST_CONSUME_BOARDS.get(boardName));
            return true;
        } else if (WORKFLOW_ITSELF_BOARDS.containsKey(boardName)) {
            JS7ConverterHelper.createNoticeBoardByParentPath(result, p, boardName, AdditionalInstructionsHelper.WORKFLOW_ITSELF_BOARDS.get(
                    boardName));
            return true;
        }
        return false;
    }

    public static PostNotices tryCreatePostNoticeToWorkflowItSelf(AutosysAnalyzer analyzer, WorkflowResult wr, JobBOX box) {
        PostNotices pn = null;
        if (WORKFLOW_ITSELF_BOARDS_CREATE && analyzer.getConditionAnalyzer().getBoxRefersToItSelf().contains(box.getName())) {
            String boardName = getWorkflowItSelfBoardName(wr);

            Condition boxIfSelfBoardCondition = new Condition(ConditionType.JS7_INTERNAL, boardName);
            Path boardPath = BoardHelper.JS7_BOARDS.get(boxIfSelfBoardCondition);
            if (boardPath == null) {
                BoardHelper.JS7_BOARDS.put(boxIfSelfBoardCondition, Paths.get(boardName));
            }
            pn = new PostNotices(Collections.singletonList(boardName));

            Board ok = WORKFLOW_ITSELF_BOARDS.get(boardName);
            if (ok == null) {
                WORKFLOW_ITSELF_BOARDS.put(boardName, getWorkflowItSelfBoard(boardName));
            }
        }
        return pn;
    }

    private static String getWorkflowItSelfBoardName(WorkflowResult wr) {
        if (WORKFLOW_ITSELF_BOARDS_CREATE_ONE_BOARD) {
            return WORKFLOW_ITSELF_BOARD_NAME_PREFIX + "WORKFLOW";// OK-WORKFLOW
        } else {
            return WORKFLOW_ITSELF_BOARD_NAME_PREFIX + wr.getName();// OK-<workflowName>
        }
    }

    private static Board getWorkflowItSelfBoard(String boardName) {
        if (WORKFLOW_ITSELF_BOARDS_CREATE_ONE_BOARD) {
            return JS7ConverterHelper.createNoticeBoard(boardName, JS7ConverterHelper.getDefaultBoarderLifeTime(),
                    WORKFLOW_ITSELF_BOARDS_CREATE_ONE_BOARD_ORDER_TO_NOTICE_ID);
        } else {
            return JS7ConverterHelper.createNoticeBoard(boardName, JS7ConverterHelper.getDefaultBoarderLifeTime(), "'" + boardName + "'");
        }
    }

    public static List<Instruction> consumeNoticesIfExists(AutosysAnalyzer analyzer, WorkflowResult wr, List<Instruction> tryInstructions) {
        if (tryInstructions == null || tryInstructions.size() == 0) {
            return tryInstructions;
        }
        // 1) check if any PostNotices are set
        if (wr.getPostNotices() == null || wr.getPostNotices().size() == 0) {
            return tryInstructions;
        }

        // 2) specify an internal Board/Notice name
        String okNoticeName = POST_CONSUME_BOARD_NAME;
        // !!! OK-<wokflowName> is already used by BOX Jobs - ConverterBOXJobs.tryCreatePostNoticeToBoxSelf
        // okNotizeName = DEFAULT_POST_CONSUME_BOARD_NAME+"-"+wr.getName();// evtl. extends with the workflow name

        // 3) Create Instruction - PostNotices - internal Notice
        //PostNotices instructionPostNotice = new PostNotices(Collections.singletonList("'" + okNoticeName + "'"));
        PostNotices instructionPostNotice = new PostNotices(Collections.singletonList(okNoticeName));

        // 4.1) Create Instruction - ConsumeNotices - the Notices separated by or(||) - controller will remove the existing notices
        List<String> l = new ArrayList<>();
        l.add("'" + okNoticeName + "'");
        for (PostNotices pn : wr.getPostNotices()) {
            // !!! only without LookBack ....
            l = addNotices(analyzer, l, pn);
        }
        // 4.2) only OK, all other are LookBack ...
        if (l.size() == 1) {
            return tryInstructions;
        }
        ConsumeNotices instructionConsumeNotice = new ConsumeNotices(String.join(" || ", l), null);

        // 4) Add created instructions to workflow
        if (wr.isCycle()) {
            if (tryInstructions.get(0) instanceof Cycle) {
                Cycle c = (Cycle) tryInstructions.get(0);
                int index = 0;
                c.getCycleWorkflow().getInstructions().add(index, instructionPostNotice);
                index++;
                c.getCycleWorkflow().getInstructions().add(index, instructionConsumeNotice);
            }
        } else {
            int index = 0;
            tryInstructions.add(index, instructionPostNotice);
            index++;
            tryInstructions.add(index, instructionConsumeNotice);
        }

        // 5) Add to JS7_BOARDS
        Condition okCondition = new Condition(ConditionType.JS7_INTERNAL, okNoticeName);
        Path boardPath = BoardHelper.JS7_BOARDS.get(okCondition);
        if (boardPath == null) {
            BoardHelper.JS7_BOARDS.put(okCondition, Paths.get(okNoticeName));
        }

        // 6) Add to POST_CONSUME_BOARDS to use not a default but a Board definition created here
        Board ok = POST_CONSUME_BOARDS.get(okNoticeName);
        if (ok == null) {
            POST_CONSUME_BOARDS.put(okNoticeName, JS7ConverterHelper.createNoticeBoard(okNoticeName, POST_CONSUME_BOARD_LIFETIME,
                    POST_CONSUME_BOARD_ORDER_TO_NOTICE_ID));
        }

        return tryInstructions;
    }

    private static List<String> addNotices(AutosysAnalyzer analyzer, List<String> result, PostNotices pn) {
        if (pn == null || pn.getNoticeBoardNames() == null) {
            return result;
        }
        for (String bn : pn.getNoticeBoardNames()) {
            boolean endsWithAnySuffix = analyzer.getConditionAnalyzer().getLookBacks().stream().anyMatch(suffix -> bn.endsWith("-" + suffix));
            if (endsWithAnySuffix) {
                continue;
            }
            // if (bn.endsWith(".00") || bn.endsWith("-24") || bn.endsWith("-0") || bn.endsWith(".02") || bn.endsWith(".05") || bn.endsWith(".10") || bn
            // .endsWith(".15") || bn.endsWith(".20") || bn.endsWith(".25") || bn.endsWith(".30") || bn.endsWith(".50") || bn.endsWith(".59")) {
            // continue;
            // }

            String name = bn;
            name = "'" + bn + "'";
            if (!result.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }

}
