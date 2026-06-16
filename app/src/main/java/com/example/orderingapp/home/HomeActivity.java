package com.example.orderingapp.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.apiKeyManagement.APIKeyManagementActivity;
import com.example.orderingapp.connection.test.TestConnection;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.employeeManagement.EmployeeManagementActivity;
import com.example.orderingapp.order.OrderManagementActivity;
import com.example.orderingapp.productManagement.ProductManagementActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();

        // Delay background work slightly to avoid conflict with ActivityManager locks during startup/transition
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            activityStart();
        }, 500);
    }

    private void setListeners() {
        setManageApiKeyListener();
        setManageEmployeesListener();
        setManageProductsListener();
        setManageOrdersListener();
        setAdminDashboardListener();
        setProfileListener();
        setTestConnectionListener();
    }

    private void activityStart() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> webSocketInit());
        executorService.shutdown();
    }

    private void setManageApiKeyListener() {
        MaterialCardView card = findViewById(R.id.card_manage_api_key);
        card.setOnClickListener(v -> manageApiKeyClicked());
    }

    private void setManageEmployeesListener() {
        MaterialCardView card = findViewById(R.id.card_manage_employees);
        card.setOnClickListener(v -> manageEmployeesClicked());
    }

    private void setManageProductsListener() {
        MaterialCardView card = findViewById(R.id.card_manage_products);
        card.setOnClickListener(v -> manageProductsClicked());
    }

    private void setManageOrdersListener() {
        MaterialCardView card = findViewById(R.id.card_view_orders);
        card.setOnClickListener(v -> manageOrdersClicked());
    }

    private void setAdminDashboardListener() {
        MaterialCardView card = findViewById(R.id.card_admin_dashboard);
        card.setOnClickListener(v -> adminDashboardClicked());
    }

    private void setProfileListener() {
        MaterialCardView card = findViewById(R.id.card_profile);
        card.setOnClickListener(v -> profileClicked());
    }

    private void setTestConnectionListener() {
        MaterialCardView card = findViewById(R.id.card_test_connection);
        card.setOnClickListener(v -> testConnectionClicked());
    }

    private void manageApiKeyClicked() {
         Intent intent = new Intent(this, APIKeyManagementActivity.class);
         startActivity(intent);
    }

    private void manageEmployeesClicked() {
         Intent intent = new Intent(this, EmployeeManagementActivity.class);
         startActivity(intent);
    }

    private void manageProductsClicked() {
         Intent intent = new Intent(this, ProductManagementActivity.class);
         startActivity(intent);
    }

    private void manageOrdersClicked() {
        Intent intent = new Intent(this, OrderManagementActivity.class);
        startActivity(intent);
    }

    private void adminDashboardClicked() {
        Toast.makeText(this, "Admin Dashboard Clicked", Toast.LENGTH_SHORT).show();
    }

    private void profileClicked() {
        Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
    }

    private void testConnectionClicked() {
        new TestConnection().testConnection(this);
    }

    public void setHttpConnectionTextViewTxt(String text) {
        runOnUiThread(() -> {
            TextView textView = findViewById(R.id.http_connection_indicator);
            textView.setText(text);
        });
    }

    private void webSocketInit () {
        // Set the activity reference BEFORE connecting to ensure it's available for callbacks
        WebSocketConnection.setHomeActivity(this);
        WebSocketConnection.getInstance().connectAndSubscribe();
    }

    public void setWebSocketTextViewTxt(String payload) {

        runOnUiThread(() ->
        {
            TextView textView = findViewById(R.id.websocket_connection_indicator);
            textView.setText(payload.trim());
        });
    }
}
