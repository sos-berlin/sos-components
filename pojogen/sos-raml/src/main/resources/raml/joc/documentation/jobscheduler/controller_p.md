Contains collection of Controllers and Agents where each Controller has fields such as
* controllerId
* host
* url
* clusterUrl (if not STANDALONE)
* role (STANDALONE, PRIMARY or BACKUP)
* version
* operating system
    * name
    * architecture

and each Agent has fields such as
* controllerId
* agentId
* agentName
* url
* isClusterWatcher