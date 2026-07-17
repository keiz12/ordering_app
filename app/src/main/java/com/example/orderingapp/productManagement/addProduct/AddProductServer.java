package com.example.orderingapp.productManagement.addProduct;

import android.content.Context;
import android.net.Uri;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.example.orderingapp.dto.Product;
import com.example.orderingapp.dto.image.IMGBBResponse;
import com.example.orderingapp.dto.image.IMGData;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.productManagement.IMGBB.IMGBBConnector;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddProductServer implements ClientAuth {

    public void saveProduct(AddProductActivity activity, Product product, List<Uri> imageUris) {
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.execute(() -> serverCall(activity, product, imageUris));
        service.shutdown();
    }

    private void serverCall(AddProductActivity activity, Product product, List<Uri> imageUris)
    {
        if (!authorize(activity)) {
            remakeUI(activity);
            return;
        }

        HttpServerConnection connection = new HttpServerConnection();
        String basicCredentials = connection.getHttpBasicCredentials(activity);

        if (basicCredentials == null) {
            activity.showToast("User credentials not found");
            remakeUI(activity);
            return;
        }

        boolean b = uploadToImgBB(activity, product, imageUris, connection);
        if (b)
            serverPost(product, basicCredentials, connection, activity);
    }

    private boolean uploadToImgBB (AddProductActivity activity, Product product, List<Uri> imageUris, HttpServerConnection connection)
    {
        IMGBBConnector connector = new IMGBBConnector();

        for (Uri uri : imageUris)
        {
            IMGBBResponse imgResponse = connector.uploadToImgBB(activity, uri, connection);

            if (imgResponse == null || imgResponse.getData() == null) {
                activity.showToast("Failed to upload an image to ImgBB");
                remakeUI(activity);
                return false;
            }
            populateProductURLs(product, imgResponse.getData());
        }
        return true;
    }

    private void populateProductURLs (Product product, IMGData data)
    {
        String url = data.getUrl();
        String deleteUrl = data.getDelete_url();
        product.getImagePathToDeletePath().put(url, deleteUrl);
    }

    private void serverPost (Product product, String basicCredentials, HttpServerConnection connection, AddProductActivity activity)
    {
        Gson gson = new Gson();
        String productJson = gson.toJson(product);

        Request request = new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.postProduct)
                .post(RequestBody.create(productJson, MediaType.parse("application/json")))
                .addHeader("Authorization", basicCredentials)
                .build();

        Response response = connection.sendRequestDeprecated(request);

        if (response != null && response.isSuccessful()) {
            activity.showToast("Product saved successfully");
        } else {
            activity.showToast("An error occurred while saving the product");
        }
        remakeUI(activity);
    }

    private void remakeUI (AddProductActivity activity) {
        activity.setLoadingToInvisible();
        activity.setDeleteButtonEnabled();
        activity.setSubmitButtonEnabled();
    }

    @Override
    public boolean authorize (ShowToastFromBgThread context) {

        boolean bool = new EmployeeDatabase((Context) context).readEmployee().getRole().equalsIgnoreCase("boss");

        if (!bool)
            context.showToast(ClientAuth.message);

        return bool;
    }
}
