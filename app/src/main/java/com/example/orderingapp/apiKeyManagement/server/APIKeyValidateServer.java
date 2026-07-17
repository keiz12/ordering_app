package com.example.orderingapp.apiKeyManagement.server;

import android.content.Context;

import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.AndroidKey;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.toast.Toasts;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class APIKeyValidateServer {

    private final ShowToastFromBgThread activity;

    private final AndroidKey androidKey;

    private boolean isValid;

    public APIKeyValidateServer(ShowToastFromBgThread activity) {
        this.activity = activity;
        androidKey = new AndroidKey( new APIKeyDatabase((Context) activity).readAPIKey() );
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
            Toasts.showLongToast((Context) activity, "Failed to validate API Key. Please check your connection and credentials.");
            return;
        }

        setValid(true);
        System.out.println("--> "+isValid());

        activity.showToast(map.get(HttpServerConnection.responseBodyKey).toString());
    }


    private Request getRequest() {
        HttpServerConnection connection = new HttpServerConnection();
        String credentials = connection.getHttpBasicCredentials((Context) activity);

        return new Request.Builder()
                .addHeader("Authorization", credentials)
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.apiKey + HttpServerConnection.validateApiKey)
                .post(RequestBody.create( new Gson().toJson(androidKey), MediaType.parse("application/json")))
                .build();
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
