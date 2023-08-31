package com.sos.js7.converter.js1.output.js7.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.TryCatch;

public class TryCatchHelper {

    private final Map<String, JobChainStateHelper> allStates;
    // <error state,instruction>
    private Map<String, TryCatchPartHelper> tryCatchParts = new HashMap<>();

    public TryCatchHelper(Map<String, JobChainStateHelper> allStates) {
        this.allStates = allStates;
    }

    public boolean contains(String state) {
        return tryCatchParts.containsKey(state);
    }

    public boolean addByState(String state, List<Instruction> instructions) {
        for (Map.Entry<String, TryCatchPartHelper> e : tryCatchParts.entrySet()) {
            TryCatchPartHelper h = e.getValue();
            if (h.getTryStates().contains(state)) {
                h.tryCatch.getTry().getInstructions().addAll(instructions);
                return true;
            } else if (h.getCatchStates().contains(state)) {
                h.tryCatch.getCatch().getInstructions().addAll(instructions);
                return true;
            }
            if (h.previousPartHelper != null) {
                if (h.previousPartHelper.getTryStates().contains(state)) {
                    h.previousPartHelper.tryCatch.getTry().getInstructions().addAll(instructions);
                    return true;
                } else if (h.getCatchStates().contains(state)) {
                    h.previousPartHelper.tryCatch.getCatch().getInstructions().addAll(instructions);
                    return true;
                }
            }
        }
        return false;
    }

    public void remove(String state) {
        // tryCatchParts.remove(state);
    }

    public void add(JobChainStateHelper sh, List<Instruction> currentInstructions, TryCatchPartHelper previousPH, NamedJob errorFileOrderSink) {
        TryCatch tryCatch = new TryCatch();
        tryCatch.setTry(new Instructions(currentInstructions));
        tryCatch.setCatch(new Instructions(new ArrayList<>()));
        if (errorFileOrderSink != null) {
            // tryCatch.getCatch().getInstructions().add(errorFileOrderSink);
        }

        TryCatchPartHelper ph = new TryCatchPartHelper();
        ph.startState = sh;
        ph.errorFileOrderSink = errorFileOrderSink;
        ph.previousPartHelper = previousPH;
        ph.lastTryCatch = tryCatch;
        if (previousPH != null) {
            if (previousPH.catchStates.contains(sh.getJS1State())) {
                previousPH.isCatch = true;

                TryCatch tryCatchPrev = previousPH.tryCatch;
                tryCatchPrev.getCatch().getInstructions().add(tryCatch);
                tryCatch = tryCatchPrev;
            }
        }
        ph.tryCatch = tryCatch;
        setStates(allStates, ph);

        tryCatchParts.put(ph.startState.getJS1ErrorState(), ph);
    }

    private boolean checkErrorState(Map<String, JobChainStateHelper> allStates, JobChainStateHelper h) {
        try {
            return !allStates.entrySet().stream().anyMatch(e -> e.getValue().getJS1NextState().equals(h.getJS1ErrorState()));
        } catch (Throwable e) {
            return false;
        }
    }

    private void setStates(Map<String, JobChainStateHelper> allStates, TryCatchPartHelper ph) {
        JobChainStateHelper h = allStates.get(ph.startState.getJS1State());
        boolean checkErrorState = checkErrorState(allStates, h);
        while (h != null) {
            String nextState = h.getJS1NextState();
            String errorState = h.getJS1ErrorState();

            if (nextState.equals(ph.startState.getJS1ErrorState())) {
                return;
            }

            if (nextState.length() > 0) {
                h = allStates.get(nextState);
                if (h != null && SOSString.isEmpty(h.getJS1JobName())) {
                    h = null;
                }
                if (h != null) {
                    if (h.getJS1ErrorState().equals(ph.startState.getJS1ErrorState())) {
                        ph.tryStates.add(h.getJS1State());
                    } else {
                        ph.catchStates.add(h.getJS1State());
                    }
                }
            } else {
                h = null;
            }

            if (checkErrorState && h == null && errorState.length() > 0) {
                h = allStates.get(errorState);
                if (h == null) {
                    //
                } else if (h.getJS1JobName() == null) {
                    h = null;
                }
                if (h != null) {
                    // ph.catchStates.add(h.getJS1State());
                    if (h.getJS1ErrorState().equals(ph.startState.getJS1ErrorState())) {
                        ph.tryStates.add(h.getJS1State());
                    } else {
                        ph.catchStates.add(h.getJS1State());
                    }
                    if (h.getJS1NextState().equals(ph.startState.getJS1ErrorState())) {
                        h = null;
                    }
                }
            }
        }
        if (ph.errorFileOrderSink != null) {
            if (!ph.catchStates.contains(ph.startState.getJS1ErrorState())) {
                ph.catchStates.add(ph.startState.getJS1ErrorState());
            }
        }
    }

    public TryCatchPartHelper getTryCatchPart(String state) {
        return tryCatchParts.get(state);
    }

    @Override
    public String toString() {
        return tryCatchParts.toString();
    }

    public class TryCatchPartHelper {

        private JobChainStateHelper startState;
        private TryCatchPartHelper previousPartHelper;
        private TryCatch tryCatch;
        private TryCatch lastTryCatch;
        private NamedJob errorFileOrderSink;

        private Set<String> tryStates = new HashSet<>();
        private Set<String> catchStates = new HashSet<>();

        private boolean isCatch = false;

        public TryCatchPartHelper getPreviousPartHelper() {
            return previousPartHelper;
        }

        public boolean addFileOrderSink(NamedJob nextFileOrderSink) {
            if (errorFileOrderSink == null) {
                return false;
            }

            List<Instruction> instructions = null;
            if (previousPartHelper == null) {
                if (nextFileOrderSink != null) {
                    tryCatch.getTry().getInstructions().add(nextFileOrderSink);
                }
                instructions = tryCatch.getCatch().getInstructions();
            } else {
                if (nextFileOrderSink != null) {
                    previousPartHelper.getTryCatch().getTry().getInstructions().add(nextFileOrderSink);
                }
                if (previousPartHelper.isCatch()) {
                    instructions = previousPartHelper.getTryCatch().getCatch().getInstructions();
                } else {
                    // previousPartHelper.getTryCatch().getTry().getInstructions().add(errorFileOrderSink);
                    instructions = previousPartHelper.getTryCatch().getTry().getInstructions();
                }
            }
            if (instructions != null) {
                instructions.add(errorFileOrderSink);
                instructions.add(new Finish());
            }
            return true;
        }

        public NamedJob getErrorFileOrderSink() {
            return errorFileOrderSink;
        }

        public TryCatch getTryCatch() {
            return tryCatch;
        }

        public TryCatch getLastTryCatch() {
            return lastTryCatch;
        }

        public Set<String> getTryStates() {
            return tryStates;
        }

        public Set<String> getCatchStates() {
            return catchStates;
        }

        public boolean isCatch() {
            return isCatch;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(TryCatchPartHelper.class.getSimpleName());
            sb.append(" startState=").append(startState.getJS1State());
            sb.append(",tryStates=").append(tryStates);
            sb.append(",catchStates=").append(catchStates);
            sb.append(",previousPartHelper=").append(previousPartHelper);
            return sb.toString();
        }

    }
}
