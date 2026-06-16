package com.example.orderingapp.connection.webSocket;

import android.util.Log;

import com.example.orderingapp.home.HomeActivity;
import com.example.orderingapp.connection.Connection;

import java.lang.ref.WeakReference;

import io.reactivex.Flowable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

public class WebSocketConnection {

    private static WebSocketConnection instance;
    private WeakReference<HomeActivity> homeActivityRef;

    private WebSocketConnection () {}

    public static WebSocketConnection getInstance() {
        if (instance == null) {
            instance = new WebSocketConnection();
        }
        return instance;
    }

    public static void setHomeActivity (HomeActivity homeActivity) {
        getInstance().homeActivityRef = new WeakReference<>(homeActivity);
    }

    private final String webSocketBaseURL = "ws://" + Connection.ipV4 + ":" + Connection.port+"/all/server_connect";
    private final String receiveWebSocketTest = "/client/test/socket";
    private final String sendWebSocketTest = "/app/server/test/socket";
    
    private final StompClient stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketBaseURL);

    public void connectAndSubscribe() {
        receiveServerWebSocketTestResponse();
        webSocketInspect ();
        stompClient.connect();
    }

    private void webSocketInspect () {
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("STOMP", "Web Socket Success");
                    break;
                case ERROR:
                    Log.e("STOMP", "Web Socket Error", lifecycleEvent.getException());
                    break;
            }
        });
    }

    private void receiveServerWebSocketTestResponse() {
        Flowable<StompMessage> topicFlowable = stompClient.topic(receiveWebSocketTest);

        topicFlowable.subscribe(l -> {
            updateUI(l.getPayload());
        }, throwable -> {
            updateUI("Web Socket Error");
        });
    }

    private void updateUI(String message) {
        if ( homeActivityRef == null || homeActivityRef.get() == null)
            return;
        homeActivityRef.get().setWebSocketTextViewTxt(message);
    }

    public void sendToServerWebSocketTest () {
        stompClient.send(sendWebSocketTest).subscribe(() -> {
            Log.d("STOMP", "Web Socket Send Success");
        }, throwable -> {
            Log.e("STOMP", "Web Socket Send Error", throwable);
        });
    }
}
