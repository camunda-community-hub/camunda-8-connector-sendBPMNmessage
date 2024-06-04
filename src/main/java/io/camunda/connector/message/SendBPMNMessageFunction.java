package io.camunda.connector.message;

import com.google.gson.Gson;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/* inputVariables is empty to get all of them  */
@OutboundConnector(name = "SendBPMNMessage", inputVariables = {}, type = "c-send-message")

@Component
public class SendBPMNMessageFunction implements OutboundConnectorFunction, CherryConnector {
  public static final String WORKERTYPE_SEND_MESSAGE = "c-send-message";
  public static final String BPMNERROR_INCORRECT_VARIABLE_DEFINITION = "INCORRECT_VARIABLE_DEFINITION";
  public static final String BPMNERROR_SENDMESSAGE = "SEND_MESSAGE";
  private static final Logger logger = LoggerFactory.getLogger(SendBPMNMessageFunction.class);
  ZeebeClient zeebeClient;

  // WARNING: Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'sendBPMNMessageFunction' defined in file [D:\dev\intellij\community\connector\camunda-8-connector-sendBPMNmessage\target\classes\io\camunda\connector\message\SendBPMNMessageFunction.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'zeebeClient' defined in class path resource [io/camunda/zeebe/spring/client/configuration/ZeebeClientProdAutoConfiguration.class]: Failed to instantiate [io.camunda.zeebe.client.ZeebeClient]: Factory method 'zeebeClient' threw exception with message: Receiver class io.camunda.zeebe.spring.client.configuration.ZeebeClientConfiguration does not define or inherit an implementation of the resolved method 'abstract java.net.URI getGrpcAddress()' of interface io.camunda.zeebe.client.ZeebeClientConfiguration.

