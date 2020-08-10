package com.sos.joc.report.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.report.Agents;
import com.sos.joc.model.report.AgentsFilter;
import com.sos.joc.report.resource.IAgentsResource;

@Path("report")
public class AgentsResourceImpl extends JOCResourceImpl implements IAgentsResource {

	private static final String API_CALL = "./report/agents";
//	private class AgentsGroupedByJobAndDate {
//	    
//	    private String job;
//	    private Instant date;
//	    
//	    public AgentsGroupedByJobAndDate(DBItemReportTask reportTask) {
//	        this.job = reportTask.getName();
//	        this.date = reportTask.getStartTime().toInstant().truncatedTo(ChronoUnit.DAYS);
//	    }
//	    
//	    @Override
//	    public String toString() {
//	        return ToStringBuilder.reflectionToString(this);
//	    }
//
//	    @Override
//	    public int hashCode() {
//	        return new HashCodeBuilder().append(job).append(date).toHashCode();
//	    }
//
//	    @Override
//	    public boolean equals(Object other) {
//	        if (other == this) {
//	            return true;
//	        }
//	        if ((other instanceof AgentsGroupedByJobAndDate) == false) {
//	            return false;
//	        }
//	        AgentsGroupedByJobAndDate rhs = ((AgentsGroupedByJobAndDate) other);
//	        return new EqualsBuilder().append(job, rhs.job).append(date, rhs.date).isEquals();
//	    }
//	}

	@Override
	public JOCDefaultResponse postAgentsReport(String accessToken, AgentsFilter agentsFilter) throws Exception {
//		SOSHibernateSession connection = null;

		try {
			if (agentsFilter.getJobschedulerId() == null) {
				agentsFilter.setJobschedulerId("");
			}
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, agentsFilter, accessToken,
					agentsFilter.getJobschedulerId(),
					getPermissonsJocCockpit(agentsFilter.getJobschedulerId(), accessToken)
							.getJS7UniversalAgent().getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
//			connection = Globals.createSosHibernateStatelessConnection(API_CALL);
//			Map<Agent, List<DBItemReportTask>> agentsGroupedBySchedulerIdAndUrlAndCause = new JobSchedulerReportDBLayer(connection).getAllStartedAgentTasks(
//                    agentsFilter.getJobschedulerId(), agentsFilter.getAgents(),
//                    JobSchedulerDate.getDateFrom(agentsFilter.getDateFrom(), agentsFilter.getTimeZone()),
//                    JobSchedulerDate.getDateTo(agentsFilter.getDateTo(), agentsFilter.getTimeZone()));
//			
//			List<Agent> agentList = new ArrayList<Agent>();
//            Long totalSuccessfulTasks = 0L;
//            Long totalJobs = 0L;
//            
//            if (agentsGroupedBySchedulerIdAndUrlAndCause != null) {
//                for (Map.Entry<Agent, List<DBItemReportTask>> entry : agentsGroupedBySchedulerIdAndUrlAndCause.entrySet()) {
//                    Agent agent = entry.getKey();
//                    agent.setNumOfSuccessfulTasks(entry.getValue().stream().filter(item -> item.getEndTime() != null && !item.getError()).count());
//                    totalSuccessfulTasks += agent.getNumOfSuccessfulTasks();
//                    agent.setNumOfJobs(entry.getValue().stream().collect(Collectors.groupingBy(reportTask -> new AgentsGroupedByJobAndDate(
//                            reportTask))).keySet().stream().count());
//                    totalJobs += agent.getNumOfJobs();
//                    agentList.add(agent);
//                }
//
//                agentList.sort(new Comparator<Agent>() {
//
//                    @Override
//                    public int compare(Agent o1, Agent o2) {
//                        int compareJobScheduler = o1.getJobschedulerId().compareTo(o2.getJobschedulerId());
//                        if (compareJobScheduler == 0) {
//                            int compareAgent = o1.getAgent().compareTo(o2.getAgent());
//                            if (compareAgent == 0) {
//                                return o1.getCause().compareTo(o2.getCause());
//                            } else {
//                                return compareAgent;
//                            }
//                        } else {
//                            return compareJobScheduler;
//                        }
//                    }
//
//                });
//            }
//	        Agents agents = new Agents();
//	        agents.setAgents(agentList);
//	        agents.setTotalNumOfSuccessfulTasks(totalSuccessfulTasks);
//	        agents.setTotalNumOfJobs(totalJobs);
//	        agents.setDeliveryDate(Date.from(Instant.now()));
	        
	        Agents agents = new Agents();
	        agents.setAgents(null);
	        agents.setTotalNumOfSuccessfulTasks(0L);
	        agents.setTotalNumOfJobs(0L);
	        agents.setDeliveryDate(Date.from(Instant.now()));
			return JOCDefaultResponse.responseStatus200(agents);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//		} finally {
//			Globals.disconnect(connection);
		}
	}
	
}