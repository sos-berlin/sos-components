
package com.sos.jobscheduler.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * try catch
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "try",
    "catch"
})
public class TryCatch
    extends Instruction
    implements com.sos.jobscheduler.model.instruction.IInstructible
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("try")
    private List<com.sos.jobscheduler.model.instruction.IInstructible> _try = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("catch")
    private List<com.sos.jobscheduler.model.instruction.IInstructible> _catch = null;

    @JsonProperty("try")
    public List<com.sos.jobscheduler.model.instruction.IInstructible> getTry() {
        return _try;
    }

    @JsonProperty("try")
    public void setTry(List<com.sos.jobscheduler.model.instruction.IInstructible> _try) {
        this._try = _try;
    }

    @JsonProperty("catch")
    public List<com.sos.jobscheduler.model.instruction.IInstructible> getCatch() {
        return _catch;
    }

    @JsonProperty("catch")
    public void setCatch(List<com.sos.jobscheduler.model.instruction.IInstructible> _catch) {
        this._catch = _catch;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("_try", _try).append("_catch", _catch).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(_try).append(_catch).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TryCatch) == false) {
            return false;
        }
        TryCatch rhs = ((TryCatch) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(_try, rhs._try).append(_catch, rhs._catch).isEquals();
    }

}
