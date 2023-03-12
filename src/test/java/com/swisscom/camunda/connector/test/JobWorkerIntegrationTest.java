package com.swisscom.camunda.connector.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

// TODO: Auto-generated Javadoc
/**
 * The Class JobWorkerIntegrationTest.
 */
public class JobWorkerIntegrationTest {

	/** The Constant zeebeAPI. */
	private static final String zeebeAPI = "zeebe.camunda.tanzu.ch";
	
	/** The Constant clientId. */
	private static final String clientId = "testconnector";
	
	/** The Constant clientSecret. */
	private static final String clientSecret = "uK5V9BewmqKMaDufulB3s27NA1QjueVq";
	
	/** The Constant oAuthAPI. */
	private static final String oAuthAPI = "https://camunda.camunda.tanzu.ch/auth/realms/camunda-platform/protocol/openid-connect/token/";

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]) {

		OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
				.authorizationServerUrl(oAuthAPI).audience(zeebeAPI).clientId(clientId).clientSecret(clientSecret)
				.build();

		try (ZeebeClient client = ZeebeClient.newClientBuilder().gatewayAddress(zeebeAPI)
				.credentialsProvider(credentialsProvider).build()) {
			// try (ZeebeClient client =
			// ZeebeClient.newClientBuilder().gatewayAddress(zeebeAPI).build()) {

			/* this part is launching a workflow instance */

			Map<String, Object> formVars = new HashMap<>();
			Map<String, Object> authentication = new HashMap<>();
			authentication.put("user", "demo");
			authentication.put("token", "demodemo");
			formVars.put("authentication", authentication);
			formVars.put("message", "a very spicy message");

			// does not work this way....

			final ProcessInstanceEvent wfInstance = client.newCreateInstanceCommand()
					.bpmnProcessId("testconnector-smoketest").latestVersion().variables(formVars).send().join();
			final long workflowInstanceKey = wfInstance.getProcessInstanceKey();

			System.out.println("Workflow instance created. Key: " + workflowInstanceKey);

			/* this part is listening to instances of this type */
			final JobWorker jobWorker = client.newWorker().jobType("io.camunda:testconnector:1")
					.handler((jobClient, job) -> {

						System.out.println("JOB: " + job.toJson());
						System.out.println("VARS: " + job.getVariablesAsMap());

						// you can use any java bean class
						// TestConnectorResult result = new TestConnectorResult();
						// result.setMyProperty("yeah");

						Map<String, Object> result = new HashMap<>();
						result.put("result", "a very impressing result");
						jobClient.newCompleteCommand(job.getKey()).variables(result).send().join();
					}).open();

			waitUntilClose();

			jobWorker.close();

			client.close();
			System.out.println("Closed.");

		}
	}

	/**
	 * Wait until close.
	 */
	private static void waitUntilClose() {
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				final String nextLine = scanner.nextLine();
				if (nextLine.contains("close")) {
					return;
				}
			}
		}
	}
}
