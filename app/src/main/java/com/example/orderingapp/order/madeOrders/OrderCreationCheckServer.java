package com.example.orderingapp.order.madeOrders;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Order;
import com.example.orderingapp.gson.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

import okhttp3.Request;
import okhttp3.Response;

public class OrderCreationCheckServer {

    public Order checkOrderCreation(String uuid) {

        String url = HttpServerConnection.httpBaseURL + HttpServerConnection.orderID + uuid;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = new HttpServerConnection().sendRequestDeprecated(request);

        if (response == null || !response.isSuccessful())
            return null;

        return getOrder(response);
    }

    private Order getOrder(Response response) {
        try {

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            String body = response.body().string().trim();

            if (body.equalsIgnoreCase("null") || body.isEmpty())
                return null;

            return gson.fromJson(body, Order.class);

        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }
}
