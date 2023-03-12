package com.swisscom.camunda.connector.kafka.outbound.model;

import java.util.Objects;

import javax.validation.constraints.NotEmpty;

import io.camunda.connector.api.annotation.Secret;


/**
 * The Class MessageSpec.
 */
public class MessageSpec {

	/** The request type. */
	@NotEmpty
	RequestType requestType;
	
	/** The topic. */
	private String topic;
	
	/** The message. */
	@Secret
	private String message;
	
	/** The group id. */
	private String groupId;
	
	/** The search expression. */
	private String searchExpression;

	/**
	 * Gets the topic.
	 *
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Sets the topic.
	 *
	 * @param topic the new topic
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

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
	 * Gets the request type.
	 *
	 * @return the request type
	 */
	public RequestType getRequestType() {
		return requestType;
	}

	/**
	 * Sets the request type.
	 *
	 * @param requestType the new request type
	 */
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	/**
	 * Gets the group id.
	 *
	 * @return the group id
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets the group id.
	 *
	 * @param groupId the new group id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Gets the search expression.
	 *
	 * @return the search expression
	 */
	public String getSearchExpression() {
		return searchExpression;
	}

	/**
	 * Sets the search expression.
	 *
	 * @param searchExpression the new search expression
	 */
	public void setSearchExpression(String searchExpression) {
		this.searchExpression = searchExpression;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(groupId, message, requestType, searchExpression, topic);
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
		MessageSpec other = (MessageSpec) obj;
		return Objects.equals(groupId, other.groupId) && Objects.equals(message, other.message)
				&& requestType == other.requestType && Objects.equals(searchExpression, other.searchExpression)
				&& Objects.equals(topic, other.topic);
	}

}
