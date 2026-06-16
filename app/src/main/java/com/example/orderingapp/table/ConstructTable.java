package com.example.orderingapp.table;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;

import java.util.ArrayList;
import java.util.List;

public class ConstructTable  {

    private final Context context;
    private final TableLayout tableLayout;
    private final List<String> viewTypes;
    private final List<List<String>> rowsData;

    public ConstructTable(Context context, TableLayout tableLayout, List<String> viewTypes, List<List<String>> rowsData) {
        this.context = context;
        this.tableLayout = tableLayout;
        this.viewTypes = viewTypes;
        this.rowsData = rowsData;
    }

    public void populateTable()
    {

        // Clear any existing default rows from the XML
        tableLayout.removeAllViews();

        addTableHeader ();


        // Loop through each row of text content data
        for (List<String> rowData : rowsData)
        {
            TableRow tableRow = new TableRow(context);

            // Set layout parameters for standard row structure
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            tableRow.setLayoutParams(rowParams);

            populateTableRow(tableRow, rowData, viewTypes);

            // Append completed row into the parent layout container
            tableLayout.addView(tableRow);
        }
    }

    private void addTableHeader () {

        TableRow tableRow = new TableRow(context);
        List<String> viewTypes = new ArrayList<>();

        for (int i = 0; i < this.viewTypes.size(); i++)
            viewTypes.add("TextView");

        populateTableRow(tableRow, rowsData.remove(0), viewTypes);
        tableRow.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_color));
        tableLayout.addView(tableRow);
    }

    private void populateTableRow (TableRow tableRow, List<String> rowData, List<String> viewTypes)
    {

        tableRow.setPadding(new UnitConverter().dpToPx(context,10),new UnitConverter().dpToPx(context,10), new UnitConverter().dpToPx(context,10), new UnitConverter().dpToPx(context, 10));

        for (int i = 0; i < rowData.size(); i++)
        {
            String type = viewTypes.get(i);
            String content = rowData.get(i);
            View viewToInject;

            // Dynamically build the exact requested view type
            if ("Button".equalsIgnoreCase(type)) {
                Button button = new Button(context);
                viewToInject = button;
                addViewStyle(button, Button.class, content);
                setButtonEventListener (button, rowData);
            } else {
                // Default fallback to TextView
                TextView textView = new TextView(context);
                viewToInject = textView;
                addViewStyle(textView, TextView.class, content);
            }



            // Apply basic cell parameter weights to fill columns evenly
            TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            viewToInject.setLayoutParams(cellParams);

            // Add cell to row
            tableRow.addView(viewToInject);
        }
    }

    private void addViewStyle (TextView view, Class viewClass, String text) {

        if (viewClass.equals(Button.class)) {
            setViewTextAlignment(view, View.TEXT_ALIGNMENT_CENTER);
            setViewBackground(view, ContextCompat.getColor(context, R.color.white));
        }
        commonTextStyle(view, text);
    }

    private void commonTextStyle (TextView view, String text) {
        view.setText(text);
        view.setTextColor(Color.BLACK);
        view.setTextSize(16);
        view.setTypeface(view.getTypeface(), Typeface.BOLD);
    }

    private void setViewBackground (View view, int color) {
        view.setBackgroundColor(color);
    }

    private void setViewTextAlignment (View view, int alignment) {
        view.setTextAlignment(alignment);
    }

    private void setButtonEventListener (Button button,List<String> rowData) {

        if (!(context instanceof TableEventListener))
            return;

        TableEventListener listener = (TableEventListener) context;

        button.setOnClickListener(v -> listener.rowData(rowData));
    }
}
