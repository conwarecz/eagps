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
}
