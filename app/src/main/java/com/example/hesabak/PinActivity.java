
package com.example.hesabak;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.time.LocalDateTime;

public class PinActivity extends Activity {

    EditText etPin;
    Button btnContinue;
    DatabaseHelper dbHelper;
    boolean isFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        etPin = findViewById(R.id.etPin);
        btnContinue = findViewById(R.id.btnContinue);
        dbHelper = new DatabaseHelper(this);

        // Check whether a PIN already exists
        try (Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery(
                        "SELECT pin_hash FROM settings WHERE id = 1",
                        null)) {

            isFirstTime = !cursor.moveToFirst();
        }

        btnContinue.setText(
                isFirstTime ? "تعیین رمز" : "ورود"
        );

        btnContinue.setOnClickListener(v -> {

            String pin = etPin.getText().toString();

            // Validate PIN
            if (!pin.matches("[0-9]{4}")) {
                etPin.setError("رمز باید دقیقاً ۴ رقم باشد");
                return;
            }

            if (isFirstTime) {

                // Create a secure hash
                String hashedPin = PinSecurity.hashPin(pin);

                ContentValues values = new ContentValues();

                values.put("id", 1);
                values.put("pin_hash", hashedPin);
                values.put(
                        "created_at",
                        LocalDateTime.now().toString()
                );

                long result = dbHelper.getWritableDatabase()
                        .insert("settings", null, values);

                if (result != -1) {

                    Toast.makeText(
                            this,
                            "رمز موفقانه تعیین شد",
                            Toast.LENGTH_SHORT
                    ).show();

                    openMain();

                } else {

                    Toast.makeText(
                            this,
                            "خطا در ذخیره رمز",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } else {

                // Read saved PIN hash
                String storedHash;

                try (Cursor cursor = dbHelper.getReadableDatabase()
                        .rawQuery(
                                "SELECT pin_hash FROM settings WHERE id = 1",
                                null)) {

                    if (!cursor.moveToFirst()) {
                        Toast.makeText(
                                this,
                                "رمز ذخیره‌شده پیدا نشد",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    storedHash = cursor.getString(0);
                }

                // Verify entered PIN
                if (PinSecurity.verifyPin(pin, storedHash)) {

                    // Upgrade old SHA-256 hash to PBKDF2
                    if (PinSecurity.isLegacyHash(storedHash)) {

                        ContentValues values = new ContentValues();

                        values.put(
                                "pin_hash",
                                PinSecurity.hashPin(pin)
                        );

                        SQLiteDatabase db =
                                dbHelper.getWritableDatabase();

                        int updated = db.update(
                                "settings",
                                values,
                                "id = ?",
                                new String[]{"1"}
                        );

                        if (updated != 1) {
                            Toast.makeText(
                                    this,
                                    "خطا در به‌روزرسانی امنیت رمز",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                    }

                    openMain();

                } else {

                    Toast.makeText(
                            this,
                            "رمز اشتباه است",
                            Toast.LENGTH_SHORT
                    ).show();

                    etPin.setText("");
                }
            }
        });
    }

    private void openMain() {

        Intent intent = new Intent(
                PinActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }
}
