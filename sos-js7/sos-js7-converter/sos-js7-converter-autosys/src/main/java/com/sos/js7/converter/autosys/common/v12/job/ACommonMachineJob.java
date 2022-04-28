package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;

public abstract class ACommonMachineJob extends ACommonJob {

    /** machine - This attribute is required for all job types. This attribute does not apply to Box jobs.<br/>
     * Format: machine: {machine_name [, machine_name]...| `machine chooser`}<br/>
     * Limits:Up to 80 alphanumeric characters<br/>
     * Example run on two machines: machine: prod, test<br/>
     * Example chooser: machine: `C\:\MYSTUFF\MYCHOOSER.BAT`<br/>
     * Note:<br/>
     * When specifying drive letters for Windows in job definitions, you must escape the colon with backslashes.<br/>
     * For example, 'C\:\tmp' is valid; 'C:\tmp' is not. <br/>
     * <br/>
     * JS7 - Mapping to Agents. Agent Cluster. No "machine chooser supported" */
    private SOSArgument<String> machine = new SOSArgument<>("machine", true);

    public ACommonMachineJob(JobType type) {
        super(type);
    }

    public SOSArgument<String> getMachine() {
        return machine;
    }

    public void setMachine(String val) {
        machine.setValue(stringValue(val));
    }

}
