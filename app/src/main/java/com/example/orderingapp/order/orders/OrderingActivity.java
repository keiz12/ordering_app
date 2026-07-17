package com.example.orderingapp.order.orders;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.convert.UnitConverter;
import com.example.orderingapp.dto.*;
import com.example.orderingapp.R;
import com.example.orderingapp.customerFeedback.server.PostCustomerFeedbackServer;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.order.madeOrders.MadeOrdersActivity;
import com.example.orderingapp.productCarousel.ProductCarousel;
import com.example.orderingapp.productManagement.products.ProductsActivityServer;
import com.example.orderingapp.toast.Toasts;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderingActivity extends AppCompatActivity implements ShowToastFromBgThread, Serializable {

    private int totalOrderedCount = 0;
    private TextView tvCartBadge;
    private String uuid = UUID.randomUUID().toString();
    private final Map<String, Integer> productNameToQty = new HashMap<>();
    private final Map<String, BigDecimal> productNameToPrices = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ordering_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ordering_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCartBadge = findViewById(R.id.tv_cart_badge);

        findViewById(R.id.rl_cart_container).setOnClickListener(v -> {
            if (productNameToQty.isEmpty()) {
                Toasts.showShortToast(this, "Your selection is empty");
                return;
            }
            openOrderSummary();
        });

        setClearOrderViewListener ();

        loadMenu();

        setCustomerFeedbackButtonListener();
    }

    private void loadMenu()
    {
        new ProductsActivityServer().getProducts(this, products ->
        {
            LinearLayout root = findViewById(R.id.product_carousel_root_layout);
            ProductCarousel carousel = ProductCarousel.getInstance();
            carousel.populateCarousel(this, products, root, this::createOrderActions);
        });
    }

    /**
     * Creates quantity field and Add/Remove buttons for the carousel.
     */
    private List<View> createOrderActions(Product product) {

        List<View> actions = new ArrayList<>();

        // Quantity Input Field
        TextInputLayout tilQty = getTextInputLayout();
        TextInputEditText etQty = getTextInputEditText(tilQty);

        // Add to Order Button
        MaterialButton btnAdd = getAddToOrderBtn();
        btnAdd.setOnClickListener(v -> addToOrderListener(etQty, product));

//         Remove (optional, but requested 'remove from order' feel)
        MaterialButton btnRemove = getRemoveFromOrderBtn();
        btnRemove.setOnClickListener(v -> removeFromOrderEventListener(product));

        actions.add(tilQty);
        actions.add(btnAdd);
        actions.add(btnRemove);
        return actions;
    }

    private TextInputLayout getTextInputLayout ()
    {
        var layout = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        layout.setHint("Qty");
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(new UnitConverter().dpToPx(this,90), LinearLayout.LayoutParams.WRAP_CONTENT);
        qtyParams.setMarginEnd(12);
        layout.setLayoutParams(qtyParams);
        return layout;
    }

    private TextInputEditText getTextInputEditText (TextInputLayout tilQty)
    {
        var editTxt = new TextInputEditText(tilQty.getContext());
        editTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
        tilQty.addView(editTxt);
        return editTxt;
    }

    private MaterialButton getAddToOrderBtn ()
    {
        var btnAdd = new MaterialButton(this);
        btnAdd.setText("Add to Order");
        btnAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return btnAdd;
    }

    private void addToOrderListener (TextInputEditText etQty, Product product)
    {
        String qtyStr = etQty.getText().toString().trim();

        if (qtyStr.isEmpty()) {
            etQty.setError("Specify quantity");
            return;
        }
        int qty = Integer.parseInt(qtyStr);
        if (qty <= 0) {
            etQty.setError("Must be > 0");
            return;
        }

        addToProductNameToQty(product, qty);
        addToProductNameToPrice(product,qty);

        updateCartBadge();
        Toasts.showLongToast(this, qty + " " + product.getName() + " added to selection");
    }

    private void addToProductNameToQty (Product product, int qty) {

        if (productNameToQty.containsKey(product.getName()))
            totalOrderedCount -= productNameToQty.get(product.getName());

        productNameToQty.put(product.getName(), qty);

        totalOrderedCount += qty;
    }

    private void addToProductNameToPrice (Product product, int qty) {
        productNameToPrices.put(product.getName(), BigDecimal.valueOf(product.getPrice()*qty));
    }

    private MaterialButton getRemoveFromOrderBtn ()
    {
        var btnRemove = new MaterialButton(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Button_TextButton);
        btnRemove.setText("Clear");
        btnRemove.setTextColor(android.graphics.Color.RED);
        return btnRemove;
    }

    private void removeFromOrderEventListener (Product product)
    {
        Integer current = productNameToQty.remove(product.getName());

        productNameToQty.remove(product.getName());

        if (current.equals(null)) return;

        totalOrderedCount -= current;
        updateCartBadge();
        Toasts.showLongToast(this, product.getName() + " removed from order");
    }


    private void updateCartBadge() {
        if (totalOrderedCount > 0) {
            tvCartBadge.setText(String.valueOf(totalOrderedCount));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void openOrderSummary()
    {
        if (productNameToPrices.keySet().isEmpty()) {
            showToast("Your order list is empty");
            return;
        }

        if (((EditText)findViewById(R.id.et_table_number)).getText().toString().isBlank()) {
            tableNumberWarn();
            return;
        }

        Intent intent = new Intent(this, MadeOrdersActivity.class);
        intent.putExtra("productNameToQty", (Serializable) productNameToQty);
        intent.putExtra("productNameToPrices", (Serializable) productNameToPrices);
        intent.putExtra("table_number", ((EditText)findViewById(R.id.et_table_number)).getText().toString());
        intent.putExtra("uuid",uuid);
        intent.putExtra("title", "Your Order");
        startActivity(intent);
    }

    private void tableNumberWarn () {
        ((EditText)findViewById(R.id.et_table_number)).setError("required");
        showToast("Fill the table number please");
    }

    private void setClearOrderViewListener ()
    {
        findViewById(R.id.clear_order_view).setOnClickListener(v ->
        {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Order")
                    .setMessage("Are you sure you want to clear your order?")
                    .setPositiveButton("Yes", (dialog, num) -> {
                        resetAll();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void resetAll () {
        resetUuid();
        resetTotalOrderedCount();
        clearProductNameToQty();
        clearProductNameToPrices();
        updateCartBadge();
        Toasts.showLongToast(this, "Order cleared");
    }

    private void setCustomerFeedbackButtonListener ()
    {
        findViewById(R.id.customer_feedback_button).setOnClickListener(v -> {

            disableCustomerShareFeedbackButton ();
            showCustomerFeedbackShareLoadingTextview();
            TextInputEditText textInputEditText = findViewById(R.id.customer_feedback_textInput);
            new PostCustomerFeedbackServer(this, textInputEditText.getText().toString()).postCustomerFeedback ();
        });
    }

    public void resetUuid() {
        this.uuid = UUID.randomUUID().toString();
    }

    private void clearProductNameToQty () {
        productNameToQty.clear();
    }

    private void clearProductNameToPrices () {
        productNameToPrices.clear();
    }

    private void resetTotalOrderedCount() {
        totalOrderedCount = 0;
    }

    @Override
    public void showToast(String message) {
        Toasts.showShortToast(this, message);
    }

    public void showCustomerFeedbackShareLoadingTextview () {
        findViewById(R.id.customer_feedback_share_loading_textview).setVisibility(View.VISIBLE);
    }

    public void hideCustomerFeedbackShareLoadingTextview () {
        runOnUiThread(() -> {
            findViewById(R.id.customer_feedback_share_loading_textview).setVisibility(View.GONE);
        });
    }

    public void enableCustomerShareFeedbackButton () {
        runOnUiThread(() -> {
            findViewById(R.id.customer_feedback_button).setEnabled(true);
        });
    }

    private void disableCustomerShareFeedbackButton () {
        findViewById(R.id.customer_feedback_button).setEnabled(false);
    }

    public void setBlankCustomerShareFeedbackInput () {
        runOnUiThread(() -> {
            ((TextInputEditText) findViewById(R.id.customer_feedback_textInput)).setText("");
        });
    }
}
