package com.example.orderingapp.order.madeOrders;

import com.example.orderingapp.connection.http.HttpServerConnection;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class OrderPaymentCheckServer {

    public boolean isOrderPaid (String uuid) {

        String url = HttpServerConnection.httpBaseURL + HttpServerConnection.isOrderPaid+"/"+uuid;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = new HttpServerConnection().sendRequestDeprecated(request);
        boolean bool = false;
        try {
            bool = Boolean.parseBoolean(response.body().string().trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bool;
    }
}
