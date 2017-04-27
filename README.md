# Chaos Monkey

Chaos Monkey provides a convenient way to disrupt CDAP and hadoop services on a cluster. 
Disruptions can be scheduled, randomized, or issued on command. <br/>


## Standalone Chaos Monkey 
To start Chaos Monkey daemon and HTTP server, set configurations in chaos-monkey-site.xml and run ChaosMonkeyMain <br/>

### Configurations
**Disruptions setup** <br/>
>By default, the following disruptions will be available to each service: <br/>
>* start <br/>
>* restart <br/>
>* stop <br/>
>* terminate <br/>
>* kill <br/>
>* rolling-restart <br/>
>
>Custom disruptions can be added by extending the Disruption class and then associating them with a service.
>A custom disruption is started by calling ClusterDisruptor.disrupt(serviceName, disruptionName, actionArguments), 
>where disruptionName is set by the Disruption.getName() method.
>Disruptions receive a collection of RemoteProcess based on the actionArguments, and can be used to execute commands 
>via ssh. To add a custom disruption to a service:
>* {service}.disruptions - Class paths of custom disruptions, separated by commas

**Initialize a service for Chaos Monkey** <br/>
>Any configured service can be interacted with through ClusterDisruptor or REST endpoints. To configure a service for 
chaos Monkey, either provide custom disruptions or a pid file for the default disruptions: <br/>
>* {service}.pidFile - Path to the .pid file of the service <br/>

**Configurations for scheduled disruptions** <br/>
>These additional properties can be set for a certain service to start a scheduled disruption: <br/>
>* {service}.interval - Number of seconds between each disruption <br/>
>* {service}.killProbability - Number between 0 to 1 representing chance of kill occurring each iteration. <br/>
>* {service}.stopProbability - Number between 0 to 1 representing chance of stop occurring each iteration. <br/>
>* {service}.restartProbability - Number between 0 to 1 representing chance of restart occurring each iteration. <br/>
>* {service}.minNodesPerIteration - Minimum number of nodes affected each iteration. <br/>
>* {service}.maxNodesPerIteration - Maximum number of nodes affected each iteration. <br/>

**Cluster information collector** <br/>
>By default, Chaos Monkey will retrieve cluster information from Coopr <br/>
>To get cluster information from Coopr, the following configurations need to be set:<br/>
>* cluster.info.collector.coopr.clusterId <br/>
>* cluster.info.collector.coopr.tenantId <br/>
>* cluster.info.collector.coopr.server.uri <br/>
>
>To get cluster information from other sources, include a plugin to implement ClusterInfoCollector and set the 
following configs: <br/>
>* cluster.info.collector.class - classpath of the implementation of ClusterInfoCollector
>
>Additional properties can be passed in to the ClusterInfoCollector implementation. Setting the property
cluster.info.collector.{propertyName} in configurations will make {propertyName} available in the properties map, 
passed in via the initialize method

**SSH configurations** <br/>
>username - username of SSH profile (if different from system user)<br/>
>keyPassphrase - passphrase for private key, if applicable <br/>
>privateKey - path to private key (will check default locations unless specified)<br/>

## HTTP endpoints
HTTP server is hosted on port 11020, with the following endpoints: <br/>

>**POST /v1/services/{service}/{action}** <br/>
>{action} includes stop, kill, terminate, start, restart, and rolling-restart <br/>
>The action, by default, will be performed on all nodes configured with the service. To specify affected nodes, include
ne of the following request bodies:
>```
>{
>  nodes:[<nodeAddress1>,<nodeAddress2>...]
>}
>```
>```
>{
>  percentage:<numberFrom0To100>
>}
>```
>```
>{
>  count:<numberOfNodes>
>}
>```
>In addition to the above request bodies, rolling restart can be also configured with:
>```
>{
>  restartTime:<restartTimeSeconds>
>  delay:<delaySeconds>
>}
>```

>**GET /v1/nodes/{ip}/status** <br/>
>Get the status of all configured service on a given address <br/>

>**GET /v1/status** <br/>
>Get the status of all configured service on every node of the cluster <br/>
