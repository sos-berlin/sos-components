package com.sos.js7.converter.autosys.common.v12.job.attributes;

import com.sos.commons.util.common.SOSArgument;

public class CommonJobMonitoring extends AJobArguments {

    /** svcdesk_desc - Define the Message to Include in the CA Service Desk Request<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: svcdesk_desc: value<br/>
     * value - Specifies a description to include in the CA Service Desk request generated when the job fails.<br/>
     * Limits:Up to 255 alphanumeric characters<br/>
     * <br/>
     * JS7 - 0% - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<Integer> svcdeskDesc = new SOSArgument<>("svcdesk_desc", false);

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
    private SOSArgument<Integer> svcdeskImp = new SOSArgument<>("svcdesk_imp", false);

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
    private SOSArgument<Integer> svcdeskPri = new SOSArgument<>("svcdesk_pri", false);

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
    private SOSArgument<Integer> svcdeskSev = new SOSArgument<>("svcdesk_sev", false);

    /** service_desk - Specify Whether to Open a CA Service Desk Ticket When a Job Fails<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: service_desk: y | n<br/>
     * <br/>
     * JS7 - to be discussed in context with monitoring<br/>
     */
    private SOSArgument<Boolean> serviceDesk = new SOSArgument<>("service_desk", false);

    public SOSArgument<Integer> getSvcdeskDesc() {
        return svcdeskDesc;
    }

    public void setSvcdeskDesc(String val) {
        svcdeskDesc.setValue(AJobArguments.integerValue(val));
    }

    public SOSArgument<Integer> getSvcdeskImp() {
        return svcdeskImp;
    }

    public void setSvcdeskImp(String val) {
        svcdeskImp.setValue(AJobArguments.integerValue(val));
    }

    public SOSArgument<Integer> getSvcdeskPri() {
        return svcdeskPri;
    }

    public void setSvcdeskPri(String val) {
        svcdeskPri.setValue(AJobArguments.integerValue(val));
    }

    public SOSArgument<Integer> getSvcdeskSev() {
        return svcdeskSev;
    }

    public void setSvcdeskSev(String val) {
        svcdeskSev.setValue(AJobArguments.integerValue(val));
    }

    public SOSArgument<Boolean> getServiceDesk() {
        return serviceDesk;
    }

    public void setServiceDesk(String val) {
        serviceDesk.setValue(AJobArguments.booleanValue(val, false));
    }
}
