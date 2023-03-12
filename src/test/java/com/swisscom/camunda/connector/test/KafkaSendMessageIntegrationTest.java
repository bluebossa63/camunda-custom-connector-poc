package com.swisscom.camunda.connector.test;

import java.io.FileReader;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.camunda.connector.kafka.inbound.KafkaConsumerConfig;

// TODO: Auto-generated Javadoc
/**
 * The Class KafkaSendMessageIntegrationTest.
 */
@EnableKafka
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
		io.camunda.connector.runtime.ConnectorRuntimeApplication.class,
		com.swisscom.camunda.connector.kafka.inbound.KafkaInboundConnector.class,
		com.swisscom.camunda.connector.kafka.inbound.ProcessInstanceStartService.class })
public class KafkaSendMessageIntegrationTest {

	/** The kafka template. */
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

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

	/** The bootstrap address. */
	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;


	/**
	 * Do test 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void doTest2() throws Exception {

		ObjectMapper om = new ObjectMapper();
		JsonNode node = om.readTree(new FileReader("./src/main/resources/teams-card.json"));
		sendMessage(node.toPrettyString());
		Thread.sleep(1000);
	}


	/**
	 * Send message.
	 *
	 * @param message the message
	 */
	@Ignore
	private void sendMessage(String message) {

		ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(KafkaConsumerConfig.topicName, message);

		future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

			@Override
			public void onSuccess(SendResult<String, String> result) {
				System.out.println(
						"Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
			}

			@Override
			public void onFailure(Throwable ex) {
				System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
			}
		});
	}
}