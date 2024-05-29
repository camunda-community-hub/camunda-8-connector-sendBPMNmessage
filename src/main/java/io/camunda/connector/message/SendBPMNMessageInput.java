package io.camunda.connector.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import jakarta.validation.constraints.NotEmpty;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendBPMNMessageInput implements CherryInput {

  @NotEmpty
  private String messageName;
  private String correlationVariables;
  private String messageVariables;
  private String messageId;
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

  public String getMessageId() {
    return messageId;
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

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return List.of(Map.of(PARAMETER_MAP_NAME, "MessageName", PARAMETER_MAP_LABEL, "Message name", PARAMETER_MAP_CLASS,
            String.class, PARAMETER_MAP_LEVEL, PARAMETER_MAP_LEVEL_REQUIRED, PARAMETER_MAP_EXPLANATION,
            "Message name to send"),
        Map.of(PARAMETER_MAP_NAME, "correlationVariables", PARAMETER_MAP_LABEL, "Correlation variable",
            PARAMETER_MAP_CLASS, String.class, PARAMETER_MAP_LEVEL, PARAMETER_MAP_LEVEL_OPTIONAL,
            PARAMETER_MAP_EXPLANATION, "Value of the correlation, if the message will be catch by a correlation event"),

        Map.of(PARAMETER_MAP_NAME, "messageVariables", // name
            PARAMETER_MAP_LABEL, "Message variables", // label
            PARAMETER_MAP_CLASS, String.class, // Class
            PARAMETER_MAP_LEVEL, PARAMETER_MAP_LEVEL_OPTIONAL, //optional
            PARAMETER_MAP_EXPLANATION, "Variables to copy in the message"),

        Map.of(PARAMETER_MAP_NAME, "messageId", // Name
            PARAMETER_MAP_LABEL, "Message ID", // Label
            PARAMETER_MAP_CLASS, String.class, // Class
            PARAMETER_MAP_LEVEL, PARAMETER_MAP_LEVEL_OPTIONAL,// Optional
            PARAMETER_MAP_EXPLANATION, "Id of the message"),

        Map.of(PARAMETER_MAP_NAME, "messageDuration", // Name
            PARAMETER_MAP_LABEL, "Duration (FEEL duration or time in ms)", // Label
            PARAMETER_MAP_CLASS, String.class, // Class
            PARAMETER_MAP_LEVEL, PARAMETER_MAP_LEVEL_OPTIONAL,// Optional
            PARAMETER_MAP_EXPLANATION,
            "Message duration. After this delay, message is deleted if it doesn\u0027t fit a process instance")

    );
  }
}
