package com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.inventory.model.job.Job;
import com.sos.js7.converter.autosys.common.v12.job.JobHTTP;
import com.sos.js7.converter.autosys.common.v12.job.custom.JobWSDOC;
import com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.JITLJobConverter;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class RESTJobConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RESTJobConverter.class);

    private static final String JITL_JOB_CLASSNAME = "com.sos.jitl.jobs.rest.RESTClientJob";

    public static void clear() {

    }

    public static Job setExecutable(Job j, JobHTTP jilJob, String platform) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_CLASSNAME);

        Map<String, String> request = new LinkedHashMap<>();
        request.put("endpoint", jilJob.getProviderUrl().getValue());
        request.put("method", jilJob.getInvocationType().getValue());
        if (!jilJob.getRequestBody().isEmpty()) {
            request.put("body", jilJob.getRequestBody().getValue());
        }
        if (!jilJob.getJ2eeConnUser().isEmpty()) {
            request.put("username", jilJob.getJ2eeConnUser().getValue());
            request.put("password", JITLJobConverter.DEFAULT_PASSWORD);
        }

        try {
            // double quote
            String r = JS7ConverterHelper.JSON_OM.writeValueAsString(request);
            r = JS7ConverterHelper.JSON_OM.writeValueAsString(r);
            JITLJobConverter.addArgument(j, "request", r);
        } catch (JsonProcessingException e) {
            LOGGER.error("[setExecutable][" + jilJob + "]" + e, e);
        }

        return j;
    }

    public static Job setExecutable(Job j, JobWSDOC jilJob, String platform) {
        j = JITLJobConverter.createExecutable(j, JITL_JOB_CLASSNAME);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("endpoint", jilJob.getEndpointUrl().getValue());
        request.put("method", "POST");
        request.put("headers", getWSDOCHeaders());

        request.put("body", getWSDOCBody(jilJob));

        try {
            // double quote
            String r = JS7ConverterHelper.JSON_OM.writeValueAsString(request);
            r = JS7ConverterHelper.JSON_OM.writeValueAsString(r);
            JITLJobConverter.addArgument(j, "request", r);
        } catch (JsonProcessingException e) {
            LOGGER.error("[setExecutable][" + jilJob + "]" + e, e);
        }

        return j;
    }

    private static Object getWSDOCHeaders() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("key", "Content-Type");
        m.put("value", "text/xml; charset=UTF-8");

        @SuppressWarnings("unchecked")
        Map<String, String>[] arr = new LinkedHashMap[1];
        arr[0] = m;
        return arr;
    }

    private static String getWSDOCBody(JobWSDOC jilJob) {
        String wsdl_targetNamespace = jilJob.getWsdlUrl().getValue();
        String ws_parameter_root = "";
        String ws_parameter_child = null;
        String ws_parameter_child_value = null;
        if (jilJob.getWsParameters().getValue() != null) {
            int i = 0;
            for (String p : jilJob.getWsParameters().getValue()) {
                if (i == 0) {
                    ws_parameter_root = p.replace("param_name=\"/", "").replace("\"", "");
                } else {
                    String[] arr = p.split(",");
                    if (arr.length == 2) {
                        ws_parameter_child = arr[0].replace("param_name=\"/" + ws_parameter_root + "/", "").replace("\"", "");
                        ws_parameter_child_value = arr[1].replace("param_value=", "").replace("\"", "");
                    }
                }
                i++;
            }
        }
        return buildWSDOCXml(wsdl_targetNamespace, ws_parameter_root, ws_parameter_child, ws_parameter_child_value);
    }

    public static String buildWSDOCXml(String wsdl_targetNamespace, String ws_parameter_root, String ws_parameter_child,
            String ws_parameter_child_value) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("<soapenv:Envelope ");
        sb.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        sb.append("xmlns:tns=\"").append(wsdl_targetNamespace).append("\">").append(JS7ConverterHelper.JS7_NEW_LINE);

        sb.append("  <soapenv:Header/>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("  <soapenv:Body>").append(JS7ConverterHelper.JS7_NEW_LINE);

        sb.append("    <tns:").append(ws_parameter_root).append(">").append(JS7ConverterHelper.JS7_NEW_LINE);

        if (ws_parameter_child != null) {
            sb.append("      <tns:").append(ws_parameter_child).append(">");
            sb.append(ws_parameter_child_value);
            sb.append("</tns:").append(ws_parameter_child).append(">").append(JS7ConverterHelper.JS7_NEW_LINE);
        }

        sb.append("    </tns:").append(ws_parameter_root).append(">").append(JS7ConverterHelper.JS7_NEW_LINE);

        sb.append("  </soapenv:Body>").append(JS7ConverterHelper.JS7_NEW_LINE);
        sb.append("</soapenv:Envelope>");
        return sb.toString();
    }

}
