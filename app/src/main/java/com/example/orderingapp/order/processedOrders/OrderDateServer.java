package com.example.orderingapp.order.processedOrders;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import okhttp3.Request;
import okhttp3.Response;

public class OrderDateServer implements ClientAuth {

    public void getAllDates(ProcessedOrdersActivity activity) {

        if (!authorize(activity))
            return;

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            Request request = getRequest(activity);
            HashMap<String, Object> responseMap = new HttpServerConnection().getResponseMap(request);
            afterResponseResult(responseMap, activity);
        });
        service.shutdown();
    }

    private Request getRequest(ProcessedOrdersActivity activity) {

        HttpServerConnection connection = new HttpServerConnection();
        String authHeader = connection.getHttpBasicCredentials(activity);

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getCreatedAtOrder)
                .get()
                .addHeader("Authorization", authHeader)
                .build();
    }

    private void afterResponseResult(HashMap<String, Object> responseMap, ProcessedOrdersActivity activity) {
        if (responseMap != null && !responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE))
            populateUIWithData(responseMap, activity);

        else
            activity.showToast("Failed to fetch dates");

    }

    private void populateUIWithData(HashMap<String, Object> responseMap, ProcessedOrdersActivity activity) {
        try {
            String responseData = responseMap.get(HttpServerConnection.responseBodyKey).toString();
            Gson gson = HttpServerConnection.getLocalDateTimeAdapterGson();
            Type listType = new TypeToken<List<LocalDateTime>>() {}.getType();
            List<LocalDateTime> localDateTimeList = gson.fromJson(responseData, listType);
            activity.onDatesReceived(getFormattedDates(localDateTimeList));
        }
        catch (Exception e) {
            activity.showToast("Failed to parse dates");
        }
    }

    private List<String> getFormattedDates (List<LocalDateTime> localDateTimeList) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return localDateTimeList.stream()
                .map(date -> date.format(formatter))
                .distinct()
                .collect(Collectors.toList());
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

    private boolean isEmployeeValid(Employee employee) {
        boolean isEmployeeStaff = employee.getRole().equalsIgnoreCase("staff");
        boolean isEmployeeManager = employee.getRole().equalsIgnoreCase("manager");
        boolean isEmployeeBoss = employee.getRole().equalsIgnoreCase("boss");
        return isEmployeeStaff || isEmployeeManager || isEmployeeBoss;
    }
}
