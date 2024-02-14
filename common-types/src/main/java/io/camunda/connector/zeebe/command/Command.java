package io.camunda.connector.zeebe.command;

import static io.camunda.connector.zeebe.ZeebeGatewayConnectorConstants.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.camunda.connector.generator.java.annotation.TemplateDiscriminatorProperty;

@TemplateDiscriminatorProperty(
    name = "command",
    label = "Command",
    group = GROUP_COMMAND_ID,
    description = "The command that should be executed")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "command")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PublishMessageCommand.class, name = PUBLISH_MESSAGE_TYPENAME)
})
public sealed interface Command permits PublishMessageCommand {}
