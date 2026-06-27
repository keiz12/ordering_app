package com.example.orderingapp.apiKeyManagement.server;

import com.example.orderingapp.apiKeyManagement.APIKeyManagementActivity;
import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.AndroidKey;
import com.example.orderingapp.toast.Toasts;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SaveApiKeyServer {

    private final APIKeyManagementActivity activity;
    private final String apiKey;

    public SaveApiKeyServer(APIKeyManagementActivity activity, String apiKey) {
        this.activity = activity;
        this.apiKey = apiKey;
    }

    public void run() {
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {
            Request request = getRequest();
            runAfterResponse(new HttpServerConnection().getResponseMap(request));
        });

        service.close();
    }

    private void runAfterResponse(HashMap<String, Object> map) {

        if (map == null || Boolean.FALSE.equals(map.get(HttpServerConnection.responseStatusKey))) {
            Toasts.showLongToast(activity, "Failed to save API Key to server. Please check your connection and credentials.");
            return;
        }

        activity.addAPIKey(apiKey);
    }

    private Request getRequest() {

        HttpServerConnection connection = new HttpServerConnection();
        Gson gson = new Gson();
        AndroidKey androidKey = new AndroidKey(apiKey);
        String json = gson.toJson(androidKey);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        String credentials = connection.getHttpBasicCredentials(activity);

        return new Request.Builder()
                .addHeader("Authorization", credentials)
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.apiKey)
                .post(body)
                .build();
    }
}
