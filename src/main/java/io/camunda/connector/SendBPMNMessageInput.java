package io.camunda.connector;

import io.camunda.cherry.definition.AbstractConnectorInput;
import io.camunda.cherry.definition.RunnerParameter;
import jakarta.validation.constraints.NotEmpty;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class SendBPMNMessageInput extends AbstractConnectorInput {
    private static final String INPUT_MESSAGE_NAME = "messageName";
    private static final String INPUT_CORRELATION_VARIABLES = "correlationVariables";
    private static final String INPUT_MESSAGE_VARIABLES = "messageVariables";
    private static final String INPUT_MESSAGE_ACCESS = "*";
    private static final String INPUT_MESSAGE_ID_VARIABLES = "messageId";
    private static final String INPUT_MESSAGE_DURATION = "messageDuration";

    @NotEmpty
    private String messageName;
    private String correlationVariables;
    private String messageVariables;
    private String idMessage;
    private Object messageDuration;


    public String getMessageName() {
        return messageName;
    }

    public String getCorrelationVariables() {
        return correlationVariables;
    }

    public String getMessageVariables() {
        return messageVariables;
    }

    public String getIdMessage() {
        return idMessage;
    }

    public Object getMessageDuration() {
        return messageDuration;
    }

    public Duration getDuration() {
        if (messageDuration == null)
            return null;
        if (messageDuration instanceof Duration valueDuration)
            return valueDuration;
        if (messageDuration instanceof Long valueLong)
            return Duration.ofMillis(valueLong);
        try {
            return Duration.parse(messageDuration.toString());
        } catch (Exception var6) {
            return null;
        }

    }

    /**
     * Return the parameters definition
     *
     * @return list of parameters
     */
    @Override
    public List<RunnerParameter> getInputParameters() {
        return Arrays.asList(
                RunnerParameter.getInstance(INPUT_MESSAGE_NAME, "Message name", String.class, RunnerParameter.Level.REQUIRED, "Message name"),
                RunnerParameter.getInstance(INPUT_CORRELATION_VARIABLES, "Correlation variables", String.class, RunnerParameter.Level.OPTIONAL, "Correlation variables. The content of theses variable is used to find the process instance to unfroze"),
                RunnerParameter.getInstance(INPUT_MESSAGE_VARIABLES, "Message variables", String.class, RunnerParameter.Level.OPTIONAL, "Variables to copy in the message"),
                RunnerParameter.getInstance(INPUT_MESSAGE_ID_VARIABLES, "ID message", String.class, RunnerParameter.Level.OPTIONAL, "Id of the message"),
                RunnerParameter.getInstance(INPUT_MESSAGE_ACCESS, "*", String.class, RunnerParameter.Level.OPTIONAL, "Access any variables referenced in the Correlation of the Message list"),
                RunnerParameter.getInstance(INPUT_MESSAGE_DURATION, "Duration (FEEL duration, String like 'PT1S', or time in ms)", Object.class, RunnerParameter.Level.OPTIONAL, "Message duration. After this delay, message is deleted if it doesn't fit a process instance"));

    }
}
