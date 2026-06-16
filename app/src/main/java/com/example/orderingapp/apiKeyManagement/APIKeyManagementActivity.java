package com.example.orderingapp.apiKeyManagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.sql.enums.SQLLiteConstant;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class APIKeyManagementActivity extends AppCompatActivity
{
    private final APIKeyDatabase database = new APIKeyDatabase(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.api_key_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.api_key_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        populateAPIKey ();
        setListeners();
    }

    private void setListeners() {
        setAddAPIKeyButtonListener();
        setDeleteAPIKeyButtonListener();
    }

    private void setAddAPIKeyButtonListener() {
        MaterialButton addButton = findViewById(R.id.add_api_key_button);
        addButton.setOnClickListener(l -> addAPIKey());
    }

    private void setDeleteAPIKeyButtonListener() {
        MaterialButton deleteButton = findViewById(R.id.delete_api_key_button);
        deleteButton.setOnClickListener(l -> deleteAPIKey());
    }

    private void populateAPIKey() {
        String apiKey = database.readAPIKey();
        boolean exists = !apiKey.isBlank();
        updateAPIKeyView(getAPIKey());
        toggleField(exists);
    }

    public void deleteAPIKey () {
        SQLLiteConstant constant = database.deleteAPIKey();
        afterApiKeyDelete(constant);
    }

    public void addAPIKey () {

        TextInputEditText input = findViewById(R.id.api_key_input);
        String apiKey = input.getText().toString().trim();

        if (apiKey.isBlank()) {
            Toasts.showShortToast(this, "API Key cannot be empty");
            return;
        }

        SQLLiteConstant constant = database.addAPIKey(apiKey);
        afterApiKeyCreate(constant);
    }

    private void afterApiKeyCreate (SQLLiteConstant constant)
    {
        if (constant.equals(SQLLiteConstant.RECORD_CREATE_FAILED))
            Toasts.showShortToast(this, "Api Key creation failed");

        else if (constant.equals(SQLLiteConstant.RECORD_EXISTS))
            Toasts.showShortToast(this, "Api Key already exists");

        else if (constant.equals(SQLLiteConstant.RECORD_CREATE_SUCCESSFULLY)) {
            Toasts.showShortToast(this, "Api Key created successfully");
            populateAPIKey();
        }
    }

    private void afterApiKeyDelete (SQLLiteConstant constant)
    {
        if (constant.equals(SQLLiteConstant.RECORD_DELETE_FAILED))
            Toasts.showShortToast(this, "Api Key deletion failed");

        else if (constant.equals(SQLLiteConstant.RECORD_NOT_EXISTS))
            Toasts.showShortToast(this, "Api Key doesn't exist");

        else if (constant.equals(SQLLiteConstant.RECORD_DELETE_SUCCESSFULLY)) {
            Toasts.showShortToast(this, "Api Key deleted successfully");
            populateAPIKey();
        }
    }

    public String getAPIKey () {
        String apikey = database.readAPIKey();
        return apikey.isBlank() ? "No API Key" : apikey;
    }

    private void updateAPIKeyView (String apiKey) {
        TextView textView = findViewById(R.id.api_key_textview_display);
        textView.setText(apiKey);
    }

    private void toggleField(boolean exists) {
        UI.setTextInputField(new TextInputEditText[]{findViewById(R.id.api_key_input)}, new String[]{""});
        UI.enableButton(new View[] { findViewById(R.id.add_api_key_button), findViewById(R.id.api_key_input), findViewById(R.id.delete_api_key_button)},
                new boolean[] {!exists, !exists, exists});
    }

}
