package com.sos.js7.converter.js1.common.job;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Include;
import com.sos.js7.converter.js1.common.Monitor;
import com.sos.js7.converter.js1.common.Params;
import com.sos.js7.converter.js1.common.RunTime;
import com.sos.js7.converter.js1.common.Script;
import com.sos.js7.converter.js1.common.lock.LockUse;

public abstract class ACommonJob {

    public enum Type {
        STANDALONE, ORDER
    }

    private static final String ATTR_NAME = "name";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_ORDER = "order";
    private static final String ATTR_SPOOLER_ID = "spooler_id";
    private static final String ATTR_MIN_TASKS = "min_tasks";
    private static final String ATTR_TASKS = "tasks";
    private static final String ATTR_PROCESS_CLASS = "process_class";
    private static final String ATTR_ENABLED = "enabled";
    private static final String ATTR_PRIORITY = "priority";
    private static final String ATTR_JAVA_OPTIONS = "java_options";
    private static final String ATTR_TIMEOUT = "timeout";
    private static final String ATTR_FORCE_IDLE_TIMEOUT = "force_idle_timeout";
    private static final String ATTR_WARN_IF_LONGER_THAN = "warn_if_longer_than";
    private static final String ATTR_WARN_IF_SHORTER_THAN = "warn_if_shorter_than";
    private static final String ATTR_IGNORE_SIGNALS = "ignore_signals";
    private static final String ATTR_REPLACE = "replace";
    private static final String ATTR_STOP_ON_ERROR = "stop_on_error";
    private static final String ATTR_TEMPORARY = "temporary";
    private static final String ATTR_VISIBLE = "visible";

    private static final String ELEMENT_SETTINGS = "settings";
    private static final String ELEMENT_DESCRIPTION = "description";
    private static final String ELEMENT_LOCK_USE = "lock.use";
    private static final String ELEMENT_ENVIRONMENT = "environment";
    private static final String ELEMENT_PARAMS = "params";
    private static final String ELEMENT_SCRIPT = "script";
    private static final String ELEMENT_MONITOR = "monitor";
    private static final String ELEMENT_START_WHEN_DIRECTORY_CHANGED = "start_when_directory_changed";
    private static final String ELEMENT_DELAY_AFTER_ERROR = "delay_after_error";
    private static final String ELEMENT_RUN_TIME = "run_time";

    private final Type type;
    private Path path;

    private Settings settings;
    private Description description;
    private List<LockUse> lockUses;
    private Environment environment;
    private Params params;
    private Script script;
    private List<Monitor> monitors = new ArrayList<>();
    private List<StartWhenDirectoryChanged> startWhenDirectoryChanged = new ArrayList<>();
    private List<DelayAfterError> delayAfterError = new ArrayList<>(); // delayOrderAfterSetback is declared in the OrderJob
    private RunTime runTime;

    // ATTRIBUTE
    private String name;
    private String title;
    private String spoolerId;
    private Integer minTasks; // The minimum number of tasks kept running
    private Integer tasks;

    private String processClass;
    // This attribute can be given the following values: idle, below_normal, normal, above_normal and high or
    // the numerical values allowed for the operating system being used.
    private String priority;
    private String javaOptions;
    // duration - The time allowed for an operation
    // duration can be specified in seconds or in the HH:MM or HH:MM:SS formats.
    private String timeout;
    private Boolean forceIdleTimeout;// yes_no (Initial value: no) Task ended by idle_timeout despite min_task

    private String warnIfLongerThan;// HH:MM:SS|seconds|percentage%
    private String warnIfShorterThan;// HH:MM:SS|seconds|percentage%

    private Boolean enabled; // yes|no Disable a Job.
    private String ignoreSignals; // all|signalnames. (Initial value: no) .Is only relevant for UNIX systems.
    private Boolean replace; // yes|no (Initial value: yes)
    private Boolean stopOnError; // yes|no (Initial value: yes)
    private Boolean temporary; // yes_no
    private String visible; // yes|no|never

