package net.aineuron.eagps.model.database.order;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.aineuron.eagps.adapter.RealmStringListTypeAdapter;
import net.aineuron.eagps.model.database.Car;
import net.aineuron.eagps.model.database.Message;
import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.database.UserWhoKickedMeFromCar;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 20.09.2017.
 */

public class Tender extends RealmObject implements Serializable {
	@PrimaryKey
	private String tenderEntityUniId;
	private int pushId;
	private Order Order;
	private Message Message;
	private Long Status;
	private Long TenderId;
	private Car Entity;
	private UserWhoKickedMeFromCar AssignedUser;
	private UserWhoKickedMeFromCar User;
	private Long EntityId;
	private Date incomeTime;
	private int allowedDepartureDelayMinutes;

	public static Order getOrderFromJson(String json) {
		Tender tender = getTender(json);
		return tender.getOrder();
	}

	public static Message getMessageFromJson(String json) {
		Tender tender = getTender(json);
		return tender.getMessage();
	}

	public static Long getNewStatusFromJson(String json) {
		Tender tender = getTender(json);
		return tender.getStatus();
	}

	public static UserWhoKickedMeFromCar getUserWhoKickedMeFromCar(String json) {
		Tender tender = getTender(json);
		return tender.getAssignedUser();
	}

	public static UserWhoKickedMeFromCar getUser(String json) {
		Tender tender = getTender(json);
		return tender.getUser();
	}

	public static Tender getTender(String json) {
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:sss")
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
				.registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
						}.getType(),
						RealmStringListTypeAdapter.INSTANCE)
				.create();
		Tender tender = gson.fromJson(json, Tender.class);
		tender.setIncomeTime(Calendar.getInstance().getTime());

		Long entityId = 0L;
		if (tender.getEntity() != null) {
			entityId = tender.getEntity().getId();
		} else if (tender.getOrder() != null && tender.getOrder().getEntityId() != null) {
			entityId = tender.getOrder().getEntityId();
		}

		tender.setTenderEntityUniId(tender.getTenderId() + "_" + entityId);
		return tender;
	}

	public net.aineuron.eagps.model.database.order.Order getOrder() {
		return Order;
	}

	public void setOrder(net.aineuron.eagps.model.database.order.Order order) {
		Order = order;
	}

	public net.aineuron.eagps.model.database.Message getMessage() {
		return Message;
	}

	public void setMessage(net.aineuron.eagps.model.database.Message message) {
		Message = message;
	}

	public Long getTenderId() {
		return TenderId;
	}

	public void setTenderId(Long tenderId) {
		TenderId = tenderId;
	}

	public Car getEntity() {
		return Entity;
	}

	public void setEntity(Car entity) {
		Entity = entity;
	}

	public Date getIncomeTime() {
		return incomeTime;
	}

	public void setIncomeTime(Date incomeTime) {
		this.incomeTime = incomeTime;
	}

	public String getTenderEntityUniId() {
		return tenderEntityUniId;
	}

	public void setTenderEntityUniId(String tenderEntityUniId) {
		this.tenderEntityUniId = tenderEntityUniId;
	}

	public UserWhoKickedMeFromCar getAssignedUser() {
		return AssignedUser;
	}

	public void setAssignedUser(UserWhoKickedMeFromCar assignedUser) {
		AssignedUser = assignedUser;
	}

	public Long getStatus() {
		return Status;
	}

	public void setStatus(Long status) {
		Status = status;
	}

	public Long getEntityId() {
		return EntityId;
	}

	public void setEntityId(Long entityId) {
		EntityId = entityId;
	}

	public UserWhoKickedMeFromCar getUser() {
		return User;
	}

	public void setUser(UserWhoKickedMeFromCar user) {
		User = user;
	}

	public int getPushId() {
		return pushId;
	}

	public void setPushId(int pushId) {
		this.pushId = pushId;
	}

	public int getAllowedDepartureDelayMinutes() {
		return allowedDepartureDelayMinutes;
	}

	public void setAllowedDepartureDelayMinutes(int allowedDepartureDelayMinutes) {
		allowedDepartureDelayMinutes = allowedDepartureDelayMinutes;
	}
}
