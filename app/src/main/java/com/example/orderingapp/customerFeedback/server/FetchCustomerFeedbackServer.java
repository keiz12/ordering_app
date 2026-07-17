package com.example.orderingapp.customerFeedback.server;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.customerFeedback.CustomerFeedbackActivity;
import com.example.orderingapp.dto.CustomerFeedback;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;

public class FetchCustomerFeedbackServer implements ClientAuth {

    private final CustomerFeedbackActivity activity;

    public FetchCustomerFeedbackServer(CustomerFeedbackActivity activity) {
        this.activity = activity;
    }

    public void run() {

        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() ->
        {
            if (!authorize(activity)) {
                remakeUI();
                return;
            }

            Request request = getRequest();
            runAfterResponse(new HttpServerConnection().getResponseMap(request));
        });

        service.shutdown();
    }

    private void runAfterResponse(HashMap<String, Object> map) {
        if (map == null || map.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE)) {
            activity.showToast("Failed to fetch feedback");
            remakeUI();
            return;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<CustomerFeedback>>() {}.getType();
        List<CustomerFeedback> feedbacks = gson.fromJson(String.valueOf(map.get(HttpServerConnection.responseBodyKey)), listType);

        activity.setCustomerFeedbacks(feedbacks);
        remakeUI();
    }

    private Request getRequest() {
        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getCustomerFeedback)
                .header("Authorization", new HttpServerConnection().getHttpBasicCredentials(activity))
                .get()
                .build();
    }

    private void remakeUI () {
        activity.hideLoadingTest();
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {
        var employee = new EmployeeDatabase((Context) toast).readEmployee();

        if (employee == null) {
            toast.showToast(ClientAuth.message);
            return false;
        }

        String role = employee.getRole();
        boolean isAuthorized = role.equalsIgnoreCase("boss") || role.equalsIgnoreCase("manager");

        if (!isAuthorized)
            toast.showToast(ClientAuth.message);

        return isAuthorized;
    }
}