  /**
   * @param zeebeClient zeebe client to connect the server
   */
  public SendBPMNMessageFunction(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @Override
  public SendBPMNMessageOutput execute(OutboundConnectorContext outboundConnectorContext) throws ConnectorException {
    SendBPMNMessageInput messageInput = outboundConnectorContext.bindVariables(SendBPMNMessageInput.class);

    try {
      String allVariablesSt = outboundConnectorContext.getJobContext().getVariables();
      Gson gson = new Gson();
      Map<String, Object> allVariables = gson.fromJson(allVariablesSt, Map.class);

      Map<String, Object> messageVariables = extractVariables(messageInput.getMessageVariables(), allVariables);

      logger.debug("Prepare messageName[{}] messageId[{}] correlation[{}] variables[{}] duration[{}]",
          messageInput.getMessageName(), messageInput.getMessageId(), messageInput.getCorrelationVariables(),
          messageVariables, messageInput.getDuration());

      String correlationValue = messageInput.getCorrelationVariables();

      PublishMessageCommandStep1.PublishMessageCommandStep3 messageCommand = zeebeClient.newPublishMessageCommand()
          .messageName(messageInput.getMessageName())
          .correlationKey(correlationValue == null ? "" : correlationValue);
      if (messageInput.getDuration() != null) {
        messageCommand = messageCommand.timeToLive(messageInput.getDuration());
      }
      if (!messageVariables.isEmpty()) {
        messageCommand = messageCommand.variables(messageVariables);
      }
      if (messageInput.getMessageId() != null) {
        messageCommand = messageCommand.messageId(messageInput.getMessageId());
      }

      PublishMessageResponse answer = messageCommand.send().join();
      logger.info("Message sent messageKey[{}] messageName[{}] messageId[{}] correlation[{}] variables[{}] duration[{}]",
          answer.getMessageKey(),
          messageInput.getMessageName(), messageInput.getMessageId(), correlationValue, messageVariables,
          messageInput.getDuration());
      return new SendBPMNMessageOutput(answer.getMessageKey(), answer.getTenantId());

    } catch (Exception e) {
      logger.error("Error during sendMessage messageName[{}] messageId[{}] correlation[{}] variables[{}] duration[{}] : {}",
          messageInput.getMessageName(),
          messageInput.getMessageId(), messageInput.getCorrelationVariables(), messageInput.getCorrelationVariables(),
          messageInput.getDuration(),e);
      throw new ConnectorException(
          BPMNERROR_SENDMESSAGE + ":Error during sendMessage [" + messageInput.getMessageName() + "] :" + e);
    }
  }

  /**
   * Return a Map of variable value, from a list of variable. Variables are separate by a comma.
   * Example firstName,lastName
   *
   * @param variableList list of variables, separate by a comma
   * @param allVariables All variables accessible
   * @return map of variable Name / variable value
   */
  private Map<String, Object> extractVariables(String variableList, final Map<String, Object> allVariables)
      throws ConnectorException {
    Map<String, Object> variables = new HashMap<>();

    if (variableList == null)
      return Collections.emptyMap();
    StringTokenizer stVariable = new StringTokenizer(variableList, ",");
    while (stVariable.hasMoreTokens()) {
      String oneExpression = stVariable.nextToken();
      StringTokenizer stOneVariable = new StringTokenizer(oneExpression, "=");
      String name = (stOneVariable.nextToken());
      if (!stOneVariable.hasMoreTokens())
        throw new ConnectorException(BPMNERROR_INCORRECT_VARIABLE_DEFINITION
            + ":the expression to calculate variables must be (<name>=<value>,)+ no = for variable [" + name
            + "] expression[" + oneExpression + "]");
      Object value = (stOneVariable.hasMoreTokens() ? stOneVariable.nextToken() : null);
      if (value != null && value.toString().startsWith("${")) {
        // extract the variable from variables
        if (!value.toString().endsWith("}")) {
          logger.error("Value [{}] for key [{}] must start by ${ and end by } like ${amount}", value, name);
          throw new ConnectorException(
              BPMNERROR_INCORRECT_VARIABLE_DEFINITION + ":Value [" + value + "] for key [" + name
                  + "] must start by ${ and end by } like ${amount} for expression[" + oneExpression + "]");
        }
        String variableName = value.toString().substring(0, value.toString().length() - 1).substring(2);
        value = allVariables.get(variableName);
      }

      variables.put(name, value);
    }
    return variables;
  }

  @Override
  public String getDescription() {
    return "Send a BPMN Message, with or without a correlation key. Variables can be onboard in the message";
  }

  @Override
  public String getLogo() {
    return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABsAAAARCAIAAACjLUBkAAAB9UlEQVQ4T2Ps7OplYWHJSE/h4uLatn1na1vnixcvGUgBxsaGrc0Nqqoqjx8/6ejsYbazc5w9Z96q1Wv5+fn9fL1joiOAFly4eOnv379QHbiBhIR4W2tTdWUZGxtr/4TJRcXlt27fYWJkZATKvX37rrKqzssn8OKly/l52Xt2bfXydIdowwrY2dnzcrP27t7m4e66ZOlyR2fP2XPm//nzByjF7OTkcuHCRYi6d+/erV234dq165YW5lFR4TbWVkDHAgUhsnAAtGzWjCmurs5HjhxTVla6eOn6jx8/oHJoJkLAvfsPli5b+eHDRw9Pt8SEWFFRkfPnL/74+RMoBQysGdMmJScnvHz5UkRE+PDRU4JCohBdcMDY3NIxf8EiKA8V8PPz5eZkxsVGf/nydd78hcBQiwgPff/+fV//5JWr1vz79w+qDhXgMxECFBTkK8tLXFycfv/+PWfugukzZn/9+hUqhw0QNhECNDTUgOFATMJigtKEwI0bt4hMp8SaSDxggdIwcPf2VSAJSaT///8HizEoq2oDxYGCSipa9+5cA4pDRIAkUBbOgAAsbgRKI5sFYQABUBBiHJQPsx4NYDER4hw4G8KAAGTjIADTUMJuhDsTwkB2NTIbDrC7EcrCAJhGYIoQmx6JB4yLFi/bvmMnlEc5YGAAAMbF6NQzlAdoAAAAAElFTkSuQmCC";
  }

  @Override
  public String getCollectionName() {
    return "BPMN";
  }

  @Override
  public Map<String, String> getListBpmnErrors() {
    return Map.of(BPMNERROR_INCORRECT_VARIABLE_DEFINITION,
        "Incorrect variable expression definition. The value must be a list of (name=variable,)+",

        BPMNERROR_SENDMESSAGE, "Message can't be send");

  }

  @Override
  public Class<?> getInputParameterClass() {
    return SendBPMNMessageInput.class;
  }

  @Override
  public Class<?> getOutputParameterClass() {
    return SendBPMNMessageOutput.class;
  }

  @Override
  public List<String> getAppliesTo() {
    return List.of("bpmn:Task", "bpmn:IntermediateThrowEvent");
  }

}
