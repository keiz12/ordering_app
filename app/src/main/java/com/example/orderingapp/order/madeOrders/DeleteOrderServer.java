package com.example.orderingapp.order.madeOrders;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Order;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DeleteOrderServer implements ClientAuth {

    public void deleteOrder(MadeOrdersActivity activity, Order order) {

        if (!authorize(activity))
            return;

        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {

            HttpServerConnection connection = new HttpServerConnection();
            String authHeader = connection.getHttpBasicCredentials(activity);

            Request request = getRequest(authHeader, order);

            HashMap<String, Object> responseMap = connection.getResponseMap(request);

            runAfterResponse(activity, responseMap);
        });
        service.shutdown();
    }

    private Request getRequest (String authHeader, Order order) {
        Gson gson = new Gson();
        String orderJson = gson.toJson(order);
        RequestBody body = RequestBody.create(orderJson, MediaType.parse("application/json"));

        Request.Builder builder = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.deleteOrder)
                .delete(body);

        if (authHeader != null) {
            builder.addHeader("Authorization", authHeader);
        }

        return builder.build();
    }

    private void runAfterResponse (MadeOrdersActivity activity, HashMap<String, Object> responseMap)
    {
        if (responseMap == null || responseMap.get(HttpServerConnection.responseStatusKey).equals(Boolean.FALSE)) {
            activity.showToast("Failed to delete order");
            return;
        }

        activity.showToast("Order deleted successfully");
        activity.runOnUiThread(activity::finish);
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {
        return ((MadeOrdersActivity) toast).authorize(toast);
    }
}
