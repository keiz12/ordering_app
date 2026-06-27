package com.example.orderingapp.statistics;

import static com.example.orderingapp.R.*;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.orderingapp.R;
import com.example.orderingapp.databinding.StatisticsActivityBinding;
import com.example.orderingapp.dto.statistic.StatisticRequest;
import com.example.orderingapp.dto.statistic.StatisticResponse;
import com.example.orderingapp.dto.statistic.StatisticType;
import com.example.orderingapp.interfaces.activity.ShowToastFromBgThread;
import com.example.orderingapp.loading.LoadingDialog;
import com.example.orderingapp.toast.Toasts;
import com.example.orderingapp.ui.UI;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.internal.cache2.Relay;

public class StatisticsActivity extends AppCompatActivity implements ShowToastFromBgThread {

    private StatisticsActivityBinding binding;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private RelativeLayout cardLayout;
    private StatisticResponse currentStatistics;
    private LoadingDialog loadingDialog;
    private volatile int bgThreads;
    private final Lock lock = new ReentrantLock();
    private final Condition incrementingCondition = lock.newCondition();
    private final Condition decrementingCondition = lock.newCondition();
    private boolean isIncrementingBgThreads, isDecrementingBgThreads;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = StatisticsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadingDialog = LoadingDialog.newInstance();

    }


    @Override
    protected void onStart() {

        super.onStart();

        setHeader();
        setSelectedMaterialCardView(binding.revenueStatisticsRelativeLayout);
        setupListeners();
        setupDates();
        setUpTotalTitles();
        setupCharts();
        refreshData();
    }


    // Call this method to show the full-screen loader
    private void showLoading() {
        runOnUiThread(() -> {

            System.out.println("added: "+loadingDialog.isAdded());

            System.out.println("Visible: "+loadingDialog.isVisible());

            if (loadingDialog != null && !loadingDialog.isAdded())
                loadingDialog.show(getSupportFragmentManager(), "loading");
        });
    }

    // Call this method to dismiss it when processing finishes
    private void hideLoading() {
        runOnUiThread(() -> {

            System.out.println("added: "+loadingDialog.isAdded());

            System.out.println("Visible: "+loadingDialog.isVisible());

            if (loadingDialog != null && loadingDialog.isAdded())
                loadingDialog.dismiss();
        });
    }

    private void setHeader () {
        TextView textView = findViewById(id.header_title_textview);
        textView.setText("Statistics");

        ImageButton button = findViewById(id.btn_header_back);
        button.setOnClickListener(l -> finish());
    }

    private void setupDates() {
        binding.textviewEndDate.setText(LocalDateTime.now().getDayOfMonth() + "/" +
                (LocalDateTime.now().getMonthValue() + 1) + "/" +
                LocalDateTime.now().getYear());
    }

    private void setUpTotalTitles() {
        new StatisticTotalServer(this).run();
    }


    public void setTotals (int totalRevenue, int totalProducts, int totalStaff)
    {
        runOnUiThread(() -> {
            binding.textviewRevenueValue.setText(String.valueOf(totalRevenue));
            binding.textviewProductValue.setText(String.valueOf(totalProducts));
            binding.textviewStaffValue.setText(String.valueOf(totalStaff));
        });
    }

    private void setupListeners() {
        binding.cardRevenue.setOnClickListener(v -> handleMetricClick("Revenue", binding.revenueStatisticsRelativeLayout, getStatisticRequest(StatisticType.REVENUE)));
        binding.cardProduct.setOnClickListener(v -> handleMetricClick("Products", binding.productsStatisticsRelativeLayout, getStatisticRequest(StatisticType.PRODUCT)));
        binding.cardStaff.setOnClickListener(v -> handleMetricClick("Staff", binding.staffStatisticsRelativeLayout, getStatisticRequest(StatisticType.STAFF)));

        binding.textviewStartDate.setOnClickListener(v -> showDatePicker(true));
        binding.textviewEndDate.setOnClickListener(v -> showDatePicker(false));
        binding.buttonSubmitDates.setOnClickListener(v -> {
            refreshData();
        });
    }

    private StatisticRequest getStatisticRequest(StatisticType type) {
        StatisticRequest request = new StatisticRequest();
        request.setStatisticType(type);
        request.setStartingDate(binding.textviewStartDate.getText().toString().replace("/", "-"));
        request.setEndingDate(binding.textviewEndDate.getText().toString().replace("/", "-"));
        return request;
    }

    private void handleMetricClick(String metric, RelativeLayout layout, StatisticRequest request) {
        binding.textviewGraphMainTitle.setText(metric + " Analysis");
        setSelectedMaterialCardView(layout);
        
        sendStatisticsRequest(request);
    }

    private void sendStatisticsRequest (StatisticRequest request) {
        new StatisticServer().getStatistics(this, request);
    }

    public void updateStatisticsUI(StatisticResponse response) {
        this.currentStatistics = response;
        setupCharts();
        populateProductList(response);
    }

    private void setSelectedMaterialCardView (RelativeLayout layout)
    {
        if (cardLayout != null)
            cardLayout.setBackgroundColor(ContextCompat.getColor(this, color.card_background));

        cardLayout = layout;
        cardLayout.setBackgroundResource(drawable.primary_gradient);
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? startDate : endDate;
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
            if (isStart) {
                binding.textviewStartDate.setText(dateStr);
            } else {
                binding.textviewEndDate.setText(dateStr);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupCharts() {
        ChartPagerAdapter adapter = new ChartPagerAdapter(currentStatistics);
        binding.viewpagerCharts.setAdapter(adapter);
        
        binding.viewpagerCharts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.textviewGraphIndicator.setText((position + 1) + "/" + adapter.getItemCount());
            }
        });
    }

    private void refreshData() {

        if (cardLayout == binding.revenueStatisticsRelativeLayout)
            handleMetricClick("Revenue", binding.revenueStatisticsRelativeLayout, getStatisticRequest(StatisticType.REVENUE));

        else if (cardLayout == binding.productsStatisticsRelativeLayout)
            handleMetricClick("Products", binding.productsStatisticsRelativeLayout, getStatisticRequest(StatisticType.PRODUCT));

        else if (cardLayout == binding.staffStatisticsRelativeLayout)
            handleMetricClick("Staff", binding.staffStatisticsRelativeLayout, getStatisticRequest(StatisticType.STAFF));

    }

    private void populateProductList(StatisticResponse response) {
        binding.linearlayoutProductListContainer.removeAllViews();
        
        if (response == null) return;

        Map<String, BigDecimal> data = response.getRawDataMap();
        if (data == null) return;

        for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {

            View itemView = LayoutInflater.from(this).inflate(R.layout.item_statistics_data, binding.linearlayoutProductListContainer, false);

            UI.setTextViewTxt(new TextView[]{itemView.findViewById(R.id.textview_item_name), itemView.findViewById(R.id.textview_item_value)},
                    new String[]{entry.getKey(), entry.getValue().toString()});
            
            binding.linearlayoutProductListContainer.addView(itemView);
        }
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> Toasts.showLongToast(this, message));
    }

    // Inner class for Chart Pager
    private class ChartPagerAdapter extends RecyclerView.Adapter<ChartViewHolder> {
        private final StatisticResponse response;

        public ChartPagerAdapter(StatisticResponse response) {
            this.response = response;
        }

        @NonNull
        @Override
        public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = new BarChart(StatisticsActivity.this);
            } else if (viewType == 1) {
                view = new LineChart(StatisticsActivity.this);
            } else {
                view = new PieChart(StatisticsActivity.this);
            }
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new ChartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
            if (response == null) return;

            if (holder.itemView instanceof BarChart) {
                setupBarChart((BarChart) holder.itemView, response.getRawDataMap());
            } else if (holder.itemView instanceof LineChart) {
                setupLineChart((LineChart) holder.itemView, response.getPercentageDataMap());
            } else if (holder.itemView instanceof PieChart) {
                setupPieChart((PieChart) holder.itemView, response.getRatings());
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Bar, Line, Pie
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        private void setupBarChart(BarChart chart, Map<String, BigDecimal> rawData) {
            if (rawData == null || rawData.isEmpty()) return;

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            populateBarEntries_Labels(rawData, entries, labels);

            BarDataSet dataSet = new BarDataSet(entries, "Raw Data");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            BarData data = new BarData(dataSet);
            chart.setData(data);
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.getXAxis().setGranularity(1f);
//            chart.getDescription().setEnabled(false);
            chart.animateY(1000);
            chart.invalidate();
        }

        private void setupLineChart(LineChart chart, Map<String, BigDecimal> percentageData) {
            if (percentageData == null || percentageData.isEmpty()) return;

            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            populateEntries_Labels(percentageData, entries, labels);

            LineDataSet dataSet = new LineDataSet(entries, "Percentage Data");
            dataSet.setColor(ContextCompat.getColor(StatisticsActivity.this, R.color.primary_color));
            dataSet.setCircleColor(ContextCompat.getColor(StatisticsActivity.this, R.color.primary_color));
            dataSet.setLineWidth(2f);

            LineData data = new LineData(dataSet);
            chart.setData(data);
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.getXAxis().setGranularity(1f);
            chart.getDescription().setEnabled(false);
            chart.animateX(1000);
            chart.invalidate();
        }

        private void populateEntries_Labels (Map<String, BigDecimal> data, List<Entry> entries, List<String> labels)
        {
            int index = 0;
            for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                if (index >= 5) break;
                entries.add(new Entry(index, entry.getValue().floatValue()));
                labels.add(entry.getKey());
                index++;
            }
        }

        private void populateBarEntries_Labels (Map<String, BigDecimal> data, List<BarEntry> entries, List<String> labels)
        {
            int index = 0;
            for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                if (index >= 5) break;
                entries.add(new BarEntry(index, entry.getValue().floatValue()));
                labels.add(entry.getKey());
                index++;
            }
        }

        private void setupPieChart(PieChart chart, Map<String, Double> ratings) {
            if (ratings == null || ratings.isEmpty()) return;

            List<PieEntry> entries = new ArrayList<>();

            for (Map.Entry<String, Double> entry : ratings.entrySet()) {

                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }

            PieDataSet dataSet = new PieDataSet(entries, "Ratings");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            PieData data = new PieData(dataSet);
            chart.setData(data);
            chart.getDescription().setEnabled(false);
            chart.setCenterText("Ratings");
            chart.animateXY(1000, 1000);
            chart.invalidate();
        }
    }

    public void initNewBgThread () {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            incrementBgThreads();
//            setIsIncrementingBgThreadToFalse();
        });

        executorService.shutdown();
    }

    public void windUpBgThread () {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            decrementBgThreads();
        });

        executorService.shutdown();
    }

    private void incrementBgThreads ()
    {
        lock.lock();

        try {

            while (isIncrementingBgThreads)
                incrementingCondition.await();

            isIncrementingBgThreads = true;
            bgThreads += 1;
            showLoading();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            isIncrementingBgThreads = false;
            lock.unlock();
        }

    }

    private void decrementBgThreads ()
    {
        lock.lock();

        try
        {
            while (isDecrementingBgThreads)
                decrementingCondition.await();

            isDecrementingBgThreads = true;
            bgThreads -= 1;
            canHideLoading();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            isDecrementingBgThreads = false;
            lock.unlock();
        }

    }

    private void setIsDecrementingBgThreadToFalse ()
    {
        lock.lock();

        try {

            while (isDecrementingBgThreads)
                decrementingCondition.await();

            isDecrementingBgThreads = false;
            decrementingCondition.signalAll();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }

    }

    private void canHideLoading () {
        if (bgThreads == 0)
            hideLoading();
    }

    private static class ChartViewHolder extends RecyclerView.ViewHolder {
        public ChartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
