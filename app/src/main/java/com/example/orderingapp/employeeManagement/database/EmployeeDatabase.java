package com.example.orderingapp.employeeManagement.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.sql.SQLLite;
import com.example.orderingapp.sql.enums.SQLLiteConstant;

public class EmployeeDatabase extends SQLiteOpenHelper
{
    private final String tableName = "employee";

    private final String idColumn = "id";
    private final String userNameColumn = "username";
    private final String passwordColumn = "password";
    private final String roleColumn = "role";

    public EmployeeDatabase(@Nullable Context context) {
        super(context, SQLLite.DATABASE_NAME, null, SQLLite.VERSION);
    }

    public void startEmployeeDatabase (String username, String password, String role)
    {
        onCreate(getWritableDatabase());
        deleteEmployee();

        if (username.isBlank())
            return;

        addEmployee(new Employee(0,username,role, password));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(sqLiteDatabase);
    }

    public void dropTables () {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + tableName +
                " ("+ idColumn +" INTEGER PRIMARY KEY AUTOINCREMENT" +
                " ,"+ userNameColumn +" TEXT UNIQUE" +
                " ,"+ passwordColumn +" TEXT" +
                " ,"+ roleColumn +" TEXT)"
        );
    }

    public SQLLiteConstant addEmployee (Employee employee)
    {
        if (doesEmployeeExist())
            return SQLLiteConstant.RECORD_EXISTS;

        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(userNameColumn, employee.getUsername());
        contentValues.put(passwordColumn, employee.getPassword());
        contentValues.put(roleColumn, employee.getRole());

        long res = database.insert(tableName, null, contentValues);

        return res == -1 ? SQLLiteConstant.RECORD_CREATE_FAILED : SQLLiteConstant.RECORD_CREATE_SUCCESSFULLY;
    }

    // must be only 1 employee, as users are kept on the server
    public Employee readEmployee () {

        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT "+ idColumn +", "+ userNameColumn +", "+ roleColumn +", "+ passwordColumn +" FROM " + tableName;

        Cursor cursor = database.rawQuery(sql, null);

        Employee employee = null;

        while (cursor.moveToNext())
            employee = new Employee(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );

        cursor.close();
        return employee;
    }

    public SQLLiteConstant updateEmployee (Employee employee) {

        SQLLiteConstant constant = deleteEmployee();

        if (!constant.equals(SQLLiteConstant.RECORD_DELETE_SUCCESSFULLY))
            return constant;

        return addEmployee(employee);
    }

    public SQLLiteConstant deleteEmployee ()
    {
        if (!doesEmployeeExist())
            return SQLLiteConstant.RECORD_NOT_EXISTS;

        SQLiteDatabase database = getWritableDatabase();

        long res = database.delete(tableName, null, null);

        return res == -1 ? SQLLiteConstant.RECORD_DELETE_FAILED : SQLLiteConstant.RECORD_DELETE_SUCCESSFULLY;
    }

    private boolean doesEmployeeExist () {
        Employee employee = readEmployee();
        return employee != null;
    }
}
