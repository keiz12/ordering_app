package com.example.orderingapp.productManagement.products;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.example.orderingapp.dto.Product;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.productCarousel.ProductCarousel;
import com.example.orderingapp.productManagement.editProduct.EditProductActivity;
import com.example.orderingapp.toast.Toasts;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductsActivity extends AppCompatActivity implements ShowToastFromBgThread {

    private static final int EDIT_PRODUCT_REQUEST = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.products_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.products_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setHeader();
        addProducts();
    }

    private void setHeader() {
        TextView textView = findViewById(R.id.header_title_textview);
        textView.setText("All Products");

        ImageButton button = findViewById(R.id.btn_header_back);
        button.setOnClickListener(l -> finish());
    }

    private void addProducts()
    {
        new ProductsActivityServer().getProducts(this, products ->
        {
            LinearLayout root = findViewById(R.id.product_carousel_root_layout);
            ProductCarousel carousel = ProductCarousel.getInstance();
            carousel.populateCarousel(this, products, root, this::createActionButtons);
        });
    }

    /**
     * Creates Edit and Delete buttons for a specific product.
     */
    private List<View> createActionButtons(Product product) {
        List<View> actions = new ArrayList<>();

        // Edit Button
        MaterialButton btnEdit = new MaterialButton(this);
        btnEdit.setText("Edit");
        LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        editLp.setMarginEnd(16);
        btnEdit.setLayoutParams(editLp);
        btnEdit.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, EditProductActivity.class);
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_description", product.getDescription());
            intent.putStringArrayListExtra("product_images", new ArrayList<>(product.getImageURLPath()));
            startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
        });

        // Delete Button
        MaterialButton btnDelete = new MaterialButton(this);
        btnDelete.setText("Delete");
        btnDelete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
        btnDelete.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete " + product.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        performDeletion(product);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        actions.add(btnEdit);
        actions.add(btnDelete);
        return actions;
    }

    private void performDeletion(Product product) {
        new ProductsActivityServerDelete().deleteProduct(this, product, success ->
        {
            if (success) {
                Toasts.showShortToast(this, "Product deleted successfully");
                addProducts();
            }
            else
                Toasts.showShortToast(this, "Failed to delete product");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PRODUCT_REQUEST && resultCode == RESULT_OK) {
            addProducts(); // Refresh after update
        }
    }

    @Override
    public void showToast(String message) {
        Toasts.showShortToast(this, message);
    }
}
