package com.sos.js7.converter.autosys.common.v12.job.attributes;

import com.sos.commons.util.common.SOSArgument;

public class CommonJobFolder extends AJobArguments {

    /** application - Associate a Job with an Application<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: application: application_name<br/>
     * <br/>
     * The application attribute associates a job with a specific application so users can classify, sort, and filter jobs by application name.<br/>
     * Note: If both the box and child job belongs to the same application, you have to specify the application attribute only for a box job.<br/>
     * Limits: Up to 64 characters; valid characters are a-z, A-Z, 0-9, period (.), underscore (_), pound (#), and hyphen (-);<br/>
     * do not include embedded spaces or tabs<br/>
     * <br/>
     * JS7 - 100% - Mapping Folder - Inventory <br/>
     */
    private SOSArgument<String> application = new SOSArgument<>("application", false);

    /** group - Associate a Job with a Group<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The group attribute associates a job with a specific group so users can classify, sort, and filter jobs by group name.<br/>
     * You can use the group attribute with the sendevent, job_depends, and autorep commands.<br/>
     * For example, you could use a single sendevent command to start all of the jobs associated with a specific group.<br/>
     * Note: If both the box and child job belongs to the same group, you have to specify the group attribute only for a box job.<br/>
     * Format: group: group_name<br/>
     * Limits: Up to 64 characters; valid characters are a-z, A-Z, 0-9, period (.), underscore (_), pound (#), and hyphen (-); do not include embedded spaces or
     * tabs<br/>
     * <br/>
     * JS7 - 50% - Most probably will be mapped to search criteria for jobs and will be made available from the REST API<br/>
     */
    private SOSArgument<String> group = new SOSArgument<>("group", false);

    public SOSArgument<String> getApplication() {
        return application;
    }

    public void setApplication(String val) {
        application.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getGroup() {
        return group;
    }

    public void setGroup(String val) {
        group.setValue(AJobArguments.stringValue(val));
    }
}
