package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sos.inventory.model.instruction.PostNotices;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;

public class BoardTryCatchHelper {

    private PostNotices tryPostNotices;
    private PostNotices catchPostNotices;

    public BoardTryCatchHelper(ACommonJob j, AutosysAnalyzer analyzer) {
        Map<ConditionType, Set<Condition>> outConditions = BoardHelper.getDistinctOutConditionsByType(j, analyzer.getConditionAnalyzer());
        if (outConditions != null) {
            Set<Condition> tryPN = new HashSet<>();
            Set<Condition> catchPN = new HashSet<>();

            for (Map.Entry<ConditionType, Set<Condition>> e : outConditions.entrySet()) {
                switch (e.getKey()) {
                case DONE:
                    for (Condition c : e.getValue()) {
                        if (!tryPN.contains(c)) {
                            tryPN.add(c);
                        }
                        if (!catchPN.contains(c)) {
                            catchPN.add(c);
                        }
                    }
                    break;
                case FAILURE:
                    for (Condition c : e.getValue()) {
                        if (!catchPN.contains(c)) {
                            catchPN.add(c);
                        }
                    }
                    break;
                case SUCCESS:
                default:
                    for (Condition c : e.getValue()) {
                        if (!tryPN.contains(c)) {
                            tryPN.add(c);
                        }
                    }
                    break;

                }
            }
            tryPostNotices = BoardHelper.newPostNotices(analyzer, tryPN);
            catchPostNotices = BoardHelper.newPostNotices(analyzer, catchPN);
        }
    }

    public PostNotices getTryPostNotices() {
        return tryPostNotices;
    }

    public void resetTryPostNotices() {
        tryPostNotices = null;
    }

    public PostNotices getCatchPostNotices() {
        return catchPostNotices;
    }
}
