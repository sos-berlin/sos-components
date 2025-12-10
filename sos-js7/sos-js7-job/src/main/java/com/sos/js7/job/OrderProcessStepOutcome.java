package com.sos.js7.job;

import java.util.HashMap;
import java.util.Map;

import com.sos.commons.util.SOSString;

/** Represents a js7Step outcome object.
 * <p>
 * This object can be used, for example, to set the return code or outcome variables.
 * </p>
 **/
public class OrderProcessStepOutcome {

    private final Map<String, Object> variables;

    private Integer returnCode;
    private String message;
    private boolean failed;

    protected OrderProcessStepOutcome() {
        this.variables = new HashMap<>();
    }

    /** Returns the return code of this step.
     *
     * @return the return code, or {@code null} if not set */
    public Integer getReturnCode() {
        return returnCode;
    }

    /** Sets the return code for this step.
     *
     * @param val the return code to set */
    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    /** Adds an outcome variable to this step outcome.
     * <p>
     * The variable is represented by an {@link OrderProcessStepOutcomeVariable} object, which is declared in the {@code JobArguments} class for this job.<br/>
     * If the provided variable is {@code null}, this method does nothing.
     * </p>
     *
     * @param var the outcome variable to add; may be {@code null} */
    public void putVariable(OrderProcessStepOutcomeVariable<?> var) {
        if (var == null) {
            return;
        }
        variables.put(var.getName(), var.getValue());
    }

    /** Adds or updates an outcome variable with the given name and value.
     *
     * @param name the name of the outcome variable
     * @param val the value of the outcome variable */
    public void putVariable(String name, Object val) {
        variables.put(name, val);
    }

    /** Adds or updates multiple outcome variables from the provided map.
     * <p>
     * If the provided map is {@code null}, this method does nothing.
     * </p>
     *
     * @param val a map of variable names and values; may be {@code null} */
    public void putVariables(Map<String, Object> val) {
        if (val != null) {
            variables.putAll(val);
        }
    }

    /** Returns a map of all outcome variables.
     *
     * @return a map containing all outcome variables */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /** Returns the message associated with this step outcome.
     * <p>
     * The message can be used to identify or describe the reason for failure or any other information related to the step outcome.
     * </p>
     *
     * @return the message, or {@code null} if not set */
    public String getMessage() {
        return message;
    }

    /** Sets the message associated with this step outcome.
     * 
     * @param val the message to set */
    public void setMessage(String val) {
        message = val;
    }

    /** Shortcut method to mark this step as failed and set a failure message.
     * <p>
     * This method is equivalent to calling {@link #setMessage(String)} with the provided message and then {@link #setFailed()} to mark the step as failed.<br/>
     * The message can be used to identify or describe the failure.
     * </p>
     *
     * @param msg the failure message */
    public void setFailed(String msg) {
        setMessage(msg);
        setFailed();
    }

    /** Marks this step as failed without providing a message.
     * <p>
     * Setting this flag indicates that the step execution failed.
     * </p>
     */
    public void setFailed() {
        failed = true;
    }

    /** Returns whether this step has been marked as failed.
     *
     * @return {@code true} if the step failed; {@code false} otherwise */
    public boolean isFailed() {
        return failed;
    }

    protected boolean hasVariables() {
        return variables != null && variables.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("returnCode=").append(returnCode == null ? "" : returnCode);
        sb.append(",failed=").append(failed);
        if (variables != null) {
            sb.append(",variables=").append(SOSString.toString(variables));
        }
        if (message != null) {
            sb.append(",message=").append(message);
        }
        return sb.toString();
    }

}
