package com.example.orderingapp.productManagement.addProduct;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;
import com.example.orderingapp.dto.Product;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity implements ShowToastFromBgThread
{
    private final ManageAddedProduct addedProduct = new ManageAddedProduct();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_product_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_product_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setHeader ();
        setListeners();
    }

    private void setListeners() {
        setAddProductImageListener();
        setClearButtonListener();
        setSaveButtonListener();
    }

    private void setAddProductImageListener() {
        MaterialCardView addButton = findViewById(R.id.add_product_image_button);
        addButton.setOnClickListener(l -> addProductImage());
    }

    private void setClearButtonListener() {
        MaterialButton clearButton = findViewById(R.id.btn_delete_product);
        clearButton.setOnClickListener(l -> clearButtonClick());
    }

    private void setSaveButtonListener() {
        MaterialButton saveButton = findViewById(R.id.btn_save_product);
        saveButton.setOnClickListener(l -> saveButtonClicked());
    }

    private void setHeader() {
        TextView textView = findViewById( R.id.header_title_textview);
        textView.setText("Add New Product");
    }

    public void addProductImage()
    {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        addedProduct.onActivityResult(this,
                findViewById(R.id.added_product_images_container),
                requestCode,
                resultCode,
                data);
    }

    private void clearButtonClick() {
        addedProduct.clearButtonClicked(this,
                findViewById(R.id.add_product_name_input),
                findViewById(R.id.add_product_price_input),
                findViewById(R.id.add_product_description_input),
                findViewById(R.id.added_product_images_container));
    }

    public void saveButtonClicked() {

        String productName = ((EditText)findViewById(R.id.add_product_name_input)).getText().toString();

        String productPrice = ((EditText)findViewById(R.id.add_product_price_input)).getText().toString();

        String productDescription = ((EditText)findViewById(R.id.add_product_description_input)).getText().toString();

        if (productName.isBlank() || productPrice.isBlank() || productDescription.isBlank()) {
            showToast("Please fill in all fields");
            return;
        }

        addedProduct.saveButtonClicked(this, getAddedProduct(productName, productPrice, productDescription));
    }

    private Product getAddedProduct (String productName, String productPrice, String productDescription)
    {
        Product product = new Product();

        product.setName(productName);
        product.setPrice(Double.parseDouble(productPrice));
        product.setDescription(productDescription);

        return product;
    }

    public void showToast (String txt) {
        runOnUiThread(() -> Toasts.showShortToast(this, txt));
    }
}
