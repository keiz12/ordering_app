package com.example.orderingapp.toast;

import android.content.Context;
import android.widget.Toast;

public class Toasts {
    public static void showShortToast (Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast (Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
