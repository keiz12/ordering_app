package com.example.orderingapp.connection.webSocket;

import com.example.orderingapp.connection.Connection;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.LifecycleEvent;

public class WebSocketConnection {

    private static WebSocketConnection instance;
    private WebSocketConnection () {}

    public static WebSocketConnection getInstance() {
        if (instance == null) {
            instance = new WebSocketConnection();
        }
        return instance;
    }

    private final String webSocketBaseURL = "ws://" + Connection.ipV4 + ":" + Connection.port+"/all/server_connect";

    private final StompClient stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketBaseURL);

    public void runSocket(WebSocketRunnable runnable) {

        if (stompClient.isConnected()) {
            runnable.onSuccess();
            return;
        }

        stompClient.connect();

        stompClient.lifecycle().subscribe(lifecycleEvent -> {

            if (lifecycleEvent.getType() == LifecycleEvent.Type.OPENED)
                runnable.onSuccess();
            else
                runnable.onFailure();
        });
    }

    public StompClient getStompClient() {
        return stompClient;
    }
}
