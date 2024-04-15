package io.camunda.connector.bpmnmessage;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringTokenizer;

@OutboundConnector(name = "SendBPMNMessage", inputVariables = {}, type = SendBPMNMessageFunction.WORKERTYPE_SEND_MESSAGE)
@Component

public class SendBPMNMessageFunction implements OutboundConnectorFunction, CherryConnector {
  private static final Logger logger = LoggerFactory.getLogger(SendBPMNMessageFunction.class);
  public static final String WORKERTYPE_SEND_MESSAGE = "c-send-message";

  public static final String BPMNERROR_INCORRECT_VARIABLE = "INCORRECT_VARIABLE";
  public static final String BPMNERROR_SENDMESSAGE_VARIABLE = "SEND_MESSAGE";

  private static final String WORKERLOGO = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABsAAAARCAIAAACjLUBkAAAB9UlEQVQ4T2Ps7OplYWHJSE/h4uLatn1na1vnixcvGUgBxsaGrc0Nqqoqjx8/6ejsYbazc5w9Z96q1Wv5+fn9fL1joiOAFly4eOnv379QHbiBhIR4W2tTdWUZGxtr/4TJRcXlt27fYWJkZATKvX37rrKqzssn8OKly/l52Xt2bfXydIdowwrY2dnzcrP27t7m4e66ZOlyR2fP2XPm//nzByjF7OTkcuHCRYi6d+/erV234dq165YW5lFR4TbWVkDHAgUhsnAAtGzWjCmurs5HjhxTVla6eOn6jx8/oHJoJkLAvfsPli5b+eHDRw9Pt8SEWFFRkfPnL/74+RMoBQysGdMmJScnvHz5UkRE+PDRU4JCohBdcMDY3NIxf8EiKA8V8PPz5eZkxsVGf/nydd78hcBQiwgPff/+fV//5JWr1vz79w+qDhXgMxECFBTkK8tLXFycfv/+PWfugukzZn/9+hUqhw0QNhECNDTUgOFATMJigtKEwI0bt4hMp8SaSDxggdIwcPf2VSAJSaT///8HizEoq2oDxYGCSipa9+5cA4pDRIAkUBbOgAAsbgRKI5sFYQABUBBiHJQPsx4NYDER4hw4G8KAAGTjIADTUMJuhDsTwkB2NTIbDrC7EcrCAJhGYIoQmx6JB4yLFi/bvmMnlEc5YGAAAMbF6NQzlAdoAAAAAElFTkSuQmCC";

  private ZeebeClient myZeebeClient;

  public SendBPMNMessageFunction(ZeebeClient zeebeClient) {
    this.myZeebeClient = zeebeClient;
  }

  public ZeebeClient getZeebeClient() {
    ServiceLoader<ZeebeClient> loader = ServiceLoader.load(ZeebeClient.class);
    return loader.findFirst().isPresent() ? loader.findFirst().get() : null;

  }

  /*
  @Autowired OutboundConnectorFactory outboundConnectorFactory;

  void springBasedConnectorPresent() throws Exception {

    List<OutboundConnectorConfiguration> listConf = outboundConnectorFactory.getConfigurations()
        .stream()
        .toList();
  }
*/

  // @ E ventListener

  /**
   * The connectorruntime handle this method
   * public void handleStart(ZeebeClientCreatedEvent evt) {
   * this.zeebeClientRuntime = evt.getClient();
   */

