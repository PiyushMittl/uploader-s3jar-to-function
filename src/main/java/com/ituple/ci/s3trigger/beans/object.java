package com.ituple.ci.s3trigger.beans;

import java.util.LinkedHashMap;

public class object {

	String key;
	String size;
	String eTag;
	String versionId;
	String sequencer;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getSequencer() {
		return sequencer;
	}

	public void setSequencer(String sequencer) {
		this.sequencer = sequencer;
	}

	public static object objectFacory(LinkedHashMap<String, Object> obj) {
		object object = new object();
		System.out.println("inside object and passed map is " + obj);
		if (obj != null && !obj.isEmpty()) {

			String key;
			String size;
			String eTag;
			String versionId;
			String sequencer;

			if (obj.containsKey("key"))
				object.setKey((String) obj.get("key"));
			if (obj.containsKey("size"))
				object.setSize("" + obj.get("size"));
			if (obj.containsKey("eTag"))
				object.seteTag((String) obj.get("eTag"));
			if (obj.containsKey("versionId"))
				object.setVersionId((String) obj.get("versionId"));
			if (obj.containsKey("sequencer"))
				object.setSequencer((String) obj.get("sequencer"));
		}
		return object;

	}

}
