
package com.example.hesabak;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView tvBalance, tvIncome, tvExpense, tvRemaining;
    LinearLayout recentTransactionsContainer;
    Button btnAddTransaction, btnViewTransactions;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvRemaining = findViewById(R.id.tvRemaining);

        recentTransactionsContainer =
                findViewById(R.id.recentTransactionsContainer);

        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnViewTransactions = findViewById(R.id.btnViewTransactions);



        Button btnAboutDeveloper =
                findViewById(R.id.btnAboutDeveloper);

        btnAboutDeveloper.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AboutActivity.class
            );
            startActivity(intent);
        });



        // اتصال دکمه‌ها را در مرحله بعد انجام می‌دهیم.

        btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    TransactionActivity.class
            );
            startActivity(intent);
        });


        // connect button all transactions
        btnViewTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    TransactionsActivity.class
            );
            startActivity(intent);
        });

        // دکمه گزارش ها

        Button btnReports = findViewById(R.id.btnReports);

        btnReports.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ReportsActivity.class
            );
            startActivity(intent);
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private String formatMoney(long amount) {
        return NumberFormat.getNumberInstance(Locale.US)
                .format(amount) + " افغانی";
    }

    private long getTotal(SQLiteDatabase db,
                          String type,
                          String startDate,
                          String endDate) {

        String sql = "SELECT COALESCE(SUM(amount), 0) " +
                "FROM transactions WHERE type = ?";

        String[] args;

        if (startDate != null && endDate != null) {
            sql += " AND date >= ? AND date < ?";
            args = new String[]{type, startDate, endDate};
        } else {
            args = new String[]{type};
        }

        try (Cursor cursor = db.rawQuery(sql, args)) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }
        return 0;
    }

    private void loadDashboard() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.MONTH, 1);
        String endDate = sdf.format(calendar.getTime());

        long totalIncome = getTotal(db, "income", null, null);
        long totalExpense = getTotal(db, "expense", null, null);

        long monthlyIncome =
                getTotal(db, "income", startDate, endDate);

        long monthlyExpense =
                getTotal(db, "expense", startDate, endDate);

        long balance = totalIncome - totalExpense;
        long remaining = monthlyIncome - monthlyExpense;

        tvBalance.setText(formatMoney(balance));
        tvIncome.setText(formatMoney(monthlyIncome));
        tvExpense.setText(formatMoney(monthlyExpense));
        tvRemaining.setText(formatMoney(remaining));

        loadRecentTransactions(db);
    }

    private void loadRecentTransactions(SQLiteDatabase db) {

        recentTransactionsContainer.removeAllViews();

        String sql = "SELECT title, amount, type, date " +
                "FROM transactions " +
                "ORDER BY date DESC, id DESC LIMIT 5";

        try (Cursor cursor = db.rawQuery(sql, null)) {

            if (cursor.getCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText("هنوز هیچ تراکنشی ثبت نشده است.");
                empty.setTextColor(
                        getColor(R.color.text_secondary));
                empty.setTextSize(15);
                empty.setPadding(12, 24, 12, 24);

                recentTransactionsContainer.addView(empty);
                return;
            }

            while (cursor.moveToNext()) {

                String title = cursor.getString(0);
                long amount = cursor.getLong(1);
                String type = cursor.getString(2);
                String date = cursor.getString(3);

                TextView item = new TextView(this);

                String sign = type.equals("income") ? "+ " : "- ";

                item.setText(title + "\n" + date + "\n"
                        + sign + formatMoney(amount));

                item.setTextSize(16);
                item.setTextColor(getColor(
                        type.equals("income") ?
                                R.color.income : R.color.expense
                ));

                item.setBackgroundColor(
                        getColor(R.color.surface));

                item.setPadding(20, 16, 20, 16);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);

                params.bottomMargin = 12;

                recentTransactionsContainer.addView(item, params);
            }
        }
    }
}
