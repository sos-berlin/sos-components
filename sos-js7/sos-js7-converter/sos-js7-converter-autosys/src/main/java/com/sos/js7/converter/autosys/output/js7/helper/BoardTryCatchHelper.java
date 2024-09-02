package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.input.analyzer.ConditionAnalyzer.OutConditionHolder;

public class BoardTryCatchHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardTryCatchHelper.class);

    private final BoardExpectConsumHelper expectConsumHelper;
    private PostNotices tryPostNotices;
    private PostNotices catchPostNotices;

    public BoardTryCatchHelper(ACommonJob j, AutosysAnalyzer analyzer, BoardExpectConsumHelper expectConsumHelper) {
        this.expectConsumHelper = expectConsumHelper;

        OutConditionHolder h = analyzer.getConditionAnalyzer().getJobOUTConditions(j);
        if (h == null) {
            return;
        }

        Set<Job2Condition> tryPN = new HashSet<>();
        Set<Job2Condition> catchPN = new HashSet<>();
        Set<Job2Condition> otherPN = new HashSet<>();
        for (Map.Entry<String, Map<String, Condition>> me : h.getJobConditions().entrySet()) {
            ACommonJob job = analyzer.getAllJobs().get(me.getKey());
            for (Map.Entry<String, Condition> e : me.getValue().entrySet()) {
                Job2Condition j2c = new Job2Condition(job, e.getValue());
                switch (j2c.getCondition().getType()) {
                case DONE:
                    if (!tryPN.contains(j2c)) {
                        tryPN.add(j2c);
                    }
                    if (!catchPN.contains(j2c)) {
                        catchPN.add(j2c);
                    }
                    break;
                case FAILURE:
                    if (!catchPN.contains(j2c)) {
                        catchPN.add(j2c);
                    }
                    break;
                case SUCCESS:
                    if (!tryPN.contains(j2c)) {
                        tryPN.add(j2c);
                    }
                    break;
                default:
                    if (!otherPN.contains(j2c)) {
                        otherPN.add(j2c);
                    }
                    break;
                }
            }
        }
        tryPostNotices = BoardHelper.newPostNotices(analyzer, j, tryPN);
        catchPostNotices = BoardHelper.newPostNotices(analyzer, j, catchPN);
    }

    public ConsumeNotices getConsumeNotices() {
        return expectConsumHelper == null ? null : expectConsumHelper.toConsumeNotices();
    }

    public void resetConsumeNotices() {
        if (expectConsumHelper != null) {
            expectConsumHelper.resetConsumeNotices();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("tryPostNotices=" + tryPostNotices);
        sb.append(",catchPostNotices=" + catchPostNotices);
        return sb.toString();
    }

}
