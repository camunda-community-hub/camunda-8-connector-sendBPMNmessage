package io.camunda.connector.zeebe.spring;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.zeebe.ZeebeGatewayConnector;
import io.camunda.connector.zeebe.ZeebeGatewayConnectorInput;
import io.camunda.connector.zeebe.ZeebeGatewayConnectorOutput.PublishMessageResult;
import io.camunda.connector.zeebe.command.PublishMessageCommand;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.filters.StreamFilter;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.value.MessageRecordValue;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "camunda.operate.client.enabled=false",
      "camunda.connector.webhook.enabled=false",
      "camunda.connector.polling.enabled=false"
    })
@ZeebeSpringTest
public class ConnectorTest {

  @Autowired ZeebeGatewayConnector zeebeGatewayConnector;
  @Autowired ZeebeClient zeebeClient;

  @Test
  void shouldWorkWhenInvokedDirectly() throws Exception {
    OutboundConnectorContext outboundConnectorContext = mock(OutboundConnectorContext.class);
    when(outboundConnectorContext.bindVariables(any()))
        .thenReturn(
            new ZeebeGatewayConnectorInput(
                new PublishMessageCommand(
                    "messageName", "correlationKey", "PT1H", null, Map.of("key", "value"), null)));
    Object result = zeebeGatewayConnector.execute(outboundConnectorContext);
    assertThat(result).isInstanceOf(PublishMessageResult.class);
    PublishMessageResult publishMessageResult = (PublishMessageResult) result;
    Record<MessageRecordValue> messageRecord =
        StreamFilter.message(BpmnAssert.getRecordStream())
            .withKey(publishMessageResult.key())
            .stream()
            .findFirst()
            .get();
    assertThat(messageRecord.getValue().getCorrelationKey()).isEqualTo("correlationKey");
    assertThat(messageRecord.getValue().getName()).isEqualTo("messageName");
  }

  @Test
  void shouldWorkWhenInvokedFromProcess() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("test.bpmn").send().join();
    ProcessInstanceResult messageTestProcess =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("MessageTestProcess")
            .latestVersion()
            .withResult()
            .send()
            .join();
    TestProcessResult result = messageTestProcess.getVariablesAsType(TestProcessResult.class);
    Record<MessageRecordValue> messageRecord =
        StreamFilter.message(BpmnAssert.getRecordStream()).withKey(result.message().key()).stream()
            .findFirst()
            .get();
    assertThat(messageRecord.getValue().getCorrelationKey()).isEqualTo("myKey");
    assertThat(messageRecord.getValue().getName()).isEqualTo("myMessage");
  }

  public record TestProcessResult(PublishMessageResult message) {}

  @SpringBootApplication
  public static class TestApplication {
    public static void main(String[] args) {
      SpringApplication.run(TestApplication.class, args);
    }
  }
}
