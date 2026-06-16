package com.example.orderingapp.connection.http;

import android.content.Context;

import com.example.orderingapp.connection.Connection;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpServerConnection {

    public static String httpBaseURL = "http://"+ Connection.ipV4+":"+Connection.port;
    public static String allTest = "/all/get/test";
    public static String addEmployee = "/secure/boss/create/user";
    public static String getAllEmployees = "/secure/boss/all/users";
    public static String updateEmployee = "/secure/boss/user";
    public static String deleteEmployee = "/secure/boss/user";

    private OkHttpClient client = new OkHttpClient(new OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES));

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

    public Response sendRequest(Request request) {
        try {
            return client.newCall(request).execute();
        }
        catch (Exception e) {
            return null;
        }
    }

}
