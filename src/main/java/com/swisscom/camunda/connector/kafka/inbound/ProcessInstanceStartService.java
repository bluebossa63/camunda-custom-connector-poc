package com.swisscom.camunda.connector.kafka.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

/**
 * The Class ProcessInstanceStartService.
 */
@Service
public class ProcessInstanceStartService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceStartService.class);

	/** The zeebe API. */
	@Value("${zeebe.client.broker.gateway-address}")
	private String zeebeAPI = "zeebe.camunda.tanzu.ch";
	
	/** The client id. */
	@Value("${zeebe.client.cloud.clientId}")
	private String clientId = "testconnector";
	
	/** The client secret. */
	@Value("${zeebe.client.cloud.clientSecret}")
	private String clientSecret = "uK5V9BewmqKMaDufulB3s27NA1QjueVq";
	
	/** The o auth API. */
	@Value("${zeebe.client.cloud.authUrl}")
	private String oAuthAPI = "https://camunda.camunda.tanzu.ch/auth/realms/camunda-platform/protocol/openid-connect/token/";

	/**
	 * Start process.
	 *
	 * @param message the message
	 * @param processDefinitionId the process definition id
	 */
	public void startProcess(String message, String processDefinitionId) {
		OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
				.authorizationServerUrl(oAuthAPI).audience(zeebeAPI).clientId(clientId).clientSecret(clientSecret)
				.build();

		try (ZeebeClient client = ZeebeClient.newClientBuilder().gatewayAddress(zeebeAPI)
				.credentialsProvider(credentialsProvider).build()) {
			final ProcessInstanceEvent wfInstance = client.newCreateInstanceCommand().bpmnProcessId(processDefinitionId)
					.latestVersion().variables(message).send().join();
			final long workflowInstanceKey = wfInstance.getProcessInstanceKey();
			LOGGER.info(String.format("Consumed event for processDefinitionId = %s with message = %s", processDefinitionId, message));
			LOGGER.info(String.format("Started Workflow Instance with key = %s", workflowInstanceKey));
		}
	}

}
