package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public abstract class ACommonMachineJob extends ACommonJob {

    private static final String ATTR_MACHINE = "machine";
    private static final String ATTR_PROFILE = "profile";
    private static final String ATTR_PRIORITY = "priority";

    /** machine - Define the Client Where a Job Runs<br/>
     * This attribute is required for all job types. This attribute does not apply to Box jobs.<br/>
     * 
     * Format: machine: {machine_name [, machine_name]...| `machine chooser`}<br/>
     * Limits:Up to 80 alphanumeric characters<br/>
     * Example run on two machines: machine: prod, test<br/>
     * Example chooser: machine: `C\:\MYSTUFF\MYCHOOSER.BAT`<br/>
     * Note:<br/>
     * When specifying drive letters for Windows in job definitions, you must escape the colon with backslashes.<br/>
     * For example, 'C\:\tmp' is valid; 'C:\tmp' is not. <br/>
     * <br/>
     * JS7 - 100% - Mapping to Agents. Agent Cluster. No "machine chooser supported" */
    private SOSArgument<String> machine = new SOSArgument<>(ATTR_MACHINE, true);

    /** profile - Specify a Job Profile<br/>
     * This attribute is optional for the following job types: Command(CMD), File Watcher(FW)<br/>
     * 
     * The profile attribute specifies a profile that defines the non-system environment variables that a job uses.<br/>
     * The variables in the profile are sourced before the job runs.<br/>
     * Format: profile: path_name<br/>
     * Limits: Up to 255 characters<br/>
     * <br/>
     * JS7 - 90% - Job Resource. Not used for File Watcher<br/>
     */
    private SOSArgument<String> profile = new SOSArgument<>(ATTR_PROFILE, false);

    /** priority - Define the Queue Priority of the Job<br/>
     * This attribute is optional for all job types. This attribute does not apply to box jobs.<br/>
     * 
     * Format: priority: priority_level<br/>
     * Example: priority: 0 <br/>
     * JS7 - JS7 feature currently not available and is developed for iteration 3<br/>
     */
    private SOSArgument<Integer> priority = new SOSArgument<>(ATTR_PRIORITY, false);

    public ACommonMachineJob(Path source, ConverterJobType type) {
        super(source, type);
    }

    public SOSArgument<String> getMachine() {
        return machine;
    }

    @ArgumentSetter(name = ATTR_MACHINE)
    public void setMachine(String val) {
        machine.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getProfile() {
        return profile;
    }

    @ArgumentSetter(name = ATTR_PROFILE)
    public void setProfile(String val) {
        profile.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<Integer> getPriority() {
        return priority;
    }

    @ArgumentSetter(name = ATTR_PRIORITY)
    public void setPriority(String val) {
        priority.setValue(JS7ConverterHelper.integerValue(val));
    }

}
