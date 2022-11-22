package io.camunda.connector;

import io.camunda.zeebe.client.ZeebeClient;
import com.google.gson.Gson;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.time.Duration;
import java.util.*;

@OutboundConnector(
         name = "SendBPMNMessage", inputVariables = {"messageName", "correlationVariables", "messageVariables", "messageId", "messageDuration"},
         type = "c-send-message")

public class SendBPMNMessageFunction implements OutboundConnectorFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendBPMNMessageFunction.class);
    public static final String WORKERTYPE_SEND_MESSAGE = "c-send-message";

    public static final String BPMNERROR_TOO_MANY_CORRELATION_VARIABLE_ERROR = "TOO_MANY_CORRELATION_VARIABLE_ERROR";
    public static final String BPMNERROR_INCORRECT_VARIABLE = "INCORRECT_VARIABLE";



    ZeebeClient zeebeClient;

    /**
     * I need the zeebeContainer: how do I get it
     * @param zeebeClient client to call the API to send message
     */
    public void setZeebeClient( ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }
    @Override
    public SendBPMNMessageOutput execute(OutboundConnectorContext context) throws ConnectorException {

        SendBPMNMessageInput messageInput = context.getVariablesAsType(SendBPMNMessageInput.class);

        try {
            sendMessageViaGrpc(messageInput.getMessageName(),
                    messageInput.getCorrelationVariables(),
                    messageInput.getMessageVariables(),
                    messageInput.getMessageId(),
                    messageInput.getDuration(),
                    context);


        } catch (Exception e) {
            LOGGER.error("Error during sendMessage [" + messageInput.getMessageName() + "] :" + e);
            throw new ConnectorException("FAIL", "Error during sendMessage [" + messageInput.getMessageName() + "] :" + e);
        }
        return new SendBPMNMessageOutput();
    }


    /**
     * Send a message
     * https://docs.camunda.io/docs/apis-clients/grpc/#publishmessage-rpc
     *
     * @param messageName              the message name
     * @param correlationVariablesList List of variable where the value has to be fetched to get the correlation key of the message. Attention: only one variable is expected
     * @param messageVariableList      the message variables send to the message
     * @param messageId                the unique ID of the message; can be omitted. only useful to ensure only one message with the given ID will ever be published (during its lifetime)
     * @param timeToLiveDuration       how long the message should be buffered on the broker
     * @param context                  context on job to execute
     */
    private void sendMessageViaGrpc(String messageName,
                                    String correlationVariablesList,
                                    String messageVariableList,
                                    String messageId,
                                    Duration timeToLiveDuration,
                                    final OutboundConnectorContext context) throws ConnectorException {
        String allVariablesSt = context.getVariables();
        Gson gson = new Gson();
        Map<String, Object> allVariables =(Map<String, Object>) gson.fromJson(allVariablesSt, Map.class);


        Map<String, Object> correlationVariables = extractVariable(correlationVariablesList, allVariables, context);
        Map<String, Object> messageVariables = extractVariable(messageVariableList, allVariables, context);

        LOGGER.info("MessageName[" + messageName
                + "] messageId[" + messageId
                + "] Correlation Variable[" + correlationVariables
                + "] Variable[" + messageVariables + "]"
                + "] Duration[" + timeToLiveDuration + "]");

        // At this moment, we expect only one variable for the correlation key
        if (correlationVariables.size() > 1) {
            LOGGER.error("One (and only one) variable is expected for the correction");
            throw new ConnectorException(BPMNERROR_TOO_MANY_CORRELATION_VARIABLE_ERROR, "One variable expected for the correction:[" + correlationVariablesList + "]");
        }
        String correlationValue = null;
        if (!correlationVariables.isEmpty()) {
            Map.Entry<String, Object> entry = correlationVariables.entrySet().iterator().next();
            correlationValue = entry.getValue() == null ? null : entry.getValue().toString();
        }

        PublishMessageCommandStep1.PublishMessageCommandStep3 messageCommand = zeebeClient
                .newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationValue == null ? "" : correlationValue);
        if (timeToLiveDuration != null)
            messageCommand = messageCommand.timeToLive(timeToLiveDuration);
        if (!messageVariables.isEmpty()) {
            messageCommand = messageCommand.variables(messageVariables);
        }
        if (messageId != null)
            messageCommand = messageCommand.messageId(messageId);

        messageCommand.send().join();
    }

    /**
     * Return a Map of variable value, from a list of variable. Variables are separate by a comma.
     * Example firstName,lastName
     *
     * @param variableList list of variables, separate by a comma
     * @param allVariables All variables accessible
     * @param context context of the execution
     * @return map of variable Name / variable value
     */
    private Map<String, Object> extractVariable(String variableList, final Map<String, Object> allVariables, final OutboundConnectorContext context) {
        Map<String, Object> variables = new HashMap<>();

        if (variableList == null)
            return Collections.emptyMap();
        StringTokenizer stVariable = new StringTokenizer(variableList, ",");
        while (stVariable.hasMoreTokens()) {
            StringTokenizer stOneVariable = new StringTokenizer(stVariable.nextToken(), "=");
            String name = (stOneVariable.nextToken());
            if (!stOneVariable.hasMoreTokens())
                throw new ConnectorException(BPMNERROR_INCORRECT_VARIABLE, "A variable must be <name>=<value>, no = for variable [" + name + "]");
            Object value = (stOneVariable.hasMoreTokens() ? stOneVariable.nextToken() : null);
            if (value != null && value.toString().startsWith("${")) {
                // extract the variable from variables
                if (!value.toString().endsWith("}")) {
                    LOGGER.error("Value [" + value + "] for key [" + name + "] must start by ${ and end by } like ${amount}");
                    throw new ConnectorException(BPMNERROR_INCORRECT_VARIABLE, "Value [" + value + "] for key [" + name + "] must start by ${ and end by } like ${amount}");
                }
                String variableName = value.toString().substring(0, value.toString().length() - 1).substring(2);
                value = allVariables.get(variableName);
            }

            variables.put(name, value);
        }
        return variables;
    }


}
