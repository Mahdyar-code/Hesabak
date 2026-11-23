
package com.example.hesabak;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionActivity extends Activity {

    EditText etAmount, etTitle, etNotes;
    RadioButton rbIncome, rbExpense;
    Spinner spCategory;
    TextView tvDate;
    Button btnSaveTransaction;

    DatabaseHelper dbHelper;
    Calendar selectedDate = Calendar.getInstance();
    int transactionId = -1;

    SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        dbHelper = new DatabaseHelper(this);

        etAmount = findViewById(R.id.etAmount);
        etTitle = findViewById(R.id.etTitle);
        etNotes = findViewById(R.id.etNotes);

        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);

        spCategory = findViewById(R.id.spCategory);
        tvDate = findViewById(R.id.tvDate);
        btnSaveTransaction =
                findViewById(R.id.btnSaveTransaction);

        // Categories

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_filter_item,
                        categories
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_filter_item
        );

        spCategory.setAdapter(adapter);


        // Spinner text color for dark mode
        spCategory.setSelection(0);

        // Default date
        tvDate.setText(sdf.format(selectedDate.getTime()));

        // Date picker
        tvDate.setOnClickListener(v -> showDatePicker());

        // Save button
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());

        transactionId = getIntent().getIntExtra("transaction_id", -1);

        if (transactionId != -1) {
            btnSaveTransaction.setText("ذخیره تغییرات");
            loadTransaction();
        }

    }


    private void loadTransaction() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT type, amount, title, category, date, notes " +
                        "FROM transactions WHERE id = ?",
                new String[]{String.valueOf(transactionId)})) {

            if (cursor.moveToFirst()) {

                String type = cursor.getString(0);
                long amount = cursor.getLong(1);
                String title = cursor.getString(2);
                String category = cursor.getString(3);
                String date = cursor.getString(4);
                String notes = cursor.getString(5);

                rbIncome.setChecked(type.equals("income"));
                rbExpense.setChecked(type.equals("expense"));

                etAmount.setText(String.valueOf(amount));
                etTitle.setText(title);
                etNotes.setText(notes == null ? "" : notes);

                for (int i = 0; i < categories.length; i++) {
                    if (categories[i].equals(category)) {
                        spCategory.setSelection(i);
                        break;
                    }
                }

                try {
                    selectedDate.setTime(sdf.parse(date));
                    tvDate.setText(date);
                } catch (Exception e) {
                    tvDate.setText(
                            sdf.format(selectedDate.getTime()));
                }

            } else {
                Toast.makeText(this,
                        "تراکنش پیدا نشد",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void showDatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    selectedDate.set(year, month, day);
                    tvDate.setText(
                            sdf.format(selectedDate.getTime())
                    );
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void saveTransaction() {

        String amountText =
                etAmount.getText().toString().trim();

        String title =
                etTitle.getText().toString().trim();

        String notes =
                etNotes.getText().toString().trim();

        if (amountText.isEmpty()) {
            etAmount.setError("مبلغ را وارد کنید");
            return;
        }

        long amount;

        try {
            amount = Long.parseLong(amountText);
            if (amount <= 0) {
                etAmount.setError("مبلغ باید بیشتر از صفر باشد");
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("مبلغ معتبر نیست");
            return;
        }

        if (title.isEmpty()) {
            etTitle.setError("عنوان را وارد کنید");
            return;
        }

        String type = rbIncome.isChecked()
                ? "income" : "expense";

        String category =
                spCategory.getSelectedItem().toString();

        String date =
                sdf.format(selectedDate.getTime());

        String createdAt = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.US
        ).format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();

        values.put("type", type);
        values.put("amount", amount);
        values.put("title", title);
        values.put("category", category);
        values.put("date", date);
        values.put("notes", notes);

        if (transactionId == -1) {
            values.put("created_at", createdAt);
        }



        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long result;

        if (transactionId == -1) {

            // ثبت تراکنش جدید
            result = db.insert(
                    "transactions",
                    null,
                    values
            );

        } else {

            // ویرایش تراکنش قبلی
            result = db.update(
                    "transactions",
                    values,
                    "id = ?",
                    new String[]{String.valueOf(transactionId)}
            );
        }



        if (result > 0) {

            Toast.makeText(
                    this,
                    transactionId == -1
                            ? "تراکنش موفقانه ثبت شد"
                            : "تراکنش موفقانه ویرایش شد",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "خطا در ذخیره تغییرات",
                    Toast.LENGTH_SHORT
            ).show();
        }

    }
}
