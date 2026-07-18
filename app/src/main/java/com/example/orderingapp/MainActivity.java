package com.example.orderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.apiKeyManagement.server.APIKeyValidateServer;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.home.HomeActivity;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.login.LoginActivity;
import com.example.orderingapp.order.orders.OrderingActivity;
import com.example.orderingapp.toast.Toasts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ShowToastFromBgThread {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        super.onCreate(savedInstanceState);

        runApp();

//        testingModeAppStartUp();

//        service.shutdown();
    }


    private void runApp () {
        new Handler().postDelayed(() -> {
            sqlCheck();
        }, 1000);
    }

    private void sqlCheck ()
    {
        var employeeDb = new EmployeeDatabase(this);
        var apiKeyDb = new APIKeyDatabase(this);

        employeeDb.onCreate(employeeDb.getWritableDatabase());
        apiKeyDb.onCreate(apiKeyDb.getWritableDatabase());

        Employee employee = employeeDb.readEmployee();
        String apiKey = apiKeyDb.readAPIKey();

        boolean apiKeyIsAbsent = apiKey.isBlank();

        if (!apiKeyIsAbsent) {
            if (handleRetrievedAPIKey(apiKey, apiKeyDb)) {
                employeeDb.deleteEmployee();
                startActivity(new Intent(this, OrderingActivity.class));
                return;
            }
        }

        if (employee == null) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        startActivity(new Intent(this, HomeActivity.class));
    }


    private boolean handleRetrievedAPIKey (String apiKey, APIKeyDatabase apiKeyDatabase) {
        /*
        * employeeDb.deleteEmployee();
            startActivity(new Intent(this, OrderingActivity.class));
        * */

        APIKeyValidateServer apiKeyValidateServer = new APIKeyValidateServer(this);

        apiKeyValidateServer.run();

        boolean isValid = apiKeyValidateServer.isValid();

        System.out.println(isValid);

        if (!isValid)
            apiKeyDatabase.deleteAPIKey();

        return isValid;
    }


    public void loginButtonClicked () {
        System.out.println("Hello I'm Clicked");
    }

    private void testingModeAppStartUp ()
    {
        new Handler().postDelayed(() ->
        {
            String userFileName = "user.properties";
            String apiFileName = "api.properties";

            Properties properties = new Properties();

            loadProperties(properties, userFileName);
            loadProperties(properties, apiFileName);

            initSQLLite(properties);
            showActivity(properties);

        }, 1000);
    }

    private void loadProperties (Properties properties, String fileName)
    {

        try (InputStream inputStream = getAssets().open(fileName))
        {
            properties.load(inputStream);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void initSQLLite (Properties properties)
    {
        var apiKeyDb = new APIKeyDatabase(this);

        apiKeyDb.startApiKeyDatabase(properties.getProperty("api_key"));

        var employeeDB = new EmployeeDatabase(this);

        employeeDB.startEmployeeDatabase(properties.getProperty("username"), properties.getProperty("password"), properties.getProperty("role"));
    }

    private void showActivity (Properties properties) {

        String apiKey = String.valueOf(properties.get("api_key"));

        if (apiKey.isBlank())
            startActivity(new Intent(this, HomeActivity.class));
        else
            startActivity(new Intent(this, OrderingActivity.class));
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> {
            Toasts.showShortToast(this, message);
        });
    }
}
