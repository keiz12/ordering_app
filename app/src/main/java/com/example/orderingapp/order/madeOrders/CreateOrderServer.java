package com.example.orderingapp.order.madeOrders;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Order;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateOrderServer {

    public void makeOrder(ShowToastFromBgThread activity, Order order)
    {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {


            Request request = getRequest(order);

            Response response = new HttpServerConnection().sendRequestDeprecated(request);

            afterResponseResult(response, activity);
        });
        service.shutdown();
    }

    private Request getRequest (Order order)
    {
        Gson gson = new Gson();
        String orderJson = gson.toJson(order);

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.createOrder)
                .post(RequestBody.create(orderJson, MediaType.parse("application/json")))
                .build();
    }

    private void afterResponseResult (Response response, ShowToastFromBgThread activity) {

        if (response != null && response.isSuccessful()) {
            // call web sockets here
            madeOrderActivityAct(activity);
        }
        else
            activity.showToast("Failed to create order");
    }

    private void madeOrderActivityAct (ShowToastFromBgThread activity)
    {
        if (!(activity instanceof MadeOrdersActivity))
            return;



        MadeOrdersActivity madeOrdersActivity = (MadeOrdersActivity) activity;

        activity.showToast("Order created successfully");

        madeOrdersActivity.updateUI(() -> madeOrdersActivity.makeOrderSubmitButtonGone(true));
        madeOrdersActivity.updateUI(() -> madeOrdersActivity.setOrderDeleteButtonFunction(true));

    }
}
