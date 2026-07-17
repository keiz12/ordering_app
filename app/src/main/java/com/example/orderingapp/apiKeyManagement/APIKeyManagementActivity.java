package com.example.orderingapp.apiKeyManagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.apiKeyManagement.server.APIKeyDeleteServer;
import com.example.orderingapp.apiKeyManagement.server.APIKeyValidateServer;
import com.example.orderingapp.apiKeyManagement.server.ApiKeyCountServer;
import com.example.orderingapp.apiKeyManagement.server.SaveApiKeyServer;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.sql.enums.SQLLiteConstant;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class APIKeyManagementActivity extends AppCompatActivity implements ShowToastFromBgThread
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
        setAddLocalApiKeyButtonListener();
        setAddServerAPIKeyButtonListener();
        setAddTestAPIKeyButtonListener();
        setDeleteAPIKeyLocallyButtonListener();
        setDeleteAPIKeyServerButtonListener();
    }

    private void setAddLocalApiKeyButtonListener() {
        MaterialButton addButton = findViewById(R.id.add_local_api_key_button);
        addButton.setOnClickListener(l -> {
            TextInputEditText editText =  findViewById(R.id.api_key_input);
            addAPIKey(editText.getText().toString().trim());
        });
    }

    private void setAddServerAPIKeyButtonListener() {
        MaterialButton addButton = findViewById(R.id.add_server_api_key_button);
        addButton.setOnClickListener(l -> {
            TextInputEditText editText =  findViewById(R.id.api_key_input);
            new SaveApiKeyServer(this,editText.getText().toString().trim()).run();
        });
    }

    private void setAddTestAPIKeyButtonListener() {
        MaterialButton addButton = findViewById(R.id.api_key_test_button);
        addButton.setOnClickListener(l -> {
            TextInputEditText editText =  findViewById(R.id.api_key_input);
            new APIKeyValidateServer(this).run();
        });
    }

    private void setDeleteAPIKeyLocallyButtonListener() {
        MaterialButton deleteButton = findViewById(R.id.delete_api_key_button);
        deleteButton.setOnClickListener(l -> showConfirmationDialog("Local Deletion",
                "Are you sure you want to remove the API key locally?",
                this::removeAPIKeyFromDevice));
    }

    private void setDeleteAPIKeyServerButtonListener() {
        MaterialButton deleteServerButton = findViewById(R.id.delete_api_key_server_button);
        deleteServerButton.setOnClickListener(l -> showConfirmationDialog("Server Deletion",
                "Are you sure you want to delete the API key from the server? This action cannot be undone.",
                this::deleteAPIKeyFromServer));
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

    public void deleteAPIKeyFromServer() {
        new APIKeyDeleteServer(this).run();
    }

    public void populateAPIKey() {

        runOnUiThread(() -> {
            String apiKey = database.readAPIKey();
            boolean exists = !apiKey.isBlank();
            updateAPIKeyView(activityStartApiKeyCheck());
            toggleField(exists);
        });
    }



    public void removeAPIKeyFromDevice () {
        SQLLiteConstant constant = database.deleteAPIKey();
        afterApiKeyDelete(constant);
    }

    public void addAPIKey (String apiKey) {

        runOnUiThread(() ->
        {
            if (apiKey.isBlank()) {
                Toasts.showShortToast(this, "API Key cannot be empty");
                return;
            }

            boolean isCreated = database.addAPIKey(apiKey).equals(SQLLiteConstant.RECORD_CREATE_SUCCESSFULLY);

            if (isCreated) {
                Toasts.showLongToast(this, "Api Key saved locally, please test it to match it with the server version.");
                populateAPIKey();
            }
        });
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

    public String activityStartApiKeyCheck () {
        String apikey = database.readAPIKey();

        if (apikey.isBlank())
            new ApiKeyCountServer(this).run();

        return apikey;
    }

    public void updateAPIKeyView (String apiKey) {
        runOnUiThread(() ->
        {
            TextView textView = findViewById(R.id.api_key_textview_display);
            textView.setText(apiKey);
        });
    }

    private void toggleField(boolean exists) {
        UI.setTextViewTxt(new TextInputEditText[]{findViewById(R.id.api_key_input)}, new String[]{""});
        UI.enableButton(new View[] {
                        findViewById(R.id.add_server_api_key_button),
                        findViewById(R.id.add_local_api_key_button),
                        findViewById(R.id.api_key_input),
                        findViewById(R.id.delete_api_key_button),
                        findViewById(R.id.delete_api_key_server_button)
                },
                new boolean[] {!exists, !exists, !exists, exists, exists});
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> {
            Toasts.showShortToast(this, message);
        });
    }
}
