package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.LockDemand;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobResource;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.autosys.output.js7.WorkflowResult;
import com.sos.js7.converter.autosys.output.js7.helper.beans.Resource2Lock;

public class LockHelper {

    public static Map<String, Resource2Lock> LOCKS = new HashMap<>();
    public static Map<WorkflowResult, ACommonJob> WORKFLOWS_2_LOCKS = AutosysConverterHelper.newWorkflowResultsTreeMap();

    public static void clear() {
        LOCKS.clear();
        WORKFLOWS_2_LOCKS.clear();
    }

    public static List<Instruction> getLockInstructions(AutosysAnalyzer analyzer, WorkflowResult wr, ACommonJob j, List<Instruction> in) {
        if (!Autosys2JS7Converter.CONFIG.getGenerateConfig().getLocks() || !j.hasResources()) {
            return in;
        }
        Lock l = new Lock();
        List<LockDemand> demands = new ArrayList<>();

        for (CommonJobResource r : j.getResources().getValue()) {
            String key = r.getName();
            Resource2Lock r2l = LOCKS.get(key);
            if (r2l == null) {
                r2l = new Resource2Lock(analyzer, r);
                if (Autosys2JS7Converter.HAS_REFERENCES) {
                    r2l.setReference(j.isReference());
                }
                LOCKS.put(key, r2l);
            } else {
                if (Autosys2JS7Converter.HAS_REFERENCES) {
                    if (!r2l.isReference() && j.isReference()) {
                        r2l.setReference(true);
                        LOCKS.put(key, r2l);
                    }
                }
            }

            demands.add(new LockDemand(r2l.getLock().getName(), r.getQuantity() > 0 ? r.getQuantity() : null));
        }

        l.setDemands(demands);
        l.setLockedWorkflow(new Instructions(in));

        in = new ArrayList<>();
        in.add(l);

        WORKFLOWS_2_LOCKS.put(wr, j);

        return in;
    }

}
