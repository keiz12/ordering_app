package com.example.orderingapp.productManagement.IMGBB;

import android.content.Context;
import android.net.Uri;
import android.util.*;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.image.IMGBBResponse;
import com.google.gson.Gson;

import java.io.*;

import okhttp3.*;

public class IMGBBConnector {

    public IMGBBResponse uploadToImgBB (Context context, Uri uri, HttpServerConnection connection) {

        byte[] imageBytes = getBytes(context, uri);
        if (imageBytes == null) return null;

        // 2. Build URL forcing the key as a Query Parameter (Required by ImgBB)
        HttpUrl url = HttpUrl.parse(HttpServerConnection.imgBB).newBuilder()
                .addQueryParameter("key", HttpServerConnection.imgBBApiKey)
                .build();

        // 3. Create binary RequestBody directly from bytes (Bypasses slow Base64 encoding)
        RequestBody imagePayload = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageBytes
        );

        // 4. Wrap inside MultipartBody format
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "upload.jpg", imagePayload)
                .build();

        // 5. Build the final request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = connection.sendRequestDeprecated(request);
        String responseBody = null;

        if (response == null)
            return null;

        try {
            responseBody = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Gson().fromJson(responseBody, IMGBBResponse.class);
    }

    private byte[] getBytes(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private String getImageBas64String (byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
