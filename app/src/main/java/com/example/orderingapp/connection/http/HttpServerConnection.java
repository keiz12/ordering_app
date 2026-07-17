package com.example.orderingapp.connection.http;

import android.content.Context;

import com.example.orderingapp.connection.Connection;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.gson.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpServerConnection {

    public static String httpBaseURL = "http://"+ Connection.ipV4+":"+Connection.port;
    public static String allTest = "/all/get/test";
    public static String apiKey = "/secure/boss/android/key";
    public static String validateApiKey = "/validate";
    public static String countApiKey = "/count";
    public static String addEmployee = "/secure/boss/create/user";
    public static String getAllEmployees = "/secure/boss/all/users";
    public static String getEmployee = "/secure/user";
    public static String updateEmployee = "/secure/boss/user";
    public static String deleteEmployee = "/secure/boss/user";
    public static String postProduct = "/secure/boss/product";
    public static String userLogin = "/secure/user/login";
    public static String putProduct = "/secure/boss/product";
    public static String deleteProduct = "/secure/boss/product";
    public static String getAllProducts = "/all/product";
    public static String createOrder = "/all/orders";
    public static String deleteOrder = "/all/orders";
    public static String orderID = "/all/orders/order/";
    public static String isOrderPaid = "/all/orders";
    public static String getOrdersByDate = "/secure/order/by-date/";
    public static String postStaffProcessedOrder = "/staff-orders/process/";
    public static String getStaffProcessedOrder = "/secure/order/";
    public static String orderPaymentConfirm = "/staff-orders/pay/confirm/";
    public static String orderPaymentRollback = "/staff-orders/pay/rollback/";
    public static String getCreatedAtOrder = "/secure/all/order/date";
    public static String getTotal = "/secure/statistic/total";
    public static String getStatistic = "/secure/statistics";
    public static String postCustomerFeedback = "/all/post/customer-feedback";
    public static String getCustomerFeedback = "/secure/get/all/customer-feedback";
    public static String deleteCustomerFeedback = "/secure/boss/customer-feedback";
    public static String imgBB = "https://api.imgbb.com/1/upload";
    public static String imgBBApiKey = "76c232e2b61eebbb603017446eccdf9d";

    public static String responseStatusKey = "status";
    public static String responseBodyKey = "body";

    private OkHttpClient client = new OkHttpClient(new OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.MINUTES)
            .connectTimeout(30, TimeUnit.SECONDS));

    public static Gson getLocalDateTimeAdapterGson () {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public String getHttpBasicCredentials (Context context) {

        Employee employee = new EmployeeDatabase(context).readEmployee();

        if (employee == null)
            return null;

        String rawCredentials = employee.getUsername() + ":" + employee.getPassword();

        // Get bytes using UTF-8 to support special characters safely
        byte[] credentialBytes = rawCredentials.getBytes(StandardCharsets.UTF_8);

        // Perform Base64 encoding
        byte[] encodedBytes = Base64.encode(credentialBytes, Base64.NO_WRAP);

        // Correctly convert the encoded byte array into a String and add "Basic " prefix
        return "Basic " + new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public String getHttpBasicCredentials (String username, String password) {

        String rawCredentials = username + ":" + password;

        // Get bytes using UTF-8 to support special characters safely
        byte[] credentialBytes = rawCredentials.getBytes(StandardCharsets.UTF_8);

        // Perform Base64 encoding
        byte[] encodedBytes = Base64.encode(credentialBytes, Base64.NO_WRAP);

        // Correctly convert the encoded byte array into a String and add "Basic " prefix
        return "Basic " + new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public HashMap<String,Object> getResponseMap(Request request) {

        try (Response response = client.newCall(request).execute())
        {
            HashMap<String, Object> map = new HashMap<>();

            map.put(responseBodyKey, response.body().string());
            map.put(responseStatusKey, response.isSuccessful());

            return map;
        }
        catch (IOException e) {
            return null;
        }
    }

    @Deprecated
    public Response sendRequestDeprecated(Request request) {
        try {
            return client.newCall(request).execute();
        }
        catch (Exception e) {
            return null;
        }
    }
}
