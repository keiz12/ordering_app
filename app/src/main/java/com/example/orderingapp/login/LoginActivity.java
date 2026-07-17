package com.example.orderingapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();
        sqlInit();
    }



    private void setListeners () {
        setLoginButtonListener();
    }

    private void setLoginButtonListener () {
        MaterialButton loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(l -> loginButtonClicked());
    }

    public void loginButtonClicked () {

        TextInputEditText username = findViewById( R.id.username_input );

        TextInputEditText password = findViewById(R.id.password_input);

        if (username.getText() == null || password.getText() == null)
            return;

        setLoadingToVisible();
        new LoginServer(this, username.getText().toString(), password.getText().toString()).run();

//        showHomeActivity();
    }


    private boolean serverSideLoginCall () {
        return true;
    }

    public void showHomeActivity () {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    private void sqlInit ()
    {
        new Handler().postDelayed(() ->
        {
            var employeeDB = new EmployeeDatabase(this);
            var apiKeyDb = new APIKeyDatabase(this);

            employeeDB.onCreate(employeeDB.getWritableDatabase());
            apiKeyDb.onCreate(apiKeyDb.getWritableDatabase());

        }, 1000);
    }

    public void setLoadingToVisible () {
        runOnUiThread(()-> {
            findViewById(R.id.loging_loading_textview).setVisibility(View.VISIBLE);
        });
    }

    public void setLoadingToInvisible () {
        runOnUiThread(() -> {
            findViewById(R.id.loging_loading_textview).setVisibility(View.GONE);
        });
    }
}
