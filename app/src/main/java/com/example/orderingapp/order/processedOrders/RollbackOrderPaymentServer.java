package com.example.orderingapp.order.processedOrders;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.example.orderingapp.order.madeOrders.MadeOrdersActivity;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RollbackOrderPaymentServer implements ClientAuth {

    public void rollbackOrderPayment(MadeOrdersActivity activity, String uuid) {

        if (!authorize(activity))
            return;

        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {

            HttpServerConnection connection = new HttpServerConnection();
            String authHeader = connection.getHttpBasicCredentials(activity);

            Request request = getRequest(authHeader, uuid);

            HashMap<String, Object> responseMap = connection.getResponseMap(request);

            runAfterResponse(activity, responseMap);
        });
        service.shutdown();
    }

    private Request getRequest (String authHeader, String uuid) {
        RequestBody body = RequestBody.create("", MediaType.parse("application/json"));

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.orderPaymentRollback + uuid)
                .patch(body)
                .addHeader("Authorization", authHeader)
                .build();
    }

    private void runAfterResponse (MadeOrdersActivity activity, HashMap<String, Object> responseMap)
    {
        if (responseMap == null || responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE)) {
            activity.showToast("Failed to rollback order payment");
            return;
        }

        activity.showToast("Order payment rolled back successfully");

        activity.updateUI(() -> activity.makePaymentRollbackButtonGone(true));
        activity.updateUI(() -> activity.setConfirmOrderPaymentButtonFunction(true));
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {
        var employee = new EmployeeDatabase((Context) toast).readEmployee();

        if (employee == null) {
            toast.showToast(ClientAuth.message);
            return false;
        }

        String role = employee.getRole();
        boolean isAuthorized = role.equalsIgnoreCase("staff");

        if (!isAuthorized)
            toast.showToast(ClientAuth.message);

        return isAuthorized;
    }
}
