package com.example.orderingapp.order.madeOrders;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;

import java.util.HashMap;
import java.util.Map;

public class MadeOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.made_orders_activity);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.receiptCard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        loadOrderData();
    }

    private void loadOrderData() {
        HashMap<String, Integer> cartItems = (HashMap<String, Integer>) getIntent().getSerializableExtra("cart_items");
        HashMap<String, Double> productPrices = (HashMap<String, Double>) getIntent().getSerializableExtra("product_prices");
        
        if (cartItems == null || productPrices == null) return;

        LinearLayout container = findViewById(R.id.ll_ordered_items_container);
        container.removeAllViews();
        
        double total = 0;

        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
            double price = productPrices.getOrDefault(name, 0.0);
            double subtotal = price * quantity;
            total += subtotal;

            View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, container, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            text1.setText(name + " (x" + quantity + ")");
            text1.setTextColor(getResources().getColor(R.color.black));
            text2.setText(String.format("$%.2f", subtotal));
            text2.setTextColor(getResources().getColor(R.color.primary_color));

            container.addView(itemView);
        }

        TextView tvTotal = findViewById(R.id.tv_total_price);
        tvTotal.setText(String.format("$%.2f", total));
    }
}
