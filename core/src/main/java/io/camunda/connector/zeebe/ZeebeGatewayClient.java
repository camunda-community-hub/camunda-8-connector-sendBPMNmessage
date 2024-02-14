package io.camunda.connector.zeebe;

import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput.PublishMessageResult;
import io.camunda.connector.zeebe.command.PublishMessageCommand;
import java.util.ServiceLoader;

public interface ZeebeGatewayClient {
  static ZeebeGatewayClient load() {
    return ServiceLoader.load(ZeebeGatewayClient.class).findFirst().orElseThrow();
  }

  PublishMessageResult publishMessage(PublishMessageCommand publishMessageRequest);
}
