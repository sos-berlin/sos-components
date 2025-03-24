package com.sos.joc.classes.board;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsDailyPlan;
import com.sos.joc.db.deploy.items.WorkflowBoards;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.sign.model.board.Board;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.board.BoardPath;
import js7.data.controller.ControllerCommand.Response;
import js7.data.plan.PlanSchemaId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.board.JBoardItem;
import js7.data_for_java.board.JBoardState;
import js7.data_for_java.board.JGlobalBoard;
import js7.data_for_java.board.JPlannableBoard;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.value.JExprFunction;
import js7.data_for_java.value.JExpression;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;

public class BoardConverter {

    private final static String DailyplanDateAndOrderNamePattern =
            "replaceAll($js7OrderId,'^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*-([^:]*)(?::[^|]*)?([|].*)?$','$1$2$3')";
    private final static String DailyplanDatePattern = "replaceAll($js7OrderId,'^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$','$1')";
    
    private static JControllerCommand convert(JBoardItem newBoard, JBoardItem oldBoard) {
        
        if (oldBoard == null) {
            return null;
        }
        if (oldBoard.asScala().isGlobal() && !newBoard.asScala().isGlobal()) {
            JGlobalBoard gb = (JGlobalBoard) oldBoard;
            JExpression.apply(gb.asScala().postOrderToNoticeKey()).toString();
            JExprFunction ef = null;
            String postOrderToNoticeKey = JExpression.apply(gb.asScala().postOrderToNoticeKey()).toString().replace(" ", "");
            if (DailyplanDateAndOrderNamePattern.equals(postOrderToNoticeKey)) {
                ef = JExprFunction.apply("(noticeKey) => [ substring($noticeKey, 0, 10), substring($noticeKey, 10) ]");
            } else if (DailyplanDatePattern.equals(postOrderToNoticeKey)) {
                ef = JExprFunction.apply("(noticeKey) => [ $noticeKey, \"\" ]");
            } else { // TODO is it ok?
                ef = JExprFunction.apply("(noticeKey) => [ '" + getDailyPlanDate() + "', $noticeKey ]");
            }
            return JControllerCommand.apply(JControllerCommand.changeGlobalToPlannableBoard(((JPlannableBoard) newBoard).asScala(), PlanSchemaId.of(
                    "DailyPlan"), ef));
        } else if (!oldBoard.asScala().isGlobal() && newBoard.asScala().isGlobal()) {
            JExprFunction ef = JExprFunction.apply("(planKey, noticeKey) => \"$planKey$noticeKey\"");
            return JControllerCommand.apply(JControllerCommand.changePlannableToGlobalBoard(((JGlobalBoard) newBoard).asScala(), PlanSchemaId.of(
                    "DailyPlan"), ef));
        }

        return null;
    }
    
    private static String getDailyPlanDate() {
        ConfigurationGlobalsDailyPlan dailyPlanConf = Globals.getConfigurationGlobalsDailyPlan();
        Instant now = Instant.now();
        String timeZone = dailyPlanConf.getTimeZone().getValue();
        if (SOSString.isEmpty(timeZone)) {
            timeZone = SOSDate.TIMEZONE_UTC;
        }
        long periodBeginSeconds = Optional.ofNullable(JobSchedulerDate.getSecondsOfHHmmss(dailyPlanConf.getPeriodBegin().getValue())).map(
                Long::longValue).orElse(0L);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        return format.format(Date.from(now.minusSeconds(periodBeginSeconds)));
    }
    
