package com.example.orderingapp.apiKeyManagement.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.sql.enums.SQLLiteConstant;
import com.example.orderingapp.sql.SQLLite;

public class APIKeyDatabase extends SQLiteOpenHelper
{
    private final String tableName = "api_key";

    private final String idColumn = "id";
    private final String apiKeyColumn = "api_key";

    public APIKeyDatabase(@Nullable Context context) {
        super(context, SQLLite.DATABASE_NAME, null, SQLLite.VERSION);
    }

    public void startApiKeyDatabase (String apiKey)
    {
        onCreate(getWritableDatabase());
        deleteAPIKey();

        if (apiKey.isBlank())
            return;

        addAPIKey(apiKey);
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
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + tableName +
                " ("+ idColumn +" INTEGER PRIMARY KEY AUTOINCREMENT" +
                " ,"+ apiKeyColumn +" TEXT)"
        );
    }

    public SQLLiteConstant addAPIKey (String apiKey)
    {
        if (doesApiKeyExists())
            return SQLLiteConstant.RECORD_EXISTS;

        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(apiKeyColumn, apiKey);

        long res = database.insert(tableName, null, contentValues);

        return res == -1 ? SQLLiteConstant.RECORD_CREATE_FAILED : SQLLiteConstant.RECORD_CREATE_SUCCESSFULLY;
    }

    public String readAPIKey () {

        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT "+apiKeyColumn+" FROM " + tableName;

        Cursor cursor = database.rawQuery(sql, null);

        String apiKey = "";

        while (cursor.moveToNext())
            apiKey = cursor.getString(0);

        cursor.close();
        return apiKey;
    }

    public SQLLiteConstant deleteAPIKey ()
    {
        if (!doesApiKeyExists())
            return SQLLiteConstant.RECORD_NOT_EXISTS;

        SQLiteDatabase database = getWritableDatabase();

        long res = database.delete(tableName,null,null);

        return res == -1 ? SQLLiteConstant.RECORD_DELETE_FAILED : SQLLiteConstant.RECORD_DELETE_SUCCESSFULLY;
    }

    private boolean doesApiKeyExists () {
        return ! readAPIKey().isBlank();
    }
}
