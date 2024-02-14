package io.camunda.connector.zeebe;

import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput.PublishMessageResult;
import io.camunda.connector.zeebe.command.PublishMessageCommand;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import java.time.Duration;
import java.util.function.Supplier;

public class ZeebeGatewayClientImpl implements ZeebeGatewayClient {
  private final Supplier<ZeebeClient> zeebeClientSupplier;

  public ZeebeGatewayClientImpl(Supplier<ZeebeClient> zeebeClientSupplier) {
    this.zeebeClientSupplier = zeebeClientSupplier;
  }

  public ZeebeGatewayClientImpl() {
    this(ZeebeClient::newClient);
  }

  @Override
  public PublishMessageResult publishMessage(PublishMessageCommand publishMessageRequest) {
    PublishMessageCommandStep3 builder =
        zeebeClientSupplier
            .get()
            .newPublishMessageCommand()
            .messageName(publishMessageRequest.name())
            .correlationKey(publishMessageRequest.correlationKey());
    applyIfNotNull(
        publishMessageRequest.timeToLive(),
        () -> builder.timeToLive(handleDurationExpression(publishMessageRequest.timeToLive())));
    applyIfNotNull(
        publishMessageRequest.messageId(),
        () -> builder.messageId(publishMessageRequest.messageId()));
    applyIfNotNull(
        publishMessageRequest.variables(),
        () -> builder.variables(publishMessageRequest.variables()));
    applyIfNotNull(
        publishMessageRequest.tenantId(), () -> builder.tenantId(publishMessageRequest.tenantId()));
    PublishMessageResponse response = builder.send().join();
    return new PublishMessageResult(response.getMessageKey(), response.getTenantId());
  }

  private Duration handleDurationExpression(String expression) {
    try {
      return Duration.ofSeconds(Integer.parseInt(expression));
    } catch (Exception e) {
      // ignore
    }
    return Duration.parse(expression);
  }

  private void applyIfNotNull(Object value, Runnable applier) {
    if (value != null) {
      applier.run();
    }
  }
}
