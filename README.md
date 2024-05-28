[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)


# camunda-8-connector-sendBPMNmessage

A Camunda 8 Connector to send a BPMN Message



## Principle
From a PDF document to an extract expression, a new PDF is built.

## Inputs

| Name                 | Description                                        | Class            | Default | Level     |
|----------------------|----------------------------------------------------|------------------|---------|-----------|
| messageName          | Name of message to send, used to find the receiver | java.lang.String |         | REQUIRED  |
| messageId            | Message Id                                         | java.lang.String |         | OPTIONAL  | 
| correlationVariables | Correlation value (1)                              | java.lang.String |         | OPTIONAL  |
| MessageVariables     | Expression to get the variable expression (2)      | java.lang.String |         | REQUIRED  | 
| messageDuration      | Duration of message on server                      | java.lang.String |         | OPTIONAL  |


(1) *CorrelationVariables*
If the receiver is a intermediate catch event message, a correlation is mandatory to find the process instance to unfroze.

(2) *MessageVariables*:

Variables to copy in the message. The format is "(<variableName>=<value>)*
An string can be done explicitaly
To give the value, a FEEL expression can be used
`````
statusTransferExpense=Correct,dateTransferExpense=2024/12/05
`````

Note: A FEEL Expression can be provide:
`````
= "statusTransferExpense="+statusTransfer+",dateTransferExpense="+dateTransfer
`````

where statusTransfer and dateTransfer are process variables.


But attention, [Feel](https://docs.camunda.io/docs/components/modeler/feel/what-is-feel/) expression accept only string:
Variables `statusTransfer`, `dateTransfer` must be String processes variable.

If not, the complete expression returns null if requestId is not a String (but a long), without any information.

To avoid this and use any kind of variable, use a constant expression with placeholder `${<processVariable>}`.

For example, in the expression:

`````
statusTransferExpense=${statusTransfer}, dateTransferExpense=${dateTransfer}, amountTransfer=${amount}
`````

`statusTransfer` will be replace by its value, a String, `dateTransfer` can be a Date, and `amount` a double.

See test/resources/SendMessage.bpmn"


## Output
| Name             | Description                     | Class            | Level    |
|------------------|---------------------------------|------------------|----------|
| destinationFile  | Reference to the file produced  | java.lang.String | REQUIRED |

## BPMN Errors

| Name                                     | Explanation                                                                                                                                      |
|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| LOAD_ERROR                               | An error occurs during the load                                                                                                                   |
| LOAD_DOCSOURCE                           | The reference can't be decoded                                                                                                                   |
| BAD_STORAGE_DEFINITION                   | The storage definition does not correctly describe |
| NO_DESTINATION_STORAGE_DEFINITION_DEFINE | A storage definition must be set to store the result document                                                                                    |
| LOAD_PDF_ERROR                           | Error reading the document - is that a PDF?                                                                                                      |
| ENCRYPTED_PDF_NOT_SUPPORTED              | Encrypted PDF is not supported                                                                                                                   |
| ERROR_CREATE_FILEVARIABLE                | Error when reading the PDF to create a fileVariable to save                                                                                      |
| SAVE_ERROR                               | An error occurs during the save                                                                                                                  |
| INVALID_EXPRESSION                       | Invalid expression to pilot the extraction. Format must be <number1>-<number2> where number1<=number2. n means 'end of document' : example, 10-n |
| EXTRACTION_ERROR                         | Extraction error                                                                                                                                 |



### BPMN Errors

These errors can be thrown by the connector:

| Code | Explanation                                                          |
|---|----------------------------------------------------------------------|
| TOO_MANY_CORRELATION_VARIABLE_ERROR| Correlation error. The Correlation expect one and only one variable. |
| INCORRECT_VARIABLE| A variable must `name=value`                                         |


# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependencies can be upload in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element template can be found in the [element-templates](element-templates) directory.
