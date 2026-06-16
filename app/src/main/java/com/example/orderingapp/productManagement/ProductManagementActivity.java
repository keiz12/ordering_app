package com.example.orderingapp.productManagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.productManagement.addProduct.AddProductActivity;
import com.example.orderingapp.productManagement.products.ProductsActivity;
import com.google.android.material.card.MaterialCardView;

public class ProductManagementActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_product_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manage_product_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setHeader ();
        setListeners();
    }

    private void setListeners() {
        setAddNewProductCardListener();
        setViewProductsCardListener();
    }

    private void setAddNewProductCardListener() {
        MaterialCardView card = findViewById(R.id.add_employee);
        card.setOnClickListener(l -> addNewProductClicked());
    }

    private void setViewProductsCardListener() {
        MaterialCardView card = findViewById(R.id.card_manage_api_key);
        card.setOnClickListener(l -> productsClicked());
    }

    private void setHeader() {
        TextView textView = findViewById( R.id.header_title_textview);
        textView.setText("Products");
    }

    public void addNewProductClicked () {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    public void productsClicked () {
        Intent intent = new Intent(this, ProductsActivity.class);
        startActivity(intent);
    }
}
