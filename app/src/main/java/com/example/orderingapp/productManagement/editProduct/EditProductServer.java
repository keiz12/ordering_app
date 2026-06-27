package com.example.orderingapp.productManagement.editProduct;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.example.orderingapp.dto.Product;
import com.example.orderingapp.dto.ProductUpdate;
import com.example.orderingapp.dto.image.IMGBBResponse;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.productManagement.IMGBB.IMGBBConnector;
import com.example.orderingapp.toast.Toasts;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProductServer implements ClientAuth {
    private final List<String> oldRemovedImages;
    private final List<String> newImages;
    private final String originalName;
    private final Context context;
    private final String name;
    private final double price;
    private final String description;

    public EditProductServer(List<String> oldRemovedImages, List<String> newImages, String originalName, Context context, String name, double price, String description) {
        this.oldRemovedImages = oldRemovedImages;
        this.newImages = newImages;
        this.originalName = originalName;
        this.context = context;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public void execute() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(this::runUpdateProcess);
        executor.shutdown();
    }

    private void runUpdateProcess() {

        if (!authorize((ShowToastFromBgThread) context))
            return;

        HashMap<String, String> uploadedImages = uploadImages();
        ProductUpdate productUpdate = createProductUpdate(uploadedImages);
        sendRequest(productUpdate);
    }

    private HashMap<String, String> uploadImages() {

        HashMap<String, String> uploadedImages = new HashMap<>();

        if (newImages == null || newImages.isEmpty())
            return uploadedImages;


        IMGBBConnector connector = new IMGBBConnector();

        HttpServerConnection connection = new HttpServerConnection();

        for (String imageUriString : newImages)
        {
            IMGBBResponse response = connector.uploadToImgBB(context, Uri.parse(imageUriString), connection);

            if (response != null && response.getData() != null)
                uploadedImages.put(response.getData().getUrl(), response.getData().getDelete_url());
            else
                showToast("Failed to upload an image to ImgBB");
        }
        return uploadedImages;
    }

    private ProductUpdate createProductUpdate(HashMap<String, String> uploadedImages) {

        Product newProduct = new Product();

        newProduct.setName(name);
        newProduct.setPrice(price);
        newProduct.setDescription(description);
        newProduct.setImagePathToDeletePath(uploadedImages);
        newProduct.setImageURLPath(oldRemovedImages);

        ProductUpdate productUpdate = new ProductUpdate();
        productUpdate.setOldProductName(originalName);
        productUpdate.setNewProduct(newProduct);
        return productUpdate;
    }

    private void sendRequest(ProductUpdate productUpdate) {

        HttpServerConnection connection = new HttpServerConnection();
        String credentials = connection.getHttpBasicCredentials(context);

        if (credentials == null) {
            showToast("User credentials not found");
            return;
        }

        Gson gson = new Gson();
        String json = gson.toJson(productUpdate);

        Request request = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.putProduct)
                .put(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Authorization", credentials)
                .build();

        Response response = connection.sendRequestDeprecated(request);

        if (response == null || !response.isSuccessful())
            showToast("An error occurred while updating the product");
        else {
            showToast("Product updated successfully");
            ((Activity) context).finish();
        }
    }

    private void showToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> Toasts.showShortToast(context, message));
        }
    }

    @Override
    public boolean authorize (ShowToastFromBgThread context) {

        boolean bool = new EmployeeDatabase(this.context).readEmployee().getRole().equalsIgnoreCase("boss");

        if (!bool)
            context.showToast(ClientAuth.message);

        return bool;
    }
}
