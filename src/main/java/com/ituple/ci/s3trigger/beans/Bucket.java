package com.ituple.ci.s3trigger.beans;

import java.util.LinkedHashMap;
import java.util.Map;

public class Bucket {
	String name;
	Map<String, Object> ownerIdentity;
	String arn;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getOwnerIdentity() {
		return ownerIdentity;
	}

	public void setOwnerIdentity(Map<String, Object> ownerIdentity) {
		this.ownerIdentity = ownerIdentity;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public static Bucket BucketFactory(LinkedHashMap<String, Object> obj) {
		Bucket bucket = new Bucket();
		System.out.println("inside Bucket and passed map is " + obj);
		if (obj != null && !obj.isEmpty()) {
			if (obj.containsKey("name"))
				bucket.setName((String) obj.get("name"));

			if (obj.containsKey("ownerIdentity"))
				bucket.setOwnerIdentity((Map<String, Object>) obj.get("ownerIdentity"));

			if (obj.containsKey("arn"))
				bucket.setArn((String) obj.get("arn"));
		}
		return bucket;

	}

}
