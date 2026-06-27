package com.example.orderingapp.interfaces.http;

import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;

public interface ClientAuth {
    String message = "Unauthorized user trying to perform this action.";
    boolean authorize(ShowToastFromBgThread toast);
}
