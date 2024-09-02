package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;

public class RetryHelper {

    public static List<Instruction> getRetryInstructions(ACommonJob j, List<Instruction> in) {
        Integer val = getMaxTries(j);
        if (val == null || val.intValue() == 0) {
            return in;
        }
        RetryCatch tryCatch = new RetryCatch();
        tryCatch.setMaxTries(val);
        tryCatch.setTry(new Instructions(in));
        tryCatch.setRetryDelays(getDelays());

        in = new ArrayList<>();
        in.add(tryCatch);
        return in;
    }

    private static Integer getMaxTries(ACommonJob j) {
        Integer val = Autosys2JS7Converter.CONFIG.getJobConfig().getForcedRetryMaxTries();
        if (val == null) {
            val = j.getNRetrys().getValue();
        }
        return val;
    }

    private static List<Integer> getDelays() {
        List<Integer> val = Autosys2JS7Converter.CONFIG.getJobConfig().getForcedRetryDelays();
        return val == null ? Collections.singletonList(0) : val;
    }
}
