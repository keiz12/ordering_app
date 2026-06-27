package com.example.orderingapp.order.madeOrders;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.connection.webSocket.WebSocketConnection;
import com.example.orderingapp.dto.Employee;
import com.example.orderingapp.dto.Order;
import com.example.orderingapp.order.processedOrders.ConfirmOrderPaymentServer;
import com.example.orderingapp.order.processedOrders.PostStaffProcessedOrderServer;
import com.example.orderingapp.order.processedOrders.RollbackOrderPaymentServer;
import com.example.orderingapp.dto.StaffProcessedOrder;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import  com.example.orderingapp.apiKeyManagement.database.APIKeyDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.interfaces.http.ClientAuth;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MadeOrdersActivity extends AppCompatActivity implements ClientAuth, ShowToastFromBgThread
{
    private interface SetField {
        void setFields();
    }

    public interface UIUpdateThread  {
        void run ();
    }

    private String apiKey;
    private int tableNumber;
    private String uuid;
    private HashMap<String, Integer> productNameToQty;
    private HashMap<String, BigDecimal> productNameToPrices;
    private boolean isPaid;
    private Employee employee;
    boolean isCustomer, isStaff, isEmployee;
    private String orderCreatedAt;
    private StaffProcessedOrder staffProcessedOrder;
    private Order order;
    private final Lock lock = new ReentrantLock();
    private final Condition uiRunningCondition = lock.newCondition();
    private boolean currentlyRunningOnUI;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.made_orders_activity);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.made_orders_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uuid = getIntent().getStringExtra("uuid");
        staffProcessedOrder = (StaffProcessedOrder) getIntent().getSerializableExtra("staff_processed_order");

        setTitle();

        setBottomLayoutGone();
        setBottomButtonsGone();

        checkOrderDetails();
        webSocketInit ();
    }

    private void setTitle () {
        TextView title = findViewById(R.id.header_title_textview);
        title.setText(getIntent().getStringExtra("title"));
        findViewById(R.id.btn_header_back).setOnClickListener(v -> finish());
    }

    private void checkOrderDetails() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->
        {
            isPaid = new OrderPaymentCheckServer().isOrderPaid(uuid);
            order = getOrder();

            runOnUiThread(() ->
            {
                setFieldBy(order);
                loadOrderData();

                metadataUIPopulate();
//                staffMetadataUIPopulate(staffProcessedOrder);

                setButtonsBottomLayoutVisible();
                setBottomButtonFunction();
            });
        });
        executorService.close();
    }

    private void webSocketInit () {
        WebSocketConnection.getInstance().runSocket(new MadeOrdersWebSocket(this));
    }

    private Order getOrder () {
        return staffProcessedOrder == null ? new OrderCreationCheckServer().checkOrderCreation(uuid)
                :staffProcessedOrder.getOrder();
    }

    private void setFieldBy (Order order)
    {
        if (order == null)
            setFieldByIntent().setFields();
        else
            setFieldByOrder(order).setFields();
    }

    private SetField setFieldByIntent ()
    {
        return () -> {
            setFields ((HashMap<String, Integer>) getIntent().getSerializableExtra("productNameToQty"), (HashMap<String, BigDecimal>) getIntent().getSerializableExtra("productNameToPrices"),
                        getIntent().getStringExtra("uuid"), Integer.parseInt(getIntent().getStringExtra("table_number")), "");
        };
    }

    private SetField setFieldByOrder (Order order)
    {
        for (Map.Entry<String, BigDecimal> entry : order.getProductNameToPrice().entrySet()) {

            String productName = entry.getKey();
            int qty = order.getProductNameToQty().get(productName);
            order.getProductNameToPrice().put(productName, entry.getValue().multiply(new BigDecimal(qty)));
        }

        return () -> {
            setFields (order.getProductNameToQty(), order.getProductNameToPrice(),
                    order.getUuid(), order.getTableNumber(), order.getCreatedAt().format(DateTimeFormatter.ofPattern("d MMMM uuuu h:m:s")));
        };
    }

    private void setFields
            (HashMap<String, Integer> productNameToQty, HashMap<String, BigDecimal> productNameToPrices,
             String uuid, int tableNumber, String orderCreatedAt)
    {
        this.productNameToQty = productNameToQty;
        this.productNameToPrices = productNameToPrices;
        this.uuid = uuid;
        this.tableNumber = tableNumber;
        this.orderCreatedAt = orderCreatedAt;

        this.apiKey = getApiKey();
        this.employee = employee();

        this.isCustomer = (employee == null) && (!apiKey.isBlank());
        this.isStaff = (employee != null) && (employee.getRole().equalsIgnoreCase("staff")) && (apiKey.isBlank());

        this.isEmployee = (employee != null) && (employee.getRole().equalsIgnoreCase("staff")
                || employee.getRole().equalsIgnoreCase("boss") || employee.getRole().equalsIgnoreCase("boss"));
    }

    private void metadataUIPopulate ()
    {
//        runOnUiThread(() ->
//        {
            UI.setTextViewTxt(new TextView[] {findViewById(R.id.order_id_textview), findViewById(R.id.table_number_textview), findViewById(R.id.is_order_paid_textview), findViewById(R.id.order_created_at)},
                    new String[] {uuid, String.valueOf(tableNumber), String.valueOf(isPaid), orderCreatedAt});

//        });
    }

    public void setIsOrderPaidTextView (boolean isPaid) {
        runOnUiThread(() -> {
            ((TextView)findViewById(R.id.is_order_paid_textview)).setText(String.valueOf(isPaid));
            notifyUIUpdatingThreads();
        });
    }

