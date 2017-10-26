package net.aineuron.eagps.model.database;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vit Veres on 26-Apr-17
 * as a part of Android-EAGPS project.
 */

public class Car {

	private Long id;
	@SerializedName("Status")
	private Long statusId;
	@SerializedName("User")
	private Long userId;
	private String licencePlate;
	private String model;
	private String name;
	private String userName;

	private boolean isSent = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getLicencePlate() {
		return licencePlate;
	}

	public void setLicencePlate(String licencePlate) {
		this.licencePlate = licencePlate;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public boolean isSent() {
		return isSent;
	}

	public void setSent(boolean sent) {
		isSent = sent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
