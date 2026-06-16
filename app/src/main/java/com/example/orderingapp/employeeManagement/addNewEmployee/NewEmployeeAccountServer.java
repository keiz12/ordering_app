package com.example.orderingapp.employeeManagement.addNewEmployee;

import android.content.Context;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Employee;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewEmployeeAccountServer {

    public void addNewEmployee (Context context, Employee employee) {

        ExecutorService service = Executors.newFixedThreadPool(1);

        service.submit(() -> {
            serverCall(context, employee);
        });

        service.shutdown();
        service.close();
    }

    private void serverCall ( Context context, Employee employee)
    {

        var connection = new HttpServerConnection();

        String basicsCredentials = connection.getHttpBasicCredentials(context);

        if (basicsCredentials == null)
        {
            contextDisplayMessage((NewEmployeeAccountActivity) context,"User credentials not found");
            return;
        }

        String employeeJSONString = getEmployeeJSONString(employee);

        Request request = getRequest(employeeJSONString, basicsCredentials);

        Response response = connection.sendRequest(request);

        if (response == null || !response.isSuccessful())
            contextDisplayMessage((NewEmployeeAccountActivity) context, employee.getUsername()+" account creation failed\nTry again letter");
        else if (response.isSuccessful())
            contextDisplayMessage((NewEmployeeAccountActivity) context, employee.getUsername()+" account added successfully");
        else
            contextDisplayMessage((NewEmployeeAccountActivity) context, "Unknown error occurred\nContact the developer if the error persists");
    }



    private String getEmployeeJSONString (Employee employee) {
        Gson gson = new Gson();
        return gson.toJson(employee);
    }

    private Request getRequest (String employeeJSONString, String basicsCredentials)
    {
        return new Request
                .Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.addEmployee)
                .post(RequestBody.create(employeeJSONString.getBytes(), MediaType.parse("application/json")))
                .addHeader("Authorization", basicsCredentials)
                .build();
    }

    private void contextDisplayMessage(NewEmployeeAccountActivity context, String s) {
        context.contextDisplayMessage(s);
    }

}
