package com.example.orderingapp.employeeManagement.manipulate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.dto.UserUpdateRequest;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.employeeManagement.manipulate.delete.DeleteEmployeeServer;
import com.example.orderingapp.employeeManagement.manipulate.update.UpdateEmployeeServer;
import com.example.orderingapp.employeeManagement.myEmployees.MyEmployeesActivity;
import com.example.orderingapp.toast.Toasts;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Response;

public class ManipulateEmployeeCredentialActivity extends AppCompatActivity
{
    private Employee oldEmployee;
    private EditText etFullName, etRole;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manipulate_employee_credentials);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manipulate_employee_credentials), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setHeaderTitle();
        setOldEmployee(getIntent().getStringExtra("employee"));
        populateDataToFields(oldEmployee);
        setListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etRole = findViewById(R.id.etRole);
        etPassword = findViewById(R.id.password_edit);
    }

    private void setListeners() {
        setEditEmployeeButtonListener();
        setDeleteEmployeeButtonListener();
    }
    private void setOldEmployee (String employeeJson) {

        if (employeeJson == null)
            return;

        oldEmployee = new Gson().fromJson(employeeJson, Employee.class);
    }

    private void setEditEmployeeButtonListener() {
        Button saveButton = findViewById(R.id.btnSaveChanges);
        saveButton.setOnClickListener(l -> editEmployeeButtonClicked());
    }

    private void setDeleteEmployeeButtonListener() {
        Button deleteButton = findViewById(R.id.btnDeleteAccount);
        deleteButton.setOnClickListener(l -> deleteEmployeeButtonClicked());
    }

    private void setHeaderTitle() {
        TextView textView = findViewById( R.id.header_title_textview);
        textView.setText("Employee Profile Manipulate");
    }

    private void populateDataToFields (Employee employee)
    {
        if (employee == null)
            return;

        etFullName.setText(employee.getUsername());
        etRole.setText(employee.getRole());
    }

    public void editEmployeeButtonClicked()
    {
        if (oldEmployee == null) return;

        Employee newEmployee = getNewEmployee();

        UserUpdateRequest updateRequest = new UserUpdateRequest(oldEmployee, newEmployee);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() ->
        {
            Response response = new UpdateEmployeeServer().updateEmployee(this, updateRequest);
            boolean isSuccessful = response != null && response.isSuccessful();
            if (response != null) response.close();

            updateBossUserDatabase(newEmployee, isSuccessful);

            runOnUiThread(() -> {
                runAfterUpdate(isSuccessful);
            });
            executor.shutdown();
        });
    }

    private Employee getNewEmployee ()
    {

        Employee newEmployee = new Employee();

        newEmployee.setId(oldEmployee.getId());
        newEmployee.setUsername(etFullName.getText().toString());
        newEmployee.setRole(etRole.getText().toString());
        newEmployee.setPassword(etPassword.getText().toString());

        return newEmployee;
    }

    private void updateBossUserDatabase (Employee employee, boolean isSuccessful) {

        boolean isBoss = employee.getRole().equals("BOSS");

        if (!isBoss || !isSuccessful)
            return;

        var database = new EmployeeDatabase(this);
        var currEmployee = database.readEmployee();

        if (currEmployee.getUsername().equals(employee.getUsername()))
            database.updateEmployee(employee);
    }

    private void runAfterUpdate (boolean isSuccessful)
    {
        if (!isSuccessful) {
            Toasts.showShortToast(this, "Update failed");
            return;
        }

        Toasts.showShortToast(this, "update is successful");
        Intent intent = new Intent(this, MyEmployeesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void deleteEmployeeButtonClicked() {
        if (oldEmployee == null) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            Response response = new DeleteEmployeeServer().deleteEmployee(this, oldEmployee.getId());
            boolean isSuccessful = response != null && response.isSuccessful();
            if (response != null) response.close();

            runOnUiThread(() -> {
                runAfterDelete(isSuccessful);
            });
            executor.shutdown();
        });
    }

    private void runAfterDelete(boolean isSuccessful) {
        if (!isSuccessful) {
            Toasts.showShortToast(this, "Delete failed");
            return;
        }

        Toasts.showShortToast(this, "delete is successful");
        Intent intent = new Intent(this, MyEmployeesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
