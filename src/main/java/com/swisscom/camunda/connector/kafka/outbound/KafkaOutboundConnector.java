/**
 *
 */
package com.swisscom.camunda.connector.kafka.outbound;

import static com.swisscom.camunda.connector.kafka.inbound.KafkaConsumerConfig.groupId;
import static com.swisscom.camunda.connector.kafka.inbound.KafkaConsumerConfig.kafkaClientId;
import static com.swisscom.camunda.connector.kafka.inbound.KafkaConsumerConfig.topicName;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.swisscom.camunda.connector.kafka.outbound.model.KafkaConnectorRequest;
import com.swisscom.camunda.connector.kafka.outbound.model.KafkaConnectorResponse;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;

/**
 * The Class KafkaOutboundConnector.
 *
 * @author Daniele
 */
@OutboundConnector(name = "Swisscom Kafka Connector", inputVariables = {
		"messageSpec" }, type = "com.swisscom.camunda.kafka.outbound.connector:1")
@Service
public class KafkaOutboundConnector implements OutboundConnectorFunction {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOutboundConnector.class);

	/** The bootstrap address. */
	private String bootstrapAddress;

	/** The kafka template. */
	private KafkaTemplate<String, String> kafkaTemplate;

	/**
	 * Instantiates a new kafka outbound connector.
	 */
	public KafkaOutboundConnector() {
		Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			bootstrapAddress = properties.getProperty("spring.kafka.bootstrap-servers");
			kafkaTemplate = kafkaTemplate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Execute.
	 *
	 * @param context the context
	 * @return the object
	 * @throws Exception the exception
	 */
	@Override
	public Object execute(OutboundConnectorContext context) throws Exception {
		var connectorRequest = context.getVariablesAsType(KafkaConnectorRequest.class);
		context.validate(connectorRequest);
		context.replaceSecrets(connectorRequest);
		return executeConnector(connectorRequest);
	}

	/**
	 * Execute connector.
	 *
	 * @param connectorRequest the connector request
	 * @return the kafka connector response
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	private KafkaConnectorResponse executeConnector(final KafkaConnectorRequest connectorRequest) {
		LOGGER.info("Executing my connector with request {}", connectorRequest);
		var retVal = new KafkaConnectorResponse();
		switch (connectorRequest.getMessageSpec().getRequestType()) {

		case SEND_MSG:

			ListenableFuture<SendResult<String, String>> future = kafkaTemplate
					.send(connectorRequest.getMessageSpec().getTopic(), connectorRequest.getMessageSpec().getMessage());

			future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

				@Override
				public void onSuccess(SendResult<String, String> result) {
					LOGGER.info("Sent message=[" + connectorRequest.getMessageSpec().getMessage() + "] with offset=["
							+ result.getRecordMetadata().offset() + "]");
					retVal.setMessage("Sent message=[" + connectorRequest.getMessageSpec().getMessage()
							+ "] with offset=[" + result.getRecordMetadata().offset() + "]");
				}

				@Override
				public void onFailure(Throwable ex) {
					LOGGER.info("Unable to send message=[" + connectorRequest.getMessageSpec().getMessage()
							+ "] due to : " + ex.getMessage());
					retVal.setMessage("Unable to send message=[" + connectorRequest.getMessageSpec().getMessage()
							+ "] due to : " + ex.getMessage());
				}
			});
			while (!future.isDone())
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return retVal;

		case WAIT_MSG:

			Consumer<String, String> consumer = null;

			try {

				ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
				factory.setConsumerFactory(consumerFactory());
				if (connectorRequest.getMessageSpec().getSearchExpression() != null
						&& connectorRequest.getMessageSpec().getSearchExpression().trim().length() > 0) {
					factory.setRecordFilterStrategy(
							record -> record.value().matches(connectorRequest.getMessageSpec().getSearchExpression()));
				}
				consumer = (Consumer<String, String>) factory.getConsumerFactory().createConsumer(groupId, "reader1");
				consumer.subscribe(Arrays.asList(connectorRequest.getMessageSpec().getTopic()));
				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
					for (ConsumerRecord<String, String> record : records) {
						String key = record.key();
						String value = record.value();
						LOGGER.info(String.format("Consumed event from topic %s: key = %-10s value = %s", topicName,
								key, value));
						retVal.setMessage(value);
						return retVal;

					}
				}
			} finally {
				consumer.close();
			}

		default:
			throw new RuntimeException("invalid request type");
		}
	}

	/**
	 * Producer factory.
	 *
	 * @return the producer factory
	 */
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaClientId);
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	/**
	 * Kafka template.
	 *
	 * @return the kafka template
	 */
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	/**
	 * Consumer factory.
	 *
	 * @return the consumer factory
	 */
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaClientId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	/**
	 * Kafka listener container factory.
	 *
	 * @return the concurrent kafka listener container factory
	 */
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}
}
