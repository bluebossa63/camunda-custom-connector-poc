/**
 *
 */
package com.swisscom.camunda.connector.kafka.inbound;

import static com.swisscom.camunda.connector.kafka.inbound.KafkaConsumerConfig.topicName;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * The Class KafkaInboundConnector.
 *
 * @author Daniele
 */
@Component
@EnableAsync
public class KafkaInboundConnector {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaInboundConnector.class);

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

	/** The process instance start service. */
	@Autowired
	ProcessInstanceStartService processInstanceStartService;

	/** The concurrent kafka listener container factory. */
	@Autowired
	ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory;

	/** The task executor. */
	@Autowired
	private TaskExecutor taskExecutor;

	/**
	 * Adds the listener.
	 *
	 * @param topic the topic
	 * @param processDefinitionId the process definition id
	 */
	public void addListener(String topic, String processDefinitionId) {
		taskExecutor.execute(new KafkaTopicConsumer(processInstanceStartService, topic,
				concurrentKafkaListenerContainerFactory, processDefinitionId));

		// TODO: persist configuration and implement api for controller

	}

	/**
	 * After properties set.
	 *
	 * @throws Exception the exception
	 */
	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		addListener(topicName, "teams-cards-sender");

	}

	/**
	 * Task executor.
	 *
	 * @return the task executor
	 */
	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor(); // Or use another one of your liking
	}

}