    public static ACommonJob parse(Path file) throws Exception {
        Node node = JS7ConverterHelper.getDocumentRoot(file);

        ACommonJob job = null;
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        String order = map == null ? "no" : map.get(ATTR_ORDER);
        if (order == null || order.equals("no")) {
            job = new StandaloneJob();
        } else {
            job = new OrderJob();
        }
        job.name = EConfigFileExtensions.getName(EConfigFileExtensions.JOB, file.getFileName().toString());
        job.path = file;
        job.parse(node, map);
        return job;
    }

    public SOSXMLXPath parse(Node node, Map<String, String> attributes) throws Exception {
        // ATTRIBUTES ...
        setAttributes(attributes);

        // ELEMENTS
        SOSXMLXPath xpath = SOSXML.newXPath();
        Node settings = xpath.selectNode(node, "./" + ELEMENT_SETTINGS);
        if (settings != null) {
            this.settings = new Settings(xpath, settings);
        }
        Node description = xpath.selectNode(node, "./" + ELEMENT_DESCRIPTION);
        if (description != null) {
            this.description = new Description(xpath, description);
        }
        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_LOCK_USE);
        if (l != null && l.getLength() > 0) {
            for (int i = 0; i < l.getLength(); i++) {
                this.lockUses.add(new LockUse(xpath, l.item(i)));
            }
        }

        Node environment = xpath.selectNode(node, "./" + ELEMENT_ENVIRONMENT);
        if (environment != null) {
            this.environment = new Environment(xpath, environment);
        }

        Node params = xpath.selectNode(node, "./" + ELEMENT_PARAMS);
        if (params != null) {
            this.params = new Params(xpath, params);
        }

        Node script = xpath.selectNode(node, "./" + ELEMENT_SCRIPT);
        if (script != null) {
            this.script = new Script(xpath, script);
        }

        l = xpath.selectNodes(node, "./" + ELEMENT_MONITOR);
        if (l != null && l.getLength() > 0) {
            for (int i = 0; i < l.getLength(); i++) {
                this.monitors.add(new Monitor(xpath, l.item(i)));
            }
        }

        l = xpath.selectNodes(node, "./" + ELEMENT_START_WHEN_DIRECTORY_CHANGED);
        if (l != null && l.getLength() > 0) {
            for (int i = 0; i < l.getLength(); i++) {
                this.startWhenDirectoryChanged.add(new StartWhenDirectoryChanged(xpath, l.item(i)));
            }
        }

