package io.camunda.connector.zeebe;

import static io.camunda.connector.zeebe.ZeebeGatewayConnectorConstants.*;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.generator.java.annotation.ElementTemplate.PropertyGroup;
import io.camunda.connector.zeebe.command.processor.ZeebeGatewayCommandProcessor;
import java.util.Set;

@OutboundConnector(
    name = "Zeebe Gateway Connector",
    type = "io.camunda:zeebe:1",
    inputVariables = {})
@ElementTemplate(
    id = "io.camunda.zeebe",
    name = "Zeebe Gateway Connector",
    version = 1,
    inputDataClass = ZeebeGatewayConnectorInput.class,
    propertyGroups = {
      @PropertyGroup(id = GROUP_COMMAND_ID, label = "Command"),
      @PropertyGroup(id = GROUP_PARAMETERS_ID, label = "Parameters")
    })
public class ZeebeGatewayConnector implements OutboundConnectorFunction {
  private final ZeebeGatewayClient zeebeGatewayClient;
  private final Set<ZeebeGatewayCommandProcessor> zeebeGatewayCommandProcessors;

  public ZeebeGatewayConnector(
      ZeebeGatewayClient zeebeGatewayClient,
      Set<ZeebeGatewayCommandProcessor> zeebeGatewayCommandProcessors) {
    this.zeebeGatewayClient = zeebeGatewayClient;
    this.zeebeGatewayCommandProcessors = zeebeGatewayCommandProcessors;
  }

  public ZeebeGatewayConnector(ZeebeGatewayClient zeebeGatewayClient) {
    this(zeebeGatewayClient, ZeebeGatewayCommandProcessor.load());
  }

  public ZeebeGatewayConnector() {
    this(ZeebeGatewayClient.load());
  }

  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    ZeebeGatewayConnectorInput input = getInput(outboundConnectorContext);
    try {
      ZeebeGatewayConnectorOutput output = handleInput(input);
      return handleOutput(output);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  protected ZeebeGatewayConnectorInput getInput(OutboundConnectorContext outboundConnectorContext) {
    return outboundConnectorContext.bindVariables(ZeebeGatewayConnectorInput.class);
  }

  protected ZeebeGatewayConnectorOutput handleOutput(ZeebeGatewayConnectorOutput output) {
    return output;
  }

  protected Exception handleException(Exception e) {
    return e;
  }

  private ZeebeGatewayConnectorOutput handleInput(ZeebeGatewayConnectorInput input) {
    for (ZeebeGatewayCommandProcessor processor : zeebeGatewayCommandProcessors) {
      if (processor.canProcess(input.command())) {
        return processor.process(input.command(), zeebeGatewayClient);
      }
    }
    throw new IllegalStateException("No processor found for command " + input.command());
  }
}
