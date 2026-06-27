package com.example.orderingapp.employeeManagement.myEmployees;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.employeeManagement.manipulate.ManipulateEmployeeCredentialActivity;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.table.ConstructTable;
import com.example.orderingapp.table.TableEventListener;
import com.example.orderingapp.toast.Toasts;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyEmployeesActivity extends AppCompatActivity implements TableEventListener, ShowToastFromBgThread
{
    private List<Employee> employeeList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.my_employees_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_employees_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setHeaderTitle();
        populateEmployeesToTable ();
    }

    private void setHeaderTitle () {
        TextView textView = findViewById( R.id.header_title_textview);
        textView.setText("Employees");
    }
    private void populateEmployeesToTable ()
    {
        TableLayout tableLayout = findViewById(R.id.table_layout);
        serverCall(tableLayout);
    }

    private void serverCall (TableLayout tableLayout) {

        ExecutorService service = Executors.newFixedThreadPool(1);

        try
        {
            Future<List<Employee>> future = service.submit(() -> new EmployeesServer().getEmployees(this));
            employeeList = future.get();
            runOnUiThread(() -> {
                new ConstructTable(this, tableLayout, List.of("TextView","TextView", "TextView", "Button", "Button"), getRowsData(employeeList)).populateTable();
//                tableLayout.getChildAt(1).setBackgroundColor(com.denzcoskun.imageslider.R.color.cardview_shadow_start_color);
            });
        }
        catch (Exception e) {}
        finally {
            service.shutdown();
        }
    }



    private List<List<String>> getRowsData (List<Employee> employees)
    {
        List<List<String>> rowsData = new ArrayList<>();
        int i = 0;

        rowsData.add(List.of("ID", "Username", "Role", "Options"));
        for (Employee employee : employees) {
            rowsData.add(List.of(String.valueOf(employee.getId()), employee.getUsername(), employee.getRole(), "Option"));
        }

        return rowsData;
    }



    @Override
    public void rowData(List<String> rowData)
    {
        String username = rowData.get(1);
        Employee selectedEmployee = null;

        for (Employee e : employeeList) {
            if (e.getUsername().equals(username)) {
                selectedEmployee = e;
                break;
            }
        }

        var i = new Intent(this, ManipulateEmployeeCredentialActivity.class);
        i.putExtra("employee", new Gson().toJson(selectedEmployee));
        startActivity(i);
    }

    @Override
    public void showToast(String message) {
        Toasts.showShortToast(this,message);
    }
}
