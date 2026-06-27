package com.example.orderingapp.table;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;

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

    /**
     *         if (notStartingFreshTable.length == 0)
     *             startingFreshTable();
     *         else
     *             notStartingFreshTable();
     *
     * */

    private void startingFreshTable () {
        tableLayout.removeAllViews();
        addTableHeader ();
    }

    private void notStartingFreshTable () {

        if (tableLayout.getChildCount() > 0)
            return;

        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
    }

    public void populateTable()
    {
            tableLayout.removeAllViews();
            addTableHeader ();

            for (List<String> rowData : rowsData)
            {
                TableRow tableRow = getDataTableRow();

                populateTableRow(tableRow, rowData, viewTypes);

                // Append completed row into the table
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

    public TableRow getDataTableRow ()
    {
        TableRow tableRow = new TableRow(context);

        // Set layout parameters for standard row structure
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        tableRow.setLayoutParams(rowParams);

        return tableRow;
    }
    public void populateTableRow (TableRow tableRow, List<String> rowData, List<String> viewTypes)
    {

        setTableRowPadding(tableRow);

        for (int i = 0; i < rowData.size(); i++)
        {
            String type = viewTypes.get(i);
            String content = rowData.get(i);
            View viewToInject;

            // Apply basic cell parameter weights to fill columns evenly
            TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1.0f
            );

            // Dynamically build the exact requested view type
            if ("Button".equalsIgnoreCase(type)) {
                Button button = new Button(context);
                viewToInject = button;
                setButtonEventListener (button, rowData);
                addViewStyle(button, Button.class, content);
                setGravity(cellParams);
            }
            else {
                // Default fallback to TextView
                TextView textView = new TextView(context);
                viewToInject = textView;
                addViewStyle(textView, TextView.class, content);
            }

            viewToInject.setLayoutParams(cellParams);

            // Add cell to row
            tableRow.addView(viewToInject);
        }
    }

    private void setTableRowPadding (TableRow tableRow) {
        int dp = new UnitConverter().dpToPx(context,10);
        tableRow.setPadding(dp, dp, dp, dp);
    }

    private void addViewStyle (TextView view, Class viewClass, String text) {

        if (viewClass.equals(Button.class)) {
            setViewTextAlignment(view, View.TEXT_ALIGNMENT_CENTER);
            setViewBackground(view, ContextCompat.getColor(context, R.color.primary_color));
            view.setTextColor(Color.WHITE);
            view.setAllCaps(false);
        } else {
            view.setTextColor(Color.BLACK);
        }
        commonTextStyle(view, text);
    }

    private void commonTextStyle (TextView view, String text) {
        view.setText(text);
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

    private void setGravity ( TableRow.LayoutParams cellParams) {
        int margin = new UnitConverter().dpToPx(context, 8);
        cellParams.setMargins(margin, margin, margin, margin);
        cellParams.gravity = Gravity.CENTER;
    }
}
