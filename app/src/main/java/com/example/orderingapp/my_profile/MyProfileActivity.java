package com.example.orderingapp.my_profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderingapp.R;
import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.dto.UserDTO;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.login.LoginActivity;
import com.example.orderingapp.my_profile.server.GetEmployeeServer;

public class MyProfileActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etRole;
    private View passwordLayout;
    private Button btnSaveChanges;
    private Button btnDeleteAccount;
    private TextView headerTitle;
    private View btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manipulate_employee_credentials);

        initViews();
        setupHeader();
        hideButtons();
        renameButtons();
        fetchProfileData();
        setListeners ();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etRole = findViewById(R.id.etRole);
        passwordLayout = findViewById(R.id.til_password);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        headerTitle = findViewById(R.id.header_title_textview);
        btnBack = findViewById(R.id.btn_header_back);

        // Making them non-editable as it's just viewing profile
        etFullName.setEnabled(false);
        etRole.setEnabled(false);
        
        // The user said "except for the password", so I'll hide the password field
        passwordLayout.setVisibility(View.GONE);
    }

    private void setupHeader() {
        headerTitle.setText("My Profile");
        btnBack.setOnClickListener(v -> finish());
    }

    private void hideButtons() {
        btnSaveChanges.setVisibility(View.GONE);
    }

    private void renameButtons () {
        btnDeleteAccount.setText("Log Out");
    }

    private void fetchProfileData() {
        new GetEmployeeServer(this).run();
    }

    public void populateData(UserDTO userDTO) {
        runOnUiThread(() -> {
            etFullName.setText(userDTO.getUsername());
            etRole.setText(userDTO.getRole());
        });
    }

    private void setListeners () {
        setLogOutEventListener();
    }

    private void setLogOutEventListener () {

        btnDeleteAccount.setOnClickListener(l ->
        {
            new EmployeeDatabase(this).dropTables();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
