package com.example.orderingapp.employeeManagement.myEmployees;

import android.content.Context;
import android.util.Log;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Employee;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class EmployeesServer {

    public List<Employee> getEmployees (Context context)
    {
        return run(context);
    }


    private List<Employee> run (Context context) {

        Gson gson = new Gson();

        HttpServerConnection connection = new HttpServerConnection();

        Request request = getRequest(connection,context);

        try (Response response = connection.sendRequest(request))
        {
            if (response == null || !response.isSuccessful())
                throw new IOException();

            return extractEmployeesFromResponseBody (gson, response.body() );

        } catch (IOException e) {
            Log.e("EmployeesServer", "Network error", e);
        }

        return new ArrayList<>();
    }


    public Request getRequest (HttpServerConnection connection, Context context)
    {
        String credentials = connection.getHttpBasicCredentials(context);
        Request.Builder builder = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getAllEmployees)
                .get();

        if (credentials != null) {
            builder.addHeader("Authorization", credentials);
        }

        // Some servers might require this to avoid redirects on auth failure
        // builder.addHeader("X-Requested-With", "XMLHttpRequest");

        return builder.build();
    }

    private List<Employee> extractEmployeesFromResponseBody (Gson gson, ResponseBody body) throws IOException
    {
        if (body == null)
            return new ArrayList<>();

        String content = body.string();
        Type listType = new TypeToken<ArrayList<Employee>>() {}.getType();
        return gson.fromJson(content, listType);

    }
}
