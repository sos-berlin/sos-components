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
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class LockHelper {

    public static Map<String, String> LOCKS = new HashMap<>();

    public static Integer CAPACITY = getCapacity();

    public static void clear() {
        LOCKS.clear();
        CAPACITY = getCapacity();
    }

    private static Integer getCapacity() {
        Integer capacity = null;
        if (Autosys2JS7Converter.CONFIG.getGenerateConfig().getLocks()) {
            capacity = Autosys2JS7Converter.CONFIG.getLockConfig().getForcedCapacity();
            if (capacity == null) {
                capacity = Autosys2JS7Converter.CONFIG.getLockConfig().getDefaultCapacity();
            }
        }
        return capacity;
    }

    public static List<Instruction> getLockInstructions(ACommonJob j, List<Instruction> in) {
        if (!Autosys2JS7Converter.CONFIG.getGenerateConfig().getLocks() || !j.hasResources()) {
            return in;
        }
        Lock l = new Lock();
        List<LockDemand> demands = new ArrayList<>();

        for (CommonJobResource r : j.getResources().getValue()) {
            String key = r.getName();
            String js7Name = LOCKS.get(key);
            if (js7Name == null) {
                js7Name = JS7ConverterHelper.getJS7ObjectName(key);
                LOCKS.put(key, js7Name);
            }
            demands.add(new LockDemand(js7Name, r.getQuantity()));
        }

        l.setDemands(demands);
        l.setLockedWorkflow(new Instructions(in));

        in = new ArrayList<>();
        in.add(l);
        return in;
    }

}
