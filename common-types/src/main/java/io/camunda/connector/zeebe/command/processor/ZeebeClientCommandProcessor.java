package io.camunda.connector.zeebe.command.processor;

import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput;
import io.camunda.connector.zeebe.command.Command;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.stream.Collectors;

public interface ZeebeClientCommandProcessor {
  static Set<ZeebeClientCommandProcessor> load() {
    return ServiceLoader.load(ZeebeClientCommandProcessor.class).stream()
        .map(Provider::get)
        .collect(Collectors.toSet());
  }

  boolean canProcess(Command command);

  ZeebeGatewayConnectorOutput process(Command command, ZeebeClient zeebeClient);

  abstract class TypedZeebeGatewayCommandProcessor<T extends Command>
      implements ZeebeClientCommandProcessor {

    @Override
    public boolean canProcess(Command command) {
      return command.getClass().isAssignableFrom(type());
    }

    @Override
    public ZeebeGatewayConnectorOutput process(Command command, ZeebeClient zeebeGatewayClient) {
      if (canProcess(command)) {
        return processTyped(type().cast(command), zeebeGatewayClient);
      }
      throw new IllegalStateException(
          "Command "
              + command
              + " cannot be processed by "
              + this
              + " as the handled type is "
              + type());
    }

    protected abstract ZeebeGatewayConnectorOutput processTyped(
        T command, ZeebeClient zeebeGatewayClient);

    protected abstract Class<T> type();
  }
}
