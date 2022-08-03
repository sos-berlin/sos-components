package com.sos.js7.converter.js1.common.jobchain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.jobchain.node.AJobChainNode;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.JS7Converter;

public class JobChain {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobChain.class);

    private static final String ATTR_TITLE = "title";
    private static final String ATTR_PROCESS_CLASS = "process_class";
    private static final String ATTR_FW_PROCESS_CLASS = "file_watching_process_class";
    private static final String ATTR_ORDERS_RECOVERABLE = "orders_recoverable";
    private static final String ATTR_MAX_ORDERS = "max_orders";
    private static final String ATTR_DISTRIBUTED = "distributed";
    private static final String ATTR_VISIBLE = "visible";

    private Path path;// extra
    private JobChainConfig config;
    private List<JobChainOrder> orders = new ArrayList<>();
    private List<AJobChainNode> nodes = new ArrayList<>();

    private final String name;
    private String title;

    private ProcessClass processClass;
    private ProcessClass fileWatchingProcessClass;
    private Integer maxOrders;

    private Boolean ordersRecoverable; // yes|no
    private Boolean distributed; // yes|no
    private String visible; // yes|no|never

    public JobChain(DirectoryParserResult pr, String name, List<Path> jobChainFiles) throws Exception {
        this.name = name;

        Path jobChainFile = handleFiles(pr, jobChainFiles);
        if (jobChainFile == null) {
            throw new Exception("job chain file not found");
        }
        parse(pr, jobChainFile);
    }

    private void parse(DirectoryParserResult pr, Path file) throws Exception {
        path = file;
        Node node = JS7ConverterHelper.getDocumentRoot(file);
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        title = JS7ConverterHelper.stringValue(map.get(ATTR_TITLE));
        processClass = convertProcessClass(pr, file, map, ATTR_PROCESS_CLASS);
        fileWatchingProcessClass = convertProcessClass(pr, file, map, ATTR_FW_PROCESS_CLASS);
        maxOrders = JS7ConverterHelper.integerValue(map.get(ATTR_MAX_ORDERS));
        ordersRecoverable = JS7ConverterHelper.booleanValue(map.get(ATTR_ORDERS_RECOVERABLE));
        distributed = JS7ConverterHelper.booleanValue(map.get(ATTR_DISTRIBUTED));
        visible = JS7ConverterHelper.stringValue(map.get(ATTR_VISIBLE));
        parseNode(node);
    }

    private void parseNode(Node node) throws Exception {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                AJobChainNode jcn = AJobChainNode.parse(path,n);
                if (jcn != null) {
                    nodes.add(jcn);
                }
            }
        }
    }

    private ProcessClass convertProcessClass(DirectoryParserResult pr, Path currentPath, Map<String, String> m, String attrName) {
        String includePath = JS7ConverterHelper.stringValue(m.get(attrName));
        if (SOSString.isEmpty(includePath)) {
            return null;
        }
        try {
            Path p = JS7Converter.findIncludeFile(pr, currentPath, Paths.get(includePath + EConfigFileExtensions.PROCESS_CLASS.extension()));
            if (p != null) {
                return new ProcessClass(p);
            } else {
                ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + attrName + "]ProcessClass not found=" + includePath, "");
                return null;
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addErrorRecord(currentPath, "[attribute=" + attrName + "]ProcessClass not found=" + includePath, e);
            return null;
        }
    }

    private Path handleFiles(DirectoryParserResult pr, List<Path> files) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        Path jobChainFile = null;
        for (Path file : files) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[handleFiles][%s]%s", this.name, file));
            }
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(EConfigFileExtensions.ORDER.extension())) {
                orders.add(new JobChainOrder(pr, file));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN.extension())) {
                jobChainFile = file;
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN_CONFIG.extension())) {
                config = new JobChainConfig(pr, file);
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[handleFiles][%s]orders=%s,config=%s", this.name, orders.size(), (config == null ? "0" : config.getPath())));
        }
        return jobChainFile;
    }

    public List<JobChainOrder> getOrders() {
        return orders;
    }

    public JobChainConfig getConfig() {
        return config;
    }

    public List<AJobChainNode> getNodes() {
        return nodes;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public ProcessClass getProcessClass() {
        return processClass;
    }

    public ProcessClass getFileWatchingProcessClass() {
        return fileWatchingProcessClass;
    }

    public Integer getMaxOrders() {
        return maxOrders;
    }

    public Boolean getOrdersRecoverable() {
        return ordersRecoverable;
    }

    public Boolean getDistributed() {
        return distributed;
    }

    public String getVisible() {
        return visible;
    }

    public Path getPath() {
        return path;
    }
}
