package net.aineuron.eagps.model.database;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vit Veres on 29-May-17
 * as a part of Android-EAGPS project.
 */

public class User {
	private Long userId;
	private String name;
	private String roleName;
    @SerializedName("Role")
    private Integer roleId;
	private String phone;
	private String token;
	private Long carId;
	private Car car;

	public User() {
	}

	public User(long userId, String name, String roleName, Integer roleId, String phone) {
		this.userId = userId;
		this.name = name;
		this.roleName = roleName;
		this.roleId = roleId;
		this.phone = phone;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getCarId() {
		return carId;
	}

	public void setCarId(Long carId) {
		this.carId = carId;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
}
