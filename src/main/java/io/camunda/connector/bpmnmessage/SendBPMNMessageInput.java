package io.camunda.connector.bpmnmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import jakarta.validation.constraints.NotEmpty;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Cherry Input is not mandatory, this is just a guideline
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendBPMNMessageInput implements CherryInput {


    public static final String INPUT_MESSAGE_NAME = "messageName";
    public static final String INPUT_CORRELATION_KEY = "correlationKey";
    public static final String INPUT_MESSAGE_VARIABLES = "messageVariables";
    public static final String INPUT_MESSAGE_ID = "messageId";
    public static final String INPUT_MESSAGE_DURATION = "messageDuration";

    public static final String LABEL = "label";
    public static final String NAME = "name";
    public static final String CLASS = "class";
    public static final String LEVEL = "level";
    public static final String EXPLANATION = "explanation";


    @NotEmpty
    private String messageName;
    private String correlationKey;
    private String messageVariables;
    private String messageId;
    private Object messageDuration;

    private Map<String,Object> allVariables;

    public static SendBPMNMessageInput bindVariables(Map<String,Object> allVariables) {
        SendBPMNMessageInput input = new SendBPMNMessageInput();
        input.messageName = getString( allVariables.get(INPUT_MESSAGE_NAME));
        input.correlationKey = getString( allVariables.get(INPUT_CORRELATION_KEY));
        input.messageVariables = getString( allVariables.get(INPUT_MESSAGE_VARIABLES));
        input.messageId = getString( allVariables.get(INPUT_MESSAGE_ID));
        input.correlationKey = getString( allVariables.get(INPUT_CORRELATION_KEY));
        input.messageDuration = allVariables.get(INPUT_MESSAGE_DURATION);
        input.allVariables = allVariables;
        return input;
    }


    public String getMessageName() {
        return messageName;
    }

    public String getCorrelationKey() {
        return correlationKey;
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

    public Map<String,Object> getAllVariables() {
        return allVariables;
    }
    /**
     * this method is exploded by Cherry Runtime to produce a nice element-template
     *
     * @return list of parameters
     */
    public List<Map<String, Object>> getInputParameters() {
        return Arrays.asList(Map.of(NAME, INPUT_MESSAGE_NAME, // name
                LABEL, "Message name", // label
                CLASS, Object.class, // class
                LEVEL, "REQUIRED", // level
                EXPLANATION, "Message name. Mandatory to find the Bpmn element to target"),
            Map.of(NAME, INPUT_CORRELATION_KEY,// name
                LABEL, "Correlation key", // label
                CLASS, String.class, // class
                LEVEL, "OPTIONAL",// level
                EXPLANATION, "This information is mandatory for a Intermediate Catch event"),
            Map.of(NAME, INPUT_MESSAGE_ID, // name
                LABEL, "Message Id", // label
                CLASS, String.class, // class
                LEVEL, "OPTIONAL", // level
                EXPLANATION,
                "Default value."),
        Map.of(NAME, INPUT_MESSAGE_DURATION, // name
            LABEL, "Duration", // label
            CLASS, String.class, // class
            LEVEL, "OPTIONAL", // level
            EXPLANATION,
            "java.time.Duration object, a Long or a String to be parse by the duration class."));
    }

    /**
     * return the object as a String
     * @param value to return
     * @return String of the value
     */
    private static String getString(Object value) {
        if (value==null)
            return null;
        if (value instanceof  String)
            return (String)value;
        return value.toString();
    }
}