        l = xpath.selectNodes(node, "./" + ELEMENT_DELAY_AFTER_ERROR);
        if (l != null && l.getLength() > 0) {
            this.delayAfterError = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                this.delayAfterError.add(new DelayAfterError(xpath, l.item(i)));
            }
        }

        Node runTime = xpath.selectNode(node, "./" + ELEMENT_RUN_TIME);
        if (runTime != null) {
            this.runTime = new RunTime(xpath, runTime);
        }
        return xpath;
    }

    private void setAttributes(Map<String, String> attributes) {
        String name = JS7ConverterHelper.stringValue(attributes.get(ATTR_NAME));
        if (name != null) { // overwrite the this.name from file name
            this.name = name;
        }
        title = JS7ConverterHelper.stringValue(attributes.get(ATTR_TITLE));
        spoolerId = JS7ConverterHelper.stringValue(attributes.get(ATTR_SPOOLER_ID));
        minTasks = JS7ConverterHelper.integerValue(attributes.get(ATTR_MIN_TASKS));
        tasks = JS7ConverterHelper.integerValue(attributes.get(ATTR_TASKS));

        processClass = JS7ConverterHelper.stringValue(attributes.get(ATTR_PROCESS_CLASS));
        priority = JS7ConverterHelper.stringValue(attributes.get(ATTR_PRIORITY));
        javaOptions = JS7ConverterHelper.stringValue(attributes.get(ATTR_JAVA_OPTIONS));
        timeout = JS7ConverterHelper.stringValue(attributes.get(ATTR_TIMEOUT));
        forceIdleTimeout = JS7ConverterHelper.booleanValue(attributes.get(ATTR_FORCE_IDLE_TIMEOUT), false);
        warnIfLongerThan = JS7ConverterHelper.stringValue(attributes.get(ATTR_WARN_IF_LONGER_THAN));
        warnIfShorterThan = JS7ConverterHelper.stringValue(attributes.get(ATTR_WARN_IF_SHORTER_THAN));
        enabled = JS7ConverterHelper.booleanValue(attributes.get(ATTR_ENABLED), true);

        ignoreSignals = JS7ConverterHelper.stringValue(attributes.get(ATTR_IGNORE_SIGNALS));
        replace = JS7ConverterHelper.booleanValue(attributes.get(ATTR_REPLACE), true);
        stopOnError = JS7ConverterHelper.booleanValue(attributes.get(ATTR_STOP_ON_ERROR), true);
        temporary = JS7ConverterHelper.booleanValue(attributes.get(ATTR_TEMPORARY));
        visible = JS7ConverterHelper.stringValue(attributes.get(ATTR_VISIBLE));

    }

    public ACommonJob(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Path getPath() {
        return path;
    }

    public Settings getSettings() {
        return settings;
    }

    public Description getDescription() {
        return description;
    }

    public List<LockUse> getLockUses() {
        return lockUses;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Params getParams() {
        return params;
    }

    public Script getScript() {
        return script;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public List<StartWhenDirectoryChanged> getStartWhenDirectoryChanged() {
        return startWhenDirectoryChanged;
    }

    public List<DelayAfterError> getDelayAfterError() {
        return delayAfterError;
    }

    public RunTime getRunTime() {
        return runTime;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getSpoolerId() {
        return spoolerId;
    }

    public Integer getMinTasks() {
        return minTasks;
    }

    public Integer getTasks() {
        return tasks;
    }

    public String getProcessClass() {
        return processClass;
    }

    public String getPriority() {
        return priority;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public String getTimeout() {
        return timeout;
    }

    public Boolean getForceIdleTimeout() {
        return forceIdleTimeout;
    }

    public String getWarnIfLongerThan() {
        return warnIfLongerThan;
    }

    public String getWarnIfShorterThan() {
        return warnIfShorterThan;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getIgnoreSignals() {
        return ignoreSignals;
    }

    public Boolean getReplace() {
        return replace;
    }

    public Boolean getStopOnError() {
        return stopOnError;
    }

    public Boolean getTemporary() {
        return temporary;
    }

    public String getVisible() {
        return visible;
    }

    public class Settings {

        private static final String ELEMENT_MAIL_ON_ERROR = "mail_on_error";
        private static final String ELEMENT_MAIL_ON_WARNING = "mail_on_warning";
        private static final String ELEMENT_MAIL_ON_SUCCESS = "mail_on_success";
        private static final String ELEMENT_MAIL_ON_PROCESS = "mail_on_process";
        private static final String ELEMENT_MAIL_ON_DELAY_AFTER_ERROR = "mail_on_delay_after_error";
        private static final String ELEMENT_MAIL_TO = "log_mail_to";
        private static final String ELEMENT_MAIL_CC = "log_mail_cc";
        private static final String ELEMENT_MAIL_BCC = "log_mail_bcc";

        private boolean mailOnError;
        private boolean mailOnWarning;
        private boolean mailOnSuccess;
        private boolean mailOnProcess;
        private boolean mailOnDelayAfterError;
        private String mailTo;
        private String mailCC;
        private String mailBCC;

        public Settings(SOSXMLXPath xpath, Node node) throws Exception {
            mailOnError = getBooleanValue(xpath, node, ELEMENT_MAIL_ON_ERROR);
            mailOnWarning = getBooleanValue(xpath, node, ELEMENT_MAIL_ON_WARNING);
            mailOnSuccess = getBooleanValue(xpath, node, ELEMENT_MAIL_ON_SUCCESS);
            mailOnProcess = getBooleanValue(xpath, node, ELEMENT_MAIL_ON_PROCESS);
            mailOnDelayAfterError = getBooleanValue(xpath, node, ELEMENT_MAIL_ON_DELAY_AFTER_ERROR);

            mailTo = getStringValue(xpath, node, ELEMENT_MAIL_TO);
            mailCC = getStringValue(xpath, node, ELEMENT_MAIL_CC);
            mailBCC = getStringValue(xpath, node, ELEMENT_MAIL_BCC);
        }

        private boolean getBooleanValue(SOSXMLXPath xpath, Node node, String elementName) throws SOSXMLXPathException {
            Node n = xpath.selectNode(node, "./" + elementName);
            return JS7ConverterHelper.booleanValue(JS7ConverterHelper.getTextValue(n), false);
        }

        private String getStringValue(SOSXMLXPath xpath, Node node, String elementName) throws SOSXMLXPathException {
            return JS7ConverterHelper.getTextValue(xpath.selectNode(node, "./" + elementName));
        }

        public boolean isMailOnError() {
            return mailOnError;
        }

        public boolean isMailOnWarning() {
            return mailOnWarning;
        }

        public boolean isMailOnSuccess() {
            return mailOnSuccess;
        }

        public boolean isMailOnProcess() {
            return mailOnProcess;
        }

        public boolean isMailOnDelayAfterError() {
            return mailOnDelayAfterError;
        }

        public String getMailTo() {
            return mailTo;
        }

        public String getMailCC() {
            return mailCC;
        }

        public String getMailBCC() {
            return mailBCC;
        }

    }

    public class Description {

        private static final String ELEMENT_INCLUDE = "include";

        private final Include include;
        private final String text;

        public Description(SOSXMLXPath xpath, Node node) throws Exception {
            Node include = xpath.selectNode(node, "./" + ELEMENT_INCLUDE);
            if (include == null) {
                this.include = null;
            } else {
                this.include = new Include(xpath, include);
            }
            this.text = JS7ConverterHelper.getTextValue(node);
        }

        public Include getInclude() {
            return include;
        }

        public String getText() {
            return text;
        }
    }

    public class Environment {

        private static final String ELEMENT_VARIABLE = "variable";

        private Map<String, String> variables;

        public Environment(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            NodeList nl = xpath.selectNodes(node, "./" + ELEMENT_VARIABLE);
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    NamedNodeMap m = nl.item(i).getAttributes();
                    variables.put(JS7ConverterHelper.getAttributeValue(m, "name"), JS7ConverterHelper.getAttributeValue(m, "value"));
                }
            }
        }

        public Map<String, String> getVariables() {
            return variables;
        }
    }

    public class StartWhenDirectoryChanged {

        private static final String ATTR_DIRECTORY = "directory";
        private static final String ATTR_REGEX = "regex";

        private String directory;
        private String regex;

        private StartWhenDirectoryChanged(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> attributes = JS7ConverterHelper.attribute2map(node);
            directory = JS7ConverterHelper.stringValue(attributes.get(ATTR_DIRECTORY));
            regex = JS7ConverterHelper.stringValue(attributes.get(ATTR_REGEX));
        }

        public String getDirectory() {
            return directory;
        }

        public String getRegex() {
            return regex;
        }

    }

    public class DelayAfterError {

        private static final String ATTR_DELAY = "delay";
        private static final String ATTR_ERROR_COUNT = "error_count";

        // seconds|HH:MM|HH:MM:SS|stop
        private String delay;
        private Integer errorCount;

        private DelayAfterError(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> attributes = JS7ConverterHelper.attribute2map(node);
            delay = JS7ConverterHelper.stringValue(attributes.get(ATTR_DELAY));
            errorCount = JS7ConverterHelper.integerValue(attributes.get(ATTR_ERROR_COUNT));
        }

        public String getDelay() {
            return delay;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

    }

}
