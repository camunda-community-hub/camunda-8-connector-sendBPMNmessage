package io.camunda.connector.zeebe.command.processor;

import io.camunda.connector.zeebe.ZeebeGatewayClient;
import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput;
import io.camunda.connector.zeebe.command.PublishMessageCommand;
import io.camunda.connector.zeebe.command.processor.ZeebeGatewayCommandProcessor.TypedZeebeGatewayCommandProcessor;

public class PublishMessageProcessor
    extends TypedZeebeGatewayCommandProcessor<PublishMessageCommand> {
  @Override
  protected ZeebeGatewayConnectorOutput processTyped(
      PublishMessageCommand command, ZeebeGatewayClient zeebeGatewayClient) {
    return zeebeGatewayClient.publishMessage(command);
  }

  @Override
  protected Class<PublishMessageCommand> type() {
    return PublishMessageCommand.class;
  }
}
