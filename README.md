# Flink SQL Gateway Server

This project start [Flink SQL Gateway](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/table/sql-gateway/rest/) REST Endpoint in embedded mode.

In this mode we can add security, monitor Flink SQL Gateway `Sessions/Operations` and so on...

# How to use

Make your onw configuration of `SqlGatewayProperties`, then run `FlinkSqlGatewayEmbeddedServer`. 
After `FlinkSqlGatewayEmbeddedServer` started `SqlGateway` also started, you can use [Flink SQL Gateway Client](https://github.com/40coderplus/flink-sql-gateway-client) to send requests;

Example:
```java
DefaultApi api = FlinkSqlGateway.sqlGatewayApi("http://127.0.0.1:8083");
OpenSessionResponseBody response = api.openSession(new OpenSessionRequestBody()
    .putPropertiesItem("execution.target", "yarn-session")
    .putPropertiesItem("flink.hadoop.yarn.resourcemanager.ha.enabled", "true")
    .putPropertiesItem("flink.hadoop.yarn.resourcemanager.ha.rm-ids", "rm1,rm2")
    .putPropertiesItem("flink.hadoop.yarn.resourcemanager.hostname.rm1", "yarn01")
    .putPropertiesItem("flink.hadoop.yarn.resourcemanager.hostname.rm2", "yarn01")
    .putPropertiesItem("flink.hadoop.yarn.resourcemanager.cluster-id", "yarn-cluster")
    .putPropertiesItem(
            "flink.hadoop.yarn.client.failover-proxy-provider",
            "org.apache.hadoop.yarn.client.ConfiguredRMFailoverProxyProvider")
    .putPropertiesItem("yarn.application.id", "application_1667789375191_XXXX"));
System.out.println(response.getSessionHandle());

ExecuteStatementResponseBody executeStatementResponseBody = api.executeStatement(
    UUID.fromString(response.getSessionHandle()),
    new ExecuteStatementRequestBody()
            .statement("select 1")
            .putExecutionConfigItem("pipeline.name", "Flink SQL Gateway SDK Example"));
System.out.println(executeStatementResponseBody.getOperationHandle());
```

# Deal with `Sessions/Operations`

Just define a bean witch implementation `SqlGatewayEventListener`, you can do anything about `Sessions/Operations`. `Sessions/Operations` only contains changes.

