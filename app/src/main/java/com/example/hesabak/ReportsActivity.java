
package com.example.hesabak;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportsActivity extends Activity {

    Spinner spReportPeriod;
    TextView tvReportIncome, tvReportExpense;

    LinearLayout chartIncomeExpenseContainer;
    LinearLayout chartCategoryContainer;
    LinearLayout categoryDetailsContainer;

    DatabaseHelper dbHelper;

    String[] periods = {
            "این ماه",
            "ماه قبل",
            "امسال",
            "همه زمان‌ها"
    };

    String[] categories = {
            "خانه/اتاق",
            "خوراک",
            "آموزش",
            "ترانسپورت",
            "درمان",
            "قرض",
            "تفریح",
            "ورزش",
            "سایر"
    };

    int[] chartColors = {
            0xFF8B7CFF,
            0xFF35D39A,
            0xFFFF6B7A,
            0xFFFFB86B,
            0xFF62B5FF,
            0xFFB98AFF,
            0xFFFF92CB,
            0xFF57D6D1,
            0xFF9AA6BD
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new DatabaseHelper(this);

        spReportPeriod = findViewById(R.id.spReportPeriod);

        tvReportIncome = findViewById(R.id.tvReportIncome);
        tvReportExpense = findViewById(R.id.tvReportExpense);

        chartIncomeExpenseContainer =
                findViewById(R.id.chartIncomeExpenseContainer);

        chartCategoryContainer =
                findViewById(R.id.chartCategoryContainer);

        categoryDetailsContainer =
                findViewById(R.id.categoryDetailsContainer);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_filter_item,
                        periods
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_filter_item
        );

        spReportPeriod.setAdapter(adapter);

        spReportPeriod.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        loadReports();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (spReportPeriod != null &&
                spReportPeriod.getSelectedItemPosition() >= 0) {
            loadReports();
        }
    }

    // Convert dp to pixels
    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }

    // Format amount
    private String money(long amount) {
        return NumberFormat.getNumberInstance(Locale.US)
                .format(amount) + " AFN";
    }

    // Get selected date range
    private String[] getDateRange() {

        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();

        int selected = spReportPeriod.getSelectedItemPosition();

        if (selected == 3) {
            return null;
        }

        if (selected == 0) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);

        } else if (selected == 1) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.add(Calendar.MONTH, -1);
            end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);

        } else if (selected == 2) {
            start.set(Calendar.DAY_OF_YEAR, 1);
            end = (Calendar) start.clone();
            end.add(Calendar.YEAR, 1);
        }

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        return new String[]{
                sdf.format(start.getTime()),
                sdf.format(end.getTime())
        };
    }

    // Sum transactions from SQLite
    private long getTotal(
            SQLiteDatabase db,
            String type,
            String category,
            String[] range) {

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(SUM(amount), 0) " +
                        "FROM transactions WHERE type = ?"
        );

        java.util.ArrayList<String> args =
                new java.util.ArrayList<>();

        args.add(type);

        if (category != null) {
            sql.append(" AND category = ?");
            args.add(category);
        }

        if (range != null) {
            sql.append(" AND date >= ? AND date < ?");
            args.add(range[0]);
            args.add(range[1]);
        }

        try (Cursor cursor = db.rawQuery(
                sql.toString(),
                args.toArray(new String[0]))) {

            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }

        return 0;
    }

    private void loadReports() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] range = getDateRange();

        long income = getTotal(db, "income", null, range);
        long expense = getTotal(db, "expense", null, range);

        tvReportIncome.setText(money(income));
        tvReportExpense.setText(money(expense));

        showIncomeExpenseChart(income, expense);
        showCategoryChart(db, range);
    }

    // Create TextView dynamically
    private TextView createText(
            String value,
            int size,
            int color) {

        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER_VERTICAL);

        return text;
    }

    // Create horizontal progress bar
    private ProgressBar createProgress(
            int progress,
            int color) {

        ProgressBar bar = new ProgressBar(
                this, null,
                android.R.attr.progressBarStyleHorizontal
        );

        bar.setMax(100);
        bar.setProgress(Math.max(0, Math.min(100, progress)));

        bar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(color)
        );

        bar.setProgressBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.surface_alt))
        );

        return bar;
    }

    private void showIncomeExpenseChart(
            long income, long expense) {

        chartIncomeExpenseContainer.removeAllViews();
        chartIncomeExpenseContainer.setGravity(Gravity.CENTER_VERTICAL);
        chartIncomeExpenseContainer.setPadding(
                dp(18), dp(16), dp(18), dp(16));

        long max = Math.max(income, expense);

        int incomePercent = max == 0 ? 0 :
                (int) (income * 100.0 / max);

        int expensePercent = max == 0 ? 0 :
                (int) (expense * 100.0 / max);

        addChartRow(
                chartIncomeExpenseContainer,
                "درآمد",
                income,
                incomePercent,
                getColor(R.color.income)
        );

        addChartRow(
                chartIncomeExpenseContainer,
                "مصرف",
                expense,
                expensePercent,
                getColor(R.color.expense)
        );
    }

    private void addChartRow(
            LinearLayout parent,
            String title,
            long amount,
            int percent,
            int color) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = createText(title, 15, color);

        TextView value = createText(
                money(amount),
                14,
                getColor(R.color.text_primary)
        );

        value.setGravity(Gravity.END);

        row.addView(label, new LinearLayout.LayoutParams(
                0, dp(35), 1));

        row.addView(value, new LinearLayout.LayoutParams(
                0, dp(35), 1));

        parent.addView(row);

        ProgressBar bar = createProgress(percent, color);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(12)
                );

        params.bottomMargin = dp(18);
        parent.addView(bar, params);
    }

    private void showCategoryChart(
            SQLiteDatabase db,
            String[] range) {

        chartCategoryContainer.removeAllViews();
        categoryDetailsContainer.removeAllViews();

        chartCategoryContainer.setGravity(Gravity.TOP);
        chartCategoryContainer.setPadding(
                dp(15), dp(15), dp(15), dp(15));

        long[] amounts = new long[categories.length];
        long total = 0;

        for (int i = 0; i < categories.length; i++) {

            amounts[i] = getTotal(
                    db, "expense", categories[i], range);

            total += amounts[i];
        }

        // Dynamic content instead of fixed height
        android.view.ViewGroup.LayoutParams layoutParams =
                chartCategoryContainer.getLayoutParams();

        layoutParams.height =
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

        chartCategoryContainer.setLayoutParams(layoutParams);

        if (total == 0) {

            TextView empty = createText(
                    "هیچ مصرفی در این بازه ثبت نشده است.",
                    14,
                    getColor(R.color.text_secondary)
            );

            empty.setPadding(dp(8), dp(20), dp(8), dp(20));
            chartCategoryContainer.addView(empty);
            return;
        }

        for (int i = 0; i < categories.length; i++) {

            if (amounts[i] == 0) {
                continue;
            }

            int percent = (int) Math.round(
                    amounts[i] * 100.0 / total
            );

            addChartRow(
                    chartCategoryContainer,
                    categories[i],
                    amounts[i],
                    percent,
                    chartColors[i]
            );

            TextView detail = createText(
                    categories[i] + "  -  " +
                            money(amounts[i]) +
                            "  (" + percent + "%)",
                    14,
                    getColor(R.color.text_primary)
            );

            detail.setPadding(
                    dp(15), dp(13), dp(15), dp(13));

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.bottomMargin = dp(8);

            detail.setBackgroundResource(
                    R.drawable.dashboard_card_bg);

            categoryDetailsContainer.addView(detail, params);
        }
    }
}
