package com.example.orderingapp.connection.test;


import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.home.HomeActivity;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;

public class TestConnection {

    public void testConnection (HomeActivity homeActivity)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(() -> {
            run(homeActivity);
        });

        executorService.execute(() -> {
            WebSocketConnection.getInstance().runSocket(new TestWebSocketConnection(homeActivity));
        });

        executorService.shutdown();
    }

    private void run (HomeActivity homeActivity)
    {
        setConnectionIndicatorText(homeActivity, "Loading ...");

        Request request = buildRequest();

        HashMap<String, Object> responseMap = new HttpServerConnection().getResponseMap(request);

        if (responseMap == null || responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE))
            setConnectionIndicatorText(homeActivity, "Disconnected");

        else if (responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.TRUE))
            setConnectionIndicatorText(homeActivity, "Connected");

        else
            setConnectionIndicatorText(homeActivity, "Unknown Error");

    }

    private void setConnectionIndicatorText (HomeActivity homeActivity, String text) {
        homeActivity.setHttpConnectionTextViewTxt(text);
    }

    private Request buildRequest () {
        return new Request
                .Builder()
                .get()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.allTest)
                .build();
    }
}
