package io.camunda.connector.message.util;

import io.camunda.connector.message.SendBPMNMessageFunction;
import io.camunda.connector.message.SendBPMNMessageOutput;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeebeClientHelper {

  private static final Logger logger = LoggerFactory.getLogger(SendBPMNMessageFunction.class);
  private static final String GATEWAY_ADDRESS_ENV_VAR = "ZEEBE_CLIENT_BROKER_GATEWAY_ADDRESS";

  public static ZeebeClient buildClient() {
    String gatewayAddress = System.getenv(GATEWAY_ADDRESS_ENV_VAR);
    if (gatewayAddress == null || gatewayAddress.isEmpty()) {
      logger.info("Environment variable [{}] is not set or is empty", GATEWAY_ADDRESS_ENV_VAR);
      gatewayAddress = "localhost:26500";
    }

    ZeebeClientBuilder zeebeClientBuilder = ZeebeClient.newClientBuilder()
            .gatewayAddress(gatewayAddress);

    zeebeClientBuilder.usePlaintext();

    return zeebeClientBuilder.build();
  }
}
