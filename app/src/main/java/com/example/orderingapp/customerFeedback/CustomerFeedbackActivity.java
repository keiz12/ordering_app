package com.example.orderingapp.customerFeedback;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.orderingapp.R;
import com.example.orderingapp.customerFeedback.server.DeleteCustomFeedbackServer;
import com.example.orderingapp.customerFeedback.server.FetchCustomerFeedbackServer;
import com.example.orderingapp.dto.CustomerFeedback;
import com.example.orderingapp.employeeManagement.database.EmployeeDatabase;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.toast.Toasts;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CustomerFeedbackActivity extends AppCompatActivity implements ShowToastFromBgThread {

    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_feedback_activity);
        setHeader();
        setUserRole(getIntent());
        new FetchCustomerFeedbackServer(this).run();
    }

    private void setUserRole(Intent intent) {

        userRole = intent.getStringExtra("userRole");
    }
    private void setHeader() {
        TextView textView = findViewById(R.id.header_title_textview);
        textView.setText("Customer Feedback");

        findViewById(R.id.btn_header_back).setOnClickListener(v -> finish());
    }

    public void setCustomerFeedbacks(List<CustomerFeedback> feedbacks) {

        runOnUiThread(() ->
        {
            LinearLayout parent = findViewById(R.id.customer_feedback_scrollView_child);
            parent.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < feedbacks.size(); i++) {

                CustomerFeedback feedback = feedbacks.get(i);
                View view = inflater.inflate(R.layout.customer_feedback_inflater, parent, false);

                ((TextView)view.findViewById(R.id.feedback_id_textview)).setText(String.valueOf(i + 1));
                ((TextView)view.findViewById(R.id.feedback_time_textview)).setText(feedback.getFeedbackLocalDateTime());
                ((TextView)view.findViewById(R.id.feedback_textview)).setText(feedback.getFeedback());
                setRemoveFeedbackButton(feedback, parent, view);
                parent.addView(view);
            }
        });
    }

    private void setRemoveFeedbackButton (CustomerFeedback feedback, LinearLayout parent, View view) {

        if (!userRole.equalsIgnoreCase("boss")) {
            view.findViewById(R.id.feedback_delete_btn).setVisibility(View.GONE);
            return;
        }

        setRemoveFeedbackButtonEventListener(view.findViewById(R.id.feedback_delete_btn), feedback.getUuid(), parent, view);
    }

    private void setRemoveFeedbackButtonEventListener (MaterialButton button, String uuid, LinearLayout parentLayout, View view) {
        button.setOnClickListener(l -> {
            showLoadingTest();
            new DeleteCustomFeedbackServer(this, uuid, parentLayout, view).run();
        });
    }

    public void removeAView (LinearLayout layout, View view) {
        runOnUiThread(() -> {
            layout.removeView(view);
        });
    }

    private void showLoadingTest () {
        runOnUiThread(() -> findViewById(R.id.all_customer_feedbacks_loading_textview).setVisibility(View.VISIBLE));
    }

    public void hideLoadingTest () {
        runOnUiThread(() -> findViewById(R.id.all_customer_feedbacks_loading_textview).setVisibility(View.GONE));
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> Toasts.showLongToast(this, message));
    }
}
