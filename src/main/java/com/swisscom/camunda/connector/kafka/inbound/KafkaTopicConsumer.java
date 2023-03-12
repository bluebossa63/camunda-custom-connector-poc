package com.swisscom.camunda.connector.kafka.inbound;

import java.time.Duration;
import java.util.Arrays;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/**
 * The Class KafkaTopicConsumer.
 */
public class KafkaTopicConsumer implements Runnable {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicConsumer.class);

	/** The service. */
	private ProcessInstanceStartService service;
	
	/** The concurrent kafka listener container factory. */
	private ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory;
	
	/** The topic name. */
	private String topicName;
	
	/** The process definition name. */
	private String processDefinitionName;

	/**
	 * Instantiates a new kafka topic consumer.
	 *
	 * @param service the service
	 * @param topicName the topic name
	 * @param concurrentKafkaListenerContainerFactory the concurrent kafka listener container factory
	 * @param processDefinitionName the process definition name
	 */
	public KafkaTopicConsumer(ProcessInstanceStartService service, String topicName,
			ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory,
			String processDefinitionName) {
		this.concurrentKafkaListenerContainerFactory = concurrentKafkaListenerContainerFactory;
		this.topicName = topicName;
		this.processDefinitionName = processDefinitionName;
		this.service = service;
	}

	/**
	 * Run.
	 */
	@Override
	public void run() {

		try (@SuppressWarnings("unchecked")
		final Consumer<String, String> consumer = (Consumer<String, String>) concurrentKafkaListenerContainerFactory
				.getConsumerFactory().createConsumer("camunda8", "reader1")) {
			consumer.subscribe(Arrays.asList(topicName));
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
				for (ConsumerRecord<String, String> record : records) {
					String key = record.key();
					String value = record.value();
					LOGGER.info(String.format("Consumed event from topic %s: key = %-10s value = %s", topicName, key,
							value));
					service.startProcess(value, processDefinitionName);
				}
			}
		}
	}

}
