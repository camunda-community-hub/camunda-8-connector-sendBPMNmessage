package io.camunda.connector.zeebe.spring;

import io.camunda.connector.zeebe.ZeebeClientSupplier;
import io.camunda.connector.zeebe.ZeebeGatewayClient;
import io.camunda.connector.zeebe.ZeebeGatewayClientImpl;
import io.camunda.connector.zeebe.ZeebeGatewayConnector;
import io.camunda.connector.zeebe.command.processor.ZeebeGatewayCommandProcessor;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeGatewayConnectorConfiguration {
  @Bean
  public ZeebeGatewayConnector zeebeGatewayConnector(
      ZeebeGatewayClient zeebeGatewayClient,
      Set<ZeebeGatewayCommandProcessor> zeebeGatewayCommandProcessors) {
    Set<ZeebeGatewayCommandProcessor> commandProcessors =
        new HashSet<>(zeebeGatewayCommandProcessors);
    commandProcessors.addAll(ZeebeGatewayCommandProcessor.load());
    return new ZeebeGatewayConnector(zeebeGatewayClient, commandProcessors);
  }

  @Bean
  public ZeebeGatewayClient zeebeGatewayClient(ZeebeClientSupplier zeebeClientSupplier) {
    return new ZeebeGatewayClientImpl(zeebeClientSupplier);
  }

  @Bean
  public ZeebeClientSupplier zeebeClientSupplier() {
    return new SpringZeebeClientSupplier();
  }
}
