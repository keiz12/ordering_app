package com.example.orderingapp.productCarousel;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.orderingapp.dto.*;
import com.example.orderingapp.R;
import com.example.orderingapp.convert.UnitConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to programmatically create product carousel views.
 */
public class ProductCarousel {

    private static ProductCarousel instance;
    private final UnitConverter unitConverter = new UnitConverter();

    private ProductCarousel() {}

    public static synchronized ProductCarousel getInstance() {
        if (instance == null) {
            instance = new ProductCarousel();
        }
        return instance;
    }

    /**
     * Interface to provide custom action views (like buttons) for each product.
     */
    public interface ActionButtonProvider {
        List<View> getButtons(Product product);
    }

    /**
     * Populates a parent layout with product items.
     */
    public void populateCarousel(Context context, List<Product> products, LinearLayout root, @Nullable ActionButtonProvider actionButtonProvider) {
        root.removeAllViews();
        for (Product product : products) {
            View productView = createProductItemView(context, product, actionButtonProvider);
            root.addView(productView);
        }
    }

    private View createProductItemView(Context context, Product product, @Nullable ActionButtonProvider provider) {
        LinearLayout itemContainer = new LinearLayout(context);
        itemContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        itemContainer.setOrientation(LinearLayout.VERTICAL);

        // Main Card Content Area
        LinearLayout mainContent = createMainContentContainer(context);

        mainContent.addView(createHeader(context, product.getName()));
        mainContent.addView(createImageSlider(context, product.getImagePaths()));
        mainContent.addView(createPriceView(context, product.getPrice()));
        mainContent.addView(createDescriptionView(context, product.getDescription()));

        itemContainer.addView(mainContent);

        // Add action buttons if provider is set
        if (provider != null) {
            List<View> buttons = provider.getButtons(product);
            if (buttons != null && !buttons.isEmpty()) {
                itemContainer.addView(createExtraViewsLayout(context, buttons));
            }
        }

        return itemContainer;
    }

    private LinearLayout createMainContentContainer(Context context) {
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, unitConverter.dpToPx(context, 10), 0, unitConverter.dpToPx(context, 20));
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        return layout;
    }

    private View createHeader(Context context, String name) {
        LinearLayout header = new LinearLayout(context);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = unitConverter.dpToPx(context, 12);
        header.setPadding(padding, padding, padding, padding);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        ImageView imgProfile = new ImageView(context);
        imgProfile.setLayoutParams(new LinearLayout.LayoutParams(unitConverter.dpToPx(context, 40), unitConverter.dpToPx(context, 40)));
        imgProfile.setImageResource(R.drawable.home_activity_design);
        imgProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
        header.addView(imgProfile);

        LinearLayout nameContainer = new LinearLayout(context);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        nameParams.setMarginStart(unitConverter.dpToPx(context, 10));
        nameContainer.setLayoutParams(nameParams);
        nameContainer.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(context);
        tvName.setText(name);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextColor(context.getResources().getColor(R.color.black));
        nameContainer.addView(tvName);

        header.addView(nameContainer);
        return header;
    }

    private View createImageSlider(Context context, List<String> paths) {
        ImageSlider slider = new ImageSlider(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                unitConverter.dpToPx(context, 350));
        slider.setLayoutParams(params);

        List<SlideModel> slideModels = new ArrayList<>();
        if (paths != null) {
            for (String path : paths) {
                int resId = context.getResources().getIdentifier(path, "drawable", context.getPackageName());
                if (resId != 0) {
                    slideModels.add(new SlideModel(resId, ScaleTypes.CENTER_CROP));
                }
            }
        }
        slider.setImageList(slideModels);
        return slider;
    }

    private View createPriceView(Context context, String price) {
        TextView tvPrice = new TextView(context);
        int px = unitConverter.dpToPx(context, 12);
        tvPrice.setPadding(px, px, px, 0);
        tvPrice.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvPrice.setText(price);
        tvPrice.setTextSize(22);
        tvPrice.setTextColor(context.getResources().getColor(R.color.primary_color));
        tvPrice.setTypeface(null, Typeface.BOLD);
        return tvPrice;
    }

    private View createDescriptionView(Context context, String description) {
        TextView tvDesc = new TextView(context);
        int px = unitConverter.dpToPx(context, 12);
        tvDesc.setPadding(px, unitConverter.dpToPx(context, 4), px, px);
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tvDesc.setText(description);
        tvDesc.setTextSize(16);
        tvDesc.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        return tvDesc;
    }

    private View createExtraViewsLayout(Context context, List<View> views) {
        LinearLayout extraLayout = new LinearLayout(context);
        extraLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        extraLayout.setOrientation(LinearLayout.HORIZONTAL);
        int p = unitConverter.dpToPx(context, 12);
        extraLayout.setPadding(p, 0, p, p);
        extraLayout.setGravity(Gravity.CENTER);

        for (View v : views) {
            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeView(v);
            }
            extraLayout.addView(v);
        }
        return extraLayout;
    }
}
