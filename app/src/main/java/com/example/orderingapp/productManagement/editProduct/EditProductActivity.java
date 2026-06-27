package com.example.orderingapp.productManagement.editProduct;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class EditProductActivity extends AppCompatActivity implements ShowToastFromBgThread
{
    private interface EventCallBack {
        void run();
    }

    private String originalName;
    private final List<String> oldImages = new ArrayList<>();
    private final List<String> oldRemovedImages = new ArrayList<>();
    private final List<String> newImages = new ArrayList<>();
    private final UnitConverter unitConverter = new UnitConverter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_product_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_product_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupHeader();
        loadProductData();
        setListeners();
    }

    private void setListeners() {
        setAddImageListener();
        setCancelButtonListener();
        setSaveButtonListener();
    }

    private void setAddImageListener() {
        MaterialCardView addButton = findViewById(R.id.btn_add_edit_image);
        addButton.setOnClickListener(l -> addImageClicked());
    }

    private void setCancelButtonListener() {
        MaterialButton cancelButton = findViewById(R.id.btn_cancel_edit);
        cancelButton.setOnClickListener(l -> cancelClicked());
    }

    private void setSaveButtonListener() {
        MaterialButton saveButton = findViewById(R.id.btn_save_edit);
        saveButton.setOnClickListener(l -> updateClicked());
    }

    private void setupHeader() {
        TextView title = findViewById(R.id.header_title_textview);
        if (title != null) title.setText("Edit Product");
    }

    private void loadProductData() 
    {
        Intent intent = getIntent();
        if (intent == null) return;

        originalName = intent.getStringExtra("product_name");
        double price = intent.getDoubleExtra("product_price", 0);
        String description = intent.getStringExtra("product_description");
        ArrayList<String> images = intent.getStringArrayListExtra("product_images");
        
        UI.setTextViewTxt(new TextView[] {findViewById(R.id.edit_product_name_input), findViewById(R.id.edit_product_price_input), findViewById(R.id.edit_product_description_input) }
                , new String[] { originalName, String.valueOf(price), description });

        oldImages.addAll(images);
        for (String path : images)
            addImageToLayout(path, () -> {
                oldRemovedImages.add(path);
                oldImages.remove(path);
            });
    }

    private void addImageToLayout( @Nullable String imagePath, EventCallBack callBack)
    {
        LinearLayout container = findViewById(R.id.edit_images_container);
        MaterialCardView card = createCard();
        ImageSlider slider = new ImageSlider(this);
        slider.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        slider.setImageList(List.of(new SlideModel(imagePath, ScaleTypes.CENTER_INSIDE)));

        slider.setClickable(false);
        slider.setFocusable(false);
        slider.setLongClickable(false);

        card.addView(slider);
        cardListener(card, callBack);
        container.addView(card);
    }

    private void cardListener (MaterialCardView card, EventCallBack callBack)
    {
        LinearLayout container = findViewById(R.id.edit_images_container);

        card.setOnClickListener(l -> {
            System.out.println("Clicked");
        });

        card.setOnLongClickListener(v ->
        {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Image")
                    .setMessage("Remove this image?")
                    .setPositiveButton("Yes", (d, w) ->
                    {
                        container.removeView(card);
                        callBack.run();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    public void addImageClicked() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        
        Uri uri = data.getData();
        
        newImages.add(uri.toString());
        
        addImageToLayout(newImages.get(newImages.size()-1), () -> 
        {
            newImages.remove(uri.toString());
        });
    }

    private MaterialCardView createCard() {
        int size = unitConverter.dpToPx(this, 100);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(8, 8, 8, 8);

        // Override onInterceptTouchEvent to capture all touch events at the card level,
        // preventing the ImageSlider from consuming them.
        MaterialCardView card = new MaterialCardView(this) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return true;
            }
        };

        card.setLayoutParams(lp);
        card.setRadius(unitConverter.dpToPx(this, 12));
        card.setCardElevation(unitConverter.dpToPx(this, 2));
        card.setClickable(true);
        card.setFocusable(true);
        return card;
    }

    public void cancelClicked() {
        finish();
    }

    public void updateClicked() {

        String productName = ((EditText)findViewById(R.id.edit_product_name_input)).getText().toString();

        String productPrice = ((EditText)findViewById(R.id.edit_product_price_input)).getText().toString();

        String productDescription = ((EditText)findViewById(R.id.edit_product_description_input)).getText().toString();

        if (productName.isBlank() || productPrice.isBlank() || productDescription.isBlank()) {
            Toasts.showShortToast(this, "Please fill in all fields");
            return;
        }

        double price = getPriceFromString(productPrice);

        if (price == Double.NEGATIVE_INFINITY)
            return;

        new EditProductServer(
                oldRemovedImages,
                newImages,
                originalName,
                this,
                productName,
                price,
                productDescription
        ).execute();
    }

    private double getPriceFromString (String productPrice) {
        try {
            String cleanPrice = productPrice.replace("$", "").trim();
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            Toasts.showShortToast(this, "Invalid price format");
        }
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> {
            Toasts.showShortToast(this, message);
        });
    }
}
