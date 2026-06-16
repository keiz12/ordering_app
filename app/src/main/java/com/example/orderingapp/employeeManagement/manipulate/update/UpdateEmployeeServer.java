package com.example.orderingapp.employeeManagement.manipulate.update;

import android.content.Context;
import android.util.Log;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.UserUpdateRequest;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateEmployeeServer {

    public Response updateEmployee (Context context, UserUpdateRequest updateRequest)
    {
        return run(context, updateRequest);
    }


    private Response run (Context context, UserUpdateRequest updateRequest) {

        HttpServerConnection connection = new HttpServerConnection();

        Request request = getRequest(connection, context, updateRequest);

        try
        {
            Response response = connection.sendRequest(request);

            if (response == null || !response.isSuccessful())
                throw new IOException();

            return response;

        } catch (IOException e) {
            Log.e("ManipulateEmployeeServer", "Network error", e);
        }

        return null;
    }


    public Request getRequest (HttpServerConnection connection, Context context, UserUpdateRequest updateRequest)
    {
        String credentials = connection.getHttpBasicCredentials(context);
        Gson gson = new Gson();
        String json = gson.toJson(updateRequest);

        Request.Builder builder = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.updateEmployee)
                .put(RequestBody.create(json, MediaType.parse("application/json")));

        if (credentials != null) {
            builder.addHeader("Authorization", credentials);
        }

        return builder.build();
    }
}
