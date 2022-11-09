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

### BPMN Error

These errors can be thrown by the connector:

| Code | Explanation |
|---|---|
| TOO_MANY_CORRELATION_VARIABLE_ERROR|Correlation error|
| INCORRECT_VARIABLE| A variable must `name=value`|


## Element Template

The element template can be found in the [element-templates/BpmnMessageTemplate.json](element-templates/template-connector.json) file.