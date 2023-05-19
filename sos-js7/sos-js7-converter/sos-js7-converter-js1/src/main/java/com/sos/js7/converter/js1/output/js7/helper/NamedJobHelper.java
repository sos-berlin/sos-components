package com.sos.js7.converter.js1.output.js7.helper;

import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;

public class NamedJobHelper {

    private final NamedJob namedJob;
    private final Lock lock;

    public NamedJobHelper(NamedJob namedJob, Lock lock) {
        this.namedJob = namedJob;
        this.lock = lock;
    }

    public Instruction getInstruction() {
        return lock == null ? namedJob : lock;
    }

    public NamedJob getNamedJob() {
        return namedJob;
    }

}