  @Override
  // public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
  public SendBPMNMessageOutput execute(OutboundConnectorContext context) throws ConnectorException {

    logger.info("SendBPMNMessage.start processId[{}]", context.getJobContext().getBpmnProcessId());

    Map<String, Object> allVariables = context.bindVariables(Map.class);
    SendBPMNMessageInput messageInput = SendBPMNMessageInput.bindVariables(allVariables);

    Map<String, Object> variablesToUpdate = new HashMap<>();
    SendBPMNMessageOutput bpmnMessageOutput;
    try {

      ZeebeClient zeebe = getZeebeClient();
      // springBasedConnectorPresent();

      bpmnMessageOutput = sendMessageViaGrpc(messageInput.getMessageName(), messageInput.getCorrelationKey(),
          messageInput.getMessageVariables(), messageInput.getMessageId(), messageInput.getDuration(),
          messageInput.getAllVariables(), context);
      bpmnMessageOutput.bindVariables(variablesToUpdate);

    } catch (Exception e) {
      logger.error("Error during sendMessage [" + messageInput.getMessageName() + "] :" + e);
      throw new ConnectorException(
          BPMNERROR_SENDMESSAGE_VARIABLE + ":Error during sendMessage [" + messageInput.getMessageName() + "] :" + e);
    }
    return new SendBPMNMessageOutput();
  }

  /**
   * Send a message
   * https://docs.camunda.io/docs/apis-clients/grpc/#publishmessage-rpc
   *
   * @param messageName         the message name
   * @param correlationKey      List of variable where the value has to be fetched to get the correlation key of the message. Attention: only one variable is expected
   * @param messageVariableList the message variables send to the message (list of value separate by ,)
   * @param messageId           the unique ID of the message; can be omitted. only useful to ensure only one message with the given ID will ever be published (during its lifetime)
   * @param timeToLiveDuration  how long the message should be buffered on the broker
   * @param context             context
   * @param allVariables        allVariables in processInstance
   */
  private SendBPMNMessageOutput sendMessageViaGrpc(final String messageName,
                                                   final String correlationKey,
                                                   final String messageVariableList,
                                                   final String messageId,
                                                   final Duration timeToLiveDuration,
                                                   Map<String, Object> allVariables,
                                                   OutboundConnectorContext context) throws Exception {
    SendBPMNMessageOutput messageOutput = new SendBPMNMessageOutput();

    Object correlationValue = extractValue("correlation", correlationKey, allVariables);
    Map<String, Object> messageVariables = extractVariable(messageVariableList, allVariables);

    logger.info("MessageName[" + messageName + "] messageId[" + messageId + "] Correlation Value[" + correlationValue
        + "] Variable[" + messageVariables + "]" + "] Duration[" + timeToLiveDuration + "]");

    // At this moment, we expect only one variable for the correlation key
    ZeebeClient zeebeClient = getZeebeClient();

    /*
    PublishMessageCommandStep1.PublishMessageCommandStep3 messageCommand = zeebeClient.newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationValue == null ? "" : correlationValue.toString());
    if (timeToLiveDuration != null)
      messageCommand = messageCommand.timeToLive(timeToLiveDuration);
    if (!messageVariables.isEmpty()) {
      messageCommand = messageCommand.variables(messageVariables);
    }
    if (messageId != null)
      messageCommand = messageCommand.messageId(messageId);

    PublishMessageResponse messageResponse = messageCommand.send().join();
    messageOutput.setMessageKey(messageResponse.getMessageKey());
     */
    return messageOutput;
  }

  /**
   * Return a Map of variable value, from a list of variable. Variables are separate by a comma.
   * Example firstName,lastName
   *
   * @param variableList list of variables, separate by a comma
   * @param allVariables All variables accessible
   * @return map of variable Name / variable value
   */
  private Map<String, Object> extractVariable(String variableList, final Map<String, Object> allVariables)
      throws Exception {
    Map<String, Object> variables = new HashMap<>();

    if (variableList == null)
      return Collections.emptyMap();
    StringTokenizer stVariable = new StringTokenizer(variableList, ",");
    while (stVariable.hasMoreTokens()) {
      StringTokenizer stOneVariable = new StringTokenizer(stVariable.nextToken(), "=");
      String name = (stOneVariable.nextToken());
      if (!stOneVariable.hasMoreTokens())
        throw new Exception(
            BPMNERROR_INCORRECT_VARIABLE + ":A variable must be <name>=<value>, no = for variable [" + name + "]");
      Object value = (stOneVariable.hasMoreTokens() ? stOneVariable.nextToken() : null);

      variables.put(name, extractValue(name, value, allVariables));
    }
    return variables;
  }

