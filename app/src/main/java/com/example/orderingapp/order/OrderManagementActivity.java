package com.example.orderingapp.order;

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
import com.example.orderingapp.order.orders.OrderingActivity;
import com.example.orderingapp.order.processedOrders.ProcessedOrdersActivity;
import com.google.android.material.card.MaterialCardView;

public class OrderManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_order_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manage_order_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupHeader();
        setListeners();
    }

    private void setListeners() {
        setViewOrdersCardListener();
        setViewProcessedOrdersCardListener();
    }

    private void setViewOrdersCardListener() {
        MaterialCardView card = findViewById(R.id.ordering_activity_card);
        card.setOnClickListener(l -> viewOrdersClicked());
    }

    private void setViewProcessedOrdersCardListener() {
        MaterialCardView card = findViewById(R.id.processed_order_card);
        card.setOnClickListener(l -> viewProcessedOrdersClicked());
    }

    private void setupHeader() {
        TextView textView = findViewById(R.id.header_title_textview);
        textView.setText("Manage Orders");
    }

    public void viewOrdersClicked ()
    {
        var i = new Intent(this, OrderingActivity.class);
        startActivity(i);
    }

    public void viewProcessedOrdersClicked ()
    {
        var i = new Intent(this, ProcessedOrdersActivity.class);
        startActivity(i);
    }

    public void viewFailedOrdersClicked ()
    {

    }
}
