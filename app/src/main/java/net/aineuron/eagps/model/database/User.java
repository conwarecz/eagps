package net.aineuron.eagps.model.database;

import com.google.gson.annotations.SerializedName;

import net.aineuron.eagps.model.database.order.Order;

import java.util.List;

/**
 * Created by Vit Veres on 29-May-17
 * as a part of Android-EAGPS project.
 */

public class User {
	private Long userId;
    private Long id;
    private String name;
	private String roleName;
    @SerializedName("Role")
    private Integer roleId;
	private String phone;
	private String token;
	private Long carId;
	private Car car;
    private List<Order> currentOrders;
    private Entity entity;
    private Integer supplierId;
    private Integer userRole;
    private String userName;

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

    public List<Order> getCurrentOrders() {
        return currentOrders;
    }

    public void setCurrentOrders(List<Order> currentOrders) {
        this.currentOrders = currentOrders;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getUserRole() {
        return userRole;
    }

    public void setUserRole(Integer userRole) {
        this.userRole = userRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
