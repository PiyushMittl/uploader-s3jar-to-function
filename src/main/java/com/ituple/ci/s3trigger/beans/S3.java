package com.ituple.ci.s3trigger.beans;

import java.util.LinkedHashMap;

public class S3 {

	String s3SchemaVersion;
	String configurationId;
	Bucket bucket;
	object object;

	public String getS3SchemaVersion() {
		return s3SchemaVersion;
	}

	public void setS3SchemaVersion(String s3SchemaVersion) {
		this.s3SchemaVersion = s3SchemaVersion;
	}

	public String getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(String configurationId) {
		this.configurationId = configurationId;
	}

	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	public object getObject() {
		return object;
	}

	public void setObject(object object) {
		this.object = object;
	}

	public static S3 S3Facory(LinkedHashMap<String, Object> obj) {
		S3 s3 = new S3();
System.out.println("inside S3 and passed map is "+obj);
		if (obj != null && !obj.isEmpty()) {
			if (obj.containsKey("s3SchemaVersion"))
				s3.setS3SchemaVersion((String) obj.get("s3SchemaVersion"));

			if (obj.containsKey("configurationId"))
				s3.setConfigurationId((String) obj.get("configurationId"));

			if (obj.containsKey("bucket"))
				s3.setBucket(Bucket.BucketFactory((LinkedHashMap<String, Object>) obj.get("bucket")));

			if (obj.containsKey("object"))
				s3.setObject(com.ituple.ci.s3trigger.beans.object
						.objectFacory((LinkedHashMap<String, Object>) obj.get("object")));

		}
		return s3;

	}

}