    private static Optional<JControllerCommand> convert(Map<JBoardItem, JBoardItem> newOldBoard) {
        if (newOldBoard.size() == 1) {
            Map.Entry<JBoardItem, JBoardItem> firstEntry = newOldBoard.entrySet().iterator().next();
            return Optional.ofNullable(convert(firstEntry.getKey(), firstEntry.getValue()));
        }
        List<JControllerCommand> commands = newOldBoard.entrySet().stream().map(e -> convert(e.getKey(), e.getValue())).filter(
                Objects::nonNull).collect(Collectors.toList());
        if (commands.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(JControllerCommand.batch(commands));
    }
    
    private static JGlobalBoard getJGlobalBoard(com.sos.sign.model.board.Board board) {
        if (board.getPostOrderToNoticeId() == null && board.getExpectOrderToNoticeId() != null) {
            board.setPostOrderToNoticeId(board.getExpectOrderToNoticeId()); 
        } else if (board.getPostOrderToNoticeId() != null && board.getExpectOrderToNoticeId() == null) {
            board.setExpectOrderToNoticeId(board.getPostOrderToNoticeId()); 
        }
        JExpression postOrderToNoticeIdExpression = JExpression.fromString(""); //default empty
        JExpression expectOrderToNoticeIdExpression = JExpression.fromString(""); //default empty
        if (board.getPostOrderToNoticeId() != null) {
            postOrderToNoticeIdExpression = getOrThrowEither(JExpression.parse(board.getPostOrderToNoticeId()));
        }
        if (board.getExpectOrderToNoticeId() != null) {
            expectOrderToNoticeIdExpression = getOrThrowEither(JExpression.parse(board.getExpectOrderToNoticeId()));
        }
        JExpression endOfLifeExpression = JExpression.fromString(""); //default empty
        if (board.getEndOfLife() != null) {
            endOfLifeExpression = getOrThrowEither(JExpression.parse(board.getEndOfLife()));
        }
        return JGlobalBoard.of(BoardPath.of(board.getPath()), postOrderToNoticeIdExpression, expectOrderToNoticeIdExpression, endOfLifeExpression);
    }
    
    private static JPlannableBoard getJPlannableBoard(com.sos.sign.model.board.Board board) {
        if (board.getPostOrderToNoticeId() == null && board.getExpectOrderToNoticeId() != null) {
            board.setPostOrderToNoticeId(board.getExpectOrderToNoticeId()); 
        } else if (board.getPostOrderToNoticeId() != null && board.getExpectOrderToNoticeId() == null) {
            board.setExpectOrderToNoticeId(board.getPostOrderToNoticeId()); 
        }
        JExpression postOrderToNoticeIdExpression = JExpression.fromString(""); //default empty
        JExpression expectOrderToNoticeIdExpression = JExpression.fromString(""); //default empty
        if (board.getPostOrderToNoticeId() != null) {
            postOrderToNoticeIdExpression = getOrThrowEither(JExpression.parse(board.getPostOrderToNoticeId()));
        }
        if (board.getExpectOrderToNoticeId() != null) {
            expectOrderToNoticeIdExpression = getOrThrowEither(JExpression.parse(board.getExpectOrderToNoticeId()));
        }
        return JPlannableBoard.of(BoardPath.of(board.getPath()), postOrderToNoticeIdExpression, expectOrderToNoticeIdExpression);
    }
    
    private static <T> T getOrThrowEither(Either<Problem, T> e) {
        if (e.isLeft()) {
            throw new JocDeployException(e.getLeft().toString());
        }
        return e.get();
    }
    
    private static Map<JBoardItem, JBoardItem> getNewOldBoardMapFromDepItems(Collection<DBItemDeploymentHistory> depItems,
            JControllerState currentState) {
        Map<BoardPath, JBoardState> pathToBoardState = currentState.pathToBoardState();
        Set<WorkflowBoards> workflowsWithConsumeNotices = WorkflowRefs.getWorkflowNamesWithBoards(currentState.asScala().controllerId().string())
                .values().stream().filter(b -> b.hasConsumeNotice() > 0).collect(Collectors.toSet());

        Map<JBoardItem, JBoardItem> newOldBoards = new HashMap<>();
        depItems.stream().filter(i -> i.getType().equals(DeployType.NOTICEBOARD.intValue())).forEach(i -> {
            JBoardState oldBoard = pathToBoardState.get(BoardPath.of(i.getName()));
            if (oldBoard != null) {
                if (i.readUpdateableContent() == null) {
                    try {
                        i.writeUpdateableContent(Globals.objectMapper.readValue(i.getInvContent(), Board.class));
                    } catch (Exception e) {
                        throw new DBInvalidDataException("", e);
                    }
                }
                Board newBoard = (Board) i.readUpdateableContent();

                newBoard.setPath(i.getName());
                JBoardItem jNewBoard = getJBoard(newBoard);
                
                // if change necessary then check if order exists in consumeNotices block
                if ((oldBoard.asScala().isGlobal() && !jNewBoard.asScala().isGlobal()) || (!oldBoard.asScala().isGlobal() && jNewBoard.asScala()
                        .isGlobal())) {
                    hasConsumeOrders(workflowsWithConsumeNotices, currentState, i.getName());
                }
                
                newOldBoards.put(jNewBoard, oldBoard.board());
            }
        });
        return newOldBoards;
    }
    
    private static Map<JBoardItem, JBoardItem> getNewOldBoardMapFromControllerObjs(Collection<ControllerObject> cObjs,
            JControllerState currentState) {
        Map<BoardPath, JBoardState> pathToBoardState = currentState.pathToBoardState();
        Map<JBoardItem, JBoardItem> newOldBoards = new HashMap<>();
        Set<WorkflowBoards> workflowsWithConsumeNotices = WorkflowRefs.getWorkflowNamesWithBoards(currentState.asScala().controllerId().string())
                .values().stream().filter(b -> b.hasConsumeNotice() > 0).collect(Collectors.toSet());
        
        cObjs.stream().filter(i -> i.getObjectType().equals(DeployType.NOTICEBOARD)).forEach(i -> {
            String boardName = JocInventory.pathToName(i.getPath());
            JBoardState oldBoard = pathToBoardState.get(BoardPath.of(boardName));
            if (oldBoard != null) {
                Board newBoard = (Board) i.getContent();
                newBoard.setPath(boardName);
                JBoardItem jNewBoard = getJBoard(newBoard);
                
                // if change necessary then check if order exists in consumeNotices block
                if ((oldBoard.asScala().isGlobal() && !jNewBoard.asScala().isGlobal()) || (!oldBoard.asScala().isGlobal() && jNewBoard.asScala()
                        .isGlobal())) {
                    hasConsumeOrders(workflowsWithConsumeNotices, currentState, boardName);
                }
                
                newOldBoards.put(jNewBoard, oldBoard.board());
            }
        });
        return newOldBoards;
    }
    
    private static boolean hasConsumeOrders(JControllerState currentState, String workflowPath) {
        return currentState.ordersBy(JOrderPredicates.byWorkflowPath(WorkflowPath.of(JocInventory.pathToName(workflowPath)))).anyMatch(
                o -> o.workflowPosition().position().toString().contains("/consumeNotices"));
    }
    
    private static void hasConsumeOrders(Set<WorkflowBoards> workflowsWithConsumeNotices, JControllerState currentState, String boardName) {
        workflowsWithConsumeNotices.stream().filter(w -> w.hasConsumeNotice(boardName)).forEach(w -> {
            if (hasConsumeOrders(currentState, w.getPath())) {
                throw new JocBadRequestException(String.format(
                        "Board type of '%s' cannot be changed. It is used in the Workflow '%s' that has still orders in a consumeNotices instruction",
                        boardName, w.getPath()));
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends JBoardItem> T getJBoard(com.sos.sign.model.board.Board board) {
        if (board.getBoardType() != null) {
            switch (board.getBoardType()) {
            case PLANNABLE:
                return (T) getJPlannableBoard(board);
            default: // case GLOBAL
                return (T) getJGlobalBoard(board);
            }
        } else if (board.getTYPE() != null) {
            switch (board.getTYPE()) {
            case PLANNABLEBOARD:
                return (T) getJPlannableBoard(board);
            default: // case GLOBAL
                return (T) getJGlobalBoard(board);
            }
        } else {
            return (T) getJGlobalBoard(board);
        }
    }

    public static CompletableFuture<Either<Problem, Response>> convert(JControllerApi api, Map<JBoardItem, JBoardItem> newOldBoards) {
        try {
            return convert(newOldBoards).map(api::executeCommand).orElse(CompletableFuture.supplyAsync(() -> Either.right(null)));
        } catch (Exception e) {
            return CompletableFuture.supplyAsync(() -> Either.left(Problem.of(e.toString())));
        }
    }
    
    public static CompletableFuture<Either<Problem, Response>> convertFromDepItems(JControllerProxy proxy, Collection<DBItemDeploymentHistory> depItems) {
        return convert(proxy.api(), getNewOldBoardMapFromDepItems(depItems, proxy.currentState()));
    }
    
    public static CompletableFuture<Either<Problem, Response>> convertToFromControllerObjs(JControllerProxy proxy, Collection<ControllerObject> depItems) {
        return convert(proxy.api(), getNewOldBoardMapFromControllerObjs(depItems, proxy.currentState()));
    }

}
