
package com.sos.inventory.model.instruction;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.inventory.model.common.ClassHelper;


/**
 * instruction
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "position",
    "positionString"
})
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE",
		visible = true)
//@JsonTypeIdResolver(InstructionTypeIdResolver.class)
@JsonSubTypes({ 
		@JsonSubTypes.Type(value = IfElse.class, name = "If"),
		@JsonSubTypes.Type(value = NamedJob.class, name = "Execute.Named"),
		@JsonSubTypes.Type(value = ForkJoin.class, name = "Fork"),
		@JsonSubTypes.Type(value = RetryCatch.class, name = "Try"),
		@JsonSubTypes.Type(value = TryCatch.class, name = "Try"),
		@JsonSubTypes.Type(value = RetryInCatch.class, name = "Retry"),
		@JsonSubTypes.Type(value = Finish.class, name = "Finish"),
		@JsonSubTypes.Type(value = Fail.class, name = "Fail"),
		@JsonSubTypes.Type(value = Lock.class, name = "Lock"),
		@JsonSubTypes.Type(value = ImplicitEnd.class, name = "ImplicitEnd")})
public abstract class Instruction
    extends ClassHelper
{

    /**
     * instructionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    private InstructionType tYPE;
    
    @JsonProperty("position")
    private List<Object> position;
    
    @JsonProperty("positionString")
    private String positionString;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Instruction() {
    }

    /**
     * 
     * @param tYPE
     */
    public Instruction(InstructionType tYPE) {
        super();
        this.tYPE = tYPE;
    }

    /**
     * instructionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    public InstructionType getTYPE() {
        return tYPE;
    }

    /**
     * instructionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(InstructionType tYPE) {
        this.tYPE = tYPE;
    }
    
    @JsonProperty("position")
    public List<Object> getPosition() {
        return position;
    }
    
    @JsonProperty("position")
    public void setPosition(List<Object> position) {
        this.position = position;
    }
    
    @JsonProperty("positionString")
    public String getPositionString() {
        return positionString;
    }
    
    @JsonProperty("positionString")
    public void setPositionString(String positionString) {
        this.positionString = positionString;
    }
    
    @JsonIgnore
	public Boolean isRetry() {
		try {
			if (this.getTYPE() == InstructionType.TRY) {
				java.lang.reflect.Field f = this.getClass().getSuperclass().getDeclaredField("_catch");
				f.setAccessible(true);
				return ((Instructions) f.get(this)).getInstructions().get(0).getTYPE() == InstructionType.RETRY;
			}
		} catch (Exception e) {
		}
		return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).append(position).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Instruction) == false) {
            return false;
        }
        Instruction rhs = ((Instruction) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).append(position, rhs.position).isEquals();
    }

}
