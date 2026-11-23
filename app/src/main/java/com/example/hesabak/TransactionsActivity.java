
package com.example.hesabak;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionsActivity extends Activity {

    EditText etSearch;
    Spinner spCategory, spDate, spType;
    RecyclerView recyclerView;
    TextView tvCount, tvEmpty;
    LinearLayout customDateContainer;
    Button btnStartDate, btnEndDate, btnClearFilters;

    DatabaseHelper dbHelper;
    TransactionAdapter adapter;

    String startCustomDate;
    String endCustomDate;

    SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    String[] categories = {
            "همه دسته‌ها",
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

    String[] periods = {
            "این ماه",
            "ماه قبل",
            "امروز",
            "۷ روز اخیر",
            "امسال",
            "همه زمان‌ها",
            "تاریخ دلخواه"
    };

    String[] types = {
            "همه تراکنش‌ها",
            "درآمد",
            "مصرف"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        dbHelper = new DatabaseHelper(this);


        adapter = new TransactionAdapter(
                this,
                this::confirmDelete,
                this::editTransaction
        );

        etSearch = findViewById(R.id.etSearch);
        spCategory = findViewById(R.id.spFilterCategory);
        spDate = findViewById(R.id.spFilterDate);
        spType = findViewById(R.id.spFilterType);

        recyclerView = findViewById(
                R.id.recyclerTransactions);

        tvCount = findViewById(R.id.tvTransactionCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        customDateContainer =
                findViewById(R.id.customDateContainer);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnClearFilters = findViewById(R.id.btnClearFilters);



        adapter = new TransactionAdapter(
                this,
                this::confirmDelete,
                this::editTransaction
        );



        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        setupSpinner(spCategory, categories);
        setupSpinner(spDate, periods);
        setupSpinner(spType, types);

        String today = sdf.format(Calendar.getInstance().getTime());
        startCustomDate = today;
        endCustomDate = today;

        btnStartDate.setText("از: " + today);
        btnEndDate.setText("تا: " + today);

        android.widget.AdapterView.OnItemSelectedListener listener =
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view, int position, long id) {

                        customDateContainer.setVisibility(
                                spDate.getSelectedItemPosition() == 6
                                        ? View.VISIBLE : View.GONE);

                        loadTransactions();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                };

        spCategory.setOnItemSelectedListener(listener);
        spDate.setOnItemSelectedListener(listener);
        spType.setOnItemSelectedListener(listener);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                loadTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnStartDate.setOnClickListener(v ->
                selectCustomDate(true));

        btnEndDate.setOnClickListener(v ->
                selectCustomDate(false));

        btnClearFilters.setOnClickListener(v -> {
            etSearch.setText("");
            spCategory.setSelection(0);
            spDate.setSelection(0);
            spType.setSelection(0);
            loadTransactions();
        });

        loadTransactions();
    }

    private void setupSpinner(Spinner spinner, String[] values) {

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_filter_item,
                        values);

        spinnerAdapter.setDropDownViewResource(
                R.layout.spinner_filter_item);

        spinner.setAdapter(spinnerAdapter);
    }

    private void selectCustomDate(boolean isStart) {

        Calendar calendar = Calendar.getInstance();

        String current = isStart
                ? startCustomDate : endCustomDate;

        String[] parts = current.split("-");

        calendar.set(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]) - 1,
                Integer.parseInt(parts[2]));

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    calendar.set(year, month, day);
                    String result = sdf.format(
                            calendar.getTime());

                    if (isStart) {
                        startCustomDate = result;
                        btnStartDate.setText("از: " + result);
                    } else {
                        endCustomDate = result;
                        btnEndDate.setText("تا: " + result);
                    }

                    loadTransactions();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private String[] getDateRange() {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.setTimeInMillis(start.getTimeInMillis());

        int period = spDate.getSelectedItemPosition();

        switch (period) {
            case 0: // This month
                start.set(Calendar.DAY_OF_MONTH, 1);
                end.setTimeInMillis(start.getTimeInMillis());
                end.add(Calendar.MONTH, 1);
                break;

            case 1: // Last month
                start.set(Calendar.DAY_OF_MONTH, 1);
                start.add(Calendar.MONTH, -1);
                end.setTimeInMillis(start.getTimeInMillis());
                end.add(Calendar.MONTH, 1);
                break;

            case 2: // Today
                end.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case 3: // Last 7 days, including today
                start.add(Calendar.DAY_OF_MONTH, -6);
                end.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case 4: // This year
                start.set(Calendar.DAY_OF_YEAR, 1);
                end.setTimeInMillis(start.getTimeInMillis());
                end.add(Calendar.YEAR, 1);
                break;

            case 5: // All dates
                return null;

            case 6: // Custom, inclusive of end date
                Calendar customEnd = Calendar.getInstance();
                String[] dateParts = endCustomDate.split("-");
                customEnd.clear();
                customEnd.set(
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2]));

                customEnd.add(Calendar.DAY_OF_MONTH, 1);

                return new String[]{
                        startCustomDate,
                        sdf.format(customEnd.getTime())
                };
        }

        return new String[]{
                sdf.format(start.getTime()),
                sdf.format(end.getTime())
        };
    }

    private void loadTransactions() {

        List<TransactionAdapter.Transaction> list =
                new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT id, title, category, type, date, amount " +
                        "FROM transactions WHERE 1=1");

        List<String> args = new ArrayList<>();

        // Search by title or notes
        String search = etSearch.getText().toString().trim();

        if (!search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR notes LIKE ?)");
            args.add("%" + search + "%");
            args.add("%" + search + "%");
        }

        // Category
        int categoryPosition = spCategory.getSelectedItemPosition();

        if (categoryPosition > 0) {
            sql.append(" AND category = ?");
            args.add(categories[categoryPosition]);
        }

        // Type
        int typePosition = spType.getSelectedItemPosition();

        if (typePosition == 1) {
            sql.append(" AND type = ?");
            args.add("income");
        } else if (typePosition == 2) {
            sql.append(" AND type = ?");
            args.add("expense");
        }

        // Date
        String[] range = getDateRange();

        if (range != null) {

            if (range[0].compareTo(range[1]) >= 0) {
                adapter.setTransactions(list);
                tvCount.setText("بازه زمانی نامعتبر");
                tvEmpty.setText("تاریخ شروع باید قبل یا برابر تاریخ پایان باشد.");
                tvEmpty.setVisibility(View.VISIBLE);
                return;
            }

            sql.append(" AND date >= ? AND date < ?");
            args.add(range[0]);
            args.add(range[1]);
        }

        sql.append(" ORDER BY date DESC, id DESC");

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                sql.toString(),
                args.toArray(new String[0]))) {

            while (cursor.moveToNext()) {

                list.add(new TransactionAdapter.Transaction(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5)
                ));
            }
        }

        adapter.setTransactions(list);

        tvCount.setText(list.size() + " تراکنش");
        tvEmpty.setText("هیچ تراکنشی پیدا نشد.");
        tvEmpty.setVisibility(
                list.isEmpty() ? View.VISIBLE : View.GONE);
    }


    private void editTransaction(int transactionId) {

        Intent intent = new Intent(
                TransactionsActivity.this,
                TransactionActivity.class
        );

        intent.putExtra("transaction_id", transactionId);

        startActivity(intent);
    }



    private void confirmDelete(int transactionId) {

        new AlertDialog.Builder(this)
                .setTitle("حذف تراکنش")
                .setMessage("آیا می‌خواهید این تراکنش حذف شود؟")
                .setNegativeButton("انصراف", null)
                .setPositiveButton("حذف", (dialog, which) -> {

                    SQLiteDatabase db =
                            dbHelper.getWritableDatabase();

                    int deleted = db.delete(
                            "transactions",
                            "id = ?",
                            new String[]{
                                    String.valueOf(transactionId)
                            });

                    if (deleted > 0) {
                        Toast.makeText(this,
                                "تراکنش حذف شد",
                                Toast.LENGTH_SHORT).show();
                        loadTransactions();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null && spDate.getAdapter() != null) {
            loadTransactions();
        }
    }
}
