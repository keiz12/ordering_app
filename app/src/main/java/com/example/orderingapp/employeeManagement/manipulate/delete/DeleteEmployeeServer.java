package com.example.orderingapp.employeeManagement.manipulate.delete;

import android.content.Context;
import android.util.Log;

import com.example.orderingapp.connection.http.HttpServerConnection;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class DeleteEmployeeServer {

    public Response deleteEmployee(Context context, int id) {
        return run(context, id);
    }

    private Response run(Context context, int id) {
        HttpServerConnection connection = new HttpServerConnection();
        Request request = getRequest(connection, context, id);

        try {
            Response response = connection.sendRequestDeprecated(request);

            if (response == null || !response.isSuccessful())
                throw new IOException();

            return response;

        } catch (IOException e) {
            Log.e("DeleteEmployeeServer", "Network error", e);
        }

        return null;
    }

    public Request getRequest(HttpServerConnection connection, Context context, int id)
    {
        String credentials = connection.getHttpBasicCredentials(context);

        Request.Builder builder = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.deleteEmployee + "/" + id)
                .delete();

        if (credentials != null) {
            builder.addHeader("Authorization", credentials);
        }

        return builder.build();
    }
}
