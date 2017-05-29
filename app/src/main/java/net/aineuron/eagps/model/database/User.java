package net.aineuron.eagps.model.database;

/**
 * Created by Vit Veres on 29-May-17
 * as a part of Android-EAGPS project.
 */

public class User {
	private long userId = -1;
	private String name;
	private String roleName;
	private int roleId = -1;
	private String phone;
	private String token;
	private long carId = -1;

	public User() {
	}

	public User(long userId, String name, String roleName, int roleId, String phone) {
		this.userId = userId;
		this.name = name;
		this.roleName = roleName;
		this.roleId = roleId;
		this.phone = phone;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
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

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
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

	public long getCarId() {
		return carId;
	}

	public void setCarId(long carId) {
		this.carId = carId;
	}
}
