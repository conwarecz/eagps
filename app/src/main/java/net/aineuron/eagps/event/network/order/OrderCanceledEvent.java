package net.aineuron.eagps.event.network.order;

/**
 * Created by Vit Veres on 09-Jun-17
 * as a part of Android-EAGPS project.
 */

public class OrderCanceledEvent {
	public final Long orderId;

	public OrderCanceledEvent(Long orderId) {
		this.orderId = orderId;
	}
}
