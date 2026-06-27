package com.example.orderingapp.statistics;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
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

public class StatisticTotalServer implements ClientAuth {

    private final StatisticsActivity activity;



    public StatisticTotalServer(StatisticsActivity activity) {
        this.activity = activity;
    }

    public void run ()
    {
//        activity.initNewBgThread();

        if (!authorize(activity)) {
            activity.windUpBgThread();
            return;
        }

        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() ->
        {
            Request request = getRequest();
            runAfterResponse( new HttpServerConnection().getResponseMap(request) );

//            try {
//                Thread.sleep(5_000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

//            activity.windUpBgThread();

        });

        service.close();
    }

    private void runAfterResponse (HashMap<String, Object> map) {


        if (map == null || map.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE)) {
            activity.showToast("Failed to fetch statistics");
//            activity.hideLoading();
            return;
        }

        Gson gson = new Gson();

        // Define the exact parameterized list type
        Type listType = new TypeToken<List<Integer>>(){}.getType();

        // Deserialize cleanly using the type definition
        List<Integer> totalsList = gson.fromJson(String.valueOf( map.get(HttpServerConnection.responseBodyKey) ), listType);

        activity.setTotals(totalsList.get(0), totalsList.get(1), totalsList.get(2));
    }

    private Request getRequest () {
         return new Request
                 .Builder()
                 .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getTotal)
                 .header("Authorization", new HttpServerConnection().getHttpBasicCredentials(activity))
                 .get()
                 .build();
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