//    private void staffMetadataUIPopulate (StaffProcessedOrder staffProcessedOrder)
//    {
//        if (staffProcessedOrder == null)
//            return;
//
////        runOnUiThread(() ->
////        {
//            findViewById(R.id.staff_order_metadata).setVisibility(View.VISIBLE);
//
//            UI.setTextViewTxt(new TextView[] {findViewById(R.id.processed_by_textview), findViewById(R.id.processed_at_textview)},
//                    new String[] {staffProcessedOrder.getProcessedBy(), staffProcessedOrder.getProcessedAt().format(DateTimeFormatter.ofPattern("d MMMM uuuu h:m:s"))});
////        });
//    }

    private void loadOrderData()
    {
        LinearLayout container = findViewById(R.id.ll_ordered_items_container);
        container.removeAllViews();
        
        BigDecimal total = new BigDecimal(0.0);

        Set<String> productNamesSet = new HashSet<>();

        for (Map.Entry<String, Integer> entry : productNameToQty.entrySet())
        {
            String name = entry.getKey(); // product name
            int quantity = entry.getValue(); // product_quantity
            BigDecimal price = productNameToPrices.get(name);

            if (!productNamesSet.contains(name)) {
                productNamesSet.add(name);
                total = total.add(price);
            }

            LinearLayout layout = getOrderedItemLayout();

            TextView text1 = new TextView(this);

            View betweenTxt = getBetweenTxtView();

            TextView text2 = new TextView(this);

            View divider = getDividerView();

            setTextViewStyles(text1, text2, name, quantity, price);

            addViewsToLayout(container, divider, layout, text1, betweenTxt, text2);
        }

        TextView tvTotal = findViewById(R.id.tv_total_price);
        tvTotal.setText(String.format("$%.2f", total));
    }

    private LinearLayout getOrderedItemLayout() {

        var layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.setMargins(0, 20, 0, 0);

        return layout;
    }

    private View getBetweenTxtView ()
    {
        View betweenTxt = new View(this);
        betweenTxt.setLayoutParams(new LinearLayout.LayoutParams(0,0,1));

        return betweenTxt;
    }

    private View getDividerView () {
        View divider = new View(this);

        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(getResources().getColor(R.color.primary_color));

        return divider;
    }

    private void setTextViewStyles (TextView text1, TextView text2, String name, int quantity, BigDecimal subtotal)
    {
        UI.setTextViewTxt(new TextView[] {text1, text2},
                new String[] { name + " (x" + quantity + ")", String.format("$%.2f", subtotal) });

        UI.setTextViewSize(new TextView[] {text1, text2},
                new int[] {20,20});

        UI.setTextViewStyle(new TextView[] {text1, text2},
                new int[] {Typeface.BOLD, Typeface.BOLD});
    }

    private void addViewsToLayout (LinearLayout container, View divider, LinearLayout layout, TextView text1, View betweenTxt, TextView text2)
    {
        UI.addViewsToLayout(layout, new View[] {text1, betweenTxt, text2});
        UI.addViewsToLayout(container, new View[] {layout, divider});
    }

    private void setBottomButtonFunction ()
    {

        if (isCustomer)
            setCustomer();

        if (isStaff)
            setStaff();

        if (isEmployee)
            setEmployee();
    }
    private void setBottomButtonsGone () {
        makeProcessOrderButtonGone();
        makePaymentRollbackButtonGone();
        makeConfirmOrderPaymentButtonGone();
        makeOrderDeleteButtonGone();
        makeOrderSubmitButtonGone();
    }
    public void makeProcessOrderButtonGone (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0) {
            findViewById(R.id.process_order_button).setVisibility(View.GONE);
            return;
        }
        runOnUiThread(() -> {
            findViewById(R.id.process_order_button).setVisibility(View.GONE);
            notifyUIUpdatingThreads();
        });
    }
    public void makePaymentRollbackButtonGone (boolean...callFromBgThread) {

        if (callFromBgThread.length == 0) {
            findViewById(R.id.payment_rollback_button).setVisibility(View.GONE);
            return;
        }

        runOnUiThread(() -> {
            findViewById(R.id.payment_rollback_button).setVisibility(View.GONE);
            notifyUIUpdatingThreads();
        });
    }
    public void makeConfirmOrderPaymentButtonGone () {
        runOnUiThread(() -> {
            findViewById(R.id.confirm_order_payment_button).setVisibility(View.GONE);
            notifyUIUpdatingThreads();
        });
    }
    public void makeOrderDeleteButtonGone () {
        findViewById(R.id.order_delete_button).setVisibility(View.GONE);
    }
    public void makeOrderSubmitButtonGone (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0) {
            findViewById(R.id.btn_submit_order).setVisibility(View.GONE);
            return;
        }

        runOnUiThread(() -> {
            findViewById(R.id.btn_submit_order).setVisibility(View.GONE);
            notifyUIUpdatingThreads();
        });
    }

    private void setButtonsBottomLayoutVisible () {
        findViewById(R.id.made_order_footer).setVisibility(View.VISIBLE);
    }
    private void setBottomLayoutGone () {
        findViewById(R.id.made_order_footer).setVisibility(View.GONE);
        findViewById(R.id.staff_order_metadata).setVisibility(View.GONE);
    }

    private void setCustomer () {

        // means the order is alive and it has been paid ... so no update or delete or any other manipulation will be done

        if (theOrderIsPaid())
            return;

        setOrderSubmitBtnFunction();
        setOrderDeleteBtn();
    }

    private void setStaff () {
        setConfirmOrderPaymentBtn();
        setRollbackOrderPaymentBtn();
        setStaffProcessedOrder();
    }

    private void setEmployee() {

//        if (staffProcessedOrder == null)
//            return;

        findViewById(R.id.staff_order_metadata).setVisibility(View.VISIBLE);

        String processedBy = getStaffProcessedBy();
        String dateString = getProcessedAt();

        UI.setTextViewTxt
                (new TextView[]{ findViewById(R.id.processed_by_textview), findViewById(R.id.processed_at_textview)},
                new String[] {processedBy, dateString});
    }

    public void setProcessedByTextview (String text) {
        runOnUiThread(() ->
        {
            TextView processedBy = findViewById(R.id.processed_by_textview);
            processedBy.setText(text);
            notifyUIUpdatingThreads();
        });
    }

    public void setProcessedAtTextview (String text) {
        runOnUiThread(() -> {
            TextView processedAt = findViewById(R.id.processed_at_textview);
            processedAt.setText(text);
            notifyUIUpdatingThreads();
        });
    }

    private void setConfirmOrderPaymentBtn () {

        if (theOrderIsPaid())
            return;

        if (!isAssignedStaff())
            return;

        setConfirmOrderPaymentButtonFunction();
    }

    private void setRollbackOrderPaymentBtn () {

        if (!isAssignedStaff())
            return;

        if (!isPaid)
            return;

        setRollbackButtonFunction();
    }

    private void setStaffProcessedOrder () {

        if (staffProcessedOrder.getProcessedBy() != null)
            return;

        MaterialButton button = findViewById(R.id.process_order_button);

        makeProcessOrderButtonVisible();

        button.setOnClickListener(v -> {
            new PostStaffProcessedOrderServer().postStaffProcessOrder(this, uuid);
        });
    }


    public void setOrderSubmitBtnFunction() {

        if (order != null)
            return;

        makeOrderSubmitButtonVisible();
        setSubmitOrderBtnListener();
    }

    private void setSubmitOrderBtnListener () {

        MaterialButton button = findViewById(R.id.btn_submit_order);

        button.setOnClickListener(v -> {
            Order order = createOrderObject(false);
            new CreateOrderServer().makeOrder(this, order);
        });
    }

    private void setOrderDeleteBtn () {

        if (isPaid || order == null)
            return;

        setOrderDeleteButtonFunction();
    }

    public void setOrderDeleteButtonFunction (boolean...runOnUIThread) {

        if (runOnUIThread.length == 0)
            makeOrderDeleteButtonVisible();
        else
            makeOrderDeleteButtonVisible(true);

        setOrderDeleteBtnListener();
    }

    private void setOrderDeleteBtnListener() {

        MaterialButton button = findViewById(R.id.order_delete_button);

        button.setOnClickListener(v -> {
            Order order = createOrderObject(false);
            new DeleteOrderServer().deleteOrder(this, order);
        });
    }

    public void setConfirmOrderPaymentButtonFunction (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0)
            makeConfirmOrderPaymentButtonVisible();
        else
            makeConfirmOrderPaymentButtonVisible(true);

        setConfirmOrderPaymentButtonListener();
    }

    public void setRollbackButtonFunction (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0)
            makePaymentRollbackButtonVisible();
        else
            makePaymentRollbackButtonVisible(true);

        setRollbackButtonListener();
    }

    private void setRollbackButtonListener () {

        MaterialButton button = findViewById(R.id.payment_rollback_button);

        button.setOnClickListener(v -> {
            new RollbackOrderPaymentServer().rollbackOrderPayment(this, uuid);
        });
    }

    private void setConfirmOrderPaymentButtonListener() {

        MaterialButton button = findViewById(R.id.confirm_order_payment_button);

        button.setOnClickListener(v -> {
            new ConfirmOrderPaymentServer().confirmOrderPayment(this, uuid);
        });
    }
    public void makeProcessOrderButtonVisible () {
        findViewById(R.id.process_order_button).setVisibility(View.VISIBLE);
    }
    public void makePaymentRollbackButtonVisible (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0) {
            findViewById(R.id.payment_rollback_button).setVisibility(View.VISIBLE);
            return;
        }
        runOnUiThread(() -> {
            findViewById(R.id.payment_rollback_button).setVisibility(View.VISIBLE);
            notifyUIUpdatingThreads();
        });
    }

    public void makeConfirmOrderPaymentButtonVisible (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0) {
            findViewById(R.id.confirm_order_payment_button).setVisibility(View.VISIBLE);
            return;
        }
        runOnUiThread(() -> {
            findViewById(R.id.confirm_order_payment_button).setVisibility(View.VISIBLE);
            notifyUIUpdatingThreads();
        });
    }

    public void makeOrderDeleteButtonVisible (boolean...calledFromBgThread) {

        if (calledFromBgThread.length == 0) {
            findViewById(R.id.order_delete_button).setVisibility(View.VISIBLE);
            return;
        }

        runOnUiThread(() -> {
            findViewById(R.id.order_delete_button).setVisibility(View.VISIBLE);
            notifyUIUpdatingThreads();
        });

    }

    public void makeOrderSubmitButtonVisible () {
        findViewById(R.id.btn_submit_order).setVisibility(View.VISIBLE);
    }

    private Order createOrderObject (boolean isPaid)
    {
        Order order = new Order();

        order.setUuid(uuid);
        order.setTableNumber(tableNumber);
        order.setProductNameToQty(productNameToQty);
        order.setProductNameToPrice(productNameToPrices);
        order.setPaid(isPaid);
        order.setApiKey(apiKey);

        return order;
    }

    private Employee employee () {
        return new EmployeeDatabase(this).readEmployee();
    }

    private String getApiKey () {
        return new APIKeyDatabase(this).readAPIKey();
    }

    private boolean isAssignedStaff () {
        return getStaffProcessedBy() != null && employee.getUsername().equals(getStaffProcessedBy());
    }

    private boolean theOrderIsPaid () {
        return order != null && isPaid;
    }

    private String getStaffProcessedBy () {
        return staffProcessedOrder.getProcessedBy() == null ? "" : staffProcessedOrder.getProcessedBy();
    }

    private String getProcessedAt () {
        return staffProcessedOrder.getProcessedAt() == null ? "" : staffProcessedOrder.getProcessedAt().format(DateTimeFormatter.ofPattern("d MMMM uuuu h:m:s"));
    }

    public void updateUI (UIUpdateThread uiUpdateThread)
    {
        lock.lock();

        try
        {
            while (currentlyRunningOnUI)
                uiRunningCondition.await();

            currentlyRunningOnUI = true;
            uiUpdateThread.run();
        }
        catch (Exception e) {}
        finally {
            lock.unlock();
        }
    }

    private void notifyUIUpdatingThreads () {


        lock.lock();
        try {
            currentlyRunningOnUI = false;
            uiRunningCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean authorize(ShowToastFromBgThread toast) {

        if (!isCustomer && !isStaff) {
            showToast("You are not authorized to perform this action");
            return false;
        }
        return true;
    }

    @Override
    public void showToast(String message) {
        Toasts.showShortToast(this, message);
    }

    public boolean validateUuid (String uuid) {
        return uuid.equals(this.uuid);
    }
}
