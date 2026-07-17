package com.example.orderingapp.login;

import com.example.orderingapp.connection.http.HttpServerConnection;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.dto.UserDTO;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.toast.Toasts;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LoginServer {

    private final LoginActivity activity;
    private final String username;
    private final String password;

    public LoginServer(LoginActivity activity, String username, String password) {
        this.activity = activity;
        this.username = username;
        this.password = password;
    }

    public void run() {
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() ->
        {
            Request request = getRequest();

            runAfterResponse(new HttpServerConnection().getResponseMap(request));
        });

        service.shutdown();
    }

    private void runAfterResponse(HashMap<String, Object> map) {

        if (map == null ) {
            Toasts.showLongToast(activity, "Network failed.");
            activity.setLoadingToInvisible();
            return;
        }

        if (Boolean.FALSE.equals(map.get(HttpServerConnection.responseStatusKey))) {
            Toasts.showLongToast(activity, "Authentication failed. The username or password you entered is incorrect. Please double-check your credentials and try again.");
            activity.setLoadingToInvisible();
            return;
        }

        Gson gson = new Gson();
        UserDTO userDTO = gson.fromJson((String) map.get(HttpServerConnection.responseBodyKey), UserDTO.class);

        saveEmployeeToDB(userDTO);
        activity.showHomeActivity();
    }

    private void saveEmployeeToDB (UserDTO userDTO)
    {
        EmployeeDatabase db = new EmployeeDatabase(activity);
        db.addEmployee(new Employee(0, username, userDTO.getRole(), password));

    }

    private Request getRequest() {
        Gson gson = new Gson();

        UserDTO userDTO = new UserDTO();

        userDTO.setUsername(username);
        userDTO.setPassword(password);

        String json = gson.toJson(userDTO);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        return new Request.Builder()
                .url(HttpServerConnection.httpBaseURL + HttpServerConnection.userLogin)
                .addHeader("Authorization", new HttpServerConnection().getHttpBasicCredentials(username, password))
                .post(body)
                .build();
    }
}
