package com.sos.js7.converter.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.inventory.model.lock.Lock;
import com.sos.inventory.model.script.Script;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.js7.converter.commons.agent.JS7AgentConverter;
import com.sos.js7.converter.commons.config.JS7ConverterConfig;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.commons.report.ConverterReport;

public class JS7ConverterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterHelper.class);

    public static ObjectMapper JSON_OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    public final static String JS7_NEW_LINE = "\n";

    private final static Set<Character> QUOTED_CHARS = new HashSet<>(Arrays.asList('\"', '$', '\n'));

    private final static Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    private static int converterNameCounter = 0;

    public static String getJS7JobTemplateHash(JobTemplate t) {
        try {
            return SOSString.hash256(serializeAsString(t));
        } catch (Throwable e) {
            LOGGER.error(String.format("[getJS7JobTemplateHash]%s", e.toString()), e);
            return SOSString.hash256(t.toString());
        }
    }

    @SuppressWarnings("deprecation")
    private static <T> String serializeAsString(T config) throws Exception {
        if (config == null) {
            return null;
        }
        return JSON_OM.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true).writeValueAsString(config);
    }

    public static String stringValue(String val) {
        return val == null ? null : StringUtils.strip(val.trim(), "\"");
    }

    public static Integer integerValue(String val) {
        return val == null ? null : Integer.parseInt(val.trim());
    }

    public static Long longValue(String val) {
        return val == null ? null : Long.parseLong(val.trim());
    }

    public static Boolean booleanValue(String val) {
        return booleanValue(val, null);
    }

    public static Boolean booleanValue(String val, Boolean defaultValue) {
        Boolean v = defaultValue;
        if (val != null) {
            switch (val.trim().toLowerCase()) {
            case "true":
            case "y":
            case "yes":
            case "1":
                v = true;
                break;
            case "false":
            case "n":
            case "no":
            case "0":
                v = false;
                break;
            }
        }
        return v;
    }

    public static List<String> stringListValue(String val, String listValueDelimiter) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(listValueDelimiter)).map(e -> {
            return new String(stringValue(e));
        }).collect(Collectors.toList());
    }

    public static List<Integer> integerListValue(String val, String listValueDelimiter) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(listValueDelimiter)).map(e -> {
            return integerValue(e);
        }).collect(Collectors.toList());
    }

    /** TODO: currently only Autosys days<br/>
     * 
     * @param days
     * @return */
    public static List<Integer> getDays(List<String> days) {
        if (days == null) {
            return null;
        }
        return days.stream().map(d -> {
            return getDay(d);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Integer getDay(String day) {
        if (day == null) {
            return null;
        }
        switch (day.toLowerCase()) {
        case "mo":
        case "monday":
            return 1;
        case "tu":
        case "tuesday":
            return 2;
        case "we":
        case "wednesday":
            return 3;
        case "th":
        case "thursday":
            return 4;
        case "fr":
        case "friday":
            return 5;
        case "sa":
        case "saturday":
            return 6;
        case "su":
        case "sunday":
            return 0;
        }
        String msg = String.format("[getDay][unknown day]%s", day);
        LOGGER.error(msg);
        ConverterReport.INSTANCE.addErrorRecord(msg);
        return null;
    }

    public static int toCalendarDayOfWeek(int js7DayOfWeek) {
        switch (js7DayOfWeek) {
        case 0:
            return Calendar.SUNDAY;
        case 1:
            return Calendar.MONDAY;
        case 2:
            return Calendar.TUESDAY;
        case 3:
            return Calendar.WEDNESDAY;
        case 4:
            return Calendar.THURSDAY;
        case 5:
            return Calendar.FRIDAY;
        case 6:
            return Calendar.SATURDAY;
        }
        return Calendar.MONDAY;
    }

    public static Integer getMonthNumber(String month) {
        if (month == null) {
            return null;
        }
        String m = month.toLowerCase();
        if (m.startsWith("jan")) {
            return 1;
        } else if (m.startsWith("feb")) {
            return 2;
        } else if (m.startsWith("mar")) {
            return 3;
        } else if (m.startsWith("apr")) {
            return 4;
        } else if (m.startsWith("may")) {
            return 5;
        } else if (m.startsWith("jun")) {
            return 6;
        } else if (m.startsWith("jul")) {
            return 7;
        } else if (m.startsWith("aug")) {
            return 8;
        } else if (m.startsWith("sep")) {
            return 9;
        } else if (m.startsWith("oct")) {
            return 10;
        } else if (m.startsWith("nov")) {
            return 11;
        } else if (m.startsWith("dec")) {
            return 12;
        }
        return null;
    }

    public static List<Integer> getMonths(Collection<String> months) {
        if (months == null) {
            return null;
        }
        return months.stream().map(m -> getMonthNumber(m)).filter(Objects::nonNull).sorted().collect(Collectors.toList());
    }

    public static List<String> splitEveryNChars(String text, int n) {
        List<String> r = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += n) {
            r.add(text.substring(i, Math.min(length, i + n)));
        }
        return r;
    }

    public static List<Integer> allWeekDays() {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            r.add(Integer.valueOf(i));
        }
        return r;
    }

    // TODO Autosys
    public static List<String> getTimes(List<String> times) {
        if (times == null) {
            return times;
        }
        return times.stream().map(t -> normalizeTime(t)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // TODO Autosys
    public static String normalizeTime(String time) {
        if (SOSString.isEmpty(time)) {
            return null;
        }
        try {
            return Stream.of(time.trim().split(":")).filter(t -> t.length() == 2).map(t -> toTimePart(Integer.parseInt(t.trim()))).collect(Collectors
                    .joining(":"));
        } catch (Throwable e) {
            String msg = String.format("[normalizeTime][time=%s]%s", time, e.toString());
            LOGGER.error(msg);
            ConverterReport.INSTANCE.addErrorRecord(null, msg, e);
            return null;
        }
    }

    //
    public static String toMins(Integer mins) {
        return "00:" + mins + ":00";
    }

    public static String toRepeat(List<Integer> startMinutes) {
        if (startMinutes == null || startMinutes.size() == 0) {
            return null;
        }
        // repeat every hour
        if (startMinutes.size() == 1) {
            return "01:00:00";
        }
        List<Integer> repeats = new ArrayList<>();
        Integer lastMinute = 0;
        for (Integer minute : startMinutes) {
            repeats.add(minute - lastMinute);
            lastMinute = minute;
        }
        return toTimePart(new BigDecimal(repeats.stream().mapToDouble(i -> i).average().orElse(0.00)).setScale(0, RoundingMode.HALF_UP).intValue());
    }

    private static String toTimePart(Integer i) {
        return String.format("%02d", i);
    }

    public static String toTimePart(String i) {
        return String.format("%02d", Integer.parseInt(i));
    }

    public static Node getDocumentRoot(Path p) throws Exception {
        if (Files.exists(p)) {
            return SOSXML.parse(p).getDocumentElement();
        }
        return null;
    }

    public static Map<String, String> attribute2map(Node node) {
        if (node == null) {
            return null;
        }
        NamedNodeMap nmap = node.getAttributes();
        if (nmap == null) {
            return null;
        }
        Map<String, String> map = new TreeMap<>();
        for (int i = 0; i < nmap.getLength(); i++) {
            Node n = nmap.item(i);
            map.put(n.getNodeName().trim(), n.getNodeValue().trim());
        }
        return map;
    }

    public static String getAttributeValue(NamedNodeMap map, String attrName) {
        if (map == null) {
            return null;
        }
        Node n = map.getNamedItem(attrName);
        return n == null ? null : n.getNodeValue().trim();
    }

    // TODO check and trim
    public static String getTextValue(Node node) {
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }

    public static String nodeToString(Node node) {
        try {
            return SOSXML.nodeToString(node, true, 0);
        } catch (Exception e) {
            return node + "";
        }
    }

    /** <br/>
     * TODO NUMBER<br/>
     * TODO use js7.js7.data_for_java.value.JExpression<br />
     * --- js7.data.value.expression.Expression.quote<br />
     * --- js7.data.value.ValuePrinter.quoteString<br />
     */

    @SuppressWarnings("unused")
    private static String quoteValue4JS7TODO(String val) {
        // engine use preferSingleOverDoubleQuotes = false
        // joc preferSingleOverDoubleQuotes = false?
        boolean preferSingleOverDoubleQuotes = false;
        if (SOSString.isEmpty(val)) {
            return "\"\"";
        } else if (val.startsWith("$") || isBoolean(val) || isNumeric(val)) {
            return val;
        } else if (!val.contains("'") && (preferSingleOverDoubleQuotes || hasQuotedChars(val)) && !val.contains(
                "\r")) {/* because '-parsing removes \r */
            return new StringBuilder().append("'").append(val).append("'").toString();
        } else {
            return doubleQuoteStringValue4JS7(val);
        }
    }

    public static String quoteValue4JS7(String val) {
        // joc salways seems to use the double quote ...
        if (SOSString.isEmpty(val)) {
            return "\"\"";
        } else if (val.startsWith("$")) {
            if (val.indexOf(" ") < 0) {
                return val;
            }
            return doubleQuoteStringValue4JS7(val);
        } else if (isBoolean(val) || isNumeric(val)) {
            return val;
        } else {
            return doubleQuoteStringValue4JS7(val);
        }
    }

    // without $ ...
    private static String doubleQuoteStringValue4JS7(String val) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        char[] arr = val.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];
            switch (c) {
            case '\\':
                sb.append("\\\\");
                break;
            case '"':
                sb.append("\\\"");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    @SuppressWarnings("unused")
    private static String doubleQuoteStringValue4JS7Engine(String val) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        char[] arr = val.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];
            switch (c) {
            case '\\':
                sb.append("\\\\");
                break;
            case '"':
                sb.append("\\\"");
                break;
            case '$':
                sb.append("\\$");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private static boolean hasQuotedChars(String val) {
        return val.codePoints().mapToObj(c -> Character.valueOf((char) c)).filter(e -> QUOTED_CHARS.contains(e)).findFirst().orElse(null) != null;
    }

    // /sos/xxx/ -> /sos/xxx/
    // /sos -> /sos/
    // \sos\xxx -> /sos/xxx/
    public static String normalizeDirectoryPath(String path) {
        if (path == null) {
            return null;
        }
        return "/" + StringUtils.strip(path.trim().replace('\\', '/'), "/").concat("/");
    }

    public static String normalizedPathPart(String val) {
        if (val == null) {
            return null;
        }
        return val.replaceAll(" ", "_").replaceAll(":", "_");
    }

    public static String getFileName(String p) {
        if (p.endsWith("/")) {
            return "";
        }
        int i = p.lastIndexOf("/");
        return i > -1 ? p.substring(i + 1) : p;
    }

    public static Path resolvePath(Path parent, String name) {
        try {
            return parent.resolve(name);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", parent, name, e.toString()), e);
            ConverterReport.INSTANCE.addErrorRecord(parent, name, e.toString());
            return null;
        }
    }

    public static String getRelativePath(Path dir, Path dirFile) {
        return dirFile.toString().substring(dir.toString().length() + 1);
    }

    public static StringBuilder getJS7ConverterComment(Path inputFile, boolean afterCleanup) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nExported by JS7Converter ");
        if (afterCleanup) {
            sb.append("after cleanup ");
        }
        try {
            sb.append("at ").append(SOSDate.getCurrentDateTimeAsString()).append(" ");
        } catch (Throwable e) {
        }
        sb.append("from file ").append(inputFile.getFileName());
        sb.append("\n");
        return sb;
    }

    public static Job setFromConfig(JS7ConverterConfig config, Job j) {
        if (config.getJobConfig().getForcedGraceTimeout() != null) {
            j.setGraceTimeout(config.getJobConfig().getForcedGraceTimeout());
        }
        if (config.getJobConfig().getForcedParallelism() != null) {
            j.setParallelism(config.getJobConfig().getForcedParallelism());
        }
        if (config.getJobConfig().getForcedFailOnErrWritten() != null) {
            j.setFailOnErrWritten(config.getJobConfig().getForcedFailOnErrWritten());
        }
        return j;
    }

    public static String getWorkflowName(Path workflowPath) {
        return workflowPath.getFileName().toString().replace(".workflow.json", "");
    }

    public static String getWorkflowBasePath(Path workflowPath) {
        return workflowPath.toString().replace(".workflow.json", "");
    }

    public static Path getSchedulePathFromJS7Path(Path workflowPath, String workflowName, String additionalName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return JS7ConverterHelper.resolvePath(parent, workflowName + additionalName + ".schedule.json");
    }

    public static Path getFileOrderSourcePathFromJS7Path(Path workflowPath, String workflowName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return JS7ConverterHelper.resolvePath(parent, workflowName + ".fileordersource.json");
    }

    public static Path getJobResourcePath(Path parent, String js7Name) {
        return parent.resolve(js7Name + ".jobresource.json");
    }

    public static Path getLockPath(Path parent, String js7Name) {
        return parent.resolve(js7Name + ".lock.json");
    }

    public static Path getJobTemplatePath(Path parent, String js7Name) {
        return parent.resolve(js7Name + ".jobtemplate.json");
    }

    public static Path getCalendarPath(Path parent, String js7Name) {
        return parent.resolve(js7Name + ".calendar.json");
    }

    public static Path getNoticeBoardPathFromJS7Path(Path workflowPath, String boardName) {
        Path parent = workflowPath == null ? null : workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(boardName + ".noticeboard.json");
    }

    public static void createNoticeBoardFromWorkflowPath(JS7ConverterResult result, Path workflowPath, String boardName, String boardTitle) {
        result.add(getNoticeBoardPathFromJS7Path(workflowPath, boardName), createNoticeBoard(boardTitle));
    }

    public static void createNoticeBoardByParentPath(JS7ConverterResult result, Path parentPath, String boardName, String boardTitle) {
        result.add(parentPath.resolve(boardName + ".noticeboard.json"), createNoticeBoard(boardTitle));
    }

    private static Board createNoticeBoard(String boardTitle) {
        Board b = new Board();
        b.setTitle(boardTitle);
        b.setEndOfLife("$js7EpochMilli + 1 * 24 * 60 * 60 * 1000");
        b.setExpectOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");
        b.setPostOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");
        return b;
    }

    public static Path getJS7ObjectPath(Path path) {
        Path output = path.getRoot() == null ? Paths.get("") : path.getRoot();
        for (int i = 0; i < path.getNameCount(); i++) {
            output = output.resolve(getJS7ObjectName(path, path.getName(i).toString()));
        }
        return output;
    }

    public static String getJS7ObjectName(String oldName) {
        return getJS7ObjectName(null, oldName);
    }

    public static String getJS7ObjectName(Path oldPath, String oldName) {
        String error = SOSCheckJavaVariableName.check(oldName);
        if (error == null) {
            return oldName;
        }
        String newName = SOSCheckJavaVariableName.makeStringRuleConform(oldName);
        if (SOSString.isEmpty(newName)) {
            converterNameCounter++;
            newName = "js7_converter_name_" + converterNameCounter;
        }
        ConverterReport.INSTANCE.addAnalyzerRecord(oldPath, "MAKE STRING RULE CONFORM", "[changed][" + oldName + "]" + newName);
        return newName;
    }

    public static JS7ConverterResult convertAgents(JS7ConverterResult result, List<JS7Agent> agents) {
        for (JS7Agent agent : agents) {
            Agent a = null;
            if (agent.getStandaloneAgent() != null) {
                a = new Agent();
                a.setStandaloneAgent(JS7AgentConverter.convertStandaloneAgent(agent));
            } else if (agent.getAgentCluster() != null) {
                a = new Agent();
                a.setAgentCluster(JS7AgentConverter.convertAgentCluster(agent));
                a.setSubagentClusters(JS7AgentConverter.convertSubagentClusters(agent));
            }
            if (a == null) {
                ConverterReport.INSTANCE.addErrorRecord("[agent=" + agent.getJS7AgentName()
                        + "][cannot be converted]missing standalone or agentCluster");
            } else {
                result.add(Paths.get(agent.getJS7AgentName() + ".agent.json"), a);
            }
        }
        return result;
    }

    public static JS7ConverterResult convertIncludeScripts(JS7ConverterResult result, Map<String, String> includeScripts) {
        for (Map.Entry<String, String> e : includeScripts.entrySet()) {
            Script si = new Script();
            si.setScript(e.getValue());

            result.add(Paths.get(e.getKey() + ".includescript.json"), si);
        }
        return result;
    }

    public static JS7ConverterResult convertLocks2RootFolder(JS7ConverterResult result, Map<String, Integer> locks) {
        for (Map.Entry<String, Integer> e : locks.entrySet()) {
            Lock l = new Lock();
            l.setTitle(e.getKey());
            l.setLimit(e.getValue());
            result.add(Paths.get(e.getKey() + ".lock.json"), l);
        }
        return result;
    }

    public static List<String> removeDuplicates(List<String> val) {
        return val.stream().distinct().collect(Collectors.toList());
    }

    public static PostNotices newPostNotices(List<String> val) {
        return new PostNotices(removeDuplicates(val));
    }

    public static com.sos.inventory.model.calendar.Calendar createDefaultWorkingDaysCalendar() {
        com.sos.inventory.model.calendar.Calendar c = new com.sos.inventory.model.calendar.Calendar();
        c.setType(CalendarType.WORKINGDAYSCALENDAR);

        Frequencies fr = new Frequencies();
        WeekDays wd = new WeekDays();
        wd.setDays(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        fr.setWeekdays(Collections.singletonList(wd));
        c.setIncludes(fr);

        return c;
    }

    public static String parentheses(String input) {
        long a = input.chars().filter(c -> c == '(').count();
        long b = input.chars().filter(c -> c == ')').count();
        long diff = a - b;

        if (diff == 0) {
            return input;
        }

        String s = new String(input);
        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                s += ")";
            }
        } else {
            for (int i = 0; i < Math.abs(diff); i++) {
                s = "(" + s;
            }
        }
        return s;
    }

    private static boolean isNumeric(String val) {
        if (val == null) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(val).matches();
    }

    private static boolean isBoolean(String val) {
        if (val == null) {
            return false;
        }
        return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false");
    }
}
