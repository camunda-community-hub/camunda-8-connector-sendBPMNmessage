package io.camunda.connector.zeebe;

import io.camunda.zeebe.client.ZeebeClient;

import java.util.function.Supplier;

public interface ZeebeClientSupplier extends Supplier<ZeebeClient> {}
