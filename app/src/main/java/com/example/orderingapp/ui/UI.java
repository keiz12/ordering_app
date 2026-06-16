package com.example.orderingapp.ui;

import android.view.View;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UI {
    public static void setTextInputField (TextInputEditText[] textInputEditTexts, String[] texts) {

        int i = 0;

        for (TextInputEditText textInputEditText : textInputEditTexts)
            textInputEditText.setText(texts[i++]);
    }

    public static void enableButton (View[] views, boolean[] enable) {

        int i = 0;

        for (View view : views)
            view.setEnabled(enable[i++]);

    }
}
