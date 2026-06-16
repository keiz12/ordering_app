package com.example.orderingapp.productManagement.addProduct;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity
{
    private final List<ImageView> imageViews = new LinkedList<>();
    private final List<Uri> imageUris = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_product_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_product_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setHeader ();
        setListeners();
    }

    private void setListeners() {
        setAddProductImageListener();
        setClearButtonListener();
        setSaveButtonListener();
    }

    private void setAddProductImageListener() {
        MaterialCardView addButton = findViewById(R.id.add_product_image_button);
        addButton.setOnClickListener(l -> addProductImage());
    }

    private void setClearButtonListener() {
        MaterialButton clearButton = findViewById(R.id.btn_delete_product);
        clearButton.setOnClickListener(l -> clearButtonClick());
    }

    private void setSaveButtonListener() {
        MaterialButton saveButton = findViewById(R.id.btn_save_product);
        saveButton.setOnClickListener(l -> saveButtonClicked());
    }

    private void setHeader() {
        TextView textView = findViewById( R.id.header_title_textview);
        textView.setText("Add New Product");
    }

    public void addProductImage()
    {
        launchIntent ();
    }

    private void launchIntent () {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null)
            return;
        android.net.Uri uri = data.getData();

        imageUris.add(uri);
        imageViews.add(new ImageView(this));
        ImageView imageView = imageViews.get(imageViews.size()-1);

        imageView.setImageURI(uri);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        LinearLayout layout = findViewById(R.id.added_product_images_container);
        MaterialCardView materialCardView = createMaterialCardView();
        setMaterialCardViewListener(layout, materialCardView, imageView);
        materialCardView.addView(imageView);
        layout.addView(materialCardView);
    }

    private MaterialCardView createMaterialCardView ()
    {
        UnitConverter unitConverter = new UnitConverter();

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(unitConverter.dpToPx(this, 100), unitConverter.dpToPx(this, 100));
        MaterialCardView cardView = new MaterialCardView(this);

        int id = View.generateViewId();

        cardView.setId(id);
        cardView.setLayoutParams(cardParams);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        return cardView;

    }

    private void setMaterialCardViewListener (LinearLayout layout, MaterialCardView materialCardView, ImageView imageView)
    {


        materialCardView.setOnLongClickListener(v -> {

            // 1. Build the Popup Dialog confirmation window
            new AlertDialog.Builder(this)
                    .setTitle("Remove Image")
                    .setMessage("Are you sure you want to remove this image?")

                    // 2. Handle the "Yes" action
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear the image source completely
                        imageView.setImageURI(null);
                        imageView.setImageDrawable(null);
                        imageViews.remove(imageView);
                        layout.removeView(materialCardView);
                    })

                    // 3. Handle the "No" action
                    .setNegativeButton("No", (dialog, which) -> {
                        // Just dismiss the dialog window and do nothing
                        dialog.dismiss();
                    })
                    .show();

            // 4. Return true to signal that the touch event was fully handled
            return true;
        });;
    }

    public void clearButtonClick() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Form")
                .setMessage("Are you sure you want to clear all inputs and images?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ((EditText)findViewById(R.id.add_product_name_input)).setText("");
                    ((EditText)findViewById(R.id.add_product_price_input)).setText("");
                    ((EditText)findViewById(R.id.add_product_description_input)).setText("");

                    LinearLayout layout = findViewById(R.id.added_product_images_container);
                    // Remove all images except the add button (which is at index 0 in the XML usually,
                    // but wait, it might be added programmatically or be the first child).
                    // In the XML, add_product_image_button is the first child.
                    int childCount = layout.getChildCount();
                    for (int i = childCount - 1; i > 0; i--) {
                        layout.removeViewAt(i);
                    }
                    imageViews.clear();
                    imageUris.clear();
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void saveButtonClicked() {

        String productName = ((EditText)findViewById(R.id.add_product_name_input)).getText().toString();

        if (productName.isEmpty()) {
            Toast.makeText(this, "Please enter product name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        File productsDir = new File(getFilesDir(), "products");
        if (!productsDir.exists()) {
            productsDir.mkdirs();
        }

        int count = 1;

        try {
            count = Files.list(productsDir.toPath()).count() == 0 ? 1 : (int) Files.list(productsDir.toPath()).count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            for (Uri uri : imageUris)
            {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                File file = new File(productsDir, productName + count + ".png");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                inputStream.close();
                count++;
            }
            Toast.makeText(this, "Product saved successfully with " + (count - 1) + " images", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving product images: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
