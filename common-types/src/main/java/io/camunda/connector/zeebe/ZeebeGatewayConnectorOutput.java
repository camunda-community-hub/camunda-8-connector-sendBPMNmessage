package io.camunda.connector.zeebe;

public sealed interface ZeebeGatewayConnectorOutput {
  record PublishMessageResult(Long key, String tenantId) implements ZeebeGatewayConnectorOutput {}
}
