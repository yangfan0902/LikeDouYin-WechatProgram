package com.imooc.pojo;

import javax.persistence.Column;
import javax.persistence.Id;

public class UsersFans {
	@Id
	private String id;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "fan_id")
	private String fanId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFanId() {
		return fanId;
	}

	public void setFanId(String fanId) {
		this.fanId = fanId;
	}
}
