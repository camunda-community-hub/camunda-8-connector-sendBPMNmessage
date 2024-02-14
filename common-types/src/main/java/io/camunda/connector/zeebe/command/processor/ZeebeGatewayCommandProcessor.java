package io.camunda.connector.zeebe.command.processor;

import io.camunda.connector.zeebe.ZeebeGatewayClient;
import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput;
import io.camunda.connector.zeebe.command.Command;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.stream.Collectors;

public interface ZeebeGatewayCommandProcessor {
  static Set<ZeebeGatewayCommandProcessor> load() {
    return ServiceLoader.load(ZeebeGatewayCommandProcessor.class).stream()
        .map(Provider::get)
        .collect(Collectors.toSet());
  }

  boolean canProcess(Command command);

  ZeebeGatewayConnectorOutput process(Command command, ZeebeGatewayClient zeebeGatewayClient);

  abstract class TypedZeebeGatewayCommandProcessor<T extends Command>
      implements ZeebeGatewayCommandProcessor {

    @Override
    public boolean canProcess(Command command) {
      return command.getClass().isAssignableFrom(type());
    }

    @Override
    public ZeebeGatewayConnectorOutput process(
        Command command, ZeebeGatewayClient zeebeGatewayClient) {
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
        T command, ZeebeGatewayClient zeebeGatewayClient);

    protected abstract Class<T> type();
  }
}
