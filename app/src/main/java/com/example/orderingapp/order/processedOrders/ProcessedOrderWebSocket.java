package com.example.orderingapp.order.processedOrders;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.connection.webSocket.WebSocketRunnable;
import com.example.orderingapp.dto.Order;

import io.reactivex.Flowable;
import ua.naiksoftware.stomp.dto.StompMessage;

public class ProcessedOrderWebSocket implements WebSocketRunnable {

    private final String receiveCustomerMadeOrders = "/client/made/order/employee/activity";
    private final String clientDeleteOrder = "/client/delete/order/employee/activity";
    private final String staffUnProcessedOrder = "/client/staff/unprocess/order";
    private final String staffProcessedOrder = "/client/staff/process/order";

    private final ProcessedOrdersActivity activity;

    public ProcessedOrderWebSocket(ProcessedOrdersActivity activity) {
        this.activity = activity;
    }



    @Override
    public void onSuccess() {

        customerMadeOrdersReceival();

        clientDeleteOrder();

        staffProcessedOrder();

        staffUnProcessedOrder();

        activity.showToast("Web Socket Initiated Successfully");
    }

    private void customerMadeOrdersReceival () {
        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(receiveCustomerMadeOrders);

        topicFlowable.subscribe(l -> {
            activity.clientMadeOrder(HttpServerConnection.getLocalDateTimeAdapterGson().fromJson(l.getPayload(), Order.class));
        });
    }

    private void clientDeleteOrder () {

        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(clientDeleteOrder);

        topicFlowable.subscribe(l -> {
            activity.clientDeleteOrder(l.getPayload());
        });
    }

    private void staffProcessedOrder () {

        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(staffProcessedOrder);

        topicFlowable.subscribe(l -> {
            activity.staffProcessedOrder(l.getPayload());
        });
    }

    private void staffUnProcessedOrder () {
        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(staffUnProcessedOrder);

        topicFlowable.subscribe(l -> {
            activity.staffUnProcessedOrder(l.getPayload());
        });
    }

    @Override
    public void onFailure() {
        // add a failure notifier in the ProcessedOrderActivity
        activity.showToast("Web Socket failed to initiate");
    }
}
