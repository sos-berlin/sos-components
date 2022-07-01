package com.sos.js7.converter.js1.common.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Period {

    private static final String ATTR_ABSOLUTE_REPEAT = "absolute_repeat";
    private static final String ATTR_BEGIN = "begin";
    private static final String ATTR_END = "end";
    private static final String ATTR_LET_RUN = "let_run";
    private static final String ATTR_REPEAT = "repeat";
    private static final String ATTR_SINGLE_START = "single_start";
    private static final String ATTR_WHEN_HOLIDAY = "when_holiday";

    private String absoluteRepeat; // hh:mm[:ss] | seconds
    private String begin; // hh:mm[:ss] - (Initial value: 00:00)
    private String end; // hh:mm[:ss] - (Initial value: 24:00)
    private String letRun; // yes_no
    private String repeat; // hh:mm[:ss] or seconds - default 0
    private String singleStart; // hh:mm[:ss]
    private String whenHoliday; // suppress,ignore_holiday,previous_non_holiday,next_non_holiday

    protected Period(Node node) {
        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.absoluteRepeat = JS7ConverterHelper.stringValue(m.get(ATTR_ABSOLUTE_REPEAT));
        this.begin = JS7ConverterHelper.stringValue(m.get(ATTR_BEGIN));
        this.end = JS7ConverterHelper.stringValue(m.get(ATTR_END));
        this.letRun = JS7ConverterHelper.stringValue(m.get(ATTR_LET_RUN));
        this.repeat = JS7ConverterHelper.stringValue(m.get(ATTR_REPEAT));
        this.singleStart = JS7ConverterHelper.stringValue(m.get(ATTR_SINGLE_START));
        this.whenHoliday = JS7ConverterHelper.stringValue(m.get(ATTR_WHEN_HOLIDAY));
    }

    public String getAbsoluteRepeat() {
        return absoluteRepeat;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getLetRun() {
        return letRun;
    }

    public String getRepeat() {
        return repeat;
    }

    public String getSingleStart() {
        return singleStart;
    }

    public String getWhenHoliday() {
        return whenHoliday;
    }

    @Override
    public String toString() {
        List<String> l = new ArrayList<>();
        if (absoluteRepeat != null) {
            l.add("absoluteRepeat=" + absoluteRepeat);
        }
        if (begin != null) {
            l.add("begin=" + begin);
        }
        if (end != null) {
            l.add("end=" + end);
        }
        if (letRun != null) {
            l.add("letRun=" + letRun);
        }
        if (repeat != null) {
            l.add("repeat=" + repeat);
        }
        if (singleStart != null) {
            l.add("singleStart=" + singleStart);
        }
        if (whenHoliday != null) {
            l.add("whenHoliday=" + whenHoliday);
        }
        return String.join(",", l);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        Class<?> otherClazz = other.getClass();
        if (!(otherClazz.isInstance(this))) {
            return false;
        }

        Period op = (Period) other;
        try {
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(this.absoluteRepeat, op.getAbsoluteRepeat());
            eb.append(this.begin, op.getBegin());
            eb.append(this.end, op.getBegin());
            eb.append(this.letRun, op.getLetRun());
            eb.append(this.repeat, op.getRepeat());
            eb.append(this.singleStart, op.getSingleStart());
            eb.append(this.whenHoliday, op.getWhenHoliday());
            return eb.isEquals();
        } catch (Throwable ex) {
            return false;
        }
    }

}
