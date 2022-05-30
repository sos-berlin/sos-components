package com.sos.js7.converter.js1.common.jobchain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.jobchain.node.AJobChainNode;

public class JobChain {

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

    private String processClass;
    private String fileWatchingProcessClass;
    private Integer maxOrders;

    private Boolean ordersRecoverable; // yes|no
    private Boolean distributed; // yes|no
    private String visible; // yes|no|never

    public JobChain(String name, List<Path> jobChainFiles) throws Exception {
        this.name = name;

        Path jobChainFile = handleFiles(jobChainFiles);
        if (jobChainFile == null) {
            throw new Exception("missing job chain file");
        }
        parse(jobChainFile);
    }

    private void parse(Path file) throws Exception {
        path = file;
        Node node = JS7ConverterHelper.getDocumentRoot(file);
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        title = JS7ConverterHelper.stringValue(map.get(ATTR_TITLE));
        processClass = JS7ConverterHelper.stringValue(map.get(ATTR_PROCESS_CLASS));
        fileWatchingProcessClass = JS7ConverterHelper.stringValue(map.get(ATTR_FW_PROCESS_CLASS));
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
                AJobChainNode jcn = AJobChainNode.parse(n);
                if (jcn != null) {
                    nodes.add(jcn);
                }
            }
        }
    }

    private Path handleFiles(List<Path> files) throws Exception {
        Path jobChainFile = null;
        for (Path file : files) {
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(EConfigFileExtensions.ORDER.extension())) {
                orders.add(new JobChainOrder(file));
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN.extension())) {
                jobChainFile = file;
            } else if (fileName.endsWith(EConfigFileExtensions.JOB_CHAIN_CONFIG.extension())) {
                config = new JobChainConfig(file);
            }
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

    public String getProcessClass() {
        return processClass;
    }

    public String getFileWatchingProcessClass() {
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
