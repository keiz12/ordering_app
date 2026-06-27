package com.example.orderingapp.productManagement.products;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Class responsible for retrieving products from the server.
 * Adheres to SRP by separating network logic from UI logic.
 */
public class ProductsActivityServer {

    private final ExecutorService executorService;
    private final Handler mainHandler;

    public ProductsActivityServer() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface ProductsCallback {
        void onProductsLoaded(List<Product> products);
    }

    /**
     * Retrieves all products from the server asynchronously.
     *
     * @param context  The application context.
     * @param callback The callback to be invoked when products are loaded.
     */
    public void getProducts(Context context, ProductsCallback callback) {
        executorService.execute(() ->
        {
            List<Product> products = fetchProductsSync(context);

            mainHandler.post(() -> callback.onProductsLoaded(products));
        });
        shutdown();
    }

    private List<Product> fetchProductsSync(Context context)
    {
        Gson gson = new Gson();
        HttpServerConnection connection = new HttpServerConnection();
        Request request = buildRequest();

        Response response = connection.sendRequestDeprecated(request);

        return (response == null || !response.isSuccessful()) ? new ArrayList<>() : parseResponseBody(gson, response.body());
    }

    private Request buildRequest() {

        Request.Builder builder = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.getAllProducts)
                .get();

        return builder.build();
    }

    private List<Product> parseResponseBody(Gson gson, ResponseBody body)
    {
        if (body == null) return new ArrayList<>();

        String content = null;
        try {
            content = body.string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Type listType = new TypeToken<ArrayList<Product>>() {}.getType();

        return gson.fromJson(content, listType);
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
