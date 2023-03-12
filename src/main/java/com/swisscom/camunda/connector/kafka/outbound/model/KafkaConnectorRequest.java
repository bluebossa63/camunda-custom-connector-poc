package com.swisscom.camunda.connector.kafka.outbound.model;

import java.util.Objects;

import io.camunda.connector.api.annotation.Secret;


/**
 * The Class KafkaConnectorRequest.
 */
public class KafkaConnectorRequest {

	/** The message spec. */
	@Secret
	MessageSpec messageSpec;

	/**
	 * Gets the message spec.
	 *
	 * @return the message spec
	 */
	public MessageSpec getMessageSpec() {
		return messageSpec;
	}

	/**
	 * Sets the message spec.
	 *
	 * @param messageSpec the new message spec
	 */
	public void setMessageSpec(MessageSpec messageSpec) {
		this.messageSpec = messageSpec;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(messageSpec);
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		KafkaConnectorRequest other = (KafkaConnectorRequest) obj;
		return Objects.equals(messageSpec, other.messageSpec);
	}



}
