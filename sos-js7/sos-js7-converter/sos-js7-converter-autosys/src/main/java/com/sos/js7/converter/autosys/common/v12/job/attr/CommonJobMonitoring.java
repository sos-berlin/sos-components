package com.sos.js7.converter.autosys.common.v12.job.attr;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class CommonJobMonitoring extends AJobAttributes {

    private static final String ATTR_SVCDESK_ATTR = "svcdesk_attr";
    private static final String ATTR_SVCDESK_DESC = "svcdesk_desc";
    private static final String ATTR_SVCDESK_IMP = "svcdesk_imp";
    private static final String ATTR_SVCDESK_PRI = "svcdesk_pri";

    private static final String ATTR_SVCDESK_SEV = "svcdesk_sev";
    private static final String ATTR_SERVICE_DESC = "service_desk";

    /** svcdesk_attr - defines Service Desk request attributes and values to set in the Service Desk request generated<br/>
     * when you have set the service_desk attribute to y or 1 and the job you are defining completes with a FAILURE status.<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: svcdesk_desc: value<br/>
     * value - Specifies a description to include in the CA Service Desk request generated when the job fails.<br/>
     * Limits:Up to 255 alphanumeric characters<br/>
     * <br/>
     * JS7 - 0% - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<String> svcdeskAttr = new SOSArgument<>(ATTR_SVCDESK_ATTR, false);

    /** svcdesk_desc - Define the Message to Include in the CA Service Desk Request<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: svcdesk_desc: value<br/>
     * value - Specifies a description to include in the CA Service Desk request generated when the job fails.<br/>
     * Limits:Up to 255 alphanumeric characters<br/>
     * <br/>
     * JS7 - 0% - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<String> svcdeskDesc = new SOSArgument<>(ATTR_SVCDESK_DESC, false);

    /** svcdesk_imp - Specify the Impact Level of the Service Desk Request<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The svcdesk_imp attribute specifies the impact level to assign the Service Desk request generated when you have set the service_desk attribute to y or 1
     * and the job you are defining completes with a FAILURE status.<br/>
     * The impact level indicates how much you expect the request to affect work being performed.<br/>
     * <br/>
     * Format: svcdesk_imp: level<br/>
     * level limits: 0 to 5<br/>
     * Example: svcdesk_imp: 3<br/>
     * <br/>
     * JS7 - 0% - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<Integer> svcdeskImp = new SOSArgument<>(ATTR_SVCDESK_IMP, false);

    /** svcdesk_pri - Specify the Priority Level of the Service Desk Request<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The svcdesk_pri attribute specifies the priority level of the Service Desk request generated when you set the service_desk attribute to y or 1, and the
     * job you are defining completes with a FAILURE status.<br/>
     * <br/>
     * Format: svcdesk_pri: level<br/>
     * level limits: 0 to 5<br/>
     * Example: svcdesk_pri: 3<br/>
     * <br/>
     * JS7 - 0% - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<Integer> svcdeskPri = new SOSArgument<>(ATTR_SVCDESK_PRI, false);

    /** svcdesk_sev - Specify the Severity Level of the Service Desk Request<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The svcdesk_sev attribute specifies the severity level to assign the Service Desk request generated when you have set the service_desk attribute to y or
     * 1 and the job you are defining completes with a FAILURE status.<br/>
     * The severity level indicates how much you expect the request to affect other users.<br/>
     * <br/>
     * Format: svcdesk_sev: level<br/>
     * level limits: 0 to 5<br/>
     * Example: svcdesk_sev: 3<br/>
     * <br/>
     * JS7 - 0% - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<Integer> svcdeskSev = new SOSArgument<>(ATTR_SVCDESK_SEV, false);

    /** service_desk - Specify Whether to Open a CA Service Desk Ticket When a Job Fails<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: service_desk: y | n<br/>
     * <br/>
     * JS7 - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<Boolean> serviceDesk = new SOSArgument<>(ATTR_SERVICE_DESC, false);

    public SOSArgument<String> getSvcdeskAttr() {
        return svcdeskAttr;
    }

    @ArgumentSetter(name = ATTR_SVCDESK_ATTR)
    public void setSvcdeskAttr(String val) {
        svcdeskAttr.setValue(val);
    }

    public SOSArgument<String> getSvcdeskDesc() {
        return svcdeskDesc;
    }

    @ArgumentSetter(name = ATTR_SVCDESK_DESC)
    public void setSvcdeskDesc(String val) {
        svcdeskDesc.setValue(val);
    }

    public SOSArgument<Integer> getSvcdeskImp() {
        return svcdeskImp;
    }

    @ArgumentSetter(name = ATTR_SVCDESK_IMP)
    public void setSvcdeskImp(String val) {
        svcdeskImp.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Integer> getSvcdeskPri() {
        return svcdeskPri;
    }

    @ArgumentSetter(name = ATTR_SVCDESK_PRI)
    public void setSvcdeskPri(String val) {
        svcdeskPri.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Integer> getSvcdeskSev() {
        return svcdeskSev;
    }

    @ArgumentSetter(name = ATTR_SVCDESK_SEV)
    public void setSvcdeskSev(String val) {
        svcdeskSev.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Boolean> getServiceDesk() {
        return serviceDesk;
    }

    @ArgumentSetter(name = ATTR_SERVICE_DESC)
    public void setServiceDesk(String val) {
        serviceDesk.setValue(JS7ConverterHelper.booleanValue(val, false));
    }

    public boolean exists() {
        return svcdeskAttr.getValue() != null || svcdeskDesc.getValue() != null || svcdeskImp.getValue() != null || svcdeskPri.getValue() != null
                || svcdeskSev.getValue() != null || serviceDesk.getValue() != null;
    }

    @Override
    public String toString() {
        return toString(svcdeskAttr, svcdeskDesc, svcdeskImp, svcdeskPri, svcdeskSev, serviceDesk);
    }

}
