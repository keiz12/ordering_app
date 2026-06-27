package com.example.orderingapp.order.madeOrders;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.connection.webSocket.WebSocketRunnable;
import com.example.orderingapp.dto.StaffProcessedOrder;

import java.time.format.DateTimeFormatter;

import io.reactivex.Flowable;
import ua.naiksoftware.stomp.dto.StompMessage;

public class MadeOrdersWebSocket implements WebSocketRunnable {

    private final String staffProcessOrder = "/client/staff/process/order";
    private final String staffUnProcessOrder = "/client/staff/unprocess/order";
    private final String staffConfirmOrderPayment = "/client/staff/confirm/order/payment";
    private final String staffRollbackOrderPayment = "/client/staff/rollback/order/payment";
    private final MadeOrdersActivity madeOrdersActivity;

    public MadeOrdersWebSocket(MadeOrdersActivity madeOrdersActivity) {
        this.madeOrdersActivity = madeOrdersActivity;
    }

    @Override
    public void onSuccess() {
        staffProcessOrder();
        staffConfirmOrderPayment();
        staffRollbackOrderPayment();
    }

    @Override
    public void onFailure() {
    }

    private void staffProcessOrder () {

        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(staffProcessOrder);

        topicFlowable.subscribe(l -> {

            StaffProcessedOrder staffProcessedOrderDTO = HttpServerConnection.getLocalDateTimeAdapterGson().fromJson(l.getPayload(), StaffProcessedOrder.class);

            if (!validateActivity(staffProcessedOrderDTO.getOrder().getUuid()))
                return;


            madeOrdersActivity.updateUI(() -> madeOrdersActivity.setProcessedByTextview(staffProcessedOrderDTO.getProcessedBy()));
            madeOrdersActivity.updateUI(() -> madeOrdersActivity.setProcessedAtTextview(staffProcessedOrderDTO.getProcessedAt().format(DateTimeFormatter.ofPattern("d MMMM uuuu h:m:s"))) );

        });
    }

    private void staffUnProcessOrder () {}

    private void staffConfirmOrderPayment () {

        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(staffConfirmOrderPayment);

        topicFlowable.subscribe(l -> {

            if (!validateActivity(l.getPayload()))
                return;

            madeOrdersActivity.updateUI(() -> madeOrdersActivity.setIsOrderPaidTextView(true));
        });
    }

    private void staffRollbackOrderPayment () {

        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(staffRollbackOrderPayment);

        topicFlowable.subscribe(l -> {

            if (!validateActivity(l.getPayload()))
                return;

            madeOrdersActivity.updateUI(() -> madeOrdersActivity.setIsOrderPaidTextView(false));
        });
    }

    private boolean validateActivity (String uuid) {
        return madeOrdersActivity.validateUuid(uuid);
    }
}