  /**
   * Extract the value
   *
   * @param name         name of the value (for log)
   * @param value        value
   * @param allVariables all process variables: the value may be a string referring a variables
   * @return the value resolved
   * @throws Exception
   */
  private Object extractValue(final String name, final Object value, final Map<String, Object> allVariables)
      throws Exception {
    if (value != null && value.toString().startsWith("${")) {
      // extract the variable from variables
      if (!value.toString().endsWith("}")) {
        logger.error("Value [" + value + "] for key [" + name + "] must start by ${ and end by } like ${amount}");
        throw new Exception(BPMNERROR_INCORRECT_VARIABLE + ":Value [" + value + "] for key [" + name
            + "] must start by ${ and end by } like ${amount}");
      }
      String variableName = value.toString().substring(0, value.toString().length() - 1).substring(2);
      return allVariables.get(variableName);
    }
    return value;
  }

  @Override
  public String getDescription() {
    return "Throw a BPMN Message with correlation and variables";
  }

  @Override
  public String getLogo() {
    return "data:image/svg+xml,%3C?xml version\u003d\u00271.0\u0027 encoding\u003d\u0027UTF-8\u0027 standalone\u003d\u0027no\u0027?%3E%3Csvg   xmlns:dc\u003d\u0027http://purl.org/dc/elements/1.1/\u0027   xmlns:cc\u003d\u0027http://creativecommons.org/ns%23\u0027   xmlns:rdf\u003d\u0027http://www.w3.org/1999/02/22-rdf-syntax-ns%23\u0027   xmlns:svg\u003d\u0027http://www.w3.org/2000/svg\u0027   xmlns\u003d\u0027http://www.w3.org/2000/svg\u0027   version\u003d\u00271.1\u0027   id\u003d\u0027Capa_1\u0027   x\u003d\u00270px\u0027   y\u003d\u00270px\u0027   viewBox\u003d\u00270 0 18 18\u0027   xml:space\u003d\u0027preserve\u0027   width\u003d\u002718\u0027   height\u003d\u002718\u0027%3E%3Cmetadata   id\u003d\u0027metadata55\u0027%3E%3Crdf:RDF%3E%3Ccc:Work       rdf:about\u003d\u0027\u0027%3E%3Cdc:format%3Eimage/svg+xml%3C/dc:format%3E%3Cdc:type         rdf:resource\u003d\u0027http://purl.org/dc/dcmitype/StillImage\u0027 /%3E%3Cdc:title%3E%3C/dc:title%3E%3C/cc:Work%3E%3C/rdf:RDF%3E%3C/metadata%3E%3Cdefs   id\u003d\u0027defs53\u0027 /%3E%3Cg   id\u003d\u0027g18\u0027   transform\u003d\u0027scale(0.3)\u0027%3E %3Cpath   d\u003d\u0027M 30,0 C 13.458,0 0,13.458 0,30 0,46.542 13.458,60 30,60 46.542,60 60,46.542 60,30 60,13.458 46.542,0 30,0 Z m 0,58 C 14.561,58 2,45.439 2,30 2,14.561 14.561,2 30,2 45.439,2 58,14.561 58,30 58,45.439 45.439,58 30,58 Z\u0027   id\u003d\u0027path2\u0027 /%3E %3Cpath   d\u003d\u0027M 23.165,8.459 C 23.702,8.329 24.033,7.789 23.904,7.253 23.775,6.716 23.234,6.387 22.698,6.514 18.763,7.46 15.176,9.469 12.322,12.323 9.468,15.177 7.46,18.764 6.514,22.698 c -0.129,0.536 0.202,1.076 0.739,1.206 0.078,0.019 0.157,0.027 0.234,0.027 0.451,0 0.861,-0.308 0.972,-0.767 0.859,-3.575 2.685,-6.836 5.277,-9.429 2.592,-2.593 5.854,-4.417 9.429,-5.276 z\u0027   id\u003d\u0027path4\u0027 /%3E %3Cpath   d\u003d\u0027m 52.747,36.096 c -0.538,-0.129 -1.077,0.201 -1.206,0.739 -0.859,3.575 -2.685,6.836 -5.277,9.429 -2.592,2.593 -5.854,4.418 -9.43,5.277 -0.537,0.13 -0.868,0.67 -0.739,1.206 0.11,0.459 0.521,0.767 0.972,0.767 0.077,0 0.156,-0.009 0.234,-0.027 3.936,-0.946 7.523,-2.955 10.377,-5.809 2.854,-2.854 4.862,-6.441 5.809,-10.376 0.128,-0.536 -0.203,-1.076 -0.74,-1.206 z\u0027   id\u003d\u0027path6\u0027 /%3E %3Cpath   d\u003d\u0027m 24.452,13.286 c 0.538,-0.125 0.873,-0.663 0.747,-1.2 -0.125,-0.538 -0.665,-0.878 -1.2,-0.747 -3.09,0.72 -5.904,2.282 -8.141,4.52 -2.237,2.236 -3.8,5.051 -4.52,8.141 -0.126,0.537 0.209,1.075 0.747,1.2 0.076,0.019 0.152,0.026 0.228,0.026 0.454,0 0.865,-0.312 0.973,-0.773 0.635,-2.725 2.014,-5.207 3.986,-7.18 1.972,-1.973 4.456,-3.352 7.18,-3.987 z\u0027   id\u003d\u0027path8\u0027 /%3E %3Cpath   d\u003d\u0027m 48.661,36.001 c 0.126,-0.537 -0.209,-1.075 -0.747,-1.2 -0.538,-0.133 -1.075,0.209 -1.2,0.747 -0.635,2.725 -2.014,5.207 -3.986,7.18 -1.972,1.973 -4.455,3.352 -7.18,3.986 -0.538,0.125 -0.873,0.663 -0.747,1.2 0.107,0.462 0.519,0.773 0.973,0.773 0.075,0 0.151,-0.008 0.228,-0.026 3.09,-0.72 5.904,-2.282 8.141,-4.52 2.236,-2.236 3.798,-5.05 4.518,-8.14 z\u0027   id\u003d\u0027path10\u0027 /%3E %3Cpath   d\u003d\u0027m 26.495,16.925 c -0.119,-0.541 -0.653,-0.879 -1.19,-0.763 -4.557,0.997 -8.146,4.586 -9.143,9.143 -0.118,0.539 0.224,1.072 0.763,1.19 0.072,0.016 0.144,0.023 0.215,0.023 0.46,0 0.873,-0.318 0.976,-0.786 0.831,-3.796 3.821,-6.786 7.617,-7.617 0.538,-0.118 0.88,-0.651 0.762,-1.19 z\u0027   id\u003d\u0027path12\u0027 /%3E %3Cpath   d\u003d\u0027m 43.838,34.695 c 0.118,-0.539 -0.224,-1.072 -0.763,-1.19 -0.54,-0.118 -1.072,0.222 -1.19,0.763 -0.831,3.796 -3.821,6.786 -7.617,7.617 -0.539,0.118 -0.881,0.651 -0.763,1.19 0.103,0.468 0.516,0.786 0.976,0.786 0.071,0 0.143,-0.008 0.215,-0.023 4.556,-0.997 8.145,-4.586 9.142,-9.143 z\u0027   id\u003d\u0027path14\u0027 /%3E %3Cpath   d\u003d\u0027m 38.08,30 c 0,-4.455 -3.625,-8.08 -8.08,-8.08 -4.455,0 -8.08,3.625 -8.08,8.08 0,4.455 3.625,8.08 8.08,8.08 4.455,0 8.08,-3.625 8.08,-8.08 z M 30,36.08 c -3.353,0 -6.08,-2.728 -6.08,-6.08 0,-3.352 2.728,-6.08 6.08,-6.08 3.352,0 6.08,2.728 6.08,6.08 0,3.352 -2.727,6.08 -6.08,6.08 z\u0027   id\u003d\u0027path16\u0027 /%3E%3C/g%3E%3Cg   id\u003d\u0027g20\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g22\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g24\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g26\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g28\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g30\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g32\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g34\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g36\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g38\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g40\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g42\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g44\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g46\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g48\u0027%3E%3C/g%3E%3Cg   id\u003d\u0027g85\u0027   transform\u003d\u0027matrix(1.1259408,0,0,1.1259408,33.760593,8.6647721)\u0027%3E%3Cpath     d\u003d\u0027m -15.969199,-1.3973349 c -0.04428,0.56592004 -0.49572,0.83754004 -0.58752,0.88722004 -0.4887,0.26676 -1.31112,0.17388 -1.70856,-0.38556 -0.21924,-0.30780004 -0.3294,-0.79758004 -0.1161,-1.13670004 l 0.0027,-0.0054 c 0.07668,-0.12474 0.25434,-0.33534 0.58212,-0.34236 h 0.01188 c 0.11718,0 0.25218,0.03564 0.37152,0.06696 0.0837,0.02214 0.15606,0.04104 0.21006,0.04482 0.03402,0.00216 0.08262,-0.00648 0.14418,-0.01728 0.18414,-0.0324 0.4914,-0.0864 0.76032,0.10368 0.36612,0.2597403 0.33156,0.76356 0.3294,0.78462 z\u0027     id\u003d\u0027path57\u0027     style\u003d\u0027fill:%23aa0000;fill-opacity:1;stroke-width:0.3\u0027 /%3E%3Cpath     d\u003d\u0027m -18.207499,-2.3007546 c -0.1053,0.07452 -0.17496,0.16686 -0.21708,0.2349 l -0.0027,0.00432 c -0.10908,0.17388 -0.13932,0.37584 -0.1188,0.57348 -0.0022,0.00108 -0.0043,0.00216 -0.0059,0.00378 -0.40014,0.3267 -0.85212,0.24246 -1.02762,0.19116 l -0.01026,-0.0027 c -0.5589,-0.13932 -1.11078,-0.74304 -1.00926,-1.38618 0.05616,-0.35424 0.324,-0.7614 0.72792,-0.85428 0.06966,-0.0162 0.43362,-0.0837 0.7263,0.16146 0.08964,0.0756 0.15606,0.1857601 0.21438,0.28296 0.04158,0.0702 0.07776,0.13014 0.11718,0.16956 0.02538,0.02538 0.06966,0.05022 0.12582,0.08208 0.1647,0.09234 0.4131,0.2322001 0.4779,0.53352 5.4e-4,0.00216 0.0011,0.00432 0.0022,0.00594 z\u0027     id\u003d\u0027path59\u0027     style\u003d\u0027fill:%23aa0000;fill-opacity:1;stroke-width:0.3\u0027 /%3E%3Cpath     d\u003d\u0027m -17.110219,-2.3309949 c -0.03186,0.0054 -0.05994,0.00918 -0.07992,0.00918 -0.0032,0 -0.0065,-5.4e-4 -0.0092,-5.4e-4 -0.01512,-0.00108 -0.03294,-0.00324 -0.05184,-0.00702 0.16578,-0.42336 0.23436,-1.10052 -0.34182,-1.95804 -0.0011,-0.00216 -0.0027,-0.00378 -0.0049,-0.0054 0.03348,-0.13284 0.04482,-0.22572 0.04752,-0.2484 0.0016,-0.01512 -0.0092,-0.02808 -0.02376,-0.0297 -0.01512,-0.00216 -0.02808,0.00918 -0.0297,0.02376 -0.0065,0.05778 -0.07128,0.5788801 -0.4347,1.0416602 -0.22734,0.28944 -0.58536,0.5049 -0.70902,0.56646 -0.01134,-0.00756 -0.02106,-0.01512 -0.02754,-0.0216 -0.01782,-0.01782 -0.0351,-0.04104 -0.05292,-0.06858 0.01026,-0.0054 0.02106,-0.01134 0.0324,-0.01728 0.12906,-0.06912 0.34506,-0.1841401 0.60858,-0.4600801 0.31158,-0.32616 0.37098,-0.82566 0.2835,-0.96174 -0.04212,-0.06588 -0.10962,-0.07236 -0.16362,-0.07722 -0.07074,-0.00594 -0.1134,-0.01026 -0.12096,-0.10638 -0.007,-0.0837 0.0324,-0.15768 0.1107,-0.20736 0.1053,-0.0675 0.29376,-0.0891 0.47628,0.01458 0.11394,0.06426 0.11286,0.17388 0.11232,0.27972 -5.4e-4,0.07074 -0.0011,0.1377 0.03402,0.18576 l 0.01512,0.02052 c 0.0864,0.1171801 0.28836,0.3904201 0.40932,0.86724 0.12906,0.5102999 0.007,0.9752399 -0.07992,1.1604599 z\u0027     id\u003d\u0027path61\u0027     style\u003d\u0027fill:%23502d16;fill-opacity:1;stroke-width:0.3\u0027 /%3E%3Cpath     d\u003d\u0027m -18.698899,-4.5455348 c -0.0043,0.00216 -0.0086,0.00324 -0.01296,0.00324 -0.0097,0 -0.0189,-0.0054 -0.02376,-0.01404 -5.4e-4,-5.4e-4 -0.03834,-0.06804 -0.10638,-0.13014 -0.01134,-0.00972 -0.01188,-0.027 -0.0022,-0.0378 0.01026,-0.01134 0.027,-0.01188 0.03834,-0.00216 0.07506,0.06804 0.11556,0.14094 0.11718,0.14418 0.0076,0.01296 0.0027,0.02916 -0.01026,0.03672 z\u0027     id\u003d\u0027path63\u0027     style\u003d\u0027stroke-width:0.3\u0027 /%3E%3Cpath     d\u003d\u0027m -18.644359,-4.0309149 c -0.05616,0.06642 -0.08532,0.0783 -0.1296,0.09612 l -0.01404,0.00594 c -0.0032,0.00162 -0.007,0.00216 -0.01026,0.00216 -0.0108,0 -0.02052,-0.00648 -0.02484,-0.01674 -0.0059,-0.0135 5.4e-4,-0.0297 0.01458,-0.0351 l 0.01404,-0.00594 c 0.0405,-0.01674 0.06102,-0.02538 0.10908,-0.081 0.0097,-0.01188 0.027,-0.01296 0.03834,-0.00324 0.01134,0.00972 0.01242,0.02646 0.0027,0.0378 z\u0027     id\u003d\u0027path65\u0027     style\u003d\u0027stroke-width:0.3\u0027 /%3E%3Cpath     d\u003d\u0027m -17.948839,-4.4391548 c -0.01404,-0.02214 -0.0324,-0.03402 -0.05292,-0.0405 -0.07182,0.09666 -0.32454,0.243 -0.57834,0.243 -0.0675,0 -0.13554,-0.01026 -0.1998,-0.03456 -0.1728,-0.06588 -0.29106,-0.15768 -0.40554,-0.24624 -0.09072,-0.07074 -0.17658,-0.13716 -0.28134,-0.18252 -0.01404,-0.00648 -0.01998,-0.02214 -0.01404,-0.03564 0.0059,-0.01404 0.0216,-0.01998 0.03564,-0.01404 0.1107,0.0486 0.19926,0.11664 0.29268,0.18954 0.11124,0.0864 0.2268,0.1755 0.3915,0.23814 0.16794,0.06372 0.3564,0.02214 0.4995,-0.04212 -0.06156,-0.19008 -0.45954,-0.76842 -1.11564,-0.6210001 -0.10152,0.02268 -0.19008,0.04374 -0.26784,0.0621 -0.3294,0.07722 -0.47034,0.11016 -0.61668,0.07128 0.0135,0.06804 0.04914,0.12582 0.10584,0.17226 0.1134,0.09126 0.2781,0.11394 0.3564,0.11502 -0.03996,-0.04536 -0.08262,-0.07452 -0.135,-0.09288 -0.01404,-0.00486 -0.0216,-0.01998 -0.01674,-0.03402 0.0049,-0.01458 0.02052,-0.0216 0.03456,-0.01674 0.1701,0.05994 0.2484,0.21222 0.37476,0.5227201 0.1323,0.32508 0.59508,0.62802 0.97038,0.5157 0.38232,-0.11448 0.37584,-0.51732 0.3753,-0.52164 0,-0.0108 0.0065,-0.02106 0.0162,-0.02538 0.02538,-0.01134 0.15552,-0.07074 0.24732,-0.15714 0.0016,-0.00162 0.0032,-0.0027 0.0049,-0.0027 v -5.4e-4 c -0.0043,-0.02646 -0.01188,-0.04752 -0.02106,-0.0621 m -0.89532,-0.2851201 c 0.01026,-0.01134 0.027,-0.01188 0.03834,-0.00216 0.07506,0.06804 0.11556,0.14094 0.11718,0.14418 0.0076,0.01296 0.0027,0.02916 -0.01026,0.03672 -0.0043,0.00216 -0.0086,0.00324 -0.01296,0.00324 -0.0097,0 -0.0189,-0.0054 -0.02376,-0.01404 -5.4e-4,-5.4e-4 -0.03834,-0.06804 -0.10638,-0.13014 -0.01134,-0.00972 -0.01188,-0.027 -0.0022,-0.0378 m -0.45468,0.36774 c -0.0011,0 -0.01674,0.0027 -0.04752,0.0027 -0.0243,0 -0.05724,-0.00162 -0.09936,-0.00702 -0.01512,-0.00162 -0.02538,-0.01512 -0.02322,-0.03024 0.0016,-0.01458 0.01512,-0.02484 0.03024,-0.02322 0.08748,0.01134 0.13014,0.00486 0.13068,0.00486 0.01458,-0.0027 0.02862,0.00702 0.03078,0.02214 0.0027,0.01458 -0.007,0.02808 -0.0216,0.03078 m 0.65448,0.32562 c -0.05616,0.06642 -0.08532,0.0783 -0.1296,0.09612 l -0.01404,0.00594 c -0.0032,0.00162 -0.007,0.00216 -0.01026,0.00216 -0.0108,0 -0.02052,-0.00648 -0.02484,-0.01674 -0.0059,-0.0135 5.4e-4,-0.0297 0.01458,-0.0351 l 0.01404,-0.00594 c 0.0405,-0.01674 0.06102,-0.02538 0.10908,-0.081 0.0097,-0.01188 0.027,-0.01296 0.03834,-0.00324 0.01134,0.00972 0.01242,0.02646 0.0027,0.0378 z\u0027     id\u003d\u0027path67\u0027     style\u003d\u0027fill:%23008000;fill-opacity:1;stroke-width:0.3\u0027 /%3E%3Cpath     d\u003d\u0027m -19.277239,-4.3873149 c 0.0027,0.01458 -0.007,0.02808 -0.0216,0.03078 -0.0011,0 -0.01674,0.0027 -0.04752,0.0027 -0.0243,0 -0.05724,-0.00162 -0.09936,-0.00702 -0.01512,-0.00162 -0.02538,-0.01512 -0.02322,-0.03024 0.0016,-0.01458 0.01512,-0.02484 0.03024,-0.02322 0.08748,0.01134 0.13014,0.00486 0.13068,0.00486 0.01458,-0.0027 0.02862,0.00702 0.03078,0.02214 z\u0027     id\u003d\u0027path69\u0027     style\u003d\u0027stroke-width:0.3\u0027 /%3E%3C/g%3E%3C/svg%3E";
  }

  @Override
  public String getCollectionName() {
    return null;
  }

  @Override
  public Map<String, String> getListBpmnErrors() {
    return null;
  }

  @Override
  public Class<SendBPMNMessageInput> getInputParameterClass() {
    return SendBPMNMessageInput.class;
  }

  @Override
  public Class<SendBPMNMessageOutput> getOutputParameterClass() {
    return SendBPMNMessageOutput.class;
  }

  @Override
  public ArrayList<String> appliesTo() {
    return (ArrayList<String>) Arrays.asList("bpmn:Task", "bpmn:IntermediateThrowEvent");
  }
}
