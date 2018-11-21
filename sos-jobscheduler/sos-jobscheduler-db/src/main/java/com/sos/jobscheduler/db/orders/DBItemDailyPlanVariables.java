package com.sos.jobscheduler.db.orders;

import java.util.Date;
import javax.persistence.*;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.DAILY_PLAN_VARIABLES_TABLE)
@SequenceGenerator(name = DBLayer.DAILY_PLAN_VARIABLES_TABLE_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_VARIABLES_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanVariables {

	private Long id;
	private Long planId;
	private String variableName;
	private String variableValue;
	private Date created;
	private Date modified;

	public DBItemDailyPlanVariables() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLAN_VARIABLES_TABLE_SEQUENCE)
	@Column(name = "[ID]")
	public Long getId() {
		return id;
	}

	@Id
	@Column(name = "[ID]")
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "[PLAN_ID]", nullable = false)
	public Long getPlanId() {
		return planId;
	}

	public void setPlanId(Long planId) {
		this.planId = planId;
	}

	@Column(name = "[VARIABLE_NAME]", nullable = false)
	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Column(name = "[VARIABLE_VALUE]", nullable = false)
	public String getVariableValue() {
		return variableValue;
	}

	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[CREATED]", nullable = false)
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[MODIFIED]", nullable = true)
	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

}