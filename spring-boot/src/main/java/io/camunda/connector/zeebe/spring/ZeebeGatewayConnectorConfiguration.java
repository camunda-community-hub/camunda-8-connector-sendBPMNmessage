package io.camunda.connector.zeebe.spring;

import io.camunda.connector.zeebe.ZeebeGatewayConnector;
import io.camunda.connector.zeebe.command.processor.ZeebeClientCommandProcessor;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeGatewayConnectorConfiguration {
  @Bean
  public ZeebeGatewayConnector zeebeGatewayConnector(
      ZeebeClient zeebeClient, Set<ZeebeClientCommandProcessor> zeebeGatewayCommandProcessors) {
    Set<ZeebeClientCommandProcessor> commandProcessors =
        new HashSet<>(zeebeGatewayCommandProcessors);
    commandProcessors.addAll(ZeebeClientCommandProcessor.load());
    return new ZeebeGatewayConnector(zeebeClient, commandProcessors);
  }
}
