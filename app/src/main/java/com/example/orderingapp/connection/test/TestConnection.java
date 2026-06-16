package com.example.orderingapp.connection.test;


import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.home.HomeActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;

public class TestConnection {

    public void testConnection (HomeActivity homeActivity)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            run(homeActivity);
        });

        executorService.submit(() -> {
            WebSocketConnection.getInstance().sendToServerWebSocketTest();
        });

        executorService.shutdown();
        executorService.close();
    }

    private void run (HomeActivity homeActivity)
    {
        setConnectionIndicatorText(homeActivity, "Loading ...");

        Request request = buildRequest();

        Response response = new HttpServerConnection().sendRequest(request);

        if (response == null || !response.isSuccessful()) {
            setConnectionIndicatorText(homeActivity, "Disconnected");
        }
        else if (response.isSuccessful()) {
            setConnectionIndicatorText(homeActivity, "Connected");
        }
        else {
            setConnectionIndicatorText(homeActivity, "Unknown Error");
        }
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
