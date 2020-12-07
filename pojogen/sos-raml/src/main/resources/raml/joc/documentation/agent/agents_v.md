Contains collection of Agents where each Agent has fields such as
* controllerId
* agentId
* agentName
* url
* isClusterWatcher
* state (COUPLED, DECOUPLED, COUPLINGFAILED, UNKNOWN)
* runningTasks
* orderIds (of running tasks if ``compact`` == true)
* errorMessage (if COUPLINGFAILED or INKNOWN)