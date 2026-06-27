package com.example.orderingapp.productManagement.products;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Product;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Class responsible for deleting a product from the server.
 * Adheres to SRP by separating network logic from UI logic.
 */
public class ProductsActivityServerDelete implements ClientAuth {

    private final ExecutorService executorService;
    private final Handler mainHandler;

    public ProductsActivityServerDelete() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface DeleteCallback {
        void onProductDeleted(boolean success);
    }

    /**
     * Deletes a product from the server asynchronously.
     *
     * @param context  The application context.
     * @param product  The product to be deleted.
     * @param callback The callback to be invoked when the deletion is completed.
     */
    public void deleteProduct(Context context, Product product, DeleteCallback callback) {
        executorService.execute(() -> {
            boolean success = performDeletionSync(context, product);
            mainHandler.post(() -> callback.onProductDeleted(success));
        });
        executorService.shutdown();
    }

    private boolean performDeletionSync(Context context, Product product) {

        if (!authorize((ShowToastFromBgThread) context))
            return false;

        HttpServerConnection connection = new HttpServerConnection();
        String credentials = connection.getHttpBasicCredentials(context);

        Request.Builder builder = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.deleteProduct + "/" + Uri.encode(product.getName()))
                .delete();

        if (credentials != null)
            builder.addHeader("Authorization", credentials);


        Request request = builder.build();
        Response response = connection.sendRequestDeprecated(request);

        return response != null && response.isSuccessful();
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {
        boolean bool = new EmployeeDatabase((Context) toast).readEmployee().getRole().equalsIgnoreCase("boss");

        if (!bool)
            toast.showToast(ClientAuth.message);

        return bool;
    }
}
