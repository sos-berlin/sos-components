package com.sos.joc.history.controller.proxy.fatevent;

import js7.data.workflow.Instruction;
import js7.data.workflow.instructions.Execute.Named;

public class FatInstruction {

    private final String instructionName;
    private final String jobName;

    public FatInstruction(Instruction instruction) {
        this.instructionName = instruction.instructionName();
        this.jobName = getJobName(instruction);
    }

    private String getJobName(Instruction instruction) {
        if (instruction instanceof Named) {
            return ((Named) instruction).name().string();
        }
        return null;
    }

    public String getInstructionName() {
        return instructionName;
    }

    public String getJobName() {
        return jobName;
    }

}
