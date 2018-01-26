package com.ituple.ci.s3trigger;

import java.util.LinkedHashMap;

import com.ituple.ci.s3trigger.beans.S3;

public class S3TriggerData {

	S3 s3;
	LinkedHashMap<String, Object> userIdentity;
	LinkedHashMap<String, Object> requestParameters;
	LinkedHashMap<String, Object> responseElements;

	String eventVersion;
	String eventSource;
	String awsRegion;
	String eventTime;
	String eventName;

	private S3TriggerData() {
	}

	public S3 getS3() {
		return s3;
	}

	public void setS3(S3 s3) {
		this.s3 = s3;
	}

	public LinkedHashMap<String, Object> getUserIdentity() {
		return userIdentity;
	}

	public void setUserIdentity(LinkedHashMap<String, Object> userIdentity) {
		this.userIdentity = userIdentity;
	}

	public LinkedHashMap<String, Object> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(LinkedHashMap<String, Object> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public LinkedHashMap<String, Object> getResponseElements() {
		return responseElements;
	}

	public void setResponseElements(LinkedHashMap<String, Object> responseElements) {
		this.responseElements = responseElements;
	}

	public String getEventVersion() {
		return eventVersion;
	}

	public void setEventVersion(String eventVersion) {
		this.eventVersion = eventVersion;
	}

	public String getEventSource() {
		return eventSource;
	}

	public void setEventSource(String eventSource) {
		this.eventSource = eventSource;
	}

	public String getAwsRegion() {
		return awsRegion;
	}

	public void setAwsRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public static S3TriggerData S3TriggerDataFacory(LinkedHashMap<String, Object> obj) {

		S3TriggerData s3TriggerData = new S3TriggerData();
		System.out.println("inside S3Trigger and passed map is "+obj);
		if (obj != null && !obj.isEmpty()) {
			if (obj.containsKey("s3"))
				s3TriggerData.setS3(S3.S3Facory((LinkedHashMap<String, Object>) obj.get("s3")));
			if (obj.containsKey("userIdentity"))
				s3TriggerData.setUserIdentity((LinkedHashMap<String, java.lang.Object>) obj.get("userIdentity"));
			if (obj.containsKey("requestParameters"))
				s3TriggerData
						.setRequestParameters((LinkedHashMap<String, java.lang.Object>) obj.get("requestParameters"));
			if (obj.containsKey("responseElements"))
				s3TriggerData
						.setResponseElements((LinkedHashMap<String, java.lang.Object>) obj.get("responseElements"));
			if (obj.containsKey("Version"))
				s3TriggerData.setEventVersion((String) obj.get("eventVersion"));
			if (obj.containsKey("eventSource"))
				s3TriggerData.setEventSource((String) obj.get("eventSource"));
			if (obj.containsKey("awsRegion"))
				s3TriggerData.setAwsRegion((String) obj.get("awsRegion"));
			if (obj.containsKey("eventTime"))
				s3TriggerData.setEventTime((String) obj.get("eventTime"));
			if (obj.containsKey("eventName"))
				s3TriggerData.setEventName((String) obj.get("eventName"));
		}
		return s3TriggerData;
	}

}
