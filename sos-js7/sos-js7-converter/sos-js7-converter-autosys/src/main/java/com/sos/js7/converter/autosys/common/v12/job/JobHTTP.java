package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobHTTP extends ACommonMachineJob {

    private static final String ATTR_PROVIDER_URL = "provider_url";
    private static final String ATTR_INVOCATION_TYPE = "invocation_type";
    private static final String ATTR_REQUEST_BODY = "request_body";
    private static final String ATTR_PAYLOAD = "payload";
    private static final String ATTR_CONTENT_TYPE = "content_type";
    private static final String ATTR_TIMEOUT = "timeout";
    private static final String ATTR_RESPONSE_FILE = "response_file";
    private static final String ATTR_RETURN_CODE = "return_code";

    // extensions
    private static final String ATTR_J2EE_CONN_USER = "j2ee_conn_user";
    private static final String ATTR_J2EE_NO_GLOBAL_PROXY_DEFAULTS = "j2ee_no_global_proxy_defaults";
    private static final String ATTR_J2EE_AUTHENTICATION_ORDER = "j2ee_authentication_order";
    private static final String ATTR_J2EE_PROXY_PORT = "j2ee_proxy_port";

    // https://xxxx - not null?
    private SOSArgument<String> providerUrl = new SOSArgument<>(ATTR_PROVIDER_URL, false);
    // GET/POST/PUT/DELET
    private SOSArgument<String> invocationType = new SOSArgument<>(ATTR_INVOCATION_TYPE, false);

    private SOSArgument<String> requestBody = new SOSArgument<>(ATTR_REQUEST_BODY, false);
    // private SOSArgument<String> payload = new SOSArgument<>(ATTR_PAYLOAD, false);
    private SOSArgument<String> contentType = new SOSArgument<>(ATTR_CONTENT_TYPE, false);
    private SOSArgument<String> timeout = new SOSArgument<>(ATTR_TIMEOUT, false);
    private SOSArgument<String> responseFile = new SOSArgument<>(ATTR_RESPONSE_FILE, false);
    private SOSArgument<String> returnCode = new SOSArgument<>(ATTR_RETURN_CODE, false);

    // ---------------------------------------------------------------------------------------------------------------------
    // user name
    private SOSArgument<String> j2eeConnUser = new SOSArgument<>(ATTR_J2EE_CONN_USER, false);
    // e.g. 1
    private SOSArgument<String> j2eeNoGlobalProxyDefaults = new SOSArgument<>(ATTR_J2EE_NO_GLOBAL_PROXY_DEFAULTS, false);
    // e.g. BASIC
    private SOSArgument<String> j2eeAuthenticationOrder = new SOSArgument<>(ATTR_J2EE_AUTHENTICATION_ORDER, false);
    // e.g. 80
    private SOSArgument<String> j2eeProxyPort = new SOSArgument<>(ATTR_J2EE_PROXY_PORT, false);

    public JobHTTP(Path source, boolean reference) {
        super(source, ConverterJobType.HTTP, reference);
    }

    public SOSArgument<String> getProviderUrl() {
        return providerUrl;
    }

    @ArgumentSetter(name = ATTR_PROVIDER_URL)
    public void setProviderUrl(String val) {
        providerUrl.setValue(JS7ConverterHelper.stringValue(val));
        // provider_url: \"https://xyz:9443/AEWS/job/getjobdetailsxml\
        String tm = providerUrl.getValue();
        if (tm != null) {
            tm = SOSString.trimStart(tm, "\\\"");
            tm = SOSString.trimEnd(tm, "\\");
            providerUrl.setValue(tm);
        }
    }

    public SOSArgument<String> getInvocationType() {
        return invocationType;
    }

    @ArgumentSetter(name = ATTR_INVOCATION_TYPE)
    public void setInvocationType(String val) {
        invocationType.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getRequestBody() {
        return requestBody;
    }

    @ArgumentSetter(name = ATTR_REQUEST_BODY)
    public void setRequestBody(String val) {
        requestBody.setValue(JS7ConverterHelper.stringValue(val));
    }

    @ArgumentSetter(name = ATTR_PAYLOAD)
    public void setPayload(String val) {
        requestBody.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getContentType() {
        return contentType;
    }

    @ArgumentSetter(name = ATTR_CONTENT_TYPE)
    public void setContentType(String val) {
        contentType.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getTimeout() {
        return timeout;
    }

    @ArgumentSetter(name = ATTR_TIMEOUT)
    public void setTimeout(String val) {
        timeout.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getResponseFile() {
        return responseFile;
    }

    @ArgumentSetter(name = ATTR_RESPONSE_FILE)
    public void setResponseFile(String val) {
        responseFile.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getReturnCode() {
        return returnCode;
    }

    @ArgumentSetter(name = ATTR_RETURN_CODE)
    public void setReturnCode(String val) {
        returnCode.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getJ2eeConnUser() {
        return j2eeConnUser;
    }

    @ArgumentSetter(name = ATTR_J2EE_CONN_USER)
    public void setJ2eeConnUser(String val) {
        j2eeConnUser.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getJ2eeNoGlobalProxyDefaults() {
        return j2eeNoGlobalProxyDefaults;
    }

    @ArgumentSetter(name = ATTR_J2EE_NO_GLOBAL_PROXY_DEFAULTS)
    public void setJ2eeNoGlobalProxyDefaults(String val) {
        j2eeNoGlobalProxyDefaults.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getJ2eeAuthenticationOrder() {
        return j2eeAuthenticationOrder;
    }

    @ArgumentSetter(name = ATTR_J2EE_AUTHENTICATION_ORDER)
    public void setJ2eeAuthenticationOrder(String val) {
        j2eeAuthenticationOrder.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getJ2eeProxyPort() {
        return j2eeProxyPort;
    }

    @ArgumentSetter(name = ATTR_J2EE_PROXY_PORT)
    public void setJ2eeProxyPort(String val) {
        j2eeProxyPort.setValue(JS7ConverterHelper.stringValue(val));
    }
}
