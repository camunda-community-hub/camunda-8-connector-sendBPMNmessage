package io.camunda.connector.zeebe;

import static io.camunda.connector.zeebe.ZeebeGatewayConnectorConstants.*;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.generator.java.annotation.ElementTemplate.PropertyGroup;
import io.camunda.connector.zeebe.command.processor.ZeebeClientCommandProcessor;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.Set;

@OutboundConnector(
    name = "Zeebe Gateway Connector",
    type = "io.camunda:zeebe:1",
    inputVariables = {"command"})
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
  private final ZeebeClient zeebeClient;
  private final Set<ZeebeClientCommandProcessor> zeebeClientCommandProcessors;

  public ZeebeGatewayConnector(
      ZeebeClient zeebeClient, Set<ZeebeClientCommandProcessor> zeebeGatewayCommandProcessors) {
    this.zeebeClient = zeebeClient;
    this.zeebeClientCommandProcessors = zeebeGatewayCommandProcessors;
  }

  public ZeebeGatewayConnector(ZeebeClient zeebeGatewayClient) {
    this(zeebeGatewayClient, ZeebeClientCommandProcessor.load());
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
    for (ZeebeClientCommandProcessor processor : zeebeClientCommandProcessors) {
      if (processor.canProcess(input.command())) {
        return processor.process(input.command(), zeebeClient);
      }
    }
    throw new IllegalStateException("No processor found for command " + input.command());
  }
}
