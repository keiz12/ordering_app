package com.example.orderingapp.employeeManagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.employeeManagement.addNewEmployee.NewEmployeeAccountActivity;
import com.example.orderingapp.employeeManagement.myEmployees.MyEmployeesActivity;
import com.google.android.material.card.MaterialCardView;

public class EmployeeManagementActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_employee_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manage_employees_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setHeader();
        setListeners();
    }

    private void setHeader() {
        TextView textView = findViewById(R.id.header_title_textview);
        textView.setText("Employee Management");

        ImageButton button = findViewById(R.id.btn_header_back);
        button.setOnClickListener(l -> finish());
    }

    private void setListeners() {
        setAddNewEmployeeCardListener();
        setMyEmployeesCardListener();
    }

    private void setAddNewEmployeeCardListener() {
        MaterialCardView card = findViewById(R.id.add_employee);
        card.setOnClickListener(l -> addNewEmployeeClicked());
    }

    private void setMyEmployeesCardListener() {
        MaterialCardView card = findViewById(R.id.card_manage_api_key);
        card.setOnClickListener(l -> myEmployeesClicked());
    }

    public void addNewEmployeeClicked() {

        var i = new Intent(this, NewEmployeeAccountActivity.class);
        startActivity(i);
    }


    public void myEmployeesClicked() {
        var i = new Intent(this, MyEmployeesActivity.class);
        startActivity(i);
    }

}
