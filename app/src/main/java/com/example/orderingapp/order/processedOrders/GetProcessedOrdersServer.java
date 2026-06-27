package com.example.orderingapp.order.processedOrders;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.dto.OrderProcessIndicator;
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

public class GetProcessedOrdersServer implements ClientAuth {

    public void fetchProcessedOrders(ProcessedOrdersActivity activity, String date) {

        if (!authorize(activity))
            return;

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            Request request = getRequest(activity, date);
            HashMap<String, Object> responseMap = new HttpServerConnection().getResponseMap(request);
            afterResponseResult(responseMap, activity);
        });
        service.shutdown();
    }

    private Request getRequest(ProcessedOrdersActivity activity, String date) {

        HttpServerConnection connection = new HttpServerConnection();
        String authHeader = connection.getHttpBasicCredentials(activity);

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getOrdersByDate+date)
                .get()
                .addHeader("Authorization", authHeader)
                .build();
    }

    private void afterResponseResult(HashMap<String, Object> responseMap, ProcessedOrdersActivity activity) {

        if (responseMap != null && responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.TRUE))
            populateUIWithData(responseMap, activity);
        else
            activity.showToast("Failed to fetch orders");
    }

    private void populateUIWithData (HashMap<String, Object> responseMap, ProcessedOrdersActivity activity)
    {
        try {
            String responseData = responseMap.get(HttpServerConnection.responseBodyKey).toString();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OrderProcessIndicator>>() {}.getType();
            List<OrderProcessIndicator> orders = gson.fromJson(responseData, listType);
            activity.onProcessedOrderReceived(orders);
        }
        catch (Exception e) {
            activity.showToast("Failed to parse data");
        }
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {

        var employee = new EmployeeDatabase((Context) toast).readEmployee();

        if (employee == null) {
            toast.showToast(ClientAuth.message);
            return false;
        }

        if (isEmployeeValid(employee))
            return true;

        toast.showToast(ClientAuth.message);
        return false;

    }

    private boolean isEmployeeValid (Employee employee)
    {
        boolean isEmployeeStaff = employee.getRole().equalsIgnoreCase("staff");
        boolean isEmployeeManager = employee.getRole().equalsIgnoreCase("manager");
        boolean isEmployeeBoss = employee.getRole().equalsIgnoreCase("boss");
        return isEmployeeStaff || isEmployeeManager || isEmployeeBoss;
    }
}
