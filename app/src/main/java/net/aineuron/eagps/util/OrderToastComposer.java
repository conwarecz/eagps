package net.aineuron.eagps.util;

import android.content.Context;

import net.aineuron.eagps.R;

import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ASSIGNED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_CANCELLED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_CREATED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_ENTITY_FINISHED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_FINISHED;
import static net.aineuron.eagps.model.database.order.Order.ORDER_STATE_SENT;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 23.11.2017.
 */

public class OrderToastComposer {
    public static String getOrderChangedToastMessage(Context context, int status) {
        String message = context.getResources().getString(R.string.order_changed) + "\n";
        switch (status) {
            case ORDER_STATE_CREATED:
                message = message + context.getResources().getString(R.string.order_created);
                break;
            case ORDER_STATE_ASSIGNED:
                message = message + context.getResources().getString(R.string.order_assigned);
                break;
            case ORDER_STATE_ENTITY_FINISHED:
                message = message + context.getResources().getString(R.string.order_entity_finished);
                break;
            case ORDER_STATE_FINISHED:
                message = message + context.getResources().getString(R.string.order_finished);
                break;
            case ORDER_STATE_SENT:
                message = message + context.getResources().getString(R.string.order_sent);
                break;
            case ORDER_STATE_CANCELLED:
                message = message + context.getResources().getString(R.string.order_cancelled);
                break;
        }
        return message;
    }
}
