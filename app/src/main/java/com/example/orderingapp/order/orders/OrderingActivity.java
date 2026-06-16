package com.example.orderingapp.order.orders;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.dto.*;
import com.example.orderingapp.R;
import com.example.orderingapp.order.madeOrders.MadeOrdersActivity;
import com.example.orderingapp.productCarousel.ProductCarousel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OrderingActivity extends AppCompatActivity {

    private int totalOrderedCount = 0;
    private TextView tvCartBadge;
    private final Map<String, Integer> orderItems = new HashMap<>();
    private final Map<String, Double> productPrices = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ordering_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ordering_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCartBadge = findViewById(R.id.tv_cart_badge);

        findViewById(R.id.rl_cart_container).setOnClickListener(v -> {
            if (orderItems.isEmpty()) {
                Toast.makeText(this, "Your selection is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            openOrderSummary();
        });

        findViewById(R.id.btn_submit_order).setOnClickListener(v -> {
            validateAndSubmit();
        });

        loadMenu();
    }

    private void loadMenu() {
        // Simulated menu data
        List<Product> menu = List.of(
                new Product("Spicy Chicken Burger", "$12.00", "Crispy chicken with spicy sauce", new LinkedList<>(List.of("login_activity_design"))),
                new Product("Margherita Pizza", "$14.50", "Classic tomato and mozzarella", new LinkedList<>(List.of("home_activity_design"))),
                new Product("Garden Salad", "$8.00", "Fresh greens with vinaigrette", new LinkedList<>(List.of("login_activity_design")))
        );

        for (Product p : menu) {
            productPrices.put(p.getName(), Double.parseDouble(p.getPrice().replace("$", "")));
        }

        LinearLayout root = findViewById(R.id.product_carousel_root_layout);
        ProductCarousel.getInstance().populateCarousel(this, menu, root, this::createOrderActions);
    }

    /**
     * Creates quantity field and Add/Remove buttons for the carousel.
     */
    private List<View> createOrderActions(Product product) {
        List<View> actions = new ArrayList<>();

        // Quantity Input Field
        TextInputLayout tilQty = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilQty.setHint("Qty");
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(dpToPx(90), LinearLayout.LayoutParams.WRAP_CONTENT);
        qtyParams.setMarginEnd(12);
        tilQty.setLayoutParams(qtyParams);

        TextInputEditText etQty = new TextInputEditText(tilQty.getContext());
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        tilQty.addView(etQty);

        // Add to Order Button
        MaterialButton btnAdd = new MaterialButton(this);
        btnAdd.setText("Add to Order");
        btnAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnAdd.setOnClickListener(v -> {
            String qtyStr = etQty.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                etQty.setError("Specify quantity");
                return;
            }
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                etQty.setError("Must be > 0");
                return;
            }

            orderItems.put(product.getName(), orderItems.getOrDefault(product.getName(), 0) + qty);
            totalOrderedCount += qty;
            updateCartBadge();
            etQty.setText("");
            Toast.makeText(this, qty + " " + product.getName() + " added to selection", Toast.LENGTH_SHORT).show();
        });

        // Remove (optional, but requested 'remove from order' feel)
        MaterialButton btnRemove = new MaterialButton(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Button_TextButton);
        btnRemove.setText("Clear");
        btnRemove.setTextColor(android.graphics.Color.RED);
        btnRemove.setOnClickListener(v -> {
            Integer current = orderItems.remove(product.getName());
            if (current != null) {
                totalOrderedCount -= current;
                updateCartBadge();
                Toast.makeText(this, product.getName() + " removed from order", Toast.LENGTH_SHORT).show();
            }
        });

        actions.add(tilQty);
        actions.add(btnAdd);
        actions.add(btnRemove);
        return actions;
    }

    private void updateCartBadge() {
        if (totalOrderedCount > 0) {
            tvCartBadge.setText(String.valueOf(totalOrderedCount));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void validateAndSubmit() {
        EditText etTable = findViewById(R.id.et_table_number);
        String table = etTable.getText().toString().trim();
        if (table.isEmpty()) {
            etTable.setError("Table number required");
            return;
        }
        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Your order list is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        openOrderSummary();
    }

    private void openOrderSummary() {
        Intent intent = new Intent(this, MadeOrdersActivity.class);
        intent.putExtra("cart_items", (Serializable) orderItems);
        intent.putExtra("product_prices", (Serializable) productPrices);
        intent.putExtra("table_number", ((EditText)findViewById(R.id.et_table_number)).getText().toString());
        intent.putExtra("feedback", ((EditText)findViewById(R.id.et_feedback)).getText().toString());
        startActivity(intent);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
