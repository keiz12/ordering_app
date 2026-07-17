package com.example.orderingapp.customerFeedback.server;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.customerFeedback.CustomerFeedbackActivity;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;

public class DeleteCustomFeedbackServer {

    private final ShowToastFromBgThread activity;
    private final String uuid;
    private final LinearLayout parentLayout;
    private final View view;

    public DeleteCustomFeedbackServer(ShowToastFromBgThread activity, String uuid, LinearLayout parentLayout, View view) {
        this.activity = activity;
        this.uuid = uuid;
        this.parentLayout = parentLayout;
        this.view = view;
    }

    public void run () {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            Request request = getRequest();
            runAfterResponse(new HttpServerConnection().getResponseMap(request));
        });

        executorService.shutdown();
    }

    private Request getRequest () {

        String credentials = new HttpServerConnection().getHttpBasicCredentials((Context) activity);

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.deleteCustomerFeedback+"/"+uuid)
                .addHeader("Authorization", credentials)
                .delete()
                .build();
    }

    private void runAfterResponse (HashMap<String, Object> map) {

        toastMessageShow(map);
        remakeUI(map);
    }

    private void toastMessageShow (HashMap<String, Object> map) {

        if (map == null)
            activity.showToast("Network error!");

        else if (Boolean.FALSE.equals(map.get(HttpServerConnection.responseStatusKey)))
            activity.showToast("Server error!");

        else
            activity.showToast("Feedback is deleted successfully");
    }

    private void remakeUI (HashMap<String, Object> map) {

        if (map != null && !Boolean.FALSE.equals(map.get(HttpServerConnection.responseStatusKey)))
            ((CustomerFeedbackActivity) activity).removeAView(parentLayout, view);

        ((CustomerFeedbackActivity) activity).hideLoadingTest();
    }
}
