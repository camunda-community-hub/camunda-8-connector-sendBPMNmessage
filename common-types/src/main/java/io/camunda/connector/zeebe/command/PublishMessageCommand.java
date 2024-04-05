package io.camunda.connector.zeebe.command;

import static io.camunda.connector.zeebe.ZeebeGatewayConnectorConstants.*;

import io.camunda.connector.generator.dsl.Property.FeelMode;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateSubType;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

@TemplateSubType(id = PUBLISH_MESSAGE_TYPENAME, label = "Publish message")
public record PublishMessageCommand(
    @NotEmpty
        @TemplateProperty(
            group = GROUP_PARAMETERS_ID,
            label = "Message name",
            description = "The name of the message")
        String name,
    @NotEmpty
        @TemplateProperty(
            group = GROUP_PARAMETERS_ID,
            label = "Correlation key",
            description = "The correlation key to use")
        String correlationKey,
    @TemplateProperty(
            group = GROUP_PARAMETERS_ID,
            label = "Time to live",
            description =
                "The time to live for the message in seconds or as duration expression. If 0, the message will not be buffered",
            optional = true)
        String timeToLive,
    @TemplateProperty(
            group = GROUP_PARAMETERS_ID,
            label = "Message id",
            description =
                "Unique identifier for the message. Will be applied once the message needs to be buffered",
            optional = true)
        String messageId,
    @TemplateProperty(
            group = GROUP_PARAMETERS_ID,
            feel = FeelMode.required,
            label = "Variables",
            description = "Variables to publish as context",
            optional = true)
        Map<String, Object> variables,
    @TemplateProperty(
            group = GROUP_PARAMETERS_ID,
            label = "Tenant id",
            description = "The identifier for the tenant the message should be published to",
            optional = true)
        String tenantId)
    implements Command {}
