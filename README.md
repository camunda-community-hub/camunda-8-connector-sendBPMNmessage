[![Community badge: Incubating](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)


# camunda-8-connector-sendBPMNmessage

A Camunda 8 Connector to send a BPMN Message.

## Principle
The connector publish a message. All information, messageId, messageContent, Correlation key and Time To Live are available as parameters


## Inputs
| Name                | Description                                             | Class             | Default | Level      |
|---------------------|---------------------------------------------------------|-------------------|---------|------------|
| messageName         | Name of the message                                     | java.lang.String  |         | REQUIRED   |
| correlationVariable | Name of the variable which contains the correlation key | java.lang.String  |         | OPTIONAL   |
| messageVariables    | Variables to send to the message, separate with a comma | java.lang.String  |         | OPTIONAL   |
| messageId           | Message Id                                              | java.lang.String  |         | OPTIONAL   |
| messageDuration     | Duration of the message (TimeToLive)                    | java.lang.Object  |         | OPTIONAL   |

### correlationVariable
This input contains the value.
See the FEEL expression section.

### MessageVariables
A list of ``variableName=value`` separated by a comma. The variable is the name in the message, and the value how to fulfill the variable.
See the FEEL expression section.

Examples
````
"Id="+customerId+",duration=12,address="+customerAddress
````
of this form is accepted too
````
"Id=${customerId},duration=12,address=${customerAddress}"
````

the message contains

````
Id="24455"
duration=12
address="1 champs Elysee, Paris"
````

assuming 24455 is the value inside the process variable customerId

### MessageDuration:
The duration may be
* java.time.Duration
* java.lang.Long value. The command is running to get the duration
````
return Duration.ofMillis(valueLong);
`````

* java.lang.String value. The command is running to get the duration
````
return Duration.parse(messageDuration.toString());
````

## Output
| Name       | Description                                        | Class          | Level    |
|------------|----------------------------------------------------|----------------|----------|
| messageKey | Key of the message. The key is calculated by Zeebe | java.lang.Long | Required | 

Zeebe calculated a messageKey for each message it received.

## BPMN Errors
| Name                                          | Explanation                                                            |
|-----------------------------------------------|------------------------------------------------------------------------|
| BPMNERROR_INCORRECT_VARIABLE                  | Conversion error. The format of the variable list is (Variable=value)* |
| BPMNERROR_SENDMESSAGE_VARIABLE                | Error during the send operation                                        | 


## Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependency can be upload in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element templates can be found in the [element-templates/bpmn-message.json](element-templates/bpmn-message.json) file.



### Input

```json
{
   "messageName" : "v-message-transferdone",
   "correlationVariables": "requestId=${requestId}",
   "messageId" :  "This-is-an-id",
   "messageDuration": "duration(\"PT1H\")",
   "MessageVariables":  "statusTransferExpense=${status}, dateTransferExpense=${dateTransfer}, amountTransfer=${amount}"

}
```
*MessageName* : this information is used to link the sender and the receiver 

*CorrelationVariables*:
The content of these variable is used to find the process instance to unfroze.
One variable must be provided, with the format "<variableName>=<value>"
For example  `````"requestId="+requestId`````.
Attention, the FEEL expression accept only String information in the concatenation (see bellow)

*MessageId* : internal id (optional)

*MessageDuration*: the message is stored in the engine for a limited time. After, it will be deleted. A duration can be given with the duration() FEEL function, or a long in millisecond.

*MessageVariables*:
Variables to copy in the message. The format is "(<variableName>=<value>)*
To give the value, a FEEL expression can be used 
`````"statusTransfert"+status`````


**FEEL expression**

To provide the value in the CorrelationVariables and MessageVariables, you can use a FEEL expression

Example:
`````"requestId="+requestId`````

or

`````"statusTransfert"+status+"dateTransfertExpense="+dateTransfer`````

But attention, [Feel](https://docs.camunda.io/docs/components/modeler/feel/what-is-feel/) expression accept only string: 
Variables `requestId`, `status`, `dateTransfer` must be String processes variable. 
If not, the complete expression returns null if requestId is not a String (but a long), without any information.


To transfer any other type, use `${<processVariable>}` format is possible.
For example, in the expression:

`````"statusTransferExpense=${status}, dateTransferExpense=${dateTransfer}, amountTransfer=${amount}"`````

`status` will be replace by its value, a String, `dateTransfer` can be a Date, and `amount` a double.

For example
 `````"requestId=${requestId}"`````

return the expected information when `requestId` is a Long.


See test/resources/SendMessage.bpmn"


### Output

```json
{

}
```
No output

### BPMN Errors

These errors can be thrown by the connector:

| Code | Explanation                                                          |
|---|----------------------------------------------------------------------|
| TOO_MANY_CORRELATION_VARIABLE_ERROR| Correlation error. The Correlation expect one and only one variable. |
| INCORRECT_VARIABLE| A variable must `name=value`                                         |


## Element Template

The element template can be found in the [element-templates/BpmnMessageTemplate.json](element-templates/send-bpmn-message.json) file.


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

or use `start.bat|sh`


Access then localhost:9081 to access the dashboard (the port number is specified in the `application.properties`)

