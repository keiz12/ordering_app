package com.example.orderingapp.ui;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UI {

    public static void setTextViewStyle(TextView[] textViews, int[] styles) {

        textViews[0].setTypeface(null, Typeface.BOLD);

        int i = 0;

        for (TextView textView : textViews)
            textView.setTypeface(null, styles[i++]);
    }

    public static void setTextViewSize(TextView[] textViews, int[] size) {

        int i = 0;

        for (TextView textView : textViews)
            textView.setTextSize(size[i++]);
    }

    public static void setTextViewTxt(TextView[] textViews, String[] texts) {

        int i = 0;

        for (TextView textView : textViews)
            textView.setText(texts[i++]);
    }

    public static void enableButton (View[] views, boolean[] enable) {

        int i = 0;

        for (View view : views)
            view.setEnabled(enable[i++]);

    }

    public static void setTextColor (TextView [] views, int[] colors) {

        int i = 0;

        for (View view : views)
            view.setBackgroundColor(colors[i++]);
    }

    public static void addViewsToLayout (ViewGroup viewGroup, View[] views)
    {
        for (View view : views)
            viewGroup.addView(view);
    }
}
