package io.camunda.connector;

import jakarta.validation.constraints.NotEmpty;

import java.time.Duration;

public class SendBPMNMessageInput {


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


}
