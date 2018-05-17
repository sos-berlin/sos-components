package com.sos.jobscheduler.history.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.jobscheduler.db.DBItemSchedulerVariables;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.history.db.DBLayerHistory;

public class HistoryModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final SOSHibernateFactory dbFactory;
    private final DBLayerHistory dbLayer;
    private boolean isLocked = false;
    private String lockCause = null;

    public HistoryModel(SOSHibernateFactory factory, EventHandlerMasterSettings masterSettings) {
        dbFactory = factory;
        dbLayer = new DBLayerHistory("history_" + masterSettings.getSchedulerId());
    }

    public Long process(Event event) {
        SOSHibernateSession session = null;
        Long newEventId = new Long(0);
        try {
            session = dbFactory.openStatelessSession();

            session.beginTransaction();
            for (IEntry en : event.getStampeds()) {
                Entry entry = (Entry) en;

                System.out.println("Entry: " + entry);

                switch (entry.getType()) {
                case OrderAddedFat:
                    orderAdded(entry);
                    break;
                case OrderProcessingStartedFat:
                    orderProcessingStarted(entry);
                    break;
                case OrderStdoutWrittenFat:
                    orderOutWritten(entry);
                    break;
                case OrderStderrWrittenFat:
                    orderOutWritten(entry);
                    break;
                case OrderProcessedFat:
                    orderProcessed(entry);
                    break;
                case OrderFinishedFat:
                    orderFinished(entry);
                    break;
                }
                newEventId = entry.getEventId();
            }
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return newEventId;
    }

    private void orderAdded(Entry entry) {
        System.out.println("    Type: " + entry.getType());
        System.out.println("    eventId: " + entry.getEventId() + " (" + entry.getEventIdAsInstant() + ")");
        System.out.println("    timestamp: " + entry.getTimestamp() + " (" + entry.getTimestampAsInstant() + ")");
        System.out.println("    key: " + entry.getKey());

        System.out.println("    parent: " + entry.getParent());
        System.out.println("    cause: " + entry.getCause());
        System.out.println("    scheduledAt: " + entry.getScheduledAt());

        if (entry.getWorkflowPosition() != null) {
            System.out.println("    WorkflowPosition: " + entry.getWorkflowPosition());
            System.out.println("        WorkflowId: " + entry.getWorkflowPosition().getWorkflowId());
            System.out.println("            path: " + entry.getWorkflowPosition().getWorkflowId().getPath());
            System.out.println("            versionId: " + entry.getWorkflowPosition().getWorkflowId().getVersionId());
            System.out.println("        position: " + entry.getWorkflowPosition().getPosition());
            System.out.println("        positionAsString: " + entry.getWorkflowPosition().getPositionAsString());
        }
        if (entry.getVariables() != null) {
            System.out.println("    Variables: " + entry.getVariables());
        }
    }

    private void orderProcessingStarted(Entry entry) {
        System.out.println("    Type: " + entry.getType());
        System.out.println("    eventId: " + entry.getEventId() + " (" + entry.getEventIdAsInstant() + ")");
        System.out.println("    timestamp: " + entry.getTimestamp() + " (" + entry.getTimestampAsInstant() + ")");
        System.out.println("    key: " + entry.getKey());

        System.out.println("    agentUri: " + entry.getAgentUri());
        System.out.println("    jobPath: " + entry.getJobPath());

        if (entry.getWorkflowPosition() != null) {
            System.out.println("    WorkflowPosition: " + entry.getWorkflowPosition());
            System.out.println("        WorkflowId: " + entry.getWorkflowPosition().getWorkflowId());
            System.out.println("            path: " + entry.getWorkflowPosition().getWorkflowId().getPath());
            System.out.println("            versionId: " + entry.getWorkflowPosition().getWorkflowId().getVersionId());
            System.out.println("        position: " + entry.getWorkflowPosition().getPosition());
            System.out.println("        positionAsString: " + entry.getWorkflowPosition().getPositionAsString());
        }
        if (entry.getVariables() != null) {
            System.out.println("    Variables: " + entry.getVariables());
        }
    }

    private void orderOutWritten(Entry entry) {
        System.out.println("    Type: " + entry.getType());
        System.out.println("    eventId: " + entry.getEventId() + " (" + entry.getEventIdAsInstant() + ")");
        System.out.println("    timestamp: " + entry.getTimestamp() + " (" + entry.getTimestampAsInstant() + ")");
        System.out.println("    key: " + entry.getKey());

        System.out.println("    chunk: " + entry.getChunk());
    }

    private void orderProcessed(Entry entry) {
        System.out.println("    Type: " + entry.getType());
        System.out.println("    eventId: " + entry.getEventId() + " (" + entry.getEventIdAsInstant() + ")");
        System.out.println("    timestamp: " + entry.getTimestamp() + " (" + entry.getTimestampAsInstant() + ")");
        System.out.println("    key: " + entry.getKey());

        if (entry.getOutcome() != null) {
            System.out.println("    Outcome:" + entry.getOutcome());
            System.out.println("        type: " + entry.getOutcome().getType());
            System.out.println("        returnCode: " + entry.getOutcome().getReturnCode());
        }
        if (entry.getVariables() != null) {
            System.out.println("    Variables: " + entry.getVariables());
        }
    }

    private void orderFinished(Entry entry) {
        System.out.println("    Type: " + entry.getType());
        System.out.println("    eventId: " + entry.getEventId() + " (" + entry.getEventIdAsInstant() + ")");
        System.out.println("    timestamp: " + entry.getTimestamp() + " (" + entry.getTimestampAsInstant() + ")");
        System.out.println("    key: " + entry.getKey());

        if (entry.getWorkflowPosition() != null) {
            System.out.println("    WorkflowPosition: " + entry.getWorkflowPosition());
            System.out.println("        WorkflowId: " + entry.getWorkflowPosition().getWorkflowId());
            System.out.println("            path: " + entry.getWorkflowPosition().getWorkflowId().getPath());
            System.out.println("            versionId: " + entry.getWorkflowPosition().getWorkflowId().getVersionId());
            System.out.println("        position: " + entry.getWorkflowPosition().getPosition());
            System.out.println("        positionAsString: " + entry.getWorkflowPosition().getPositionAsString());
        }

    }

    public Long getEventId() {
        isLocked = false;
        lockCause = null;
        SOSHibernateSession session = null;
        try {
            session = dbFactory.openStatelessSession();

            session.beginTransaction();
            DBItemSchedulerVariables sv = dbLayer.getSchedulerVariables(session);
            if (sv == null) {
                sv = dbLayer.insertSchedulerVariables(session, new Long(0));
            }
            session.commit();
            LOGGER.info(String.format("eventId=%s", sv.getNumericValue()));
            return sv.getNumericValue();
        } catch (SOSHibernateObjectOperationStaleStateException e) {
            isLocked = true;
            lockCause = "locked by an another instance";
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Exception ex) {
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return new Long(0);
    }

    public DBItemSchedulerVariables storeEventId(Long eventId) throws SOSHibernateException {
        SOSHibernateSession session = null;
        DBItemSchedulerVariables sv = null;
        try {
            session = dbFactory.openStatelessSession();

            session.beginTransaction();
            sv = dbLayer.insertSchedulerVariables(session, eventId);
            session.commit();
        } catch (SOSHibernateObjectOperationStaleStateException e) {
            isLocked = true;
            lockCause = "locked by an another instance";
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Exception ex) {
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return sv;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
