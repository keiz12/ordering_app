package com.example.orderingapp.employeeManagement.addNewEmployee;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.toast.Toasts;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class NewEmployeeAccountActivity extends AppCompatActivity
{
    private String employeeRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_employee_account_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.new_employee_account_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        populateEmployeeRoleSpinner ();
        setListeners();
    }

    private void setListeners() {
        setCreateAccountButtonListener();
    }

    private void setCreateAccountButtonListener() {
        MaterialButton createButton = findViewById(R.id.create_account_button);
        createButton.setOnClickListener(l -> createEmployeeAccountClicked());
    }

    private void populateEmployeeRoleSpinner ()
    {
        Spinner spinner = findViewById(R.id.role_spinner);
        addEmployeeRoleDropDownsToSpinner(spinner);
        addEmployeeSpinnerListener (spinner);
    }

    private void addEmployeeRoleDropDownsToSpinner (Spinner spinner)
    {
        //  Create an Adapter using your string array resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.employee_role_option, android.R.layout.simple_spinner_item);

        // Specify the layout style for the dropdown list items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Bind the adapter to your spinner dropdown
        spinner.setAdapter(adapter);
    }

    private void addEmployeeSpinnerListener (Spinner spinner)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected text string directly
                employeeRole = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle the case where the selection disappears
            }
        });
    }



    public void createEmployeeAccountClicked () {

        Employee employee = getEmployee();

        if (!validate(employee))
            return;

        registerEmployeeOnServer(employee);
    }

    private Employee getEmployee () {
        String username = ((TextInputEditText) findViewById(R.id.username_input)).getText().toString();
        String password = ((TextInputEditText) findViewById(R.id.password_input)).getText().toString();
        return new Employee(0, username, employeeRole, password);
    }

    private boolean validate (Employee employee)
    {
        String confirmPassword = ((TextInputEditText) findViewById(R.id.confirm_password_input)).getText().toString();

        if (employee.getRole() == null || employee.getRole().equalsIgnoreCase("employee role")) {
            Toasts.showShortToast(this, "Please select an employee role");
            return false;
        }

        if (employee.getUsername() == null || employee.getUsername().isEmpty()) {
            Toasts.showShortToast(this, "Please enter a username");
            return false;
        }

        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            Toasts.showShortToast(this, "Please enter a password");
            return false;
        }

        if (!employee.getPassword().equals(confirmPassword)) {
            Toasts.showShortToast(this, "Passwords do not match");
            return false;
        }

        return true;
    }

    private void registerEmployeeOnServer (Employee employee) {
        new NewEmployeeAccountServer().addNewEmployee(this, employee);
    }

    public void contextDisplayMessage(String message) {
        runOnUiThread(() ->
        {
            TextView statusTextView = findViewById(R.id.status_message_textview);
            statusTextView.setText(message);
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.postDelayed(() -> statusTextView.setVisibility(View.GONE), 5000);
        });
    }

}
