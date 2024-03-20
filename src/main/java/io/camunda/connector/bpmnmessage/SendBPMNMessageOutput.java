package io.camunda.connector.bpmnmessage;

import io.camunda.connector.cherrytemplate.CherryOutput;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SendBPMNMessageOutput implements CherryOutput {
  // No information on output

  public static String OUTPUT_MESSAGE_KEY = "messageKey";

  public static final String LABEL = "label";
  public static final String NAME = "name";
  public static final String CLASS = "class";
  public static final String LEVEL = "level";
  public static final String EXPLANATION = "explanation";

  private long messageKey;

  public void setMessageKey(long messageKey) {
    this.messageKey = messageKey;
  }

  /**
   * Bind the output to the variablesToUpdate map
   *
   * @param variablesToUpdate to olace variable here
   */
  public void bindVariables(Map<String, Object> variablesToUpdate) {
    // The output does not produce any variables
    variablesToUpdate.put("messageKey", Long.valueOf(messageKey));
  }

  /**
   * this method is exploded by Cherry Runtime to produce a nice element-template
   *
   * @return list of parameters
   */
  public List<Map<String, Object>> getOutputParameters() {
    return Arrays.asList(Map.of(NAME, OUTPUT_MESSAGE_KEY, // name
        LABEL, "Message Key", // label
        CLASS, Long.class, // class
        LEVEL, "REQUIRED", // level
        EXPLANATION, "Message key"));

  }
}
