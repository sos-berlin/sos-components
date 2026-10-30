package com.sos.joc.classes.agent;

import java.util.List;

import js7.data.cluster.ClusterWatchProblems.ClusterNodeLostEventNotConfirmedProblem;
import js7.data.subagent.SubagentId;

public record ClusterNodeLoss(List<SubagentId> directors, ClusterNodeLostEventNotConfirmedProblem evt) {}
