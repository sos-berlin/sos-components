package com.sos.joc.cluster.bean.history;

public class HistoryOrderOutcomeType {

    public static enum Type {
        succeeded, failed, disrupted, broken, killed, timedout
    }

    public static enum NonJobInstructionFailureReason {
        finish_instruction, fail_instruction
    }

    public static Type getSucceeded() {
        return Type.succeeded;
    }

    public static Type getFailed() {
        return Type.failed;
    }

    public static Type getDisrupted() {
        return Type.disrupted;
    }

    public static Type getBroken() {
        return Type.broken;
    }

    public static Type getKilled() {
        return Type.killed;
    }

    public static Type getTimedout() {
        return Type.timedout;
    }

    public static NonJobInstructionFailureReason getFinishInstruction() {
        return NonJobInstructionFailureReason.finish_instruction;
    }

    public static NonJobInstructionFailureReason getFailInstruction() {
        return NonJobInstructionFailureReason.fail_instruction;
    }

    public static boolean isNonJobInstructionFailureReason(String reason) {
        if (reason == null) {
            return false;
        }
        try {
            NonJobInstructionFailureReason.valueOf(reason);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String getReason(String reason) {
        if (reason == null) {
            return null;
        }
        try {
            switch (NonJobInstructionFailureReason.valueOf(reason)) {
            case fail_instruction:
                return "Fail";
            case finish_instruction:
                return "Finish";
            default:
                return reason;
            }
        } catch (IllegalArgumentException e) {
            return reason;
        }
    }

}
