package io.camunda.connector.zeebe;

import io.camunda.connector.zeebe.command.Command;
import jakarta.validation.Valid;

public record ZeebeGatewayConnectorInput(@Valid Command command) {}
