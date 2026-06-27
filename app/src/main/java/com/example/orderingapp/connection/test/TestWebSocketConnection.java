package com.example.orderingapp.connection.test;

import android.util.Log;

import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.connection.webSocket.WebSocketRunnable;
import com.example.orderingapp.home.HomeActivity;

import io.reactivex.Flowable;
import ua.naiksoftware.stomp.dto.StompMessage;

public class TestWebSocketConnection implements WebSocketRunnable {

    private final String receiveWebSocketTest = "/client/test/socket";
    private final String sendWebSocketTest = "/app/server/test/socket";
    private final HomeActivity homeActivity;

    public TestWebSocketConnection(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Override
    public void onSuccess() {
        receiveServerWebSocketTestResponse();
        sendToServerWebSocketTest ();
    }

    @Override
    public void onFailure() {
        updateHomeActivityUI("Web Socket Error");
    }

    private void receiveServerWebSocketTestResponse() {

        Flowable<StompMessage> topicFlowable = WebSocketConnection.getInstance().getStompClient().topic(receiveWebSocketTest);

        updateHomeActivityUI("Loading...");

        topicFlowable.subscribe(l -> {
            updateHomeActivityUI(l.getPayload());
        });
    }

    private void sendToServerWebSocketTest () {
        WebSocketConnection.getInstance().getStompClient().send(sendWebSocketTest).subscribe(() -> {
            Log.d("STOMP", "Web Socket Send Success");
        }, throwable -> {
            Log.e("STOMP", "Web Socket Send Error", throwable);
        });
    }

    private void updateHomeActivityUI(String message) {
        homeActivity.setWebSocketTextViewTxt(message);
    }
}
