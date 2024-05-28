package io.camunda.connector.message;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cherrytemplate.CherryOutput;

import java.util.List;
import java.util.Map;

public class SendBPMNMessageOutput implements CherryOutput {

  public final Long messageKey;
  public final String tenantId;

  SendBPMNMessageOutput(Long messageKey, String tenantId) {
    this.messageKey = messageKey;
    this.tenantId = tenantId;
  }

  @Override
  public List<Map<String, Object>> getOutputParameters() {
    return List.of(Map.of(CherryInput.PARAMETER_MAP_NAME, "messageKey",

            CherryInput.PARAMETER_MAP_LABEL, "Message Key",

            CherryInput.PARAMETER_MAP_CLASS, Long.class,

            CherryInput.PARAMETER_MAP_LEVEL, CherryInput.PARAMETER_MAP_LEVEL_REQUIRED,

            CherryInput.PARAMETER_MAP_EXPLANATION, "Message key returned by the Zeebe Engine"),

        Map.of(CherryInput.PARAMETER_MAP_NAME, "tenantId",

            CherryInput.PARAMETER_MAP_LABEL, "TenantId",

            CherryInput.PARAMETER_MAP_CLASS, String.class,

            CherryInput.PARAMETER_MAP_LEVEL, CherryInput.PARAMETER_MAP_LEVEL_REQUIRED,

            CherryInput.PARAMETER_MAP_EXPLANATION, "Tenant Id returned by the Zeebe Engine"));
  }
}
