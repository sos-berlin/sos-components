package com.sos.js7.converter.js1.common.jobchain.node;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;

public class JobChainNode extends AJobChainNode {

    private static final String ELEMENT_ON_RETURN_CODE = "on_return_code";

    private static final String ATTR_JOB = "job";
    private static final String ATTR_STATE = "state";
    private static final String ATTR_NEXT_STATE = "next_state";
    private static final String ATTR_ERROR_STATE = "error_state";
    private static final String ATTR_ON_ERROR = "on_error";
    private static final String ATTR_DELAY = "delay";

    private List<JobChainNodeOnReturnCode> onReturnCodes;

    private String job; // job_name
    private String state;
    private String nextState;
    private String errorState;

    private String onError; // suspend|setback
    private String delay; // seconds

    private Path jobPath;

    protected JobChainNode(DirectoryParserResult pr, Path jobChainPath, JobChainNodeType type, Node node) {
        super(jobChainPath, type, node);
        job = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_JOB));
        state = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_STATE));
        nextState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_NEXT_STATE));
        errorState = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_ERROR_STATE));
        onError = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_ON_ERROR));
        delay = JS7ConverterHelper.stringValue(getAttributes().get(ATTR_DELAY));

        setJobPath(pr);

        try {
            SOSXMLXPath xpath = SOSXML.newXPath();
            NodeList nl = xpath.selectNodes(node, ".//" + ELEMENT_ON_RETURN_CODE);
            if (nl != null && nl.getLength() > 0) {
                onReturnCodes = new ArrayList<>();
                for (int i = 0; i < nl.getLength(); i++) {
                    onReturnCodes.add(new JobChainNodeOnReturnCode(getJobChainPath(), nl.item(i)));
                }
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addErrorRecord(getJobChainPath(), ELEMENT_ON_RETURN_CODE, e);
        }
    }

    private void setJobPath(DirectoryParserResult pr) {
        if (!SOSString.isEmpty(job)) {
            Path jp = JS12JS7Converter.findIncludeFile(pr, getJobChainPath(), Paths.get(job + EConfigFileExtensions.JOB.extension()));
            if (jp == null) {
                jobPath = jp;
            } else {
                jobPath = jp.toAbsolutePath();
                pr.putOrderJobsUsage(getJobChainPath().toAbsolutePath(), jobPath);
            }
        }
    }

    public String getJob() {
        return job;
    }

    public String getState() {
        return state;
    }

    public String getNextState() {
        return nextState;
    }

    public String getErrorState() {
        return errorState;
    }

    public String getOnError() {
        return onError;
    }

    public String getDelay() {
        return delay;
    }

    public List<JobChainNodeOnReturnCode> getOnReturnCodes() {
        return onReturnCodes;
    }

    public Path getJobPath() {
        return jobPath;
    }

}
