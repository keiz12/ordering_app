package com.example.orderingapp.statistics;

import android.content.Context;
import android.net.Uri;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.statistic.StatisticRequest;
import com.example.orderingapp.dto.statistic.StatisticResponse;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class StatisticServer implements ClientAuth {

    public void getStatistics(StatisticsActivity activity, StatisticRequest statisticRequest) {
        if (!authorize(activity)) {
//            activity.hideLoading();
            return;
        }

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            HttpServerConnection connection = new HttpServerConnection();
            String authHeader = connection.getHttpBasicCredentials(activity);

            Request request = getRequest(authHeader, statisticRequest);
            HashMap<String, Object> responseMap = connection.getResponseMap(request);

            runAfterResponse(activity, responseMap);
        });
        service.shutdown();
    }

    private Request getRequest(String authHeader, StatisticRequest statisticRequest) {

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getStatistic + "?startingDate="+statisticRequest.getStartingDate()+"&endingDate="+statisticRequest.getEndingDate()+"&statisticType="+statisticRequest.getStatisticType())
                .addHeader("Authorization", authHeader)
                .build();
    }

    private void runAfterResponse(StatisticsActivity activity, HashMap<String, Object> responseMap) {

        if (responseMap == null || responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE)) {
            activity.showToast("Failed to fetch statistics");
//            activity.hideLoading();
            return;
        }

        String body = (String) responseMap.get(HttpServerConnection.responseBodyKey);
        Gson gson = new Gson();
        StatisticResponse statisticsResponse = gson.fromJson(body, StatisticResponse.class);

        activity.runOnUiThread(() -> activity.updateStatisticsUI(statisticsResponse));
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
