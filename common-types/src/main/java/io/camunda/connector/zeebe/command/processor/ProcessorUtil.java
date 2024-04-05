package io.camunda.connector.zeebe.command.processor;

import java.time.Duration;

public class ProcessorUtil {
  public static Duration handleDurationExpression(String expression) {
    try {
      return Duration.ofSeconds(Integer.parseInt(expression));
    } catch (Exception e) {
      // ignore
    }
    return Duration.parse(expression);
  }

  public static void applyIfNotNull(Object value, Runnable applier) {
    if (value != null) {
      applier.run();
    }
  }
}
