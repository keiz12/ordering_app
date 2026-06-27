package com.example.orderingapp.order.processedOrders;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.StaffProcessedOrder;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;

public class GetStaffProcessedOrderServer implements ClientAuth {

    public void getStaffProcessedOrder(ProcessedOrdersActivity activity, String uuid) {

        if (!authorize(activity))
            return;

        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {

            HttpServerConnection connection = new HttpServerConnection();
            String authHeader = connection.getHttpBasicCredentials(activity);

            Request request = getRequest(authHeader, uuid);

            HashMap<String, Object> responseMap = connection.getResponseMap(request);

            runAfterResponse(activity, responseMap, uuid);
        });
        service.shutdown();
    }

    private Request getRequest (String authHeader, String uuid) {
        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getStaffProcessedOrder + uuid)
                .get()
                .addHeader("Authorization", authHeader)
                .build();
    }

    private void runAfterResponse (ProcessedOrdersActivity activity, HashMap<String, Object> responseMap, String uuid)
    {
        if (responseMap == null || responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE)) {
            activity.showToast("Failed to fetch processed order details");
            return;
        }

        Gson gson = HttpServerConnection.getLocalDateTimeAdapterGson();
        StaffProcessedOrder staffProcessedOrder = gson.fromJson(responseMap.get(HttpServerConnection.responseBodyKey).toString(), StaffProcessedOrder.class);
        activity.startMadeOrdersActivity(uuid, staffProcessedOrder);
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {
        var employee = new EmployeeDatabase((Context) toast).readEmployee();

        if (employee == null) {
            toast.showToast(ClientAuth.message);
            return false;
        }

        String role = employee.getRole();
        boolean isAuthorized = role.equalsIgnoreCase("staff") || role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("boss");

        if (!isAuthorized) {
            toast.showToast(ClientAuth.message);
        }

        return isAuthorized;
    }
}
