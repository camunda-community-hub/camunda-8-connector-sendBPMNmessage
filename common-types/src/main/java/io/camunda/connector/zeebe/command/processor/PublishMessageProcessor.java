package io.camunda.connector.zeebe.command.processor;

import static io.camunda.connector.zeebe.command.processor.ProcessorUtil.*;

import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput;
import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput.PublishMessageResult;
import io.camunda.connector.zeebe.command.PublishMessageCommand;
import io.camunda.connector.zeebe.command.processor.ZeebeClientCommandProcessor.TypedZeebeGatewayCommandProcessor;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;

public class PublishMessageProcessor
    extends TypedZeebeGatewayCommandProcessor<PublishMessageCommand> {
  @Override
  protected ZeebeGatewayConnectorOutput processTyped(
      PublishMessageCommand command, ZeebeClient zeebeClient) {
    PublishMessageCommandStep3 builder =
        zeebeClient
            .newPublishMessageCommand()
            .messageName(command.name())
            .correlationKey(command.correlationKey());
    applyIfNotNull(
        command.timeToLive(),
        () -> builder.timeToLive(handleDurationExpression(command.timeToLive())));
    applyIfNotNull(command.messageId(), () -> builder.messageId(command.messageId()));
    applyIfNotNull(command.variables(), () -> builder.variables(command.variables()));
    applyIfNotNull(command.tenantId(), () -> builder.tenantId(command.tenantId()));
    PublishMessageResponse response = builder.send().join();
    return new PublishMessageResult(response.getMessageKey(), response.getTenantId());
  }

  @Override
  protected Class<PublishMessageCommand> type() {
    return PublishMessageCommand.class;
  }
}
