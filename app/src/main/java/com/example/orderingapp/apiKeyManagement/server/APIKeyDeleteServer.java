package com.example.orderingapp.apiKeyManagement.server;

import com.example.orderingapp.apiKeyManagement.APIKeyManagementActivity;
import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.toast.Toasts;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;

public class APIKeyDeleteServer {

    private final APIKeyManagementActivity activity;

    public APIKeyDeleteServer(APIKeyManagementActivity activity) {
        this.activity = activity;
    }

    public void run() {
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {
            Request request = getRequest();
            runAfterResponse(new HttpServerConnection().getResponseMap(request));
        });

        service.shutdown();
    }

    private void runAfterResponse(HashMap<String, Object> map) {

        if (map == null || Boolean.FALSE.equals(map.get(HttpServerConnection.responseStatusKey))) {
            Toasts.showLongToast(activity, "Failed to delete API Key from server. Please check your connection and credentials.");
            return;
        }

        activity.removeAPIKeyFromDevice();
    }

    private Request getRequest() {
        HttpServerConnection connection = new HttpServerConnection();
        String credentials = connection.getHttpBasicCredentials(activity);

        return new Request.Builder()
                .addHeader("Authorization", credentials)
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.apiKey)
                .delete()
                .build();
    }
}
