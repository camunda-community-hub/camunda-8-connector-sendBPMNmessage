# camunda-8-connector-sendBPMNmessage


![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)

A Camunda 8 Connector to send a BPMN Message

## Build

```bash
mvn clean package
```


### Input

```json
{
}
```

Attention, [Feel](https://docs.camunda.io/docs/components/modeler/feel/what-is-feel/) is not able to add different type of variable.



For example, an expression 

```
= "requestId="+requestId
```

return null if requestId is not a String (but a long).

The connector accept the notation ${variableName} to calculate the list of variable.
In this example, use
```
= "requestId=${requestId}
```
Doing this way, if requestId is a long, it will send in the message as a Long.


### Output

```json
{

}
```
No output

### BPMN Errors

These errors can be thrown by the connector:

| Code | Explanation |
|---|---|
| TOO_MANY_CORRELATION_VARIABLE_ERROR|Correlation error|
| INCORRECT_VARIABLE| A variable must `name=value`|


## Element Template

The element template can be found in the [element-templates/BpmnMessageTemplate.json](element-templates/template-connector.json) file.


# Start the connector
The connector runs under the Cherry framework. It is possible to start it.

## Specify the connection to Zeebe

The connection to the Zeebe engine is piloted via the application.properties located on src/main/java/resources/application.properties

### Use a Camunda Saas Cloud:

1. Follow the [Getting Started Guid](https://docs.camunda.io/docs/guides/getting-started/) to create an account, a
   cluster and client credentials

2. Use the client credentials to fill the following environment variables:
    * `ZEEBE_ADDRESS`: Address where your cluster can be reached.
    * `ZEEBE_CLIENT_ID` and `ZEEBE_CLIENT_SECRET`: Credentials to request a new access token.
    * `ZEEBE_AUTHORIZATION_SERVER_URL`: A new token can be requested at this address, using the credentials.

3. fulfill the information in the application.properties
````
# use a cloud Zeebe engine
zeebe.client.cloud.region=
zeebe.client.cloud.clusterId=
zeebe.client.cloud.clientId=
zeebe.client.cloud.clientSecret=
````

### Use a onPremise Zeebe

Use this information in the application.properties, and provide the IP address to the Zeebe engine

````
# use a onPremise Zeebe engine
zeebe.client.broker.gateway-address=127.0.0.1:26500
````

## Start the application
Attention: this connector works with the JDK 17.

Start the connector with
````
mvn spring-boot:run
````

Access then localhost:9081 to access the dashboard

