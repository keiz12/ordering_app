package com.example.orderingapp.customerFeedback.server;

import android.content.Context;

import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.customerFeedback.CustomerFeedbackActivity;
import com.example.orderingapp.dto.CustomerFeedbackPost;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.order.orders.OrderingActivity;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostCustomerFeedbackServer {

    private final ShowToastFromBgThread activity;

    private final String feedback;

    public PostCustomerFeedbackServer(ShowToastFromBgThread activity, String feedback) {
        this.feedback = feedback;
        this.activity = activity;
    }

    public void postCustomerFeedback (){

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {

            if (!isFeedbackValid())
                return;

            String apiKey = getAPIKey();

            if (apiKey.isBlank()) {
                activity.showToast("The feedback is only designed for customers");
                remakeUI ();
                return;
            }

            Request request = getRequest(new CustomerFeedbackPost(apiKey, feedback));
            HashMap<String, Object> response = sendRequest(request);
            runAfterResponse(response);
        });

        executorService.shutdown();
    }

    private boolean isFeedbackValid () {
        if (feedback.isBlank()) {
            activity.showToast("Type something in the feedback please!");
            remakeUI();
            return false;
        }
        return true;
    }

    private String getAPIKey () {
        return new APIKeyDatabase((Context) activity).readAPIKey();
    }

    private HashMap<String,Object> sendRequest (Request request) {
        return new HttpServerConnection().getResponseMap(request);
    }

    private void runAfterResponse(HashMap<String, Object> map) {
        showToast(map);
        remakeUI();
    }

    private void showToast (HashMap<String, Object> map) {
        if (map == null)
            activity.showToast("Network error!");

        else if (Boolean.FALSE.equals(map.get(HttpServerConnection.responseStatusKey)))
            activity.showToast("Server error!");

        else
            activity.showToast("Your feedback is received successfully");
    }

    private void remakeUI () {
        OrderingActivity activity = (OrderingActivity) this.activity;
        activity.hideCustomerFeedbackShareLoadingTextview();
        activity.enableCustomerShareFeedbackButton();
        activity.setBlankCustomerShareFeedbackInput();
    }


    private Request getRequest (CustomerFeedbackPost customerFeedbackPost) {

        Gson gson = new Gson();

        String json = gson.toJson(customerFeedbackPost);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        return new Request
                .Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.postCustomerFeedback)
                .post(body)
                .build();
    }



}
