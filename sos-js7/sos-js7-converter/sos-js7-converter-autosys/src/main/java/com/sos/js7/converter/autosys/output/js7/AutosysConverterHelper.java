package com.sos.js7.converter.autosys.output.js7;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.output.js7.helper.bean.Condition2ConsumeNotice;

public class AutosysConverterHelper {

    private static Collator COLLATOR = Collator.getInstance(Locale.ENGLISH);

    public static TreeSet<ACommonJob> newJobTreeSet() {
        return new TreeSet<>(Comparator.comparing(ACommonJob::getName, COLLATOR));
    }

    public static TreeSet<Condition2ConsumeNotice> newContition2ConsumeNoticeTreeSet() {
        return new TreeSet<>(Comparator.comparing((Condition2ConsumeNotice n) -> n.getCondition().getName(), COLLATOR));
    }

    public static TreeMap<ACommonJob, List<Condition>> newJobConditionsTreeMap() {
        return new TreeMap<>(Comparator.comparing(ACommonJob::getName, COLLATOR));
    }

    public static TreeMap<JobBOX, Long> newJobBoxTreeMap() {
        return new TreeMap<>(Comparator.comparing(ACommonJob::getName, COLLATOR));
    }

}
