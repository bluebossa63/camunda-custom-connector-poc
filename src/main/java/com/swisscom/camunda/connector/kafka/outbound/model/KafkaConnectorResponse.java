package com.swisscom.camunda.connector.kafka.outbound.model;

import java.util.Objects;

/**
 * The Class KafkaConnectorResponse.
 */
public class KafkaConnectorResponse {

	/** The message. */
	private String message;

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(message);
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
		KafkaConnectorResponse other = (KafkaConnectorResponse) obj;
		return Objects.equals(message, other.message);
	}

}
