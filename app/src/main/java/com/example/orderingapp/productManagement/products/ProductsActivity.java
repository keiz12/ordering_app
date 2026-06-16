package com.example.orderingapp.productManagement.products;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.orderingapp.productCarousel.ProductCarousel;
import com.example.orderingapp.productManagement.editProduct.EditProductActivity;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

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
        if (textView != null) {
            textView.setText("Products");
        }
    }

    private void addProducts() {
        // Sample data - replaced with actual loading logic if available
        List<Product> products = List.of(
                new Product("Whatever", "$12", "It's good", new LinkedList<>(List.of(
                        "order_receipt", "ic_shopping_cart", "login_activity_design"
                ))),
                new Product("Whatever 1", "$12", "It's good", new LinkedList<>(List.of(
                        "order_receipt", "ic_shopping_cart"
                ))));

        LinearLayout root = findViewById(R.id.product_carousel_root_layout);
        ProductCarousel carousel = ProductCarousel.getInstance();
        carousel.populateCarousel(this, products, root, this::createActionButtons);
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
            intent.putStringArrayListExtra("product_images", new ArrayList<>(product.getImagePaths()));
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
                        addProducts(); // Refresh list
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        actions.add(btnEdit);
        actions.add(btnDelete);
        return actions;
    }

    private void performDeletion(Product product) {
        // Delete associated images from internal storage "products" folder
        File productsDir = new File(getFilesDir(), "products");
        if (productsDir.exists()) {
            File[] files = productsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(product.getName())) {
                        file.delete();
                    }
                }
            }
        }
        Toast.makeText(this, product.getName() + " deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PRODUCT_REQUEST && resultCode == RESULT_OK) {
            addProducts(); // Refresh after update
        }
    }
}
