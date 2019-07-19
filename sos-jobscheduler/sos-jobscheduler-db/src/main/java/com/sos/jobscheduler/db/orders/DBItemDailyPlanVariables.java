package com.sos.jobscheduler.db.orders;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.DAILY_PLAN_VARIABLES_TABLE)
@SequenceGenerator(name = DBLayer.DAILY_PLAN_VARIABLES_TABLE_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_VARIABLES_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanVariables extends DBItem {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Long planOrderId;
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

	@Column(name = "[PLANNED_ORDER_ID]", nullable = false)
	public Long getPlannedOrderId() {
		return planOrderId;
	}

	public void setPlannedOrderId(Long plannedOrderId) {
		this.planOrderId = plannedOrderId;
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