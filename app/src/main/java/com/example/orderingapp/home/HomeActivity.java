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
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.employeeManagement.EmployeeManagementActivity;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.my_profile.MyProfileActivity;
import com.example.orderingapp.order.OrderManagementActivity;
import com.example.orderingapp.productManagement.ProductManagementActivity;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.google.android.material.card.MaterialCardView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity implements ShowToastFromBgThread {
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

        hideMaterialCards();
        setListeners();
    }

    private void setListeners() {


        String role = employeeRole();
        if (role == null) return;

        if (role.equalsIgnoreCase("BOSS"))
        {
            showBossUserMaterialCards();
            setEventListenerForBossUserMaterialCards();
        }
        else if (role.equalsIgnoreCase("MANAGER"))
        {
            showManagerUserMaterialCards();
            setEventListenerForManagerUserMaterialCards();
        }
        else if (role.equalsIgnoreCase("STAFF"))
        {
            showEmployeeUserMaterialCards();
            setEventListenerForEmployeeUserMaterialCards();
        }

        TextView textView =  findViewById(R.id.home_userrole_textview);
        UI.setTextViewTxt(new TextView[]{textView}, new String[]{"Hello, "+role});
    }
    private void hideMaterialCards () {
        findViewById(R.id.card_manage_api_key).setVisibility(View.GONE);
        findViewById(R.id.card_manage_employees).setVisibility(View.GONE);
        findViewById(R.id.card_manage_products).setVisibility(View.GONE);
        findViewById(R.id.card_view_orders).setVisibility(View.GONE);
        findViewById(R.id.card_admin_dashboard).setVisibility(View.GONE);
        findViewById(R.id.card_profile).setVisibility(View.GONE);
        findViewById(R.id.card_test_connection).setVisibility(View.GONE);
    }

    private void showBossUserMaterialCards () {
        showForAllMaterialCards();
        findViewById(R.id.card_manage_api_key).setVisibility(View.VISIBLE);
        findViewById(R.id.card_manage_employees).setVisibility(View.VISIBLE);
        findViewById(R.id.card_manage_products).setVisibility(View.VISIBLE);
        findViewById(R.id.card_admin_dashboard).setVisibility(View.VISIBLE);
    }

    private void showManagerUserMaterialCards () {
        showForAllMaterialCards();
        findViewById(R.id.card_admin_dashboard).setVisibility(View.VISIBLE);
    }

    private void showEmployeeUserMaterialCards () {
        showForAllMaterialCards();
    }

    private void setEventListenerForBossUserMaterialCards () {
        setManageApiKeyListener();
        setManageEmployeesListener();
        setManageProductsListener();
        setManageOrdersListener();
        setAdminDashboardListener();
        setProfileListener();
        setTestConnectionListener();
    }

    private void setEventListenerForManagerUserMaterialCards () {
        setManageOrdersListener();
        setAdminDashboardListener();
        setProfileListener();
        setTestConnectionListener();
    }

    private void setEventListenerForEmployeeUserMaterialCards () {
        setManageOrdersListener();
        setProfileListener();
        setTestConnectionListener();
    }

    private void showForAllMaterialCards () {
        findViewById(R.id.card_view_orders).setVisibility(View.VISIBLE);
        findViewById(R.id.card_profile).setVisibility(View.VISIBLE);
        findViewById(R.id.card_test_connection).setVisibility(View.VISIBLE);
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
        Intent intent = new Intent(this, com.example.orderingapp.statistics.StatisticsActivity.class);
        startActivity(intent);
    }

    private void profileClicked() {
        Intent intent = new Intent(this, MyProfileActivity.class);
        startActivity(intent);
    }

    private void testConnectionClicked() {
        new TestConnection().testConnection(this);
    }

    public void setHttpConnectionTextViewTxt(String text)
    {
        runOnUiThread(() -> {
            TextView textView = findViewById(R.id.http_connection_indicator);
            textView.setText(text);
        });
    }

    @Override
    public void showToast(String message) {
        Toasts.showShortToast(this, message);
    }

    public void setWebSocketTextViewTxt(String payload) {

        runOnUiThread(() ->
        {
            TextView textView = findViewById(R.id.websocket_connection_indicator);
            textView.setText(payload.trim());
        });
    }

    private String employeeRole () {
        Employee employee = new EmployeeDatabase(this).readEmployee();
        return employee != null ? employee.getRole() : "";
    }
}
