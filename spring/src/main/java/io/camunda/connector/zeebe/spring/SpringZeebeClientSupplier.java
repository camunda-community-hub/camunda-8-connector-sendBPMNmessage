package io.camunda.connector.zeebe.spring;

import io.camunda.connector.zeebe.ZeebeClientSupplier;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.event.ZeebeClientCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

public class SpringZeebeClientSupplier
    implements ZeebeClientSupplier, ApplicationListener<ZeebeClientCreatedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(SpringZeebeClientSupplier.class);
  private ZeebeClient zeebeClient;

  @Override
  public void onApplicationEvent(ZeebeClientCreatedEvent event) {
    LOG.debug("Zeebe client set to supplier for zeebe connector");
    zeebeClient = event.getClient();
  }

  @Override
  public ZeebeClient get() {
    if (zeebeClient == null) {
      throw new IllegalStateException("Zeebe client is not yet initialized");
    }
    return zeebeClient;
  }
}
