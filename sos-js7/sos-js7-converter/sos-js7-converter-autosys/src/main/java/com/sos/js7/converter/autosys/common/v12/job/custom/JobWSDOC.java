package com.sos.js7.converter.autosys.common.v12.job.custom;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobWSDOC extends ACommonMachineJob {

    private static final String ATTR_ENDPOINT_URL = "endpoint_url";
    private static final String ATTR_WSDL_URL = "wsdl_url";
    private static final String ATTR_WSDL_OPERATION = "wsdl_operation";
    private static final String ATTR_SERVICE_NAME = "service_name";
    private static final String ATTR_PORT_NAME = "port_name";
    private static final String ATTR_WS_GLOBAL_PROXY_DEFAULTS = "ws_global_proxy_defaults";
    private static final String ATTR_WS_PARAMETER = "ws_parameter";

    // https://xxxx - not null?
    private SOSArgument<String> endpointUrl = new SOSArgument<>(ATTR_ENDPOINT_URL, false);
    // https://xxxx - not null?
    private SOSArgument<String> wsdlUrl = new SOSArgument<>(ATTR_WSDL_URL, false);
    // e.g. RunBatch
    private SOSArgument<String> wsdlOperation = new SOSArgument<>(ATTR_WSDL_OPERATION, false);
    // e.g. RunBatchService
    private SOSArgument<String> serviceName = new SOSArgument<>(ATTR_SERVICE_NAME, false);
    // e.g. RunBatchResourcePort
    private SOSArgument<String> portName = new SOSArgument<>(ATTR_PORT_NAME, false);
    // e.g. 1
    private SOSArgument<String> wsGlobalProxyDefaults = new SOSArgument<>(ATTR_WS_GLOBAL_PROXY_DEFAULTS, false);
    private SOSArgument<List<String>> wsParameters = new SOSArgument<>(ATTR_WS_PARAMETER, false);

    public JobWSDOC(Path source, boolean reference) {
        super(source, ConverterJobType.WSDOC, reference);
    }

    public SOSArgument<String> getEndpointUrl() {
        return endpointUrl;
    }

    @ArgumentSetter(name = ATTR_ENDPOINT_URL)
    public void setProviderUrl(String val) {
        endpointUrl.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getWsdlUrl() {
        return wsdlUrl;
    }

    @ArgumentSetter(name = ATTR_WSDL_URL)
    public void setWsdlUrl(String val) {
        wsdlUrl.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getWsdlOperation() {
        return wsdlOperation;
    }

    @ArgumentSetter(name = ATTR_WSDL_OPERATION)
    public void setWsdlOperation(String val) {
        wsdlOperation.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getServiceName() {
        return serviceName;
    }

    @ArgumentSetter(name = ATTR_SERVICE_NAME)
    public void setServiceName(String val) {
        serviceName.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getPortName() {
        return portName;
    }

    @ArgumentSetter(name = ATTR_PORT_NAME)
    public void setPortName(String val) {
        portName.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getWsGlobalProxyDefaults() {
        return wsGlobalProxyDefaults;
    }

    @ArgumentSetter(name = ATTR_WS_GLOBAL_PROXY_DEFAULTS)
    public void setWsGlobalProxyDefaults(String val) {
        wsGlobalProxyDefaults.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<List<String>> getWsParameters() {
        return wsParameters;
    }

    @ArgumentSetter(name = ATTR_WS_PARAMETER)
    public void setWsParameters(String val) {
        String v = JS7ConverterHelper.stringValue(val);
        if (v != null) {
            if (wsParameters.getValue() == null) {
                wsParameters.setValue(new ArrayList<>());
            }
            if (val.contains(";")) {
                for (String c : v.split(";")) {
                    wsParameters.getValue().add(c);
                }
            } else {
                wsParameters.getValue().add(v);
            }
        }
    }

}
