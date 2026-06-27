package com.example.orderingapp.my_profile.server;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.UserDTO;
import com.example.orderingapp.my_profile.MyProfileActivity;
import com.example.orderingapp.toast.Toasts;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;

public class GetEmployeeServer {

    private final MyProfileActivity activity;

    public GetEmployeeServer(MyProfileActivity activity) {
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
            activity.runOnUiThread(() -> Toasts.showLongToast(activity, "Failed to load profile data."));
            return;
        }

        String body = (String) map.get(HttpServerConnection.responseBodyKey);
        UserDTO userDTO = new Gson().fromJson(body, UserDTO.class);

        activity.populateData(userDTO);
    }

    private Request getRequest() {
        HttpServerConnection connection = new HttpServerConnection();
        String credentials = connection.getHttpBasicCredentials(activity);

        return new Request.Builder()
                .addHeader("Authorization", credentials)
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getEmployee)
                .get()
                .build();
    }
}
