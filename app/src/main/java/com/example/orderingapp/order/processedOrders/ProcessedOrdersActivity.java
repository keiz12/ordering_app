package com.example.orderingapp.order.processedOrders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderingapp.R;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.dto.Order;
import com.example.orderingapp.dto.OrderProcessIndicator;
import com.example.orderingapp.dto.StaffProcessedOrder;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.order.madeOrders.MadeOrdersActivity;
import com.example.orderingapp.table.ConstructTable;
import com.example.orderingapp.table.TableEventListener;
import com.example.orderingapp.toast.Toasts;
import android.graphics.Color;

import android.widget.Button;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProcessedOrdersActivity extends AppCompatActivity implements TableEventListener, ShowToastFromBgThread {

    private TableLayout tableLayout;
    private TextView titleTextView;
    private LinearLayout sidebarLayout;
    private ArrayAdapter<String> datesArrayAdapter;
    private View sidebarOverlay;
    private boolean isSidebarOpen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_processed_orders_activity);

        initializeViews();
        setupHeader(); // if the back button default shows
        renderProcessedOrder();
        setupSidebar();
    }

    private void initializeViews() {

        tableLayout = findViewById(R.id.table_layout);
        titleTextView = findViewById(R.id.processed_activity_title_textview);
        sidebarLayout = findViewById(R.id.processed_orders_sidebar_layout);

        ImageButton sidebarToggle = findViewById(R.id.processed_orders_sidebar_toggle_button);
        Button sidebarCloseButton = findViewById(R.id.sidebar_close_button);
//        sidebarOverlay = findViewById(R.id.sidebar_overlay);

        sidebarToggle.setOnClickListener(v -> toggleSidebar());
        sidebarCloseButton.setOnClickListener(v -> toggleSidebar());
//        sidebarOverlay.setOnClickListener(v -> toggleSidebar());
    }

    private void setupHeader() {

        TextView headerTitle = findViewById(R.id.header_title_textview);
        headerTitle.setText("Processed Orders");

        ImageButton backButton = findViewById(R.id.btn_header_back);
        backButton.setOnClickListener(l -> finish());
    }

    private void renderProcessedOrder() {
        String strDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        fetchProcessedOrder(strDate);
        setTitleTextViewText(strDate);
    }

    public void onProcessedOrderReceived(List<OrderProcessIndicator> orders)
    {
        runOnUiThread(() -> {

            new ConstructTable(this, tableLayout, List.of("TextView", "Button"), getRowsData(orders)).populateTable();
            // Highlight processed orders with a grey-ish background
            highLightProcessedOrderRows(orders);
        });
    }

    private List<List<String>> getRowsData (List<OrderProcessIndicator> orders) {

        List<List<String>> rowsData = new ArrayList<>();

        // Header Row (Column names: Order ID, Option)
        rowsData.add(List.of("Order ID", "Option"));

        for (OrderProcessIndicator order : orders)
            rowsData.add(List.of(order.getUuid(), "View"));

        return rowsData;
    }

    private void highLightProcessedOrderRows (List<OrderProcessIndicator> orders)
    {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).isProcessed()) {
                // i + 1 to account for the header row
                markTableRowBgAsProcessed((TableRow) tableLayout.getChildAt(i + 1));
            }
        }
    }

    private void setupSidebar() {
        new OrderDateServer().getAllDates(this);
    }

    public void onDatesReceived(List<String> formattedDates) {

        runOnUiThread(() ->
        {
            datesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formattedDates);
            ListView datesListView = findViewById(R.id.processed_orders_dates_listview);
            datesListView.setAdapter(datesArrayAdapter);

            datesListView.setOnItemClickListener((adapterView, view, i, l) -> {
                String selectedDate = formattedDates.get(i);
                toggleSidebar();
                fetchProcessedOrder(selectedDate);
                setTitleTextViewText(selectedDate);
            });
            initiateWebSocket();
        });
    }



    private void toggleSidebar() {
        if (isSidebarOpen)
            animateSidebar(false);
        else
            animateSidebar(true);

        isSidebarOpen = !isSidebarOpen;
    }

    private void initiateWebSocket () {
        WebSocketConnection.getInstance().runSocket(new ProcessedOrderWebSocket(this));
    }

    public void clientMadeOrder (Order order) {

        runOnUiThread(() ->
        {
            if (canMakeNewSidebarDateItem())
                addNewSidebar_Table();
            else
                addNewTableRow(order);
        });
    }

    public void clientDeleteOrder (String uuid)
    {
        runOnUiThread(() ->
        {
            int childCount = tableLayout.getChildCount();

            for (int i = 1; i < childCount; i++) {
                if (((TextView)((TableRow) tableLayout.getChildAt(i)).getChildAt(0)).getText().equals(uuid)) {
                    tableLayout.removeViewAt(i);
                    tableLayout.requestLayout();
                    tableLayout.invalidate();
                    break;
                }
            }
        });
    }

    public void staffProcessedOrder (String uuid)
    {
        runOnUiThread(() ->
        {
            int childCount = tableLayout.getChildCount();

            for (int i = 1; i < childCount; i++) {
                if (((TextView)((TableRow) tableLayout.getChildAt(i)).getChildAt(0)).getText().equals(uuid))
                    markTableRowBgAsProcessed((TableRow) tableLayout.getChildAt(i));
            }
        });
    }

    private void markTableRowBgAsProcessed (TableRow row) {
        row.setBackgroundColor(Color.parseColor("#D3D3D3"));// Light Grey
    }

    public void staffUnProcessedOrder (String uuid)
    {
        runOnUiThread(() ->
        {
            int childCount = tableLayout.getChildCount();

            for (int i = 1; i < childCount; i++) {
                if (((TextView)((TableRow) tableLayout.getChildAt(i)).getChildAt(0)).getText().equals(uuid))
                    markTableRowBgAsUnProcessed((TableRow) tableLayout.getChildAt(i));
            }
        });
    }

    private void markTableRowBgAsUnProcessed (TableRow row) {
        row.setBackgroundColor(Color.parseColor("#FFFFFF"));// White
    }

    private boolean canMakeNewSidebarDateItem () {
        return !datesArrayAdapter.isEmpty() && LocalDate.parse(datesArrayAdapter.getItem(0), DateTimeFormatter.ofPattern("dd-MM-yyyy")).isBefore(LocalDate.now());
    }

    private void makeNewSidebarDateItem () {
        datesArrayAdapter.insert(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), 0);
    }

    private void addNewSidebar_Table () {
        makeNewSidebarDateItem();
        fetchProcessedOrder(datesArrayAdapter.getItem(0));
        setTitleTextViewText (datesArrayAdapter.getItem(0));
    }

    private void addNewTableRow (Order order) {
        var constructTable = new ConstructTable(this, tableLayout, null, null);
        TableRow tableRow = constructTable.getDataTableRow();
        constructTable.populateTableRow(tableRow, List.of(order.getUuid(), "View"), List.of("TextView", "Button"));
        tableLayout.addView(tableRow);
    }



//    private void addRow

    @Override
    public void rowData(List<String> rowData) {
        runOnUiThread(() -> {
            String orderId = rowData.get(0);
            new GetStaffProcessedOrderServer().getStaffProcessedOrder(this, orderId);
        });
    }

    public void startMadeOrdersActivity (String uuid, StaffProcessedOrder staffProcessedOrder)
    {
        runOnUiThread(() ->
        {
            Intent intent = new Intent(this, MadeOrdersActivity.class);
            intent.putExtra("uuid", uuid);
            intent.putExtra("title", "Order");
            intent.putExtra("staff_processed_order", staffProcessedOrder);
            startActivity(intent);
        });
    }

    private void animateSidebar(boolean open) {
        if (open)
            sidebarLayout.setVisibility(View.VISIBLE);
        else
            sidebarLayout.setVisibility(View.GONE);
    }

    private void fetchProcessedOrder (String strDate) {
        new GetProcessedOrdersServer().fetchProcessedOrders(this, strDate);
    }

    @Override
    public void showToast(String message) {
        Toasts.showShortToast(this,message);
    }

    private void setTitleTextViewText (String date) {
        titleTextView.setText("Date: " + date);
    }
}
