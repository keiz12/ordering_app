package com.example.orderingapp.productManagement.addProduct;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;
import com.example.orderingapp.dto.Product;
import com.example.orderingapp.ui.UI;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class ManageAddedProduct {

    private final List<ImageView> imageViews = new LinkedList<>();
    private final List<Uri> imageUris = new LinkedList<>();

    public void onActivityResult(AddProductActivity activity, LinearLayout layout, int requestCode, int resultCode, @Nullable Intent data)
    {

        if (resultCode != RESULT_OK || data == null)
            return;
        android.net.Uri uri = data.getData();

        imageUris.add(uri);
        imageViews.add(new ImageView(activity));

        ImageView imageView = imageViews.get(imageViews.size()-1);

        imageView.setImageURI(uri);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        MaterialCardView materialCardView = createMaterialCardView(activity);
        setMaterialCardViewListener(activity, layout, materialCardView, imageView, uri);
        materialCardView.addView(imageView);
        layout.addView(materialCardView);
    }

    private MaterialCardView createMaterialCardView (AddProductActivity activity)
    {
        UnitConverter unitConverter = new UnitConverter();

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(unitConverter.dpToPx(activity, 100), unitConverter.dpToPx(activity, 100));
        MaterialCardView cardView = new MaterialCardView(activity);

        int id = View.generateViewId();

        cardView.setId(id);
        cardView.setLayoutParams(cardParams);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        return cardView;

    }

    private void setMaterialCardViewListener (AddProductActivity activity, LinearLayout layout, MaterialCardView materialCardView, ImageView imageView, Uri uri)
    {


        materialCardView.setOnLongClickListener(v -> {

            // 1. Build the Popup Dialog confirmation window
            new AlertDialog.Builder(activity)
                    .setTitle("Remove Image")
                    .setMessage("Are you sure you want to remove this image?")

                    // 2. Handle the "Yes" action
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear the image source completely
                        removeProductImage(layout, materialCardView, imageView, uri);
                    })

                    // 3. Handle the "No" action
                    .setNegativeButton("No", (dialog, which) -> {
                        // Just dismiss the dialog window and do nothing
                        dialog.dismiss();
                    })
                    .show();

            // 4. Return true to signal that the touch event was fully handled
            return true;
        });
    }

    private void removeProductImage (LinearLayout layout, MaterialCardView materialCardView, ImageView imageView, Uri uri)
    {
        imageViews.remove(imageView);
        imageUris.remove(uri);
//        imageView.setImageDrawable(null); can remo
        layout.removeView(materialCardView);
    }

    public void clearButtonClicked
            (AddProductActivity activity,
             EditText productName, EditText productPrice, EditText productDescription,
             LinearLayout imagesContainer)
    {
        new AlertDialog.Builder(activity)
                .setTitle("Clear Form")
                .setMessage("Are you sure you want to clear all inputs and images?")
                .setPositiveButton("Yes", (dialog, which) ->
                {
                    removeUIProductDetails(productName, productPrice, productDescription, imagesContainer);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void removeUIProductDetails
            (EditText productName, EditText productPrice, EditText productDescription,
             LinearLayout imagesContainer)
    {
        UI.setTextViewTxt(new TextView[] { productName,productPrice,productDescription},
                new String[] {"", "", ""});

        // Remove all images except the add button (which is at index 0 in the XML usually,
        // but wait, it might be added programmatically or be the first child).
        // In the XML, add_product_image_button is the first child.

        int childCount = imagesContainer.getChildCount();

        for (int i = childCount - 1; i > 0; i--) {
            imagesContainer.removeViewAt(i);
        }

        imageViews.clear();
        imageUris.clear();
    }

    public void saveButtonClicked(AddProductActivity activity, Product product) {

        if (imageUris.isEmpty()) {
            activity.showToast("Please add at least one image");
            return;
        }

        new AddProductServer().saveProduct(activity, product, imageUris);
    }

}
