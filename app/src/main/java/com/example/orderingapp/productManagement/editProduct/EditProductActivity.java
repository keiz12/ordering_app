package com.example.orderingapp.productManagement.editProduct;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EditProductActivity extends AppCompatActivity {

    private String originalName;
    private final List<Uri> newImageUris = new LinkedList<>();
    private final List<String> currentImages = new ArrayList<>();
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

    private void loadProductData() {
        Intent intent = getIntent();
        if (intent == null) return;

        originalName = intent.getStringExtra("product_name");
        String price = intent.getStringExtra("product_price");
        String description = intent.getStringExtra("product_description");
        ArrayList<String> images = intent.getStringArrayListExtra("product_images");

        ((EditText) findViewById(R.id.edit_product_name_input)).setText(originalName);
        ((EditText) findViewById(R.id.edit_product_price_input)).setText(price != null ? price.replace("$", "") : "");
        ((EditText) findViewById(R.id.edit_product_description_input)).setText(description);

        if (images != null) {
            currentImages.addAll(images);
            for (String path : images) {
                addImageToLayout(path, null);
            }
        }
    }

    private void addImageToLayout(@Nullable String existingPath, @Nullable Uri newUri) {
        LinearLayout container = findViewById(R.id.edit_images_container);
        MaterialCardView card = createCard();
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (newUri != null) {
            iv.setImageURI(newUri);
        } else if (existingPath != null) {
            int resId = getResources().getIdentifier(existingPath, "drawable", getPackageName());
            if (resId != 0) {
                iv.setImageResource(resId);
            } else {
                File productsDir = new File(getFilesDir(), "products");
                File file = findImageFile(productsDir, existingPath);
                if (file != null) {
                    iv.setImageURI(Uri.fromFile(file));
                }
            }
        }

        card.addView(iv);
        card.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Image")
                    .setMessage("Remove this image?")
                    .setPositiveButton("Yes", (d, w) -> {
                        container.removeView(card);
                        if (newUri != null) newImageUris.remove(newUri);
                        if (existingPath != null) currentImages.remove(existingPath);
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
        container.addView(card);
    }

    private File findImageFile(File dir, String name) {
        File file = new File(dir, name);
        if (file.exists()) return file;
        String[] extensions = {".png", ".webp", ".jpg", ".jpeg", ".bmp"};
        for (String ext : extensions) {
            File f = new File(dir, name + ext);
            if (f.exists()) return f;
        }
        return null;
    }

    public void addImageClicked() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            newImageUris.add(uri);
            addImageToLayout(null, uri);
        }
    }

    private MaterialCardView createCard() {
        int size = unitConverter.dpToPx(this, 100);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(8, 8, 8, 8);
        MaterialCardView card = new MaterialCardView(this);
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
        String newName = ((EditText) findViewById(R.id.edit_product_name_input)).getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        File productsDir = new File(getFilesDir(), "products");
        if (!productsDir.exists()) productsDir.mkdirs();

        // If the name changed, we should delete/rename old files associated with the old name
        if (!newName.equals(originalName)) {
            File[] files = productsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(originalName)) {
                        file.delete();
                    }
                }
            }
        }

        try {
            int count = 1;
            // Note: Since we don't have a database, we'll re-save everything to internal storage 
            // to ensure consistency with the current name.
            
            // This is a simplified simulation of updating product metadata and images.
            for (Uri uri : newImageUris) {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bm = BitmapFactory.decodeStream(is);
                File file = new File(productsDir, newName + count + ".png");
                FileOutputStream out = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                is.close();
                count++;
            }
            
            Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